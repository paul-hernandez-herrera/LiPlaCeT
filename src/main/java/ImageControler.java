import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;

@SuppressWarnings("serial")
class ImageControler extends JPanel implements MouseListener, MouseWheelListener, MouseMotionListener{

    //an array list to save the position of ellipses in current slice
	//Variable shared for all Panels
    public static ArrayList<ArrayList<Ellipsoid>> ellipsoidList = new ArrayList<ArrayList<Ellipsoid>>();
    public static ArrayList<Ellipsoid>[] ellipsoid_selected_Panel =  new ArrayList[2];
    
    //time lapse stack will be shared by all panels
    public static ImagePlus [] timeLapseImage;
    
    private ImageProcessor temp_ip;
    
    Color ellipse_ClusterParent;
    private int index_ellipse = -1;
    
    static double [] mousePos_4Dstack = new double [3] ;

    ImagePlus stack2D_ip;

	private int stack_width, stack_heigth,stack_nSlices,  slice = 0, time_point = 0,dim1,dim2,nChannels, dim3;
	public int index_image = 0;
	private String ellipse_text = null;
	public double zoom =1.0;

	Point lineMovingSlice_pos;
    public int panel_pos_Row, panel_pos_Col;
    boolean move_syncronized_slice = true, draw_sphere3D = true;
    boolean flagMouseLeftDraggedMovingAllEllipses=false, flagMouseLeftDraggedMovingClusterEllipses=false,flagMouseDraggedMovingEllipsoid=false,flagMouseDraggedMovingOrthogonalView=false;
    
    public double scaleScreenWidth,scaleScreenHeight,anisotropy_dim1_2,anisotropy_dim1_3, draw_pos_fixed_X = 0, draw_pos_fixed_Y =0;
    BufferedImage[] imageToDisplay;    
    
    public static Calibration voxel_size;
    public static long ellapseTimeStart=-1, ellapseTimeFinished = -1, timeEllapsed;
    public static int  mouse_posX=0,mouse_posY=0, stack_nTimePoint, ellipsoidList_nTimePoint=0;
    public static String  z_PosString = ""; 
    //Image_Controler panel2;
    static ImageControler[][] allDisplayPanels;
    JLabel label_StackInfo, label_planeID;
    
    ExtractOrthogonalSlices ortPick; 
    
    
    public ImageControler(int time_point_ , Point panel_id_position) {    	
        setBorder(BorderFactory.createLineBorder(Color.black));
        
        //Adding listener to control current Panel
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);

        //getting stack information
        int[] nDimensions = timeLapseImage[0].getDimensions();
        nChannels = timeLapseImage.length;
        stack_width = nDimensions[0];
        stack_heigth = nDimensions[1];
        stack_nSlices = nDimensions[3];
        stack_nTimePoint = nDimensions[4];
        
        //variable to keep the image to display by current panel
        imageToDisplay = new BufferedImage[nChannels];
        
        //to select orthogonal slices
        ortPick = new ExtractOrthogonalSlices(timeLapseImage);  
  
		
        //array to track cells information
        if (ellipsoidList.size()==0) {
            for (int i=0;i<stack_nTimePoint;i++){
            	ellipsoidList.add(new ArrayList<Ellipsoid>());     	
            }        	
        }


        // initializing 
    	ellipsoid_selected_Panel[0] = new ArrayList<Ellipsoid>();
    	ellipsoid_selected_Panel[1] = new ArrayList<Ellipsoid>();
        
    	this.setLayout(new GridLayout(1, 3, 8, 8));
        //To display current time point and slice number
        label_StackInfo = new JLabel("");
        label_StackInfo.setFont(new Font("Serif", Font.PLAIN, 30));
        label_StackInfo.setForeground(Color.white);
        label_StackInfo.setHorizontalAlignment(SwingConstants.CENTER);
        label_StackInfo.setVerticalAlignment(SwingConstants.TOP);

		
		
        label_planeID = new JLabel("");
        label_planeID.setHorizontalAlignment(SwingConstants.RIGHT);
        label_planeID.setVerticalAlignment(SwingConstants.TOP);
        label_planeID.setFont(new Font("Serif", Font.PLAIN, 30));
        label_planeID.setForeground(Color.white);
        //planeID.setVerticalAlignment(SwingConstants.TOP);
        add(new JLabel(""));  
        add(label_StackInfo);  
		add(label_planeID); 		
		


		//calibration (pixel real size --- usually in microns)
		voxel_size = timeLapseImage[0].getCalibration();
		

        updatePanel(panel_id_position,time_point_);
        
