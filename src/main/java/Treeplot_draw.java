import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Treeplot_draw extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<int[]> List_treeNodes;
	int[] track_initialTP;
	
	private ArrayList<ArrayList<ArrayList<Integer>>> list_tree_Childrens_ID;
	private ArrayList<int[]> list_numberDescendants ;
	ArrayList<Color> colorCells;
	int panel_width, panel_height, circle_size, tree_to_draw;
	private File folder_path;
	public Treeplot_draw(ArrayList<int[]> tree_input, int[] track_initialTP, ArrayList<Color> colorCells, int frame_size) {
		List_treeNodes = tree_input;
		this.track_initialTP = track_initialTP;
		this.colorCells = colorCells;
		this.panel_width = frame_size;
		this.panel_height = frame_size;
		
		this.list_numberDescendants = compute_number_descendant();
		this.list_tree_Childrens_ID = compute_for_each_tree_childrens_IDs();
		
		//creating folder to save images
		folder_path = MenuBarGUI.create_folder("TreePlot_");		
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GREEN);
		
		ArrayList<int[]> List_n_descendant = compute_number_descendant();
		ArrayList<ArrayList<ArrayList<Integer>>> list_trees_children = get_tree_children_IDs();
		//position of node in panel
		ArrayList<Point> pos_panel_node = new ArrayList<Point>();
		
		//initial position for each node is (0,0) and getting total number of terminal points
		int n_terminalCells = 0;
		for (int id=0;id<List_treeNodes.get(tree_to_draw).length;id++) {
			pos_panel_node.add(new Point(0,0));
			if (List_treeNodes.get(tree_to_draw)[id]<0)
				n_terminalCells+= List_n_descendant.get(tree_to_draw)[id];
		}		
		
		//number of levels to be used for the tree
		int n_levels = ImageControler.get_ellipsoidList_nTimePoints();
		
		//displacement in X and Y position
		int int_sY = panel_height/(n_levels+1);
		double int_sX = panel_width/(n_terminalCells+1);
		double posX = panel_width/2;
		double posY = panel_height;
		
		circle_size = (int) (int_sX/4);
		
		//setting the position for root points
		float percentage_screen = 0;
		for (int id=0;id<List_treeNodes.get(tree_to_draw).length;id++) {
			if (List_treeNodes.get(tree_to_draw)[id]<0) {
				float ini_pos = percentage_screen;
				float end_pos = percentage_screen +  ((float)List_n_descendant.get(tree_to_draw)[id])/n_terminalCells;
				posX = (ini_pos + end_pos)*panel_width/2;
				percentage_screen = end_pos;
				pos_panel_node.get(id).setLocation(posX, posY-(track_initialTP[tree_to_draw]+1)*int_sY);
			}			
		}
		
		
		for (int node_id=0;node_id<List_treeNodes.get(tree_to_draw).length;node_id++){
			posX = pos_panel_node.get(node_id).x;
			posY = pos_panel_node.get(node_id).y -int_sY;
			//list of childs for current node
			ArrayList<Integer> children_id = list_trees_children.get(tree_to_draw).get(node_id);
			if (children_id.size()==1) {				
				pos_panel_node.get(children_id.get(0)).setLocation(posX, posY);
			}else if (children_id.size()>1){
				int n_descendant=0;
				double traslation_x = 0;
				for (int id_child:children_id) { 
					traslation_x = n_descendant;
					n_descendant += List_n_descendant.get(tree_to_draw)[id_child];
					traslation_x = (n_descendant-1-traslation_x)*int_sX/2;
					pos_panel_node.get(id_child).setLocation((n_descendant-1)*int_sX -traslation_x, posY);
				}
				
				traslation_x = (n_descendant-1)*int_sX/2;
				for (int id_child:children_id) 
					pos_panel_node.get(id_child).setLocation(pos_panel_node.get(node_id).x-pos_panel_node.get(id_child).x+traslation_x, posY);				
			}
		}
		 
		//drawing lines
		g.setColor(Color.black);
		ArrayList<Integer> roots_id = new ArrayList<Integer>();
		for (int id=0;id<List_treeNodes.get(tree_to_draw).length;id++) {
			int parent = List_treeNodes.get(tree_to_draw)[id];
			if (parent>=0) {
				//draw a vertical line
				g.drawLine(pos_panel_node.get(id).x, pos_panel_node.get(id).y, pos_panel_node.get(parent).x, pos_panel_node.get(parent).y-int_sY);
			}else {
				//we have a root point
				int pos_prev = roots_id.size()-1;
				roots_id.add(id);
				if (roots_id.size()>1)
					g.drawLine(pos_panel_node.get(roots_id.get(pos_prev)).x, pos_panel_node.get(roots_id.get(pos_prev)).y, pos_panel_node.get(id).x, pos_panel_node.get(id).y);
			}
			if (list_trees_children.get(tree_to_draw).get(id).size()>0) {
				//draw a horizontal line
				g.drawLine(pos_panel_node.get(id).x, pos_panel_node.get(id).y, pos_panel_node.get(id).x, pos_panel_node.get(id).y-int_sY);
			}
		}	
		
		//drawing labels for time
		int label_line_Y0pos = panel_height-int_sY;
		g.drawLine((int) int_sX/2, panel_height-int_sY, (int) int_sX/2, panel_height-int_sY*ImageControler.get_ellipsoidList_nTimePoints());
		int step_size_label = Math.round(ImageControler.get_ellipsoidList_nTimePoints()/10);
		
		if (step_size_label==0)
				step_size_label = 1;
		g.setFont(new Font("TimesRoman", Font.PLAIN, circle_size)); 
		for (int label_id =0;label_id<ImageControler.get_ellipsoidList_nTimePoints();label_id+=step_size_label) {
			if (label_id<10)
				g.drawString(Integer.toString(label_id),(int) (0.3*int_sX),(int) (label_line_Y0pos - label_id*int_sY+ 0.4*circle_size));
			else
				g.drawString(Integer.toString(label_id),(int) (0.1*int_sX), (int) (label_line_Y0pos - label_id*int_sY+ 0.4*circle_size));
			
			g.drawLine((int) (0.5*int_sX), label_line_Y0pos - label_id*int_sY, (int) (0.7*int_sX), label_line_Y0pos - label_id*int_sY);
		}
		
		//drawing circles
		g.setColor(colorCells.get(tree_to_draw));
		g.setFont(new Font("TimesRoman", Font.PLAIN, circle_size)); 		
		
		int parent;
		for (int id=0;id<List_treeNodes.get(tree_to_draw).length;id++) {
			parent = List_treeNodes.get(tree_to_draw)[id];
			if (parent>=0) {
				//only draw circle if it is a new mitotic event
				if (list_trees_children.get(tree_to_draw).get(parent).size()>1)	{
					g.fillOval(pos_panel_node.get(id).x-circle_size, pos_panel_node.get(id).y-circle_size, 2*circle_size, 2*circle_size);
					g.drawString(Integer.toString(id),(int) (pos_panel_node.get(id).x-1.2*circle_size), (int) (pos_panel_node.get(id).y-1.2*circle_size));
				}				
			}else {
				//root node --- draw circle
				g.fillOval(pos_panel_node.get(id).x-circle_size, pos_panel_node.get(id).y-circle_size, 2*circle_size, 2*circle_size);
			}
		}			
	}
	
	public File get_folder_path() {
		return (folder_path); 
	}
	
	public ArrayList<ArrayList<ArrayList<Integer>>> compute_for_each_tree_childrens_IDs() {
		//create an array for each tree that have a list of the children for every node of the tree
		
		//getting number of descendant for each list
		ArrayList<ArrayList<ArrayList<Integer>>> List_childs_tree = new ArrayList<ArrayList<ArrayList<Integer>>>();
		
		//initialize list array
		for (int tree_id=0;tree_id<List_treeNodes.size();tree_id++) { 
			List_childs_tree.add(new ArrayList<ArrayList<Integer>>());
			for (int node_id=0; node_id<List_treeNodes.get(tree_id).length;node_id++) {
				List_childs_tree.get(tree_id).add(new ArrayList<Integer>());
			}
		}
		
		//create child list for each tree		
		for (int tree_k=0;tree_k<List_treeNodes.size();tree_k++) { 
			for (int node_id=0; node_id<List_treeNodes.get(tree_k).length;node_id++) {
				int parent = List_treeNodes.get(tree_k)[node_id]; 
				if (parent>=0)
					List_childs_tree.get(tree_k).get(parent).add(node_id);
			}
		}		
		return (List_childs_tree);		
	}	
	
	public ArrayList<ArrayList<ArrayList<Integer>>> get_tree_children_IDs() {
		return (list_tree_Childrens_ID);
	}	
	
	public ArrayList<int[]> compute_number_descendant() {
		//getting number of descendant for each list
		ArrayList<int[]> List_n_descendant = new ArrayList<int[]>();
		int parent;
		for (int tree_id=0;tree_id<List_treeNodes.size();tree_id++) { 
			List_n_descendant.add(new int[List_treeNodes.get(tree_id).length]);
			for (int j=List_treeNodes.get(tree_id).length-1; j>=0;j--) {
				if (List_n_descendant.get(tree_id)[j]==0) {
					parent = List_treeNodes.get(tree_id)[j];
					List_n_descendant.get(tree_id)[j]=1;
					while (parent>=0) {
						List_n_descendant.get(tree_id)[parent]+=1;
						parent = List_treeNodes.get(tree_id)[parent];
					}
				}
			}
		}
		return (List_n_descendant);		
	}
	
	public ArrayList<int[]> get_number_descendant() {
		return (list_numberDescendants);
	}
	   
	public void saveImage() {
		BufferedImage image = new BufferedImage(panel_width,panel_height, BufferedImage.TYPE_INT_RGB);
		for (int tree_id=0;tree_id<List_treeNodes.size();tree_id++) {
			tree_to_draw = tree_id;
			Graphics2D    graphics = image.createGraphics();
			graphics.setPaint ( new Color ( 255, 255, 255 ) );
			graphics.fillRect ( 0, 0, image.getWidth(), image.getHeight() );
			Graphics2D g2 = image.createGraphics();
			paintComponent(g2);
			try{
				File file_name = new  File(folder_path.toString(),"TreeID_"+Integer.toString(tree_id)+"_image.png");
				System.out.println("Saving image: " +  file_name);
				ImageIO.write(image, "png", file_name);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
	}
}


