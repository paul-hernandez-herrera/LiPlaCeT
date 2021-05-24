import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
//import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
//import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
//import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
//import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
//import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.chart.ChartUtilities;
//import umontreal.ssj.functionfit.SmoothingCubicSpline;
import org.apache.commons.io.FilenameUtils;
import ij.io.FileInfo;

public class MenuBarGUI {
	
	private JMenuItem menuFile_saveState, menuFile_loadState, menuFile_loadStateOLD,menuFile_saveStateLastTimePoint,menuFile_printTracks;
	private JMenuItem menuEdit_FPS,menuEdit_BrightnessC1,menuEdit_BrightnessC2,menuEdit_Undo,menuEdit_ColorC1,menuEdit_ColorC2;
	private JMenuItem menuAnalysis_generateVTK, menuAnalysis_generateVTKIndividual,submenuAnalysis_drawTreeplot, menuAnalysis_setSmoothTrack, menuAnalysis_updateSorting,menuAnalysis_setDeltaTime;
	private JMenuBar menubar;
	public static boolean  compute_cell_cycle_from_GUI = true, smooth_tracking_segments = false, set_sphere_label_as_cycle_number=true;
	private JMenu cellCycleMenu, menuEdit_setSphereLabel;
	public static  Point[] brightnessVal_C =  new Point[2];
	public static double[] delta_time = new double[1000];
	private ImageControler panel_XY;
	public static float  FPS = 4;
	
	MenuBarGUI(){
		//initializing values for channel
		for (int Channel=0;Channel<=1;Channel++)
			brightnessVal_C[Channel] = new Point(-1,-1);
		
		for (int pos=0; pos<1000;pos++)
			delta_time[pos] = 1.0;
		
		create_menuBar();
	};
	
