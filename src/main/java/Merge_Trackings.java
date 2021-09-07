import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.plugin.PlugIn;

public class Merge_Trackings implements PlugIn{
	
	public static void main(String arg[]) {
		new Merge_Trackings().run("");
	}	

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		//System.out.println("Testing Merge");
		
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", "txt");
		chooser.setFileFilter(filter);
		int result_chooser = chooser.showOpenDialog(null);
		
		File[] selected_file = chooser.getSelectedFiles();
		PrintWriter writer;
		
        if (result_chooser == JFileChooser.APPROVE_OPTION) {
        	String basePath = selected_file[0].getParent();
        	FileReader f_input;
        	
        	//genereting writer for output
    		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");  
    		LocalDateTime now = LocalDateTime.now();  
    		
    		File file_outputMerged = new File(basePath, "Merged_track_"+dtf.format(now)+".txt");
    		writer = MenuBarGUI.get_FileWriter(file_outputMerged);
        	String formatStr = "%s %d %d %5.2f %5.2f %5.2f %5.2f %5.2f %5.2f %s %d %s %d %s %d %d %d";
        	
        	//just to keep parameters from ellipsoid
        	int ID, timePoint=0, parent, N_division, R,G,B;
        	float x_pos, y_pos, z_pos, width, height, depth; 
        	
        	int scan_it, previousFileMax_ID = -1, previousFile_TP=0, max_ID;
    		for (int i=0; i<selected_file.length;i++) {    			 
             	max_ID = -1;
             	try {
             		//READING FILE
					f_input = new FileReader(selected_file[i]);
					
					
					//scanner to read values from text file
					Scanner src = new Scanner(f_input);
					scan_it = 0;
                    while (src.hasNext()) {
                    	src.next();
                    	scan_it+=1;
                    	ID = src.nextInt();
                    	timePoint = src.nextInt();
                    	x_pos = src.nextFloat();
                    	y_pos = src.nextFloat();
                    	z_pos = src.nextFloat();
                    	width = src.nextFloat();
                    	height = src.nextFloat();
                    	depth = src.nextFloat();
                    	src.next();
                    	parent = src.nextInt();
                    	src.next();
                    	N_division = src.nextInt();
                    	src.next();
                    	R = src.nextInt();
                    	G = src.nextInt();
                    	B = src.nextInt();
                    	//System.out.println(ID + " " + timePoint);
                    	if (ID >max_ID)
                    		max_ID = ID;
    				
                    	if (i==0)
                    		writer.println(String.format(formatStr,"Ellipse:",ID,timePoint+previousFile_TP, x_pos, y_pos, z_pos, width, height, depth," Parent: ",parent, "N_division: ",N_division, "Color:", R, G, B));
                    	else if(timePoint>0)
                    		writer.println(String.format(formatStr,"Ellipse:",ID,timePoint+previousFile_TP, x_pos, y_pos, z_pos, width, height, depth," Parent: ",parent, "N_division: ",N_division, "Color:", R, G, B));
                    	
                    	if (scan_it ==1) {
                    		if (previousFileMax_ID>=ID & timePoint>0) {
                    			writer.close();
                    			file_outputMerged.delete();
                    			return;
                    		}                    		
                    	}
                    	
                    } 
                    previousFileMax_ID = max_ID;
                    previousFile_TP = previousFile_TP+timePoint;
                    src.close();
                    f_input.close();					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
            writer.close();	
    		
//        	PrintWriter writer = get_FileWriter(selected_file);
//        	if (writer!=null) {   
//
//            	String formatStr = "%s %d %d %5.2f %5.2f %5.2f %5.2f %5.2f %5.2f %s %d %s %d %s %d %d %d";
//        		int parent;
//        		int tp = ImageControler.get_nTimePoints()-1;
//            	for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
//            		parent = (ellipse.getParent()==null)?-1:ellipse.getParent().ID;	                    				
//            		writer.println(String.format(formatStr,"Ellipse:",ellipse.ID,0, ellipse.getX(), ellipse.getY(), ellipse.getZ(), ellipse.getWidth(), ellipse.getHeight(), ellipse.getDepth()," Parent: ",parent, "N_division: ",ellipse.cell_cycle, "Color:", ellipse.color.getRed(), ellipse.color.getGreen(), ellipse.color.getBlue()));
//            	}
//                writer.close();
        }else{
        	System.out.println("Save command cancelled by user." );
        }  		
	}

}
