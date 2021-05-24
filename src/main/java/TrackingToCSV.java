import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TrackingToCSV {
	DataAnalisis data;
	File folder_path;
	ArrayList<int[]> List_treeNodes;
	ArrayList<ArrayList<ArrayList<Integer>>> list_trees_children;
	
	public TrackingToCSV(DataAnalisis d_a, File folder_path, ArrayList<ArrayList<ArrayList<Integer>>> list_trees_children){
		//list_trees_children: a list organized as tree_id --- time_point --- cell_id --> containing the children for the node corresponding to cell_id
		//More than two children correspond to branching points (Mitosis)
		
		data = d_a;
		this.folder_path = folder_path;
		this.List_treeNodes = data.get_trackingAsParentNodes();
		this.list_trees_children = list_trees_children;
		
		//creating csv file
		create_csv_file();		
		
	}
	
	public void create_csv_file() {
		int parent;
		ArrayList<int[]> listnodes_divisionTimes = data.get_DivisionTimesAsListArray();
		ArrayList<int[]> listnodes_cellcycle = data.get_cellcycleAsListArray();
		
		for (int tree_id=0; tree_id<list_trees_children.size();tree_id++) {
			//creatin the file
			//FileWriter csvWriter;
			File file_name = new File(folder_path.toString(),"TreeID_"+Integer.toString(tree_id)+"_Data.csv");
			System.out.println("Saving csv: " +  file_name);
			PrintWriter csvWriter = MenuBarGUI.get_FileWriter(file_name);
			csvWriter.append("CellID, Cell_Cycle_No, Cycle duration,\n");
			
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
						
						csvWriter.append(Integer.toString(cell_id) + "," + Integer.toString(cell_cycle[cell_id])+ "," +Integer.toString(listnodes_divisionTimes.get(tree_id)[cell_id]) +"\n");
					}
				}
			}
			csvWriter.flush();
			csvWriter.close();		
		}
	}
}