    private void SaveState() {        
        menuFile_saveState = new JMenuItem("Save state ");
        menuFile_saveState.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {            	
            	File selected_file = fileChooser(CreateandDisplayGUI.infoImage, "txt", false);
                if (selected_file!=null) {
                	PrintWriter writer = get_FileWriter(selected_file);
                	if (writer!=null) {             	                	
                    	String formatStr = "%s %d %d %5.2f %5.2f %5.2f %5.2f %5.2f %5.2f %s %d %s %d %s %d %d %d %s %d %d";
                		int parent;
                        for (int tp=0; tp<ImageControler.get_stack_nTimePoints();tp++){
                        	for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
                        		parent = (ellipse.getParent()==null)?-1:ellipse.getParent().ID;	                    				
                        		writer.println(String.format(formatStr,"Ellipse:",ellipse.ID,tp, ellipse.getX(), ellipse.getY(), ellipse.getZ(), ellipse.getWidth(), ellipse.getHeight(), ellipse.getDepth()," Parent: ",parent, "N_division: ",ellipse.cell_cycle, "Color:", ellipse.color.getRed(), ellipse.color.getGreen(), ellipse.color.getBlue(),"Sort_info: ",ellipse.sort_ID, ellipse.sort_ID_parent));
                        	}
                        }	                    
                        writer.close();
                	}
                }else{
                	System.out.println("Save command cancelled by user." );
                }                    
            }	
        });   	
    };
    
    private void SaveState_lastTimePoint() {
        
    	menuFile_saveStateLastTimePoint = new JMenuItem("Save state last time point");
    	menuFile_saveStateLastTimePoint.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {            	
            	File selected_file = fileChooser(CreateandDisplayGUI.infoImage, "txt", false);
                if (selected_file!=null) {
                	PrintWriter writer = get_FileWriter(selected_file);
                	if (writer!=null) {             	                	
                    	String formatStr = "%s %d %d %5.2f %5.2f %5.2f %5.2f %5.2f %5.2f %s %d %s %d %s %d %d %d %s %d %d";
                		int parent;
                		int tp = ImageControler.get_stack_nTimePoints()-1;
                    	for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
                    		parent = (ellipse.getParent()==null)?-1:ellipse.getParent().ID;	                    				
                    		writer.println(String.format(formatStr,"Ellipse:",ellipse.ID,0, ellipse.getX(), ellipse.getY(), ellipse.getZ(), ellipse.getWidth(), ellipse.getHeight(), ellipse.getDepth()," Parent: ",parent, "N_division: ",ellipse.cell_cycle, "Color:", ellipse.color.getRed(), ellipse.color.getGreen(), ellipse.color.getBlue(),"Sort_info: ",ellipse.sort_ID, ellipse.sort_ID_parent));
                    	}
                        writer.close();
                	}
                }else{
                	System.out.println("Save command cancelled by user." );
                }                    
            }	
        });   	
    };
    
    

    private void LoadState() {
        menuFile_loadState = new JMenuItem("Open state ");
        menuFile_loadState.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	//reading ellipses from file            	 
            	File selected_file = fileChooser(CreateandDisplayGUI.infoImage, "txt", true);
                if (selected_file!=null) {
                	try
                	{
                		String ext = FilenameUtils.getExtension(selected_file.getName());
                    	File file_input = null;
                    	if (ext.equals(""))
                    		file_input =  new java.io.File(selected_file.getParent(),selected_file.getName() + ".txt");
                    	else
                    		file_input =  new java.io.File(selected_file.getParent(),selected_file.getName());	
                    	
                    	//READING FILE
                    	FileReader file_identifier = new FileReader(file_input);
                    	                		
                    	int ID, timePoint, parent, N_division, R,G,B, sort_ID, sort_ID_parent;
                    	float x_pos, y_pos, z_pos, width, height, depth; 
                    	String line;
                    	
                    	//to get fake roots
                    	ArrayList<Ellipsoid> fakeEllipsoidsRoots = new ArrayList<Ellipsoid>();
                    	
                        Scanner file_scanner = new Scanner(file_identifier);
                        
                        
                        //clearing ellipsoid list
                        for (int tp=0;tp<ImageControler.ellipsoidList.size();tp++) {
                        	ImageControler.ellipsoidList.get(tp).clear();
                        }
                        
                        int max_count = 0;
                        Ellipsoid c_el;
                        while (file_scanner.hasNext()) {
                        	line = file_scanner.nextLine();
                        	Scanner lineScanner = new Scanner(line);
                        	
                        	lineScanner.next();
                        	ID = lineScanner.nextInt();
                        	timePoint = lineScanner.nextInt();
                        	x_pos = lineScanner.nextFloat();
                        	y_pos = lineScanner.nextFloat();
                        	z_pos = lineScanner.nextFloat();
                        	width = lineScanner.nextFloat();
                        	height = lineScanner.nextFloat();
                        	depth = lineScanner.nextFloat();
                        	lineScanner.next();
                        	parent = lineScanner.nextInt();
                        	lineScanner.next();
                        	N_division = lineScanner.nextInt();
                        	lineScanner.next();
                        	R = lineScanner.nextInt();
                        	G = lineScanner.nextInt();
                        	B = lineScanner.nextInt();
                        	
                        	if (lineScanner.hasNext()) {
                        		lineScanner.next();
                        		sort_ID = lineScanner.nextInt();
                        		sort_ID_parent = lineScanner.nextInt();
                        	}else {
                        		sort_ID = -1;
                        		sort_ID_parent = -1;
                        	}
                        	
                        	lineScanner.close();
                        	
                        	if (ID>max_count)
                        		max_count = ID;
                        	
                        	//Make sure that ellipsoidList have the size to add timePoint
                        	if (timePoint>ImageControler.ellipsoidList.size()-1) {
                        		for (int add_tp=0;add_tp<(timePoint-(ImageControler.ellipsoidList.size()-1));add_tp++)
                        			ImageControler.ellipsoidList.add(new ArrayList<Ellipsoid>());   
                        		//System.out.println("ellipsoidList size: " + ImageControler.ellipsoidList.size());
                        	}
                        	
                        	//adding ellipsoid to the tracking list
                        	ImageControler.ellipsoidList.get(timePoint).add(new Ellipsoid(timePoint,x_pos,y_pos,z_pos,width,height,depth));
                        	
                        	//setting color for current ellipsoid
                        	c_el = ImageControler.ellipsoidList.get(timePoint).get(ImageControler.ellipsoidList.get(timePoint).size()-1);
                        	c_el.ID = ID;
                        	c_el.cell_cycle = N_division;
                        	c_el.setColor(new Color(R,G,B));
                        	c_el.sort_ID = sort_ID;
                        	c_el.sort_ID_parent = sort_ID_parent;
                        	
                        	if (parent>0) {
                        		//ID positive means that the current ellipsoid has a parent
                        		if (timePoint>0) {
	                        		for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(timePoint-1)) {
	                        			if (ellipse.ID == parent) {
	                        				c_el.setParent(ellipse);
	                        				break;
	                        			}
	                        		}
                        		}
                        	}else {
                        		//ID negative means that the current_ellipsoid does not have parent;
                        		Ellipsoid parent_ellipsoid_TEMP= new Ellipsoid(0,0,0,0,0,0,0);
                        		parent_ellipsoid_TEMP.setColor(c_el.color);
                        		
                        		for (int i = 0; i<fakeEllipsoidsRoots.size();i++) {
                        			if (fakeEllipsoidsRoots.get(i).ID==parent) {
                        				parent_ellipsoid_TEMP =fakeEllipsoidsRoots.get(i);
                        				break;
                        			}
                        		}
                        		parent_ellipsoid_TEMP.ID = parent;
                        		c_el.setParent(parent_ellipsoid_TEMP);                        		
                        	}
                        	
                        	Ellipsoid.count = max_count;
                        } 

                        file_scanner.close();
                        file_identifier.close();
                		ImageControler.repaintPanels();
                		                        
                	}catch(IOException i) {
                		i.printStackTrace();
                		return;
                	}
                } else {
                	System.out.println("Save command cancelled by user." );
                }                
    	      	      
            }	
        });    	
    	
    };
    
    
    private void setDeltaTime() {
    	menuAnalysis_setDeltaTime = new JMenuItem("Set delta_time");
    	menuAnalysis_setDeltaTime.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	//reading file containing delta time           	 
            	File selected_file = fileChooser(CreateandDisplayGUI.infoImage, "txt", true);
                if (selected_file!=null) {
                	try
                	{
                		String ext = FilenameUtils.getExtension(selected_file.getName());
                    	File file_input = null;
                    	if (ext.equals(""))
                    		file_input =  new java.io.File(selected_file.getParent(),selected_file.getName() + ".txt");
                    	else
                    		file_input =  new java.io.File(selected_file.getParent(),selected_file.getName());	
                    	
                    	//READING FILE
                    	FileReader file_identifier = new FileReader(file_input);
                    	
                        Scanner file_scanner = new Scanner(file_identifier);
                        
                        String line;
                        float current_delta;
                        
                        int pos = 0;
                        while (file_scanner.hasNext()) {
                        	line = file_scanner.nextLine();
                        	Scanner lineScanner = new Scanner(line);
                        	
                        	current_delta = lineScanner.nextFloat();
                        	
                        	
                        	lineScanner.close();
                        	
                        	delta_time[pos] = current_delta;
                        	System.out.println("Setting delta_time to time " + pos + " = " + current_delta );
                        	pos = pos + 1;
                        } 

                        file_scanner.close();
                        file_identifier.close();
                		                        
                	}catch(IOException i) {
                		i.printStackTrace();
                		return;
                	}
                } else {
                	System.out.println("Setting delta time command cancelled by user." );
                }                
    	      	      
            }	
        });    	
    	
    };    

    
    private void LoadStateOld() {
        menuFile_loadStateOLD = new JMenuItem("Load Stated Old ");
        menuFile_loadStateOLD.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	//reading ellipses from file            	
            	File selected_file = fileChooser(CreateandDisplayGUI.infoImage, "ser", true);
                if (selected_file!=null) {
                	try
                	{
                		String ext = FilenameUtils.getExtension(selected_file.getName());
                    	File file_input = null;
                    	if (ext.equals(""))
                    		file_input =  new java.io.File(selected_file.getParent(),selected_file.getName() + ".ser");
                    	else
                    		file_input =  new java.io.File(selected_file.getParent(),selected_file.getName());	
                		FileInputStream fileIn = new FileInputStream(file_input);
                		ObjectInputStream in = new ObjectInputStream(fileIn);
    					@SuppressWarnings("unchecked")
    					ArrayList<ArrayList<Ellipsoid>> temp_2 = (ArrayList<ArrayList<Ellipsoid>>) in.readObject();
                		in.close();
                		fileIn.close();                		


                		ImageControler.ellipsoidList =temp_2;
                		ImageControler.repaintPanels();
                		
                 		// update the ellipsoid ID count
                		int maxID=-1;
                        for (int tp=0; tp<ImageControler.get_stack_nTimePoints();tp++){
                        	for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
                        		if (ellipse.ID>maxID)
                        			maxID = ellipse.ID;                        		                        		
                        	}
                        }
                        Ellipsoid.count = maxID;
                        
                	}catch(IOException i) {
                		i.printStackTrace();
                		return;
                	}catch(ClassNotFoundException c)
                	{
                		c.printStackTrace();
                		return;
                	} 
                } else {
                	System.out.println("Save command cancelled by user." );
                }                
    	      	      
            }	
        });   	
    };

    private void printTracking_information(){
        menuFile_printTracks = new JMenuItem("Print tracking");
        menuFile_printTracks.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) { 
            	File selected_file = fileChooser(CreateandDisplayGUI.infoImage, "txt", false);
                if (selected_file!=null) {
                	PrintWriter writer = get_FileWriter(selected_file);
                	if (writer!=null) {
                    	String formatStr = "%s %d %d %5.2f %5.2f %5.2f %d %s %s %s";
                		int parent;
                        for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
                        	for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){	
                        		parent = (ellipse.getParent()==null)?-1:ellipse.getParent().ID;
                        		writer.println(String.format(formatStr,"Ellipse:",ellipse.ID,tp, ellipse.getX(), ellipse.getY(), ellipse.getZ()," Parent: ",parent, "N_division: ",ellipse.cell_cycle));
                        	}
                        }
                        writer.close();
                	}
                    
                } else {
                	System.out.println("Save command cancelled by user." );
                }
            }	
        });    	
    };
       
    private void SetSphereLabel() {
    	
    	menuEdit_setSphereLabel = new JMenu("Set Sphere Label:");
    	
    	ButtonGroup groupRadioButton = new ButtonGroup();
    	
    	JRadioButtonMenuItem jradioOption_SphereLabel_CycleNumber = new JRadioButtonMenuItem("Cycle number", true);
    	menuEdit_setSphereLabel.add(jradioOption_SphereLabel_CycleNumber);
    	groupRadioButton.add(jradioOption_SphereLabel_CycleNumber);
    	
    	JRadioButtonMenuItem jradioOption_SphereLabel_SortingInfo = new JRadioButtonMenuItem("Sorting Information");
    	menuEdit_setSphereLabel.add(jradioOption_SphereLabel_SortingInfo);
    	groupRadioButton.add(jradioOption_SphereLabel_SortingInfo);   	
    	
        ActionListener sliceActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	set_sphere_label_as_cycle_number = jradioOption_SphereLabel_CycleNumber.isSelected();
            }
          };
          jradioOption_SphereLabel_CycleNumber.addActionListener(sliceActionListener);
          jradioOption_SphereLabel_SortingInfo.addActionListener(sliceActionListener);  	
    	
          	
    };
    
    
    private void SetFPS() {
        menuEdit_FPS = new JMenuItem("Set Frames per Second ");
        menuEdit_FPS.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	JOptionPane getFPS = new JOptionPane();
            	@SuppressWarnings("static-access")
    			String input = getFPS.showInputDialog("Frames per Second: ",FPS);
            	if (input!=null)
            		FPS = Float.parseFloat(input);
            	
            }	
        });    	
    };
    
    private void SetSmoothTrack() {
    	
    	menuAnalysis_setSmoothTrack = new JMenu("Set Smooth Tracking:");
    	
    	ButtonGroup groupRadioButton = new ButtonGroup();
    	
    	JRadioButtonMenuItem jradioOption_Smooth_track = new JRadioButtonMenuItem("Smooth", true);
    	menuAnalysis_setSmoothTrack.add(jradioOption_Smooth_track);
    	groupRadioButton.add(jradioOption_Smooth_track);
    	
    	JRadioButtonMenuItem jradioOption_No_Smooth_Track = new JRadioButtonMenuItem("No smooth");
    	menuAnalysis_setSmoothTrack.add(jradioOption_No_Smooth_Track);
    	groupRadioButton.add(jradioOption_No_Smooth_Track);   	
    	
        ActionListener sliceActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	smooth_tracking_segments = jradioOption_Smooth_track.isSelected();
            }
          };
          jradioOption_Smooth_track.addActionListener(sliceActionListener);
          jradioOption_No_Smooth_Track.addActionListener(sliceActionListener);  	
    	
          	
    };
  
 
    private void setBrigthnessChannel1() {
    	menuEdit_BrightnessC1 = new JMenuItem("Channel 1");
    	menuEdit_BrightnessC1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	set_brightnessChannel(0); 
                ImageControler.repaintPanels();                	 
            }	
        }); 
    }
    
    private void setBrigthnessChannel2() {
    	menuEdit_BrightnessC2 = new JMenuItem("Channel 2");
    	menuEdit_BrightnessC2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	set_brightnessChannel(1); 
                ImageControler.repaintPanels();                	 
            }	
        }); 
    }    
         
    private void deshacer() {
        menuEdit_Undo = new JMenuItem("Undo");
        menuEdit_Undo.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	//DisplayPanel.printUndo();
            	Undo.undoAction();       
            	
            }	
        });
    }

    private void setColorC1() {
        menuEdit_ColorC1 = new JMenuItem("Channel 1");
        menuEdit_ColorC1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {            	
            	Color selected_color = get_colorChannel();
                if (selected_color!=null){
                	PanelControlFeatures.channelColors[0] = selected_color; 
                	ImageControler.repaintPanels();
                }	     
            }	
        });   	
    }

    
    private void setColorC2() {
        menuEdit_ColorC2 = new JMenuItem("Channel 2");
        menuEdit_ColorC2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	Color selected_color = get_colorChannel();
                if (selected_color!=null){
                	PanelControlFeatures.channelColors[1] = selected_color; 
                	ImageControler.repaintPanels();
                }	     
                
            }	
        });     	
    } 
    
    private void GenerateVTK() {
    	menuAnalysis_generateVTK = new JMenuItem("Unique file for all lineages");
    	menuAnalysis_generateVTK.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	Sort_tracks d_sort = new Sort_tracks();
            	DataAnalisis d_analisys = new DataAnalisis(d_sort.does_tracking_have_sorting_information());
            	TrackingToVTK vtkWriter = new TrackingToVTK(d_analisys);
            	vtkWriter.write_vtkFiles();
            }	
        });     	
    }
    
    private void GenerateVTK_Individuals() {
    	menuAnalysis_generateVTKIndividual = new JMenuItem("Separate file for each lineage");
    	menuAnalysis_generateVTKIndividual.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {	
            	Sort_tracks d_sort = new Sort_tracks();
            	DataAnalisis d_analisys = new DataAnalisis(d_sort.does_tracking_have_sorting_information());
            	TrackingToVTK vtkWriter = new TrackingToVTK(d_analisys);
            	vtkWriter.write_vtkFilesIndividual();
            }	
        });     	
    }    
    
    private void PrintTreeplot() {
    	submenuAnalysis_drawTreeplot = new JMenuItem("Save tree plot as PNG");
    	submenuAnalysis_drawTreeplot.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {

            	
            	//getting screen resolution to calculate frame size
            	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            	int frame_size = screenSize.height;
            	if (screenSize.width<screenSize.height)
            		frame_size = screenSize.width;
            	
            	Sort_tracks d_sort = new Sort_tracks();
            	DataAnalisis d_analisys = new DataAnalisis(d_sort.does_tracking_have_sorting_information());
            	
            	Treeplot_draw treeplot = new Treeplot_draw(d_analisys.get_trackingAsParentNodes(), d_analisys.get_track_initialTP(), d_analisys.get_listColor(), frame_size);
            	
            	treeplot.setPreferredSize(new Dimension(frame_size,frame_size));
            	treeplot.setBackground(Color.white);
            	
            	JFrame app = new JFrame("Smiley App");            	
            	app.add(treeplot, BorderLayout.CENTER);
            	app.setResizable(false);
                app.pack();
            	app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            	app.setVisible(true);            	
                
                treeplot.saveImage();
                
                //creating the data from the tree plot
                new TrackingToCSV(d_analisys, treeplot.get_folder_path(), treeplot.get_tree_children_IDs());
                
                //System.out.println("frame bounds " + app.getBounds().getWidth() + " " + app.getBounds().getHeight());
                //d_a.write_vtkSpheres();
            }	
        });     	
    }     
    
    private void generate_TrackSorting() {
    	menuAnalysis_updateSorting = new JMenuItem("Update IDs sorting");
    	menuAnalysis_updateSorting.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
            	Sort_tracks d = new Sort_tracks();
            	d.sort_tracks(panel_XY.get_currentTime());
            	ImageControler.repaintPanels();
            }	
        });
    }
    