		if (panel_pos_Row==0){
			set_labelOrthogonalView("XY");
		}else if (panel_pos_Row==1){
			set_labelOrthogonalView("XZ");
		}else if (panel_pos_Row==2){
			set_labelOrthogonalView("YZ");
		}        
    }
		
    
    public void updatePanel(Point panel_id_position,int time_point_){
        panel_pos_Row = (int) panel_id_position.getX();
        panel_pos_Col = (int) panel_id_position.getY();

        if (panel_pos_Row==0){
        	set_Dimension_StackRotated( stack_width, stack_heigth, stack_nSlices);//XYZ
        	anisotropy_dim1_2 = voxel_size.pixelWidth/voxel_size.pixelHeight;
        	anisotropy_dim1_3 = voxel_size.pixelWidth/voxel_size.pixelDepth;
        	
        	//set initial position to display the midpoint
            if (lineMovingSlice_pos==null){
            	lineMovingSlice_pos = new Point(dim1/2,dim2/2);
            }        	
        	
        }
        if (panel_pos_Row==1){
        	set_Dimension_StackRotated( stack_width, stack_nSlices, stack_heigth);//XZY
        	anisotropy_dim1_2 = voxel_size.pixelWidth/voxel_size.pixelDepth;
        	anisotropy_dim1_3 = voxel_size.pixelWidth/voxel_size.pixelHeight;
        }
        if (panel_pos_Row==2){
        	set_Dimension_StackRotated(stack_heigth, stack_nSlices, stack_width);//YZX
        	anisotropy_dim1_2 = voxel_size.pixelHeight/voxel_size.pixelDepth;
        	anisotropy_dim1_3 = voxel_size.pixelHeight/voxel_size.pixelWidth;
        }

        
        
        for (int i=0;i<nChannels;i++){
        	imageToDisplay[i]=null;
        }   
        //initial time point
        set_currentTime(time_point_);	
    }
    
    public void set_Dimension_StackRotated(int newWidth, int newHeight, int newDepth){
            dim1 = newWidth;
            dim2 = newHeight;
            dim3 = newDepth; 
    }
    
    public void set_all_display_panels(ImageControler[][] dp){
    	allDisplayPanels = dp;
    }
    
    public void set_move_syncronized_slice(boolean move_){
    	move_syncronized_slice = move_;
    }
    
    public void updateLabel(){
    	//update the text to display
    	if (PanelControlFeatures.displayLabels) 
    		label_StackInfo.setText("z: " + (int) (slice+1) + "   t: " + (int) (time_point));
    	else
    		label_StackInfo.setText("" );    	
    }
    
    public void set_labelOrthogonalView(String label_plane) {
    	label_planeID.setText(label_plane);
    }
    
    public int getNChannels(){
    	return nChannels;
    }
    
    public int get_slice(){
    	return slice;
    }
    
	public int get_nSlices(){
    	return dim3;
    }    

	public static int get_stack_nTimePoints(){
    	return stack_nTimePoint;
    }
	
	public static int get_ellipsoidList_nTimePoints(){
		//number of points to be used for analysis
		ellipsoidList_nTimePoint = ellipsoidList.size();
		if (ellipsoidList_nTimePoint>get_stack_nTimePoints()) {
			//return size just until the last tracked time point
			for (int tp_temp=0;tp_temp<ellipsoidList.size();tp_temp++) {
				if (ellipsoidList.get(tp_temp).size()>0)
					ellipsoidList_nTimePoint = tp_temp+1;
			}
		}
		//System.out.println("Ellipsoid size " + ellipsoidList_nTimePoint);
    	return (ellipsoidList_nTimePoint);
    }	
	
    
    public void set_currentTime(int updated_time) {    	
		updateIndexValue(slice, updated_time);
	}
    
    public int get_currentTime(){
    	return time_point;
    }
    
    public void set_currentSlice(int new_sliceNumber){
    	//update_the slice to display    	
    	if (new_sliceNumber<0){
    		new_sliceNumber = 0;
    	}
    	if (new_sliceNumber>=dim3){
    		new_sliceNumber = dim3-1;
    	}
    	updateIndexValue(new_sliceNumber, time_point);
    }
    
    public void updateIndexValue(int new_slice, int new_time_point){    	
    	//update time_point and slice
    	time_point = new_time_point;
    	slice = new_slice;
    	//index_image = time_point*(image_NChannels*image_NSlices) + slice*image_NChannels + 1;
    	index_image = time_point*(dim3) + slice + 1;
    	
    	//update the text to display
    	updateLabel();
    	    	
    	//update the image according to the new image
    	updateImage();
    	
    	//paint the panel with new information
    	repaint();
    	
    }      
    
    public void updateImage() {
    	//updating the images to display according to the current index
    	if (panel_pos_Row==0){
    		imageToDisplay = ortPick.getSliceXY(index_image);
    	}
    	if(panel_pos_Row==1){
    		imageToDisplay = ortPick.getSliceXZ(index_image);
    	}
    	if(panel_pos_Row==2){
    		imageToDisplay = ortPick.getSliceYZ(index_image);
    	}    	   	
    }
    
    
    
    public void add_ellipsoid(int tp, double posX, double posY, double posZ, double width_, double height_, double depth_){      	
    	ellipsoidList.get(tp).add(new Ellipsoid(tp, posX,posY,posZ,width_,height_,depth_));
    }
    
    private boolean checkIfClickInsideEllipsoid(double posX, double posY, double posZ){
    	//check if click is inside the ellipsoid and also return the index of the ellipsoid
    	index_ellipse = 0;
    	for (Ellipsoid ellipsoid:ellipsoidList.get(time_point)){
    		if (ellipsoid.contains(posX,posY,posZ))
    			return true;
    		index_ellipse++;
    	}
    	return false;
    }
    
    private void move_ellipsoid(int index_, double new_posX, double new_posY, double new_posZ){
    	ellipsoidList.get(time_point).get(index_).set_position(new_posX, new_posY, new_posZ);
    }
    
    
    public void distorsion_screen(){
    	//scaling due to screen display
    	scaleScreenWidth = this.getBounds().getWidth()/dim1;
    	scaleScreenHeight = anisotropy_dim1_2*this.getBounds().getHeight()/dim2;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g); 
        
        //in case that the panel is resized
        distorsion_screen();
        
        
        //draw current slice
        paint_slice(g);
        
        
        //draw ellipses
        if (PanelControlFeatures.displayEllipse){
            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(PanelControlFeatures.opacityToDisplayEllipse/100)));
            for (Ellipsoid ellipsoid:ellipsoidList.get(time_point)){
            	drawEllipses(g,ellipsoid, panel_pos_Row);
            }        	
        }    

        //draw blue line
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));        
        
        if (panel_pos_Row==0){
        	//drawing the point of visualization in the XY plane
        	g.setColor(Color.BLUE);
        	if (allDisplayPanels[1][0].panel_pos_Row==1){
        		//Display XZ visualization --- draw y axis
        		g.fillRect(0,lineMovingSlice_pos.y-1,this.getBounds().width,2);
        	}else if (allDisplayPanels[1][0].panel_pos_Row==2){
        		//Display XZ visualization --- draw y axis
        		g.fillRect(lineMovingSlice_pos.x-1,0,2,this.getBounds().height);        		
        	}
        } 
    }
    
    public void paint_slice(Graphics g) {
        if (PanelControlFeatures.display_Channel[0] | PanelControlFeatures.display_Channel[1]){
        	ImagePlus[] images = new ImagePlus[2];
        	images[0]= new ImagePlus(null, imageToDisplay[0]);
        	images[1]= new ImagePlus(null, imageToDisplay[1]);
        	    	           	
        	      	
    		int w = images[0].getWidth();
    		int h = images[0].getHeight();
    		int slices = images[0].getNSlices();
    		int frames = images[0].getNFrames();
    		ImageStack stack2D_temp = new ImageStack(w, h);
    		

    		Color[] colors = new Color[2];
    		int[] c_id = new int[2];
    		if (PanelControlFeatures.display_Channel[0] & PanelControlFeatures.display_Channel[1]){
    			//Display both channels
    			c_id[0]=0; colors[0] = PanelControlFeatures.channelColors[0];
    			c_id[1]=1; colors[1] = PanelControlFeatures.channelColors[1];
    		}else if (PanelControlFeatures.display_Channel[0]) {
    			//Display only channel1
    			c_id[0]=0; colors[0] = Color.gray;
    			c_id[1]=0; colors[1] = Color.gray;    			
    		}else if (PanelControlFeatures.display_Channel[1]) {
    			//Display only channel2
    			c_id[0]=1; colors[0] = Color.gray;
    			c_id[1]=1; colors[1] = Color.gray;
    		}    
    		
  		
    		
    		//Getting image to display
    		int[] index = new int[2];
    		for (int t=0; t<frames; t++) {
    			for (int z=0; z<slices; z++) {
    				for (int c=0; c<2; c++) {
    					temp_ip = images[c_id[c]].getImageStack().getProcessor(index[c]+1);
    					temp_ip = temp_ip.duplicate();	    					
    					stack2D_temp.addSlice(null, temp_ip);
    					index[c]++;
    				}
    			}
    		}        	

    		
	        
    		stack2D_ip = new ImagePlus(null, stack2D_temp);
    		stack2D_ip.setDimensions(2, slices, frames);
    		stack2D_ip = new CompositeImage(stack2D_ip, IJ.COMPOSITE);  

    		//Setting the assigned color for each channel
    		for (int c=0; c<2; c++) {
    			ImageProcessor ip = images[c].getProcessor();
    			IndexColorModel cm = (IndexColorModel)ip.getColorModel();
    			LUT lut = null;
    			if (c<colors.length && colors[c]!=null) {
    				lut = LUT.createLutFromColor(colors[c]);
    				if (MenuBarGUI.brightnessVal_C[c].x==-1){
    					lut.min = ip.getMin();
    					lut.max = ip.getMax();   					
    				}else{
    					lut.min = MenuBarGUI.brightnessVal_C[c].x;
    					lut.max = MenuBarGUI.brightnessVal_C[c].y;    					
    				}
    			} else {
    				System.out.println("LUT");
    				lut =  new LUT(cm, ip.getMin(), ip.getMax());
    			}
    			((CompositeImage)stack2D_ip).setChannelLut(lut, c+1);
    		}
    		stack2D_ip.setOpenAsHyperStack(true);   
    		
 		

    		//Draw image in current panel
            g.drawImage(stack2D_ip.getBufferedImage(),0,0, this.getBounds().width, this.getBounds().height,this); 	         
        }    	
    	
    }

    public void drawEllipses(Graphics g,Ellipsoid ellipsoid, int id_projection){
    	double pos1=0,width1=0,pos2=0,height1=0,pos3=0, depth_scale_ellipse=0;
    	if (id_projection==0){
    		//we are displaying xy plane
    		pos1 = (ellipsoid.getX()-ExtractOrthogonalSlices.roi_corner_posX)*ExtractOrthogonalSlices.zoom;width1=ellipsoid.getWidth();
    		pos2 = (ellipsoid.getY()-ExtractOrthogonalSlices.roi_corner_posY)*ExtractOrthogonalSlices.zoom;height1=ellipsoid.getHeight();
    		pos3 = slice/anisotropy_dim1_3;
    		
    		depth_scale_ellipse = 1- Math.abs(ellipsoid.getZ()-pos3)/ellipsoid.getDepth();
    		width1 = width1*depth_scale_ellipse*ExtractOrthogonalSlices.zoom;
    		height1 = height1*depth_scale_ellipse*ExtractOrthogonalSlices.zoom;
    	}else if (id_projection==1){
    		//we are displaying xz plane
    		pos1 = (ellipsoid.getX()-ExtractOrthogonalSlices.roi_corner_posX)*ExtractOrthogonalSlices.zoom;width1=ellipsoid.getWidth();
    		pos2 = (stack_nSlices-1)/anisotropy_dim1_2-ellipsoid.getZ();height1=ellipsoid.getDepth();
    		pos3 = slice/anisotropy_dim1_3;    		
    		
    		depth_scale_ellipse = 1- Math.abs(ellipsoid.getY()-pos3)/ellipsoid.getHeight();
    		width1 = width1*depth_scale_ellipse*ExtractOrthogonalSlices.zoom;
    		height1 = height1*depth_scale_ellipse;    		
    	}else if (id_projection==2){
    		//we are displaying yz plane
    		pos1 = (ellipsoid.getY()-ExtractOrthogonalSlices.roi_corner_posY)*ExtractOrthogonalSlices.zoom;width1=ellipsoid.getHeight();
    		pos2 = (stack_nSlices-1)/anisotropy_dim1_2-ellipsoid.getZ();height1=ellipsoid.getDepth();
    		pos3 = slice/anisotropy_dim1_3;
    		
    		depth_scale_ellipse = 1- Math.abs(ellipsoid.getX()-pos3)/ellipsoid.getWidth();
    		width1 = width1*depth_scale_ellipse*ExtractOrthogonalSlices.zoom;
    		height1 = height1*depth_scale_ellipse;    
    	}    	
    	

		
		if (depth_scale_ellipse>0){
			//drawing the ellipsoid only if it is inside the Field Of View
			g.setColor(ellipsoid.getColor());
			//((Graphics2D) g).setStroke(new BasicStroke(3));
			g.fillOval((int)(scaleScreenWidth*(pos1-width1)),(int)(scaleScreenHeight*(pos2-height1)),(int)(scaleScreenWidth*(2*width1)),(int)(scaleScreenHeight*(2*height1)));			
			if (id_projection==0) {
				g.setColor(Color.WHITE);
				if ((ellipsoid.getColor().getGreen()+ellipsoid.getColor().getBlue()+ellipsoid.getColor().getRed())>(3*255/2))
					g.setColor(Color.BLACK);
				if (MenuBarGUI.set_sphere_label_as_cycle_number) {
					ellipse_text = Integer.toString(ellipsoid.cell_cycle);
					g.setFont(new Font("TimesRoman", Font.PLAIN, (int)(scaleScreenWidth*1.5*width1 -3*ellipse_text.length())));
				}else {
					g.setColor(Color.BLACK);
					ellipse_text = Integer.toString(ellipsoid.sort_ID) + ",\n" + Integer.toString(ellipsoid.sort_ID_parent);
					g.setFont(new Font("TimesRoman", Font.PLAIN, (int)(scaleScreenWidth*1.5*width1/depth_scale_ellipse -ellipse_text.length())));
				}				
				g.drawString(ellipse_text,(int)(scaleScreenWidth*(pos1-0.4*width1)-ellipse_text.length()), (int)(scaleScreenHeight*(pos2 + 0.4*height1)-ellipse_text.length()));
				//drawStringVertical(g, ellipse_text,(int)(scaleScreenWidth*(pos1-0.4*width1)-ellipse_text.length()), (int)(scaleScreenHeight*(pos2 + 0.4*height1)-ellipse_text.length()));
			}
		}
    }
    private void drawStringVertical(Graphics g, String text, int x, int y) {
        int lineHeight = g.getFontMetrics().getHeight();
        y = y - lineHeight;
        for (String line : text.split("\n"))
            g.drawString(line, x, y += lineHeight);
    }
    
    public static void repaintPanels(){
    	for (int i=0;i<2;i++){
    		for (int j=0;j<2;j++){
    			allDisplayPanels[i][j].repaint();	        			
    		}
    	} 
    }
    
    public void update_Mouse_position_relative2stack(double posX_panel,double posY_panel,double pos_slice) {
    	//getting the mouse coordinates in XYZ coordinates
    	if (panel_pos_Row==0){
    		//plane XY
    		mousePos_4Dstack[0] = ExtractOrthogonalSlices.roi_corner_posX + (posX_panel/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
    		mousePos_4Dstack[1] = ExtractOrthogonalSlices.roi_corner_posY + (posY_panel/scaleScreenHeight)/ExtractOrthogonalSlices.zoom;
    		mousePos_4Dstack[2] = (pos_slice/anisotropy_dim1_3);
    	}else if (panel_pos_Row==1){
    		//plane XZ
    		mousePos_4Dstack[0] = ExtractOrthogonalSlices.roi_corner_posX + (posX_panel/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
    		mousePos_4Dstack[1] = (pos_slice/anisotropy_dim1_3);
    		mousePos_4Dstack[2] = (stack_nSlices-1)/anisotropy_dim1_2-posY_panel/scaleScreenHeight;
    	}else if (panel_pos_Row==2){
    		//plane YZ
    		mousePos_4Dstack[0] = (pos_slice/anisotropy_dim1_3);
    		mousePos_4Dstack[1] = ExtractOrthogonalSlices.roi_corner_posY + (posX_panel/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
    		mousePos_4Dstack[2] = (stack_nSlices-1)/anisotropy_dim1_2-posY_panel/scaleScreenHeight;
    	}   	
    }
    
    public void remove_ellipsoid_from_selected_Panel(int panel_position) {
		ImageControler.ellipsoid_selected_Panel[panel_position].get(0).setSelected(false);
		ImageControler.ellipsoid_selected_Panel[panel_position].remove(0);   	
    }
    
    
    
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		//update mouse position relative to the stack
		update_Mouse_position_relative2stack(e.getX(),e.getY(),slice);
		
		if (SwingUtilities.isLeftMouseButton(e)){
			//create a new ellipsoid        	
			if (!checkIfClickInsideEllipsoid(mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2])){
				//current left click is not inside an ellipsoid
				Undo.add_State_to_Undo_List(time_point,1);
				add_ellipsoid(time_point,mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2],25,25,25);
			}else{
				if (PanelControlFeatures.flagCellDivision){
					//clicked point is inside an already created ellipsoid
					if (ellipsoidList.get(time_point).get(index_ellipse).getSelected()){
						//unselect current ellipse
						ellipsoidList.get(time_point).get(index_ellipse).setSelected(false);
						//removing ellipsed from selected
						ImageControler.ellipsoid_selected_Panel[panel_pos_Col].remove(ellipsoidList.get(time_point).get(index_ellipse));
					}else{		
						//function to select or unselect spheres
						if (time_point==0) {
							if (ImageControler.ellipsoid_selected_Panel[0].size()==0) {
								//select current ellipse
								ellipsoidList.get(time_point).get(index_ellipse).setSelected(true);
								ImageControler.ellipsoid_selected_Panel[0].add(ellipsoidList.get(time_point).get(index_ellipse));
							}else {
								//only select spheres in the first panel and eliminate the spheres in the second
								select_n_spheres_in_GUI_from_clicked_panel(Integer.MAX_VALUE,true);
							}								
						}else {
							
							if (panel_pos_Col==0){
								select_n_spheres_in_GUI_from_clicked_panel(1,false);
							}else if (panel_pos_Col==1){
								select_n_spheres_in_GUI_from_clicked_panel(2,false);	
								
								Ellipsoid current_parent = ellipsoidList.get(time_point).get(index_ellipse).getParent();
								
								if (current_parent!=null) {
									if (ImageControler.ellipsoid_selected_Panel[0].size()>0){
										remove_ellipsoid_from_selected_Panel(0);
									}
									//select current ellipse
									current_parent.setSelected(true);
									ImageControler.ellipsoid_selected_Panel[0].add(current_parent);
								}
							}							

						}					
					}
				}else if (PanelControlFeatures.flagMovingCells | PanelControlFeatures.flagChangeCellCycle) {
					select_n_spheres_in_GUI_from_clicked_panel(1,true);
					
				}else if (PanelControlFeatures.flagUpdatingZpos) {
					select_n_spheres_in_GUI_from_clicked_panel(Integer.MAX_VALUE,true);				
				}else if (PanelControlFeatures.flagCreateConnection) {
					if (panel_pos_Col==1) {
						select_n_spheres_in_GUI_from_clicked_panel(2,true);
					}else if (get_currentTime()==0) {
						select_n_spheres_in_GUI_from_clicked_panel(1,true);
					}
				}
				repaintPanels();
			}
			
		}
		if (SwingUtilities.isRightMouseButton(e)){
			//remove ellipse only if ellipses are displayed
			if (PanelControlFeatures.displayEllipse & checkIfClickInsideEllipsoid(mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2])){
				Undo.add_State_to_Undo_List(time_point,2);
				
				//delete in case it is selected
				ellipsoid_selected_Panel[0].remove(ellipsoidList.get(time_point).get(index_ellipse));	
				ellipsoid_selected_Panel[1].remove(ellipsoidList.get(time_point).get(index_ellipse));
				
				//get ellipsoid to remove
				Ellipsoid ellipsoid_to_remove = ellipsoidList.get(time_point).get(index_ellipse);
				
				//remove ellipsoid from the list
				ellipsoidList.get(time_point).remove(index_ellipse);
				
				//remove any child of the ellipsoid removed
				//System.out.println("Children size: " + ellipsoid_to_remove.getChildren().size());
				for (Ellipsoid children:ellipsoid_to_remove.getChildren())
					children.setParent(null);
				
			}
		}
		for (int i=0;i<2;i++)
			allDisplayPanels[i][panel_pos_Col].repaint();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		flagMouseDraggedMovingEllipsoid=false;
		flagMouseDraggedMovingOrthogonalView = false;
		flagMouseLeftDraggedMovingAllEllipses = false;
		flagMouseLeftDraggedMovingClusterEllipses = false;
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub		
		int min_sphere_size = 2;
		int notches = e.getWheelRotation();
		
		update_Mouse_position_relative2stack(e.getX(),e.getY(),slice);
		
		if (PanelControlFeatures.displayEllipse & checkIfClickInsideEllipsoid(mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2])){
			//mouse wheel moved and inside an ellipse: Increase/decrease ellipse size
			
			//make sure new sphere size is bigger than min_sphere size
			Ellipsoid ce = ellipsoidList.get(time_point).get(index_ellipse);			
			if (((ce.getWidth() - notches) >=min_sphere_size) & ((ce.getHeight() - notches) >=min_sphere_size) & ((ce.getDepth() - notches)>=min_sphere_size)) {
				//updating ellipse size
				ellipsoidList.get(time_point).get(index_ellipse).setAxis( ce.getWidth()-notches,ce.getHeight() - notches, ce.getDepth() - notches);				
			}
				
			

			
			for (int i=0;i<2;i++)
				allDisplayPanels[i][panel_pos_Col].repaint();
		}else {
			//mouse wheel moved and outside an ellipse: update slice position
			
			int toMoveRow = -1;
			int endCol = -1;
			if(move_syncronized_slice){
				//move all the panels in the current row
				endCol=1;	
			}else{
				//move only current slice
				endCol=2;
			}
			
			
			if (panel_pos_Row==0){
				toMoveRow = 0;
			}else{
				toMoveRow = 1;
			}	
			
			for (int i=0; i<endCol; i++){
				allDisplayPanels[toMoveRow][i].set_currentSlice(allDisplayPanels[toMoveRow][i].slice - notches);
				if (panel_pos_Row==1){
					allDisplayPanels[0][i].lineMovingSlice_pos.y =(int) (allDisplayPanels[toMoveRow][i].get_slice()*allDisplayPanels[0][i].scaleScreenHeight);
					allDisplayPanels[0][i].repaint();
				}else if(panel_pos_Row==2){
					allDisplayPanels[0][i].lineMovingSlice_pos.x =(int) (allDisplayPanels[toMoveRow][i].get_slice()*allDisplayPanels[0][i].scaleScreenWidth);
					allDisplayPanels[0][i].repaint();
				}
			}
			
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		//System.out.println(slice);
		update_Mouse_position_relative2stack(e.getX(),e.getY(),slice);
	
		if (flagMouseDraggedMovingEllipsoid){
			if (panel_pos_Row ==0) {
				//moving sphere in the XY plane
				move_ellipsoid(index_ellipse,mousePos_4Dstack[0],mousePos_4Dstack[1], ellipsoidList.get(time_point).get(index_ellipse).getZ());
			}else if (panel_pos_Row ==1) {
				//moving sphere in the XY plane
				move_ellipsoid(index_ellipse,mousePos_4Dstack[0],ellipsoidList.get(time_point).get(index_ellipse).getY(), mousePos_4Dstack[2]);				
			}else if (panel_pos_Row ==2) {
				//moving sphere in the XY plane
				move_ellipsoid(index_ellipse,ellipsoidList.get(time_point).get(index_ellipse).getX(),mousePos_4Dstack[1], mousePos_4Dstack[2]);				
			}
			
			for (int i=0;i<2;i++)
				allDisplayPanels[i][panel_pos_Col].repaint();			
		}
		else if (flagMouseLeftDraggedMovingAllEllipses & panel_pos_Row==0) {
			//moving all ellipses in current panel XY
	    	for (int i=0; i<ellipsoidList.get(time_point).size();i++)
	    		move_ellipsoid(i,ellipsoidList.get(time_point).get(i).getX()+ mousePos_4Dstack[0] -draw_pos_fixed_X,ellipsoidList.get(time_point).get(i).getY() + mousePos_4Dstack[1] -draw_pos_fixed_Y,ellipsoidList.get(time_point).get(i).getZ());
	    	
			draw_pos_fixed_X = ExtractOrthogonalSlices.roi_corner_posX + (e.getX()/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
			draw_pos_fixed_Y = ExtractOrthogonalSlices.roi_corner_posY + (e.getY()/scaleScreenHeight)/ExtractOrthogonalSlices.zoom;	    	
			for (int i=0;i<2;i++)
				allDisplayPanels[i][panel_pos_Col].repaint();	    	
			
		}else if (flagMouseLeftDraggedMovingClusterEllipses & panel_pos_Row==0) {
	    	//Moving only ellipses with a specific color	
	    	for (int i=0; i<ellipsoidList.get(time_point).size();i++){
	    		if (ellipsoidList.get(time_point).get(i).color.equals(ellipse_ClusterParent) & ellipse_ClusterParent!=null)
	    			move_ellipsoid(i,ellipsoidList.get(time_point).get(i).getX()+ mousePos_4Dstack[0] -draw_pos_fixed_X,ellipsoidList.get(time_point).get(i).getY() + mousePos_4Dstack[1] -draw_pos_fixed_Y,ellipsoidList.get(time_point).get(i).getZ());
	    	}
			draw_pos_fixed_X = ExtractOrthogonalSlices.roi_corner_posX + (e.getX()/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
			draw_pos_fixed_Y = ExtractOrthogonalSlices.roi_corner_posY + (e.getY()/scaleScreenHeight)/ExtractOrthogonalSlices.zoom;	    	
			for (int i=0;i<2;i++)
				allDisplayPanels[i][panel_pos_Col].repaint();	    	
			
		}
		else if (flagMouseDraggedMovingOrthogonalView){
			 if (allDisplayPanels[1][0].panel_pos_Row==1){
					//the current visualization of panel1 is displaying the XZ axis
					//just move if we are doing the draging in PanelXY
					if (panel_pos_Row==0){
							if (e.getY()>0){
								lineMovingSlice_pos.y = e.getY();
							}
							allDisplayPanels[1][panel_pos_Col].set_currentSlice((int) (ExtractOrthogonalSlices.roi_corner_posY + (lineMovingSlice_pos.y/scaleScreenHeight)/ExtractOrthogonalSlices.zoom));
							repaint();
					}
			 }else if(allDisplayPanels[1][0].panel_pos_Row==2){
					//the current visualization of panel1 is displaying the YZ axis
					//just move if we are doing the draging in PanelXY
					if (panel_pos_Row==0){
							if (e.getY()>0){
								lineMovingSlice_pos.x = e.getX();
							}
							allDisplayPanels[1][panel_pos_Col].set_currentSlice((int)(ExtractOrthogonalSlices.roi_corner_posX + (lineMovingSlice_pos.x/scaleScreenWidth)/ExtractOrthogonalSlices.zoom));
							repaint();
					}
				}		
			
		}else if (PanelControlFeatures.displayEllipse & !checkIfClickInsideEllipsoid(mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2]) & SwingUtilities.isLeftMouseButton(e) & panel_pos_Row==0 & PanelControlFeatures.flagMovingCells){			
			draw_pos_fixed_X = ExtractOrthogonalSlices.roi_corner_posX + (e.getX()/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
			draw_pos_fixed_Y = ExtractOrthogonalSlices.roi_corner_posY + (e.getY()/scaleScreenHeight)/ExtractOrthogonalSlices.zoom;
			flagMouseLeftDraggedMovingClusterEllipses = true;
			ellipse_ClusterParent = null;
			if (ellipsoid_selected_Panel[0].size()>0)
				ellipse_ClusterParent = ellipsoid_selected_Panel[0].get(0).color;
			if (ellipsoid_selected_Panel[1].size()>0)
				ellipse_ClusterParent = ellipsoid_selected_Panel[1].get(0).color;	
			
			
		}
		else if (PanelControlFeatures.displayEllipse & checkIfClickInsideEllipsoid(mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2])){			
			if (panel_pos_Row ==0) {
				//moving sphere in the XY plane
				move_ellipsoid(index_ellipse,mousePos_4Dstack[0],mousePos_4Dstack[1], ellipsoidList.get(time_point).get(index_ellipse).getZ());
			}else if (panel_pos_Row ==1) {
				//moving sphere in the XZ plane
				move_ellipsoid(index_ellipse,mousePos_4Dstack[0],ellipsoidList.get(time_point).get(index_ellipse).getY(), mousePos_4Dstack[2]);				
			}else if (panel_pos_Row ==2) {
				//moving sphere in the YZ plane
				move_ellipsoid(index_ellipse,ellipsoidList.get(time_point).get(index_ellipse).getX(),mousePos_4Dstack[1], mousePos_4Dstack[2]);				
			}
			for (int i=0;i<2;i++)
				allDisplayPanels[i][panel_pos_Col].repaint();
			flagMouseDraggedMovingEllipsoid = true;
		}
		else if (PanelControlFeatures.displayEllipse & !checkIfClickInsideEllipsoid(mousePos_4Dstack[0], mousePos_4Dstack[1],mousePos_4Dstack[2]) & SwingUtilities.isLeftMouseButton(e) & panel_pos_Row==0){
			draw_pos_fixed_X = ExtractOrthogonalSlices.roi_corner_posX + (e.getX()/scaleScreenWidth)/ExtractOrthogonalSlices.zoom;
			draw_pos_fixed_Y = ExtractOrthogonalSlices.roi_corner_posY + (e.getY()/scaleScreenHeight)/ExtractOrthogonalSlices.zoom;
			flagMouseLeftDraggedMovingAllEllipses = true;
			Undo.add_State_to_Undo_List(time_point,1);
		}
		else if (allDisplayPanels[1][0].panel_pos_Row==1){
			//the current visualization of panel1 is displaying the XZ axis
			//just move if we are doing the draging in PanelXY
			if (panel_pos_Row==0){
				if(Math.abs(lineMovingSlice_pos.y - e.getY())<10){
					if (e.getY()>0){
						lineMovingSlice_pos.y = e.getY();
					}
					allDisplayPanels[1][panel_pos_Col].set_currentSlice((int) (ExtractOrthogonalSlices.roi_corner_posY + (lineMovingSlice_pos.y/scaleScreenHeight)/ExtractOrthogonalSlices.zoom));
					
					repaint();
				}
			}
			flagMouseDraggedMovingOrthogonalView = true;
		}
		else if (allDisplayPanels[1][0].panel_pos_Row==2){
			//the current visualization of panel1 is displaying the YZ axis
			//just move if we are doing the draging in PanelXY
			if (panel_pos_Row==0){
				if(Math.abs(lineMovingSlice_pos.x- e.getX())<10){
					if (e.getY()>0){
						lineMovingSlice_pos.x = e.getX();
					}
					allDisplayPanels[1][panel_pos_Col].set_currentSlice((int)(ExtractOrthogonalSlices.roi_corner_posX + (lineMovingSlice_pos.x/scaleScreenWidth)/ExtractOrthogonalSlices.zoom));
					
					repaint();
				}
			}
			flagMouseDraggedMovingOrthogonalView=true;
		}		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub	
	}

	public void keyPressed(KeyEvent arg0) {
		//System.out.println("Inside keyListener --- KeyPressed");
		
		//getting mouse position
		Point p = allDisplayPanels[0][0].getMousePosition();
		
		
		int id_panel = checkIfMouseInsideAnyPanelXY(p);
		//System.out.println("Mouse inside panel: " + id_panel);
		if (id_panel>=0) {		
			//updating mouse position
			p = allDisplayPanels[0][id_panel].getMousePosition();
			int key = arg0.getKeyCode();
			char number_char = arg0.getKeyChar();
			
			//update mouse position relative to the stack
			ImageControler.allDisplayPanels[0][id_panel].update_Mouse_position_relative2stack(p.getX(),p.getY(),slice);			
			double theta = 0;
			
			if (key == KeyEvent.VK_PLUS || key== KeyEvent.VK_ADD || key== KeyEvent.VK_E || key == KeyEvent.VK_M){
				if (PanelControlFeatures.flagChangeCellCycle) {
					if (ellipsoid_selected_Panel[0].size()>0)
						ellipsoid_selected_Panel[0].get(0).cell_cycle = ellipsoid_selected_Panel[0].get(0).cell_cycle +1;;
					if (ellipsoid_selected_Panel[1].size()>0)
						ellipsoid_selected_Panel[1].get(0).cell_cycle = ellipsoid_selected_Panel[1].get(0).cell_cycle +1;				
				}else {					
					zoom = zoom+0.5;
					update_image_all_panels_to_zoom_or_moveposition(id_panel);
				}		
			}				
			else if (key==KeyEvent.VK_MINUS || key==KeyEvent.VK_SUBTRACT || key== KeyEvent.VK_D || key == KeyEvent.VK_L){
				if (PanelControlFeatures.flagChangeCellCycle) {
					if (ellipsoid_selected_Panel[0].size()>0)
						ellipsoid_selected_Panel[0].get(0).cell_cycle = ellipsoid_selected_Panel[0].get(0).cell_cycle -1;;
					if (ellipsoid_selected_Panel[1].size()>0)
						ellipsoid_selected_Panel[1].get(0).cell_cycle = ellipsoid_selected_Panel[1].get(0).cell_cycle -1;					
				}else {
					zoom = zoom-0.5;
					if (zoom<1)
						zoom=1;
					update_image_all_panels_to_zoom_or_moveposition(id_panel);
				}
			}else if (key== KeyEvent.VK_LEFT){
				//OPTION 1: ROTATE LEFT
				theta = -5*Math.PI/180;
	            for (Ellipsoid ellipsoid:ellipsoidList.get(allDisplayPanels[0][id_panel].time_point)) {
	            	ellipsoid.set_position(Math.cos(theta)*(ellipsoid.getX()-mousePos_4Dstack[0]) - Math.sin(theta)*(ellipsoid.getY()-mousePos_4Dstack[1]) +mousePos_4Dstack[0], Math.sin(theta)*(ellipsoid.getX()-mousePos_4Dstack[0]) + Math.cos(theta)*(ellipsoid.getY()-mousePos_4Dstack[1]) + mousePos_4Dstack[1] , ellipsoid.getZ());
	            }
	            	
			}else if (key == KeyEvent.VK_R || key== KeyEvent.VK_RIGHT){
				//OPTION 2: ROTATE RIGHT				
				theta = 5*Math.PI/180;
	            for (Ellipsoid ellipsoid:ellipsoidList.get(allDisplayPanels[0][id_panel].time_point)) {
	            	ellipsoid.set_position(Math.cos(theta)*(ellipsoid.getX()-mousePos_4Dstack[0]) - Math.sin(theta)*(ellipsoid.getY()-mousePos_4Dstack[1]) +mousePos_4Dstack[0], Math.sin(theta)*(ellipsoid.getX()-mousePos_4Dstack[0]) + Math.cos(theta)*(ellipsoid.getY()-mousePos_4Dstack[1]) + mousePos_4Dstack[1] , ellipsoid.getZ());
	            }			
			}else if (key== KeyEvent.VK_UP){
				//OPTION 3: MOVE CELLS UP	
				if (PanelControlFeatures.flagMovingCells) {
					
					ellipse_ClusterParent = null;
					if (ellipsoid_selected_Panel[0].size()>0)
						ellipse_ClusterParent = ellipsoid_selected_Panel[0].get(0).color;
					if (ellipsoid_selected_Panel[1].size()>0)
						ellipse_ClusterParent = ellipsoid_selected_Panel[1].get(0).color;
		            for (Ellipsoid ellipsoid:ellipsoidList.get(allDisplayPanels[0][id_panel].time_point)) {
		            	if (ellipsoid.color.equals(ellipse_ClusterParent) & ellipse_ClusterParent!=null)
		            		ellipsoid.set_position(ellipsoid.getX(), ellipsoid.getY(), ellipsoid.getZ()+1/allDisplayPanels[1][0].anisotropy_dim1_2);
		            }										
				}else {
		            for (Ellipsoid ellipsoid:ellipsoidList.get(allDisplayPanels[0][id_panel].time_point)) {
		            	ellipsoid.set_position(ellipsoid.getX(), ellipsoid.getY(), ellipsoid.getZ()+ 1/allDisplayPanels[1][0].anisotropy_dim1_2);
		            }			
				}
				ellipse_ClusterParent = null;			
			}else if (key== KeyEvent.VK_DOWN){
				//OPTION 4: MOVE CELLS DOWN		
				if (PanelControlFeatures.flagMovingCells) {
					ellipse_ClusterParent = null;
					if (ellipsoid_selected_Panel[0].size()>0)
						ellipse_ClusterParent = ellipsoid_selected_Panel[0].get(0).color;
					if (ellipsoid_selected_Panel[1].size()>0)
						ellipse_ClusterParent = ellipsoid_selected_Panel[1].get(0).color;
		            for (Ellipsoid ellipsoid:ellipsoidList.get(allDisplayPanels[0][id_panel].time_point)) {
		            	if (ellipsoid.color.equals(ellipse_ClusterParent) & ellipse_ClusterParent!=null)
		            		ellipsoid.set_position(ellipsoid.getX(), ellipsoid.getY(), ellipsoid.getZ()-1/allDisplayPanels[1][0].anisotropy_dim1_2);
		            }										
				}else {
		            for (Ellipsoid ellipsoid:ellipsoidList.get(allDisplayPanels[0][id_panel].time_point)) {
		            	ellipsoid.set_position(ellipsoid.getX(), ellipsoid.getY(), ellipsoid.getZ()-1/allDisplayPanels[1][0].anisotropy_dim1_2);
		            }			
				}
				ellipse_ClusterParent = null;		
			}else if ((number_char >= '0') && (number_char <= '9') && PanelControlFeatures.flagUpdatingZpos){
	            if (ellapseTimeStart==-1) {
	            	ellapseTimeStart= System.currentTimeMillis();
	            }
	            else {
	            	ellapseTimeFinished = System.currentTimeMillis(); 
	            	timeEllapsed =ellapseTimeFinished  - ellapseTimeStart;
	            	if (timeEllapsed>1000 | z_PosString.length()==2) {
	            		z_PosString = "";
	            		ellapseTimeStart = ellapseTimeFinished;
	            	}            	
	            	
	            }
	            z_PosString+=number_char;
	            if (Integer.parseInt(z_PosString) >(stack_nSlices))
	            	z_PosString = Integer.toString(stack_nSlices);
	            for (Ellipsoid ellipsoid:ellipsoid_selected_Panel[0]) {
	            	ellipsoid.set_position(ellipsoid.getX(), ellipsoid.getY(), (Double.parseDouble(z_PosString)-1)/anisotropy_dim1_3);
	            }
	            for (Ellipsoid ellipsoid:ellipsoid_selected_Panel[1]) {
	            	ellipsoid.set_position(ellipsoid.getX(), ellipsoid.getY(), (Double.parseDouble(z_PosString)-1)/anisotropy_dim1_3);
	            }	         
	            if (ellipsoid_selected_Panel[0].size()>0)
	            	allDisplayPanels[0][0].set_currentSlice(Integer.parseInt(z_PosString)-1);
	            if (ellipsoid_selected_Panel[1].size()>0)
	            	allDisplayPanels[0][1].set_currentSlice(Integer.parseInt(z_PosString)-1);	            
	            //System.out.println(z_PosString);
				
			}
			else {
				update_image_all_panels_to_zoom_or_moveposition(id_panel);
			}
	    	
	    	repaintPanels();		
			
		}
	}
	
	public void update_image_all_panels_to_zoom_or_moveposition(int id_panel){
		allDisplayPanels[0][id_panel].ortPick.update_zoom_info(zoom,(int) ImageControler.mousePos_4Dstack[0],(int) ImageControler.mousePos_4Dstack[1]);
		
		allDisplayPanels[0][0].ortPick.getSliceXY(allDisplayPanels[0][0].index_image);
		allDisplayPanels[0][1].ortPick.getSliceXY(allDisplayPanels[0][1].index_image);
    	if(allDisplayPanels[1][0].panel_pos_Row==1){
    		allDisplayPanels[1][0].ortPick.getSliceXZ(allDisplayPanels[1][0].index_image);
    		allDisplayPanels[1][1].ortPick.getSliceXZ(allDisplayPanels[1][1].index_image);
    	}	
    	if(allDisplayPanels[1][0].panel_pos_Row==2){
    		//System.out.println("INSIDE PANEL 2");
    		allDisplayPanels[1][0].ortPick.getSliceYZ(allDisplayPanels[1][0].index_image);
    		allDisplayPanels[1][1].ortPick.getSliceYZ(allDisplayPanels[1][1].index_image);
    	}
	}
	public void select_n_spheres_in_GUI_from_clicked_panel(int n, boolean remove_spheres_from_other_panel) {
		//this function allows to select only a single cell in the GUI
		
		if (ellipsoidList.get(time_point).get(index_ellipse).getSelected()) {
			//unselect current ellipse
			ellipsoidList.get(time_point).get(index_ellipse).setSelected(false);
			//removing ellipsed from selected
			ImageControler.ellipsoid_selected_Panel[panel_pos_Col].remove(ellipsoidList.get(time_point).get(index_ellipse));
		}else {
			//make sure that the number of selected spheres not exceed the limit
			if (ellipsoid_selected_Panel[panel_pos_Col].size()>(n-1)) {
				remove_ellipsoid_from_selected_Panel(panel_pos_Col);							
			}
			
			//add the sphere to the list of selected spheres
			ellipsoid_selected_Panel[panel_pos_Col].add(ellipsoidList.get(time_point).get(index_ellipse));
			ellipsoidList.get(time_point).get(index_ellipse).setSelected(true);	
		}
		
		//remove from the other panel the selected ellipsoids
		int panel_not_clicked=0;
		if (panel_pos_Col==0) 
			panel_not_clicked = 1;		
		if (remove_spheres_from_other_panel) {
			while (ellipsoid_selected_Panel[panel_not_clicked].size()>0) {
				remove_ellipsoid_from_selected_Panel(panel_not_clicked);
			}
		}
	}
	
	public int checkIfMouseInsideAnyPanelXY(Point p) {
		//initialize id_panel to none
		int id_panel = -1;	

		if (p!=null) {	
			//mouse position is inside panel t
			id_panel = 0;		
		}else{
			p = allDisplayPanels[0][1].getMousePosition();	
			if (p!=null){
				//mouse position is inside panel t+1
				id_panel = 1;
			}
		}	
		return (id_panel);
	}

}