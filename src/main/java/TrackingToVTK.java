import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TrackingToVTK {
	private ArrayList<ArrayList<ArrayList<Member>>> individual_tracks = new ArrayList<ArrayList<ArrayList<Member>>>();
	DataAnalisis data;
	
	public TrackingToVTK(DataAnalisis d_a) {
		data = d_a;
		this.individual_tracks = data.get_individual_track();
	}
	
	public void write_vtkFiles() {
		File folder_path = MenuBarGUI.create_folder("ParaviewVTK_");
		write_vtkSpheres(folder_path);
		write_vtkTrajectories(folder_path);
		write_vtkVector(folder_path);
	}
	
	public void write_vtkFilesIndividual() {
		File folder_path = MenuBarGUI.create_folder("ParaviewVTK_");
		write_vtkSpheresIndividual(folder_path);
		write_vtkTrajectoriesIndividual(folder_path);
		write_vtkVectorIndividual(folder_path);
	}	

	
	private void write_vtkSpheresIndividual(File folder_path) {
		
		ArrayList<ArrayList<ArrayList<Integer>>> colormapMitosisCell = data.get_mitosisCells();
		ArrayList<ArrayList<ArrayList<Double>>> colormap_velocity = data.get_VelocityCells();
		ArrayList<ArrayList<ArrayList<Double>>> colormap_cumulative_displacement = data.get_Displacement_cumulative_to_initalNucleus();
		ArrayList<ArrayList<ArrayList<Double>>> colormap_distance2root = null;
		ArrayList<ArrayList<ArrayList<Double>>> colormap_cell_displacement_rate = null;
		ArrayList<ArrayList<ArrayList<Double>>> colormap_cell_growth_rate = null;
		ArrayList<ArrayList<ArrayList<Double>>> colormap_distance_between_nuclei = null;
		ArrayList<ArrayList<ArrayList<Integer>>> colormap_cell_linage_constant;
		
		
		if (data.tracking_has_sorting_information()) {
			colormap_distance2root = data.get_distace_to_root_from_sorted_track();
			colormap_cell_displacement_rate = data.get_cell_displacement_rate_from_sorted_track();
			colormap_cell_growth_rate = data.get_cell_growth_rate_from_sorted_track();
			colormap_distance_between_nuclei = data.get_distance_between_nuclei();
			
			//write data information to CSV
			//opening file to save data
			File file_name_average = new File(folder_path.toString(),"Data_cell_dynamics.csv");
			PrintWriter csvWriter_cell_dynamics = MenuBarGUI.get_FileWriter(file_name_average);
			csvWriter_cell_dynamics.append("Time Point, Distance_to_IN, Cell_displacement_rate, Cell_growth_rate, Distance_between_adjacent_nuclei,\n");
			DecimalFormat df = new DecimalFormat("#.##");
			
			for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
				for (int n_track = 0; n_track<individual_tracks.size(); n_track++) {
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
						csvWriter_cell_dynamics.append( Integer.toString(tp)+ "," + df.format(colormap_distance2root.get(n_track).get(tp).get(i))+ "," + df.format(colormap_cell_displacement_rate.get(n_track).get(tp).get(i))+ "," + df.format(colormap_cell_growth_rate.get(n_track).get(tp).get(i)) + ","+ df.format(colormap_distance_between_nuclei.get(n_track).get(tp).get(i))  + "\n");
					}
				}
				
			}
			
			csvWriter_cell_dynamics.close();
			
		}
		
		colormap_cell_linage_constant = data.get_cells_colormap_as_linage();

		for (int n_track = 0; n_track<individual_tracks.size(); n_track++) {
			File folder_path_cell = new File(folder_path.toString(),"track_"+Integer.toString(n_track));
			if (! folder_path_cell.exists())
				folder_path_cell.mkdir();

			for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
				//writing a vtk file for time tp
				PrintWriter writer = MenuBarGUI.get_FileWriter(new java.io.File(folder_path_cell.toString(),"Sphere_TP_"+Integer.toString(tp)+".vtk"));
				writer.println("# vtk DataFile Version 3.0");
				writer.println("Unstructured grid legacy vtk file with point scalar data");
				writer.println("ASCII");
				writer.println("");
				writer.println("DATASET UNSTRUCTURED_GRID");
				writer.println("");

				//counting total number of cells in time point tp
				int n_cell = individual_tracks.get(n_track).get(tp).size();

				writer.println("POINTS "+ Integer.toString(n_cell) + " double");

				//writing all positions
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					writer.println(Double.toString(individual_tracks.get(n_track).get(tp).get(i).x) + " " + Double.toString(individual_tracks.get(n_track).get(tp).get(i).y) + " " + Double.toString(individual_tracks.get(n_track).get(tp).get(i).z));

				writer.println();
				writer.println("POINT_DATA "+ Integer.toString(n_cell));	

				//writing colormaps 
				//colormap CONSTANT color
				writer.println("SCALARS constant_color float");
				writer.println("LOOKUP_TABLE default"); 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					writer.println(Integer.toString(colormap_cell_linage_constant.get(n_track).get(tp).get(i)));


				//colormap current cell cycle number
				writer.println("SCALARS Cycle_No_Color float");
				writer.println("LOOKUP_TABLE default"); 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					writer.println(individual_tracks.get(n_track).get(tp).get(i).cell_cycle);


				//colormap mitosis identification
				writer.println("SCALARS Mitosis_Event_Color float");
				writer.println("LOOKUP_TABLE default");
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
					writer.println(colormapMitosisCell.get(n_track).get(tp).get(i));
				
				//colormap velocity
				writer.println("SCALARS Velocity_Color float");
				writer.println("LOOKUP_TABLE default");
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
					writer.println(colormap_velocity.get(n_track).get(tp).get(i));		
				
				//colormap cumulative displacement
				writer.println("SCALARS Displacement_Cumulative_Color float");
				writer.println("LOOKUP_TABLE default");
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
					writer.println(colormap_cumulative_displacement.get(n_track).get(tp).get(i));				
				

				if (data.tracking_has_sorting_information()) {
					//colormap distance to root
					writer.println("SCALARS Distance_to_IN float");
					writer.println("LOOKUP_TABLE default");
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_distance2root.get(n_track).get(tp).get(i));
					
					//colormap cell displacement
					writer.println("SCALARS Cell_displacement_rate float");
					writer.println("LOOKUP_TABLE default");
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_cell_displacement_rate.get(n_track).get(tp).get(i));
					
					//colormap cell growth rate
					writer.println("SCALARS Cell_growth_rate float");
					writer.println("LOOKUP_TABLE default");
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_cell_growth_rate.get(n_track).get(tp).get(i));
					
					//colormap cell displacement
					writer.println("SCALARS Distance_between_adjacent_nuclei float");
					writer.println("LOOKUP_TABLE default");
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_distance_between_nuclei.get(n_track).get(tp).get(i));					
					
				}
				writer.close();        	
			}
		}
	}
	

	private void write_vtkTrajectoriesIndividual(File folder_path) { 

		for (int n_track = 0; n_track<individual_tracks.size(); n_track++) {
			File folder_path_cell = new File(folder_path.toString(),"track_"+Integer.toString(n_track));   
			for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
				//writing a vtk file for time tp
				PrintWriter writer = MenuBarGUI.get_FileWriter(new java.io.File(folder_path_cell.toString(),"Trajectory_TP_"+Integer.toString(tp)+".vtk"));
				writer.println("# vtk DataFile Version 2.0");
				writer.println("Cells trajectories");
				writer.println("ASCII");
				writer.println("");
				writer.println("DATASET POLYDATA");
				writer.println("");

				//counting total number of cells from time point 0 to time point tp
				int n_cell = 0;
				for (int tp_it=0; tp_it<=tp;tp_it++) 
					n_cell = n_cell + individual_tracks.get(n_track).get(tp_it).size();

				writer.println("POINTS "+ Integer.toString(n_cell) + " double");

				//writing all positions and counting number of connections
				int n_connections=0;
				for (int tp_it=0; tp_it<=tp;tp_it++)
					for (int i=0;i<individual_tracks.get(n_track).get(tp_it).size();i++) {
						//writing position
						writer.println(Double.toString(individual_tracks.get(n_track).get(tp_it).get(i).x) + " " + Double.toString(individual_tracks.get(n_track).get(tp_it).get(i).y) + " " + Double.toString(individual_tracks.get(n_track).get(tp_it).get(i).z));

						//checking if we are going to create a line
						if (individual_tracks.get(n_track).get(tp_it).get(i).parent>=0)
							n_connections +=1;
					}

				writer.println();
				writer.println("LINES "+ Integer.toString(n_connections) + " " + Integer.toString(3*n_connections));		

				//writing the lines connecting points in time t to time t+1
				for (int tp_it=0; tp_it<=tp;tp_it++){
					for (int i=0;i<individual_tracks.get(n_track).get(tp_it).size();i++) {
						if (individual_tracks.get(n_track).get(tp_it).get(i).parent>=0)
							writer.println("2 " + Integer.toString(+individual_tracks.get(n_track).get(tp_it).get(i).ID) + " " + Integer.toString(individual_tracks.get(n_track).get(tp_it).get(i).parent));
					}
				}
				writer.println();
				writer.println("POINT_DATA "+ Integer.toString(n_cell));    		

				//writing colormaps
				//colormap CONSTANT color
				writer.println("SCALARS constant_color double");
				writer.println("LOOKUP_TABLE default");
				for (int tp_it=0; tp_it<=tp;tp_it++)
					for (int i=0;i<individual_tracks.get(n_track).get(tp_it).size();i++)
						writer.println(Integer.toString(n_track));

				//colormap current cell cycle number
				writer.println("SCALARS cellCycle_color double");
				writer.println("LOOKUP_TABLE default");
				for (int tp_it=0; tp_it<=tp;tp_it++)
					for (int i=0;i<individual_tracks.get(n_track).get(tp_it).size();i++)
						writer.println(individual_tracks.get(n_track).get(tp_it).get(i).cell_cycle);    		

				writer.close();        	
			}
		}
	}

	private void write_vtkVectorIndividual(File folder_path) {
		
		ArrayList<ArrayList<double[]>> track_vectors = compute_normal_mitotic_Vector();
		double pos_x, pos_y, pos_z, mean_pos_x, mean_pos_y, mean_pos_z;
		double dir_x, dir_y, dir_z, mean_dir_x, mean_dir_y, mean_dir_z;
		double dir_x_norm, dir_y_norm, dir_z_norm, mean_dir_x_norm, mean_dir_y_norm, mean_dir_z_norm, norm_vec;
		double factor_increase_mean_vec = 5;
		DecimalFormat df = new DecimalFormat("#.##");
		File file_name;
		
		for (int n_track = 0; n_track<individual_tracks.size(); n_track++) {
			File folder_path_cell = new File(folder_path.toString(),"track_"+Integer.toString(n_track));
			
			File file_name_average = new File(folder_path_cell.toString(),"Direction_Data_Average.csv");
			//System.out.println("Saving csv: " +  file_name);
			PrintWriter csvWriter_average = MenuBarGUI.get_FileWriter(file_name_average);
			csvWriter_average.append("Time Point, Direction X, Direction Y, Direction Z, R, Azimuth, Elevation,\n");			
			
			for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
				file_name = new File(folder_path_cell.toString(),"Direction_Data_TP_"+ Integer.toString(tp) +".csv");
				//System.out.println("Saving csv: " +  file_name);
				PrintWriter csvWriter = MenuBarGUI.get_FileWriter(file_name);
				csvWriter.append("Time Point, Direction X, Direction Y, Direction Z, R, Azimuth, Elevation,\n");
				
				//writing a vtk file for time tp
				PrintWriter writer = MenuBarGUI.get_FileWriter(new java.io.File(folder_path_cell.toString(),"Vectors_TP_"+Integer.toString(tp)+".vtk"));
				writer.println("# vtk DataFile Version 2.0");
				writer.println("Cells normal vectors");
				writer.println("ASCII");
				writer.println("");
				
				//counting total number of cells from time point 0 to time point tp
				int n_cell = 0;
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						n_cell+=1;
					}
				}
				
				if (n_cell>0) {
					//compute average direction and add it to points
					n_cell +=1;
				}
				
				writer.println("DATASET UNSTRUCTURED_GRID");
				writer.println("POINTS " + Integer.toString(n_cell) + " double");
				
				mean_pos_x = 0;
				mean_pos_y = 0;
				mean_pos_z = 0;
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						pos_x = track_vectors.get(n_track).get(i)[1];
						pos_y = track_vectors.get(n_track).get(i)[2];
						pos_z = track_vectors.get(n_track).get(i)[3];
						mean_pos_x += pos_x;
						mean_pos_y += pos_y;
						mean_pos_z += pos_z;
						writer.println(Double.toString(pos_x) + " " + Double.toString(pos_y) + " " + Double.toString(pos_z));
					}
				}
				
				if (n_cell>0) {
					mean_pos_x/=(n_cell-1);
					mean_pos_y/=(n_cell-1);
					mean_pos_z/=(n_cell-1);
					writer.println(Double.toString(mean_pos_x) + " " + Double.toString(mean_pos_y) + " " + Double.toString(mean_pos_z));
				}			
				
				writer.println();
				writer.println("POINT_DATA "+ Integer.toString(n_cell));	
				
				writer.println();
				writer.println("VECTORS Direction_Division float");
				mean_dir_x = 0;
				mean_dir_y = 0;
				mean_dir_z = 0;
				double [] sphe_coor= new double[3];
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						dir_x = track_vectors.get(n_track).get(i)[4];
						dir_y = track_vectors.get(n_track).get(i)[5];
						dir_z = track_vectors.get(n_track).get(i)[6];
						mean_dir_x += dir_x;
						mean_dir_y += dir_y;
						mean_dir_z += dir_z;
						writer.println(Double.toString(dir_x) + " " + Double.toString(dir_y) + " " + Double.toString(dir_z));
						
						sphe_coor = convertCartesianToSphericalCoordinates(dir_x, dir_y, dir_z);
						csvWriter.append(Integer.toString((int) track_vectors.get(n_track).get(i)[0]) + "," + df.format(dir_x)+ "," + df.format(dir_y)+ "," + df.format(dir_z) + ","+ df.format(sphe_coor[0]) + ","+ df.format(sphe_coor[1]) + ","+ df.format(sphe_coor[2]) + "\n");						
					}
				}
				if (n_cell>0) {
					mean_dir_x/=(n_cell-1);
					mean_dir_y/=(n_cell-1);
					mean_dir_z/=(n_cell-1);
					
					sphe_coor = convertCartesianToSphericalCoordinates(mean_dir_x, mean_dir_y, mean_dir_z);
					
					writer.println(Double.toString(factor_increase_mean_vec*mean_dir_x) + " " + Double.toString(factor_increase_mean_vec*mean_dir_y) + " " + Double.toString(factor_increase_mean_vec*mean_dir_z));
					csvWriter_average.append( Integer.toString(tp)+ "," + df.format(mean_dir_x)+ "," + df.format(mean_dir_y)+ "," + df.format(mean_dir_z) + ","+ df.format(sphe_coor[0]) + ","+ df.format(sphe_coor[1]) + ","+ df.format(sphe_coor[2]) + "\n");
				}
				csvWriter.close();
				
				writer.println();
				writer.println("VECTORS Direction_Division_Normalized float");
				mean_dir_x_norm = 0;
				mean_dir_y_norm = 0;
				mean_dir_z_norm = 0;
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						dir_x = track_vectors.get(n_track).get(i)[4];
						dir_y = track_vectors.get(n_track).get(i)[5];
						dir_z = track_vectors.get(n_track).get(i)[6];
						
						norm_vec = Math.sqrt(Math.pow(dir_x,2) + Math.pow(dir_y,2) + Math.pow(dir_z,2));
						if (norm_vec==0)
							norm_vec = Double.POSITIVE_INFINITY;
						dir_x_norm = dir_x/norm_vec;
						dir_y_norm = dir_y/norm_vec;
						dir_z_norm = dir_z/norm_vec;
						
						mean_dir_x_norm += dir_x_norm;
						mean_dir_y_norm += dir_y_norm;
						mean_dir_z_norm += dir_z_norm;
						writer.println(Double.toString(dir_x_norm) + " " + Double.toString(dir_y_norm) + " " + Double.toString(dir_z_norm));
					}
				}
				if (n_cell>0) {
					mean_dir_x_norm/=(n_cell-1);
					mean_dir_y_norm/=(n_cell-1);
					mean_dir_z_norm/=(n_cell-1);
					writer.println(Double.toString(mean_dir_x_norm) + " " + Double.toString(mean_dir_y_norm) + " " + Double.toString(mean_dir_z_norm));
				}			
				writer.close(); 
			}			
			csvWriter_average.close();
		}
	}	

	private void write_vtkSpheres(File folder_path) {   

		ArrayList<ArrayList<ArrayList<Integer>>> colormapMitosisCell = data.get_mitosisCells();
		ArrayList<ArrayList<ArrayList<Double>>> colormap_velocity = data.get_VelocityCells();
		ArrayList<ArrayList<ArrayList<Double>>> colormap_cumulative_displacement = data.get_Displacement_cumulative_to_initalNucleus();		
		ArrayList<ArrayList<ArrayList<Double>>> colormap_distance2root = null;
		ArrayList<ArrayList<ArrayList<Double>>> colormap_cell_displacement_rate = null;
		ArrayList<ArrayList<ArrayList<Double>>> colormap_cell_growth_rate = null;
		ArrayList<ArrayList<ArrayList<Double>>> colormap_distance_between_nuclei = null;
		ArrayList<ArrayList<ArrayList<Integer>>> colormap_cell_linage_constant;
		
		if (data.tracking_has_sorting_information()) {
			colormap_distance2root = data.get_distace_to_root_from_sorted_track();
			colormap_cell_displacement_rate = data.get_cell_displacement_rate_from_sorted_track();
			colormap_cell_growth_rate = data.get_cell_growth_rate_from_sorted_track();
			colormap_distance_between_nuclei = data.get_distance_between_nuclei();
			//System.out.println(colormap_cell_displacement.size());
			
			//write data information to CSV
			//opening file to save data
			File file_name_average = new File(folder_path.toString(),"Data_cell_dynamics.csv");
			PrintWriter csvWriter_cell_dynamics = MenuBarGUI.get_FileWriter(file_name_average);
			csvWriter_cell_dynamics.append("Time Point, Distance_to_IN, Cell_displacement_rate, Cell_growth_rate, Distance_between_adjacent_nuclei,\n");
			DecimalFormat df = new DecimalFormat("#.##");
			
			for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
				for (int n_track = 0; n_track<individual_tracks.size(); n_track++) {
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
						csvWriter_cell_dynamics.append( Integer.toString(tp)+ "," + df.format(colormap_distance2root.get(n_track).get(tp).get(i))+ "," + df.format(colormap_cell_displacement_rate.get(n_track).get(tp).get(i))+ "," + df.format(colormap_cell_growth_rate.get(n_track).get(tp).get(i)) + ","+ df.format(colormap_distance_between_nuclei.get(n_track).get(tp).get(i))  + "\n");
					}
				}
				
			}
			
			csvWriter_cell_dynamics.close();
		}
		
		colormap_cell_linage_constant = data.get_cells_colormap_as_linage();

		for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
			//writing a vtk file for time tp
			PrintWriter writer = MenuBarGUI.get_FileWriter(new java.io.File(folder_path.toString(),"Sphere_TP_"+Integer.toString(tp)+".vtk"));
			writer.println("# vtk DataFile Version 3.0");
			writer.println("Unstructured grid legacy vtk file with point scalar data");
			writer.println("ASCII");
			writer.println("");
			writer.println("DATASET UNSTRUCTURED_GRID");
			writer.println("");

			//counting total number of cells in time point tp
			int n_cell = 0;
			for (ArrayList<ArrayList<Member>> track_k:individual_tracks)
				n_cell += track_k.get(tp).size();

			writer.println("POINTS "+ Integer.toString(n_cell) + " double");

			//writing all positions
			for (ArrayList<ArrayList<Member>> track_k:individual_tracks) 
				for (int i=0;i<track_k.get(tp).size();i++)
					writer.println(Double.toString(track_k.get(tp).get(i).x) + " " + Double.toString(track_k.get(tp).get(i).y) + " " + Double.toString(track_k.get(tp).get(i).z));

			writer.println();
			writer.println("POINT_DATA "+ Integer.toString(n_cell));	


			//writing colormaps //colormap CONSTANT color
			writer.println("SCALARS constant_color float");
			writer.println("LOOKUP_TABLE default"); 
			for (int n_track=0;n_track<individual_tracks.size();n_track++) 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					writer.println(colormap_cell_linage_constant.get(n_track).get(tp).get(i));


			//colormap current cell cycle number
			writer.println("SCALARS cellCycle_color float");
			writer.println("LOOKUP_TABLE default"); 
			for (int n_track=0;n_track<individual_tracks.size();n_track++) 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					writer.println(individual_tracks.get(n_track).get(tp).get(i).cell_cycle);


			//colormap mitosis identification
			writer.println("SCALARS mitosisEvent_color float");
			writer.println("LOOKUP_TABLE default");
			for (int n_track=0;n_track<individual_tracks.size();n_track++) 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
					writer.println(colormapMitosisCell.get(n_track).get(tp).get(i));
			
			//colormap velocity 
			writer.println("SCALARS Velocity_Color float");
			writer.println("LOOKUP_TABLE default");
			for (int n_track=0;n_track<individual_tracks.size();n_track++) 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
					writer.println(colormap_velocity.get(n_track).get(tp).get(i));	
			
			//colormap displacement cumulative
			writer.println("SCALARS Displacement_Cumulative_Color float");
			writer.println("LOOKUP_TABLE default");
			for (int n_track=0;n_track<individual_tracks.size();n_track++) 
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
					writer.println(colormap_cumulative_displacement.get(n_track).get(tp).get(i));				
			
			if (data.tracking_has_sorting_information()) {
				//colormap distance to root
				writer.println("SCALARS Distance_to_IN float");
				writer.println("LOOKUP_TABLE default");
				for (int n_track=0;n_track<individual_tracks.size();n_track++) 
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_distance2root.get(n_track).get(tp).get(i));
				
				//colormap cell displacement
				writer.println("SCALARS Cell_displacement_rate float");
				writer.println("LOOKUP_TABLE default");
				for (int n_track=0;n_track<individual_tracks.size();n_track++) 
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_cell_displacement_rate.get(n_track).get(tp).get(i));
				
				//colormap cell growth rate
				writer.println("SCALARS Cell_growth_rate float");
				writer.println("LOOKUP_TABLE default");
				for (int n_track=0;n_track<individual_tracks.size();n_track++) 
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_cell_growth_rate.get(n_track).get(tp).get(i));
				
				//colormap cell growth rate
				writer.println("SCALARS Distance_between_adjacent_nuclei float");
				writer.println("LOOKUP_TABLE default");
				for (int n_track=0;n_track<individual_tracks.size();n_track++) 
					for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) 				
						writer.println(colormap_distance_between_nuclei.get(n_track).get(tp).get(i));				
				
			}
			

			/*
			 * writer.println("LOOKUP_TABLE lookupTableMitosisEvent "+
			 * Integer.toString(n_cell)); for (int
			 * n_track=0;n_track<individual_tracks.size();n_track++) { for (int
			 * i=0;i<individual_tracks.get(n_track).get(tp).size();i++) { if
			 * (colormapMitosisCell.get(n_track).get(tp).get(i)==1)
			 * writer.println("0 1 0 1");//color verde else writer.println("1 1 1 1");
			 * //color blanco n+=1; } }
			 */    		
			writer.close();        	
		}  
	}

	private void write_vtkTrajectories(File folder_path) {  

		for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
			//writing a vtk file for time tp
			PrintWriter writer = MenuBarGUI.get_FileWriter(new java.io.File(folder_path.toString(),"Trajectory_TP_"+Integer.toString(tp)+".vtk"));
			writer.println("# vtk DataFile Version 2.0");
			writer.println("Cells trajectories");
			writer.println("ASCII");
			writer.println("");
			writer.println("DATASET POLYDATA");
			writer.println("");

			//counting total number of cells from time point 0 to time point tp
			int n_cell = 0;
			for (ArrayList<ArrayList<Member>> track_k:individual_tracks)
				for (int tp_it=0; tp_it<=tp;tp_it++) 
					n_cell = n_cell + track_k.get(tp_it).size();

			writer.println("POINTS "+ Integer.toString(n_cell) + " double");

			//writing all positions and counting number of connections
			int n_connections=0;
			for (ArrayList<ArrayList<Member>> track_k:individual_tracks)
				for (int tp_it=0; tp_it<=tp;tp_it++)
					for (int i=0;i<track_k.get(tp_it).size();i++) {
						//writing position
						writer.println(Double.toString(track_k.get(tp_it).get(i).x) + " " + Double.toString(track_k.get(tp_it).get(i).y) + " " + Double.toString(track_k.get(tp_it).get(i).z));

						//checking if we are going to create a line
						if (track_k.get(tp_it).get(i).parent>=0)
							n_connections +=1;

					}

			writer.println();
			writer.println("LINES "+ Integer.toString(n_connections) + " " + Integer.toString(3*n_connections));		

			int shift_ID = 0;
			int shift_count;
			//writing the lines connecting points in time t to time t+1
			for (ArrayList<ArrayList<Member>> track_k:individual_tracks) {
				shift_count = 0;
				for (int tp_it=0; tp_it<=tp;tp_it++){
					for (int i=0;i<track_k.get(tp_it).size();i++) {
						if (track_k.get(tp_it).get(i).parent>=0)
							writer.println("2 " + Integer.toString(shift_ID +track_k.get(tp_it).get(i).ID) + " " + Integer.toString(shift_ID + track_k.get(tp_it).get(i).parent));
					}
					shift_count += track_k.get(tp_it).size();
				}
				shift_ID += shift_count;
			}
			writer.println();
			writer.println("POINT_DATA "+ Integer.toString(n_cell));    		

			//writing colormaps
			//colormap CONSTANT color
			writer.println("SCALARS constant_color double");
			writer.println("LOOKUP_TABLE default");
			for (int ID=0;ID<individual_tracks.size();ID++)
				for (int tp_it=0; tp_it<=tp;tp_it++)
					for (int i=0;i<individual_tracks.get(ID).get(tp_it).size();i++)
						writer.println(Integer.toString(ID));

			//colormap current cell cycle number
			writer.println("SCALARS cellCycle_color double");
			writer.println("LOOKUP_TABLE default");
			for (int n_track=0;n_track<individual_tracks.size();n_track++)
				for (int tp_it=0; tp_it<=tp;tp_it++)
					for (int i=0;i<individual_tracks.get(n_track).get(tp_it).size();i++)
						writer.println(individual_tracks.get(n_track).get(tp_it).get(i).cell_cycle);    		

			writer.close();        	
		} 
	}	
	
	private void write_vtkVector(File folder_path) {
		
		ArrayList<ArrayList<double[]>> track_vectors = compute_normal_mitotic_Vector();
		double pos_x, pos_y, pos_z, mean_pos_x, mean_pos_y, mean_pos_z;
		double dir_x, dir_y, dir_z, mean_dir_x, mean_dir_y, mean_dir_z;
		double dir_x_norm, dir_y_norm, dir_z_norm, mean_dir_x_norm, mean_dir_y_norm, mean_dir_z_norm, norm_vec;
		double factor_increase_mean_vec = 5;
		DecimalFormat df = new DecimalFormat("#.##");
		File file_name;
		
		file_name = new File(folder_path.toString(),"Direction_Data_Average.csv");
		//System.out.println("Saving csv: " +  file_name);
		PrintWriter csvWriter_average = MenuBarGUI.get_FileWriter(file_name);
		csvWriter_average.append("Time Point, Direction X, Direction Y, Direction Z, R, Azimuth, Elevation,\n");		
		
		
		for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
			file_name = new File(folder_path.toString(),"Direction_Data_TP_"+ Integer.toString(tp) +".csv");
			//System.out.println("Saving csv: " +  file_name);
			PrintWriter csvWriter = MenuBarGUI.get_FileWriter(file_name);
			csvWriter.append("Time Point, Direction X, Direction Y, Direction Z, R, Azimuth, Elevation,\n");
			
			//writing a vtk file for time tp
			PrintWriter writer = MenuBarGUI.get_FileWriter(new java.io.File(folder_path.toString(),"Vectors_TP_"+Integer.toString(tp)+".vtk"));
			writer.println("# vtk DataFile Version 2.0");
			writer.println("Cells normal vectors");
			writer.println("ASCII");
			writer.println("");
			
			//counting total number of cells from time point 0 to time point tp
			int n_cell = 0;
			for (int n_track=0; n_track<track_vectors.size();n_track++){
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						n_cell+=1;
					}
				}
			}
			
			if (n_cell>0) {
				//compute average direction and add it to points
				n_cell +=1;
			}
			
			writer.println("DATASET UNSTRUCTURED_GRID");
			writer.println("POINTS " + Integer.toString(n_cell) + " double");
			
			mean_pos_x = 0;
			mean_pos_y = 0;
			mean_pos_z = 0;
			for (int n_track=0; n_track<track_vectors.size();n_track++){
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						pos_x = track_vectors.get(n_track).get(i)[1];
						pos_y = track_vectors.get(n_track).get(i)[2];
						pos_z = track_vectors.get(n_track).get(i)[3];
						mean_pos_x += pos_x;
						mean_pos_y += pos_y;
						mean_pos_z += pos_z;
						writer.println(Double.toString(pos_x) + " " + Double.toString(pos_y) + " " + Double.toString(pos_z));
						
					}
				}
			}
			
			if (n_cell>0) {
				mean_pos_x/=(n_cell-1);
				mean_pos_y/=(n_cell-1);
				mean_pos_z/=(n_cell-1);
				writer.println(Double.toString(mean_pos_x) + " " + Double.toString(mean_pos_y) + " " + Double.toString(mean_pos_z));
			}			
			
			writer.println();
			writer.println("POINT_DATA "+ Integer.toString(n_cell));	
			
			writer.println();
			writer.println("VECTORS Direction_Division float");
			mean_dir_x = 0;
			mean_dir_y = 0;
			mean_dir_z = 0;
			double [] sphe_coor= new double[3];
			for (int n_track=0; n_track<track_vectors.size();n_track++){
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						dir_x = track_vectors.get(n_track).get(i)[4];
						dir_y = track_vectors.get(n_track).get(i)[5];
						dir_z = track_vectors.get(n_track).get(i)[6];
						mean_dir_x += dir_x;
						mean_dir_y += dir_y;
						mean_dir_z += dir_z;
						writer.println(Double.toString(dir_x) + " " + Double.toString(dir_y) + " " + Double.toString(dir_z));
						
						sphe_coor = convertCartesianToSphericalCoordinates(dir_x, dir_y, dir_z);
						csvWriter.append( Integer.toString((int) track_vectors.get(n_track).get(i)[0]) + "," + df.format(dir_x)+ "," + df.format(dir_y)+ "," + df.format(dir_z) + ","+ df.format(sphe_coor[0]) + ","+ df.format(sphe_coor[1]) + ","+ df.format(sphe_coor[2]) + "\n");
					}
				}
			}	
			if (n_cell>0) {
				mean_dir_x/=(n_cell-1);
				mean_dir_y/=(n_cell-1);
				mean_dir_z/=(n_cell-1);
				sphe_coor = convertCartesianToSphericalCoordinates(mean_dir_x, mean_dir_y, mean_dir_z);
				
				writer.println(Double.toString(factor_increase_mean_vec*mean_dir_x) + " " + Double.toString(factor_increase_mean_vec*mean_dir_y) + " " + Double.toString(factor_increase_mean_vec*mean_dir_z));
				csvWriter_average.append(Integer.toString(tp)+ "," + df.format(mean_dir_x)+ "," + df.format(mean_dir_y)+ "," + df.format(mean_dir_z) + ","+ df.format(sphe_coor[0]) + ","+ df.format(sphe_coor[1]) + ","+ df.format(sphe_coor[2]) + "\n");
			}
			csvWriter.close();
			
			writer.println();
			writer.println("VECTORS Direction_Division_Normalized float");
			mean_dir_x_norm = 0;
			mean_dir_y_norm = 0;
			mean_dir_z_norm = 0;
			for (int n_track=0; n_track<track_vectors.size();n_track++){
				for (int i=0;i<track_vectors.get(n_track).size();i++) {
					if (track_vectors.get(n_track).get(i)[0]<=tp) {
						dir_x = track_vectors.get(n_track).get(i)[4];
						dir_y = track_vectors.get(n_track).get(i)[5];
						dir_z = track_vectors.get(n_track).get(i)[6];
						
						norm_vec = Math.sqrt(Math.pow(dir_x,2) + Math.pow(dir_y,2) + Math.pow(dir_z,2));
						if (norm_vec==0)
							norm_vec = Double.POSITIVE_INFINITY;
						dir_x_norm = dir_x/norm_vec;
						dir_y_norm = dir_y/norm_vec;
						dir_z_norm = dir_z/norm_vec;
						
						mean_dir_x_norm += dir_x_norm;
						mean_dir_y_norm += dir_y_norm;
						mean_dir_z_norm += dir_z_norm;
						writer.println(Double.toString(dir_x_norm) + " " + Double.toString(dir_y_norm) + " " + Double.toString(dir_z_norm));
					}
				}
			}	
			if (n_cell>0) {
				mean_dir_x_norm/=(n_cell-1);
				mean_dir_y_norm/=(n_cell-1);
				mean_dir_z_norm/=(n_cell-1);
				writer.println(Double.toString(mean_dir_x_norm) + " " + Double.toString(mean_dir_y_norm) + " " + Double.toString(mean_dir_z_norm));
			}			
			writer.close(); 
		}
		csvWriter_average.close();
	}
	
	private ArrayList<ArrayList<double[]>> compute_normal_mitotic_Vector() {
		ArrayList<ArrayList<ArrayList<int[]>>> track_segments = data.get_tracks_as_segments();
		
		ArrayList<ArrayList<double[]>> track_vectors  = new ArrayList<ArrayList<double[]>>();
		
		int time_point, position,cell_parent, previous_time_point, parent_position, ind_last;
		double direction_x, direction_y, direction_z, pos_x, pos_y, pos_z;
		boolean parent_not_found;
		for (int n_track=0;n_track<track_segments.size();n_track++) {
			track_vectors.add(new ArrayList<double[]> ());
			for (int s=0;s<track_segments.get(n_track).size();s++) {
				//initial point from segment (point after a mitotic event)
				ind_last = track_segments.get(n_track).get(s).size()-1;
				time_point = track_segments.get(n_track).get(s).get(ind_last)[0];
				position = track_segments.get(n_track).get(s).get(ind_last)[1];
				
				if (individual_tracks.get(n_track).get(time_point).get(position).parent>=0) {
					parent_not_found = true;
					
					
					//current cells is not a root point and must be originated from a mitotic event
					cell_parent = individual_tracks.get(n_track).get(time_point).get(position).parent;
					
					for (int s2=0;s2<track_segments.get(n_track).size();s2++) {
						
						
						previous_time_point = track_segments.get(n_track).get(s2).get(0)[0];
						parent_position = track_segments.get(n_track).get(s2).get(0)[1];
						
					
					
					//previous_time_point = time_point-1;
					//for (int i=0; i<individual_tracks.get(n_track).get(time_point-1).size();i++) {
						if (individual_tracks.get(n_track).get(previous_time_point).get(parent_position).ID == cell_parent) {
							//parent has been found
							parent_not_found = false;							
							
							ind_last = track_segments.get(n_track).get(s2).size()-1;
							previous_time_point = track_segments.get(n_track).get(s2).get(ind_last)[0];
							parent_position = track_segments.get(n_track).get(s2).get(ind_last)[1];
							
							pos_x = individual_tracks.get(n_track).get(previous_time_point).get(parent_position).x;
							pos_y = individual_tracks.get(n_track).get(previous_time_point).get(parent_position).y;
							pos_z = individual_tracks.get(n_track).get(previous_time_point).get(parent_position).z;
							
							direction_x = individual_tracks.get(n_track).get(time_point).get(position).x - pos_x;
							direction_y = individual_tracks.get(n_track).get(time_point).get(position).y - pos_y;
							direction_z = individual_tracks.get(n_track).get(time_point).get(position).z - pos_z;
							
							track_vectors.get(n_track).add(new double[] {time_point, pos_x, pos_y, pos_z, direction_x, direction_y, direction_z});
							break;
						}
					}
					if (parent_not_found)
						throw new IllegalStateException("Compute_normal_mitotic_Vector --- Current cell does not have parent... Contact developer");
				}
			}
		}		
	return(track_vectors);
	}
	
	private double[] convertCartesianToSphericalCoordinates(double x, double y, double z) {
		double r = Math.sqrt(x*x + y*y + z*z);
		double azimuth = Math.atan2(y, x);
		double elevation = Math.atan2(Math.sqrt(x*x + y*y), z);
		return(new double[] {r,Math.toDegrees(azimuth),Math.toDegrees(elevation)});
	}
	

}