//    private void computeSplineFunction() {
//    	menuAnalysis_SplineFitting = new JMenuItem("compute Spline Function");
//    	menuAnalysis_SplineFitting.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e) {
//            	Sort_tracks d_sort = new Sort_tracks();
//            	
//            	boolean is_track_sorted = d_sort.does_tracking_have_sorting_information();
//            	
//            	if (is_track_sorted) {
//                	DataAnalisis d_analisys = new DataAnalisis(is_track_sorted);
//                	
//                	ArrayList<ArrayList<ArrayList<Double>>> distance_to_apice = d_analisys.get_distace_to_root_from_sorted_track();
//                	ArrayList<ArrayList<ArrayList<Double>>> cell_displacement_rate = d_analisys.get_cell_displacement_rate_from_sorted_track();
//                	ArrayList<ArrayList<ArrayList<Double>>> cell_growth_rate = d_analisys.get_cell_growth_rate_from_sorted_track();
//                	
//                	ArrayList<double []> x_val =  new ArrayList<double []>();
//                	
//                	ArrayList<double []> y_val_displacement_rate =  new ArrayList<double []>();
//                	ArrayList<String> label_line_rates = new ArrayList<String>(); 
//                	
//                	ArrayList<double []> y_val_growth_rate =  new ArrayList<double []>();   
//                	
//                	ArrayList<double []> x_val_derivative =  new ArrayList<double []>();
//                	ArrayList<double []> y_val_displacement_rate_derivative =  new ArrayList<double []>();
//                	ArrayList<String> label_line_derivative = new ArrayList<String>(); 
//                	
//                	ArrayList<double []> y_val_growth_rate_derivative =  new ArrayList<double []>();                	
//                	
//                	for (int n_track=0; n_track<distance_to_apice.size(); n_track++) {
//                		x_val.clear();
//                		y_val_displacement_rate.clear();
//                		label_line_rates.clear();
//                		y_val_growth_rate.clear();
//                		
//                		x_val_derivative.clear();
//                		y_val_displacement_rate_derivative.clear();
//                		label_line_derivative.clear();
//                		y_val_growth_rate_derivative.clear();
//
//            			//getting total data of points to analyze
//                		int x_n = 0;
//                		for (int tp=1; tp<distance_to_apice.get(n_track).size(); tp++) {
//                			x_n = x_n + distance_to_apice.get(n_track).get(tp).size()-1;
//                		}
//                		
//            			//creating variable to save experimental values
//            			double [] xDouble = new double [x_n];
//            			double [] y_displacement_rate = new double [x_n];
//            			double [] y_growth_rate = new double [x_n];
//            			
//            			//sorting the x values
//            			double current_val, list_sorted_val;
//        				ArrayList<Integer> index_sorted_tp = new ArrayList<Integer>();
//        				ArrayList<Integer> index_sorted_i = new ArrayList<Integer>();
//            			for (int tp=1; tp<distance_to_apice.get(n_track).size(); tp++) {
//            				for (int i=1;i<distance_to_apice.get(n_track).get(tp).size();i++) {
//            					//getting current value to sort
//                				boolean current_value_is_minimum=true;
//                				current_val = distance_to_apice.get(n_track).get(tp).get(i);
//                				
//                				//getting the position to occupy in the current sorted list
//                				for (int j=index_sorted_tp.size()-1;j>=0;j--) {
//                					list_sorted_val = distance_to_apice.get(n_track).get(index_sorted_tp.get(j)).get(index_sorted_i.get(j));
//                					if (current_val>list_sorted_val) {
//                						index_sorted_tp.add(j+1,tp);
//                						index_sorted_i.add(j+1,i);
//                						current_value_is_minimum = false;
//                						break;
//                					}					
//                				}
//                				if (current_value_is_minimum) {
//                					index_sorted_tp.add(0,tp);
//                					index_sorted_i.add(0,i);
//                				}	                  				
//            				}
//            			}
//            			
//            			//setting the values sorted
//            			for (int j=0; j<index_sorted_tp.size(); j++) {
//            				xDouble[j] = distance_to_apice.get(n_track).get(index_sorted_tp.get(j)).get(index_sorted_i.get(j));
//            				y_displacement_rate[j] = cell_displacement_rate.get(n_track).get(index_sorted_tp.get(j)).get(index_sorted_i.get(j));
//            				y_growth_rate[j] = cell_growth_rate.get(n_track).get(index_sorted_tp.get(j)).get(index_sorted_i.get(j));
//            				
//            				System.out.println("raw: " + xDouble[j] + " " + y_displacement_rate[j]);
//            				
//            			}             			
//            			
//            			//computing cubic spline function
//            			SmoothingCubicSpline smoothed_function_displacement = new SmoothingCubicSpline(xDouble,y_displacement_rate,0.00001);
//            			SmoothingCubicSpline smoothed_function_growth = new SmoothingCubicSpline(xDouble,y_growth_rate,0.00001);
//            	
//                        double [] xInterp = new double [1000];
//                        double [] yInterp_disp = new double [1000];
//                        double [] yInterp_disp_derivate = new double [1000];
//                        double [] yInterp_growth = new double [1000];
//                        double [] yInterp_growth_derivate = new double [1000];
//                        
//                        
//                        double minX = xDouble[0];
//                        double maxX = xDouble[xDouble.length-1];
//                        //getting interpolated values
//                        for (int i=0; i<1000;i++) {
//                        	xInterp[i] = minX + (maxX-minX)*i/1000;
//                        	yInterp_disp[i] = smoothed_function_displacement.evaluate(xInterp[i]);
//                        	System.out.println(xInterp[i] + " " + yInterp_disp[i]);
//                        	yInterp_disp_derivate[i] = smoothed_function_displacement.derivative(xInterp[i]);
//                        	yInterp_growth[i] = smoothed_function_growth.evaluate(xInterp[i]);
//                        	yInterp_growth_derivate[i] = smoothed_function_growth.derivative(xInterp[i]);
//                        }                         
//                        
//                        //plotting displacement rate and interpolation
//            			label_line_rates.add("Experimental data");
//            			label_line_rates.add("Cubic spline interpolation");
//            			
//            			x_val.add(xDouble);
//            			x_val.add(xInterp);
//            			
//            			y_val_displacement_rate.add(y_displacement_rate);
//            			
//                        y_val_displacement_rate.add(yInterp_disp);
//                        
//                        
//                        new Plot_dataXY(x_val, y_val_displacement_rate, label_line_rates , "plot_displacement_rate_ntrack_"+n_track);                        
//
//                        //plotting growth rate and interpolation
//                        y_val_growth_rate.add(y_growth_rate);
//                        y_val_growth_rate.add(yInterp_growth);
//                        
//                        new Plot_dataXY(x_val, y_val_growth_rate, label_line_rates , "plot_growth_rate_ntrack_"+n_track);
//                        
//                        //plotting derivative displacement rate
//                        label_line_derivative.add("Derivative cubic spline interpolation");
//                        x_val_derivative.add(xInterp);
//                        y_val_displacement_rate_derivative.add(yInterp_disp_derivate);
//                        
//                        new Plot_dataXY(x_val_derivative, y_val_displacement_rate_derivative, label_line_derivative , "plot_displacement_rate_derivative_ntrack_"+n_track);
//                        
//                        //plotting derivative growth rate
//                        y_val_growth_rate_derivative.add(yInterp_growth_derivate);
//                        new Plot_dataXY(x_val_derivative, y_val_growth_rate_derivative, label_line_derivative , "plot_growth_rate_derivative_ntrack_"+n_track);
//                        
//                	}
//            	}
//      
//            }	
//        });    	
//    	
//    };    
    
