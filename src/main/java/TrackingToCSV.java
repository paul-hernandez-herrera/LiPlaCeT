import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TrackingToCSV {
	DataAnalisis data;
	File folder_path;
	ArrayList<int[]> List_treeNodes;
	ArrayList<ArrayList<ArrayList<Integer>>> list_trees_children;
	
	public TrackingToCSV(DataAnalisis d_a, File folder_path, ArrayList<ArrayList<ArrayList<Integer>>> list_trees_children){
		//list_trees_children: a list organized as tree_id --- cell id --- cell_id_children --> containing the children for the node corresponding to cell_id
		//More than two children correspond to branching points (Mitosis)
		
		data = d_a;
		this.folder_path = folder_path;
		this.List_treeNodes = data.get_trackingAsParentNodes();
		this.list_trees_children = list_trees_children;
		
		//creating csv file
		create_csv_file();	
		
		//creating number of cells per time point
		create_csv_number_of_spheres();
		
		//creating track statistics
		create_csv_track_statistics();
	}
	
	public void create_csv_file() {
		int parent;
		ArrayList<int[]> listnodes_divisionTimes = data.get_DivisionTimesAsListArray();
		ArrayList<int[]> listnodes_cellcycle = data.get_cellcycleAsListArray();
		ArrayList<int[]> listnodes_timePoint = data.get_timePointsAsListArray();
		
		for (int tree_id=0; tree_id<list_trees_children.size();tree_id++) {
			//creatin the file
			//FileWriter csvWriter;
			File file_name = new File(folder_path.toString(),"TreeID_"+Integer.toString(tree_id)+"_Data.csv");
			System.out.println("Saving csv: " +  file_name);
			PrintWriter csvWriter = MenuBarGUI.get_FileWriter(file_name);
			csvWriter.append("CellID, Cell_Cycle_No, Cycle duration, time-point-birth,time-point-division\n");
			
			int[] cell_cycle = new int[list_trees_children.get(tree_id).size()];			
			
			for (int cell_id=0;cell_id<list_trees_children.get(tree_id).size();cell_id++) {
				parent = List_treeNodes.get(tree_id)[cell_id];
				if (parent>=0 ) {
					cell_cycle[cell_id]=cell_cycle[parent];
					if (list_trees_children.get(tree_id).get(parent).size()>1) {
						if (MenuBarGUI.compute_cell_cycle_from_GUI) {
							cell_cycle[cell_id] = listnodes_cellcycle.get(tree_id)[cell_id];
						}else {
							cell_cycle[cell_id]+=1;							
						}
						
						//get the time for division
						int time_div = 0, current_tree = tree_id, current_id = cell_id;
						boolean calculate_time=false;
						while (list_trees_children.get(current_tree).get(current_id).size()>0) {
							time_div+=1;
							if (list_trees_children.get(current_tree).get(current_id).size()==1) {
								current_id = list_trees_children.get(current_tree).get(current_id).get(0);
							}else {
								//we have a division 
								calculate_time = true;
								break;								
							}
						}
						if (!calculate_time)
							time_div= -listnodes_timePoint.get(tree_id)[cell_id]-1;
						
						
						csvWriter.append(cell_id + "," + cell_cycle[cell_id]+ "," + listnodes_divisionTimes.get(tree_id)[cell_id] +", " + listnodes_timePoint.get(tree_id)[cell_id] +","+(listnodes_timePoint.get(tree_id)[cell_id]+time_div)+"\n");
					}
				}
			}
			csvWriter.flush();
			csvWriter.close();		
		}
	}
	
	public void create_csv_number_of_spheres() {
		
		ArrayList<ArrayList<ArrayList<Member>>> individual_trees = data.get_individual_track();
		
		int number_trees = individual_trees.size();
		int max_time_point = ImageControler.get_ellipsoidList_nTimePoints();
		int [][] array_number_spheres = new int[number_trees][max_time_point];
		
		for (int tree_id=0;tree_id<number_trees; tree_id++) {
			for (int tp =0; tp<max_time_point;tp++) {
				if (tp==0) {
					array_number_spheres[tree_id][tp] = individual_trees.get(tree_id).get(tp).size(); 
				}else {
					array_number_spheres[tree_id][tp] = array_number_spheres[tree_id][tp-1] + individual_trees.get(tree_id).get(tp).size()-individual_trees.get(tree_id).get(tp-1).size();
				}
			}
		}
		
		File file_name = new File(folder_path.toString(),"number_cells.csv");
		
		PrintWriter csvWriter = MenuBarGUI.get_FileWriter(file_name);
		for (int tree_id=0;tree_id<number_trees; tree_id++) {
			csvWriter.append(", Tree_"+ Integer.toString(tree_id) );
		}
		csvWriter.append("\n");
		
		for (int tp =0; tp<max_time_point;tp++){
			csvWriter.append("TP " + Integer.toString(tp)  +",");
			for (int tree_id=0;tree_id<number_trees; tree_id++)  {
				csvWriter.append(Integer.toString(array_number_spheres[tree_id][tp]) + ",");
			}
			csvWriter.append("\n");
		}		
			

		csvWriter.flush();
		csvWriter.close();
		
	}
	
	public void create_csv_track_statistics() {
		
		ArrayList<int[]> listnodes_divisionTimes = data.get_DivisionTimesAsListArray();
		ArrayList<int[]> tree_asParentNodes = data.get_trackingAsParentNodes();
		ArrayList<int[]> mitosisEvent_trees = data.get_MitoticEventAsListArray();
		double [] total_track_displacement = data.get_total_track_displacement() ;
		
		int number_trees = tree_asParentNodes.size();
		int max_time_point = ImageControler.get_ellipsoidList_nTimePoints();
		int [] total_spots = new int[number_trees];
		int [] number_divisions = new int[number_trees];
		Double [] meanDivisionTime = new Double[number_trees];
		Double [] stdDivisionTime = new Double[number_trees];
		
		//computing total #spots, total number_divisions
		for (int tree_id=0;tree_id<number_trees; tree_id++) {
			int n_cells = tree_asParentNodes.get(tree_id).length;
			total_spots[tree_id] = n_cells;

			number_divisions[tree_id] = 0;
			for (int cell_id=0; cell_id< n_cells;cell_id++) 
				number_divisions[tree_id] = number_divisions[tree_id]+ mitosisEvent_trees.get(tree_id)[cell_id]; 
		}
		
		//computing mean division time
		double cell_cycle_duration = 0;
		double mean_cycle_duration, std_cycle_duration;
		ArrayList<Double> list_cell_cycle_duration = new ArrayList<Double>(); 
		
		for (int tree_id=0;tree_id<number_trees; tree_id++) {
			//initializing the value
			meanDivisionTime[tree_id] = -1.0;
			stdDivisionTime[tree_id] = -1.0;
			std_cycle_duration  = 0;
			mean_cycle_duration = 0;
			list_cell_cycle_duration.clear();
			for (int cell_id=0; cell_id<tree_asParentNodes.get(tree_id).length; cell_id++) {
				
				cell_cycle_duration = listnodes_divisionTimes.get(tree_id)[cell_id];
				int parent = tree_asParentNodes.get(tree_id)[cell_id];
				if (parent>=0) {
					if (cell_cycle_duration > Double.MIN_VALUE && mitosisEvent_trees.get(tree_id)[parent] ==1) {
						list_cell_cycle_duration.add(cell_cycle_duration);
						//System.out.println(cell_cycle_duration + " " + cell_id);
					}	
				}
			}
			
			//computing meanDivisionTime
			if (list_cell_cycle_duration.size() > 0) {
				//sum all the cycle durations
				for (int i=0; i< list_cell_cycle_duration.size(); i++)
					mean_cycle_duration = mean_cycle_duration + list_cell_cycle_duration.get(i);
				//System.out.println("sum =" + mean_cycle_duration);
				//divide to compute mean
				meanDivisionTime[tree_id] = mean_cycle_duration/list_cell_cycle_duration.size();
				
				//sum of difference between current_value and mean_value
				for (int i=0; i< list_cell_cycle_duration.size(); i++)
					std_cycle_duration = Math.pow(list_cell_cycle_duration.get(i) - meanDivisionTime[tree_id],2);
				stdDivisionTime[tree_id] = Math.sqrt(std_cycle_duration/list_cell_cycle_duration.size());
			}
			

		}
		
		
		File file_name = new File(folder_path.toString(),"track_statistics.csv");
		
		PrintWriter csvWriter = MenuBarGUI.get_FileWriter(file_name);
		csvWriter.append("Tree_ID, total displacement, Number spots, Number divisions, Mean division time, Std division time\n");
		for (int tree_id=0;tree_id<number_trees; tree_id++) {
			csvWriter.append(Integer.toString(tree_id) + ", " + Double.toString(total_track_displacement[tree_id]) + ", " + Integer.toString(total_spots[tree_id])+ ", " + Integer.toString(number_divisions[tree_id]) + ", " + Double.toString(meanDivisionTime[tree_id]) + ", " + stdDivisionTime[tree_id] + "\n");
		}			

		csvWriter.flush();
		csvWriter.close();
		
	}	
}