//    private void computeSplineFunctionINDIVIDUALTIMES() {
//    	menuAnalysis_SplineFitting = new JMenuItem("compute Spline Function");
//    	menuAnalysis_SplineFitting.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e) {
//            	Sort_tracks d_sort = new Sort_tracks();
//            	
//            	boolean is_track_sorted = d_sort.does_tracking_have_sorting_information();
//            	
//            	if (is_track_sorted) {
//                	DataAnalisis d_analisys = new DataAnalisis(is_track_sorted);
//                	
//                	ArrayList<ArrayList<ArrayList<Double>>> distance_to_apice = d_analisys.get_distace_to_root_from_sorted_track();
//                	ArrayList<ArrayList<ArrayList<Double>>> cell_displacement_rate = d_analisys.get_cell_displacement_rate_from_sorted_track();
//                	ArrayList<ArrayList<ArrayList<Double>>> cell_growth_rate = d_analisys.get_cell_growth_rate_from_sorted_track();
//                	
//                	ArrayList<double []> x_val =  new ArrayList<double []>();
//                	
//                	ArrayList<double []> y_val_displacement_rate =  new ArrayList<double []>();
//                	ArrayList<String> label_line_rates = new ArrayList<String>(); 
//                	
//                	ArrayList<double []> y_val_growth_rate =  new ArrayList<double []>();   
//                	
//                	ArrayList<double []> x_val_derivative =  new ArrayList<double []>();
//                	ArrayList<double []> y_val_displacement_rate_derivative =  new ArrayList<double []>();
//                	ArrayList<String> label_line_derivative = new ArrayList<String>(); 
//                	
//                	ArrayList<double []> y_val_growth_rate_derivative =  new ArrayList<double []>();                	
//                	
//                	for (int n_track=0; n_track<distance_to_apice.size(); n_track++) {
//                		x_val.clear();
//                		y_val_displacement_rate.clear();
//                		label_line_rates.clear();
//                		y_val_growth_rate.clear();
//                		
//                		x_val_derivative.clear();
//                		y_val_displacement_rate_derivative.clear();
//                		label_line_derivative.clear();
//                		y_val_growth_rate_derivative.clear();
//                		
//                		for (int tp=1; tp<distance_to_apice.get(n_track).size(); tp++) {
//
//                			double [] xDouble = new double [distance_to_apice.get(n_track).get(tp).size()];
//                			double [] y_displacement_rate = new double [distance_to_apice.get(n_track).get(tp).size()];
//                			double [] y_growth_rate = new double [distance_to_apice.get(n_track).get(tp).size()];
//                			
//                			//getting the x values sorted
//                			double current_val, list_sorted_val;
//            				ArrayList<Integer> index_sorted = new ArrayList<Integer>();   
//            				index_sorted.add(0);
//                			for (int i=1; i<distance_to_apice.get(n_track).get(tp).size(); i++) {
//                				boolean current_value_is_minimum=true;
//                				current_val = distance_to_apice.get(n_track).get(tp).get(i);
//                				
//                				for (int j=index_sorted.size()-1;j>=0;j--) {
//                					list_sorted_val = distance_to_apice.get(n_track).get(tp).get(index_sorted.get(j));
//                					if (current_val>list_sorted_val) {
//                						index_sorted.add(j+1,i);
//                						current_value_is_minimum = false;
//                						break;
//                					}					
//                				}
//                				if (current_value_is_minimum)
//                					index_sorted.add(0,i);                				
//
//                			}
//                			for (int i=0; i<distance_to_apice.get(n_track).get(tp).size(); i++) {
//                				xDouble[i] = distance_to_apice.get(n_track).get(tp).get(index_sorted.get(i));
//                				y_displacement_rate[i] = cell_displacement_rate.get(n_track).get(tp).get(index_sorted.get(i));
//                				y_growth_rate[i] = cell_growth_rate.get(n_track).get(tp).get(index_sorted.get(i));
//                			} 
//                			//done sorting
//                			
//                			//computing cubic spline function
//                			SmoothingCubicSpline smoothed_function_displacement = new SmoothingCubicSpline(xDouble,y_displacement_rate,0.0001);
//                			SmoothingCubicSpline smoothed_function_growth = new SmoothingCubicSpline(xDouble,y_growth_rate,0.0001);
//                			
//                            double [] xInterp = new double [1000];
//                            double [] yInterp_disp = new double [1000];
//                            double [] yInterp_disp_derivate = new double [1000];
//                            double [] yInterp_growth = new double [1000];
//                            double [] yInterp_growth_derivate = new double [1000];
//                            
//                            double minX = xDouble[0];
//                            double maxX = xDouble[xDouble.length-1];
//                            
//                            for (int i=0; i<1000;i++) {
//                            	xInterp[i] = minX + (maxX-minX)*i/1000;
//                            	yInterp_disp[i] = smoothed_function_displacement.evaluate(xInterp[i]);
//                            	yInterp_disp_derivate[i] = smoothed_function_displacement.derivative(xInterp[i]);
//                            	yInterp_growth[i] = smoothed_function_growth.evaluate(xInterp[i]);
//                            	yInterp_growth_derivate[i] = smoothed_function_growth.derivative(xInterp[i]);
//                            } 
//                			
//                            //plotting displacement rate and interpolation
//                			label_line_rates.add("tp" + Integer.toString(tp));
//                			label_line_rates.add("Int tp "+Integer.toString(tp));
//                			
//                			x_val.add(xDouble);
//                			x_val.add(xInterp);
//                			
//                			y_val_displacement_rate.add(y_displacement_rate);
//                            y_val_displacement_rate.add(yInterp_disp);
//                            
//                            int list_size = x_val.size();
//                            
//                            new Plot_dataXY(new ArrayList<double[]>(x_val.subList(list_size-2, list_size)), new ArrayList<double[]>(y_val_displacement_rate.subList(list_size-2, list_size)), new ArrayList<String>(label_line_rates.subList(list_size-2, list_size)) , "plot_displacement_rate_ntrack_"+n_track+"_tp_"+tp);
//                            
//                            //ploting growth rate and interpolation
//                            y_val_growth_rate.add(y_growth_rate);
//                            y_val_growth_rate.add(yInterp_growth);
//                            
//                            new Plot_dataXY(new ArrayList<double[]>(x_val.subList(list_size-2, list_size)), new ArrayList<double[]>(y_val_growth_rate.subList(list_size-2, list_size)), new ArrayList<String>(label_line_rates.subList(list_size-2, list_size)) , "plot_growth_rate_ntrack_"+n_track+"_tp_"+tp);
//                            
//                            //removing raw data
//                            label_line_rates.remove(list_size-2);
//                            x_val.remove(list_size-2);
//                            y_val_displacement_rate.remove(list_size-2);
//                            y_val_growth_rate.remove(list_size-2);
//                            
//                            //plotting derivative displacement rate
//                            label_line_derivative.add("tp" + Integer.toString(tp));
//                            x_val_derivative.add(xInterp);
//                            y_val_displacement_rate_derivative.add(yInterp_disp_derivate);
//                            
//                            list_size = x_val_derivative.size();
//                            new Plot_dataXY(new ArrayList<double[]>(x_val_derivative.subList(list_size-1, list_size)), new ArrayList<double[]>(y_val_displacement_rate_derivative.subList(list_size-1, list_size)), new ArrayList<String>(label_line_derivative.subList(list_size-1, list_size)) , "plot_displacement_rate_derivative_ntrack_"+n_track+"_tp_"+tp);
//                            
//                            //plotting derivative growth rate
//                            y_val_growth_rate_derivative.add(yInterp_growth_derivate);
//                            new Plot_dataXY(new ArrayList<double[]>(x_val_derivative.subList(list_size-1, list_size)), new ArrayList<double[]>(y_val_growth_rate_derivative.subList(list_size-1, list_size)), new ArrayList<String>(label_line_derivative.subList(list_size-1, list_size)) , "plot_growth_rate_derivative_ntrack_"+n_track+"_tp_"+tp);
//                		}
//                		
//                		//plotting all times
//                		new Plot_dataXY(x_val, y_val_displacement_rate, label_line_rates , "plot_displacement_rate_ntrack_"+n_track);
//                		
//                		new Plot_dataXY(x_val, y_val_growth_rate, label_line_rates , "plot_growth_rate_ntrack_"+n_track);
//                		
//                		new Plot_dataXY(x_val_derivative, y_val_displacement_rate_derivative, label_line_derivative , "plot_displacement_rate_derivative_ntrack_"+n_track);
//                		
//                		new Plot_dataXY(x_val_derivative, y_val_growth_rate_derivative, label_line_derivative , "plot_growth_rate_derivative_ntrack_"+n_track);
//                	}
//            	}
//      
//            }	
//        });    	
//    	
//    };     
    
    
    private void create_menuBar() {
    	menubar = new JMenuBar();
    	
    	//creating menus
		JMenu file = new JMenu("File");
		JMenu edition = new JMenu("Edit");
		JMenu analysis = new JMenu("Analysis");
		
		//creating sub menus
		JMenu submenu_treePlot = new JMenu("Tree plot");
		JMenu submenu_vtkFile = new JMenu("Generate VTK");
		JMenu submenu_Edit_SetColor = new JMenu("Set Color");
		JMenu submenu_Edit_SetBrightness = new JMenu("Set Brightness");
		
		SetSphereLabel();edition.add(menuEdit_setSphereLabel);
		SetFPS(); edition.add(menuEdit_FPS);
		deshacer(); edition.add(menuEdit_Undo);
		setBrigthnessChannel1();submenu_Edit_SetBrightness.add(menuEdit_BrightnessC1);
		setBrigthnessChannel2();submenu_Edit_SetBrightness.add(menuEdit_BrightnessC2);
		edition.add(submenu_Edit_SetBrightness);
        
        setColorC1(); submenu_Edit_SetColor.add(menuEdit_ColorC1);
        setColorC2(); submenu_Edit_SetColor.add(menuEdit_ColorC2);
        edition.add(submenu_Edit_SetColor);
        
        SaveState(); file.add(menuFile_saveState);
        SaveState_lastTimePoint(); file.add(menuFile_saveStateLastTimePoint);
        LoadState(); file.add(menuFile_loadState);
        LoadStateOld(); file.add(menuFile_loadStateOLD);
        printTracking_information(); file.add(menuFile_printTracks);
        
        
        //Analysis menu
        //computeSplineFunction(); analysis.add(menuAnalysis_SplineFitting);
        
        SetSmoothTrack(); 
        GenerateVTK(); submenu_vtkFile.add(menuAnalysis_generateVTK);
        GenerateVTK_Individuals(); submenu_vtkFile.add(menuAnalysis_generateVTKIndividual);
        
        PrintTreeplot(); submenu_treePlot.add(submenuAnalysis_drawTreeplot);        
        treePlot_selectCycleCountOption();submenu_treePlot.add(cellCycleMenu);
        
        generate_TrackSorting(); analysis.add(menuAnalysis_updateSorting);  
        setDeltaTime(); analysis.add(menuAnalysis_setDeltaTime);
        
        analysis.add(menuAnalysis_setSmoothTrack);
        analysis.add(submenu_vtkFile);
        analysis.add(submenu_treePlot);
        
        menubar.add(file);
        menubar.add(edition);
        menubar.add(analysis);

    }
    
    private void treePlot_selectCycleCountOption() {
    	cellCycleMenu = new JMenu("Set cell cycle:");
    	
    	ButtonGroup groupRadioButton = new ButtonGroup();
    	
    	JRadioButtonMenuItem JradioOption_cellCycleFromGui = new JRadioButtonMenuItem("Cell cycle from GUI", true);
    	cellCycleMenu.add(JradioOption_cellCycleFromGui);
    	groupRadioButton.add(JradioOption_cellCycleFromGui);
    	
    	JRadioButtonMenuItem JradioOption_cellCycleAutomatic = new JRadioButtonMenuItem("Cell cycle Automatic");
    	cellCycleMenu.add(JradioOption_cellCycleAutomatic);
    	groupRadioButton.add(JradioOption_cellCycleAutomatic);   	
    	
        ActionListener sliceActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	compute_cell_cycle_from_GUI = JradioOption_cellCycleFromGui.isSelected();
            }
          };
          JradioOption_cellCycleFromGui.addActionListener(sliceActionListener);
          JradioOption_cellCycleAutomatic.addActionListener(sliceActionListener);
    	
    }
    

    public JMenuBar getmenuBar() {
    	return (menubar);
    }
    
    public static PrintWriter get_FileWriter(File selected_file) {
        PrintWriter writer = null ;
        try {
    		String ext = FilenameUtils.getExtension(selected_file.getName());
        	File file_output = null;
        	if (ext.equals(""))
        		file_output =  new java.io.File(selected_file.getParent(),selected_file.getName() + ".txt");
        	else
        		file_output =  new java.io.File(selected_file.getParent(),selected_file.getName());	
			writer = new PrintWriter(file_output, "UTF-8");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
        return writer;
    }

    private File fileChooser(FileInfo loadedImageInformation, String extension, boolean openDialog) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File(loadedImageInformation.directory));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*."+extension, extension);
        fc.setFileFilter(filter);
        if (openDialog)
        	fc.showOpenDialog(null);
        else
        	fc.showSaveDialog(null);
        return (fc.getSelectedFile());    	
    }
    
    private void set_brightnessChannel(int channelNumber) {
    	//brightness as point where the x coordinate is the minimum brightness while y correspond to maximum brightness
    	
    	//creating text field
        JTextField xField = new JTextField(5);
        JTextField yField = new JTextField(5);
        
        //Value to be set in the text field
        if (brightnessVal_C[channelNumber].x!=-1){
        	xField.setText(Integer.toString(brightnessVal_C[channelNumber].x));
        	yField.setText(Integer.toString(brightnessVal_C[channelNumber].y));
        }

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("min:")); myPanel.add(xField);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(new JLabel("max:")); myPanel.add(yField);
        
        int result = JOptionPane.showConfirmDialog(null, myPanel, 
                 "Please Enter min and max values", JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);  
        if (result == JOptionPane.OK_OPTION) {
        	brightnessVal_C[channelNumber].x = Integer.parseInt(xField.getText());
        	brightnessVal_C[channelNumber].y = Integer.parseInt(yField.getText());
    	}else{
    		brightnessVal_C[channelNumber].x = -1;
    		brightnessVal_C[channelNumber].y = -1;
         } 
    }
    
    private Color get_colorChannel() {
        String[] choices = { "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Orange", "Pink", "Gray"};
        String input = (String) JOptionPane.showInputDialog(null, "Choose color..",
            "Color picker", JOptionPane.QUESTION_MESSAGE, null, // Use
                                                                            // default
                                                                            // icon
            choices, // Array of choices
            choices[1]); // Initial choice
        
    	Color selected_color = null;
    	if (input !=null){
        	switch(input) {
	    	case "Red": selected_color = Color.red;
	    		break;
	    	case "Green": selected_color = Color.green;
	    		break;
	    	case "Blue": selected_color = Color.blue;
	    		break;
	    	case "Cyan": selected_color = Color.cyan;
	    		break;
	    	case "Magenta": selected_color = Color.magenta;
	    		break;   
	    	case "Yellow": selected_color = Color.yellow;
	    		break;   
	    	case "Orange": selected_color = Color.orange;
	    		break;  
	    	case "Pink": selected_color = Color.pink;
	    		break;
	    	case "Gray": selected_color = Color.gray;
	    		break;    
        	}
    	}
		return selected_color;    	
    } 
    
	public static File create_folder(String label_folder) {
		//System.out.println(CreateandDisplayGUI.infoImage.directory);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");  
		LocalDateTime now = LocalDateTime.now();  
		File folder_path = new File(CreateandDisplayGUI.infoImage.directory,label_folder+dtf.format(now));
		//folder_path = new File(CreateandDisplayGUI.infoImage.directory);
	    if (! folder_path.exists()){
	        folder_path.mkdir();
	    }	 			
	    return folder_path;
	} 
	
	public void set_panelXY(ImageControler panel_XY) {
		this.panel_XY = panel_XY;
		
	}


}
