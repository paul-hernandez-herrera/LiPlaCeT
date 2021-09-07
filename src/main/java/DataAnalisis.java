import java.awt.Color;
import java.util.ArrayList;

class Member
{
    public int ID;
    public int tp;
    public double x;
    public double y;
    public double z;
    public int cell_cycle;
    public int parent;
    public int trackID;
    public int sort_ID =-1;
    public int sort_ID_parent =-1;
    public Color cell_color;
    
    
    public Member(int ID, int tp, double x, double y, double z, int cell_cycle, int parent, int sort_ID, int sort_ID_parent, Color cell_color) {
    	this.ID = ID;
    	this.tp = tp;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.cell_cycle = cell_cycle;
    	this.parent = parent;
    	this.sort_ID = sort_ID;
    	this.sort_ID_parent = sort_ID_parent;
    	this.cell_color = cell_color;
    }
    
 };

public class DataAnalisis {
	
	private ArrayList<ArrayList<ArrayList<Member>>> individual_tracks = new ArrayList<ArrayList<ArrayList<Member>>>();
	private ArrayList<ArrayList<ArrayList<Double>>>  list_Velocity = new ArrayList<ArrayList<ArrayList<Double>>> ();
	private ArrayList<ArrayList<ArrayList<Double>>>  list_Displacement_cumulative_to_initialNucleus = new ArrayList<ArrayList<ArrayList<Double>>> ();
	private ArrayList<ArrayList<ArrayList<Integer>>>  list_MitosisDetection = new ArrayList<ArrayList<ArrayList<Integer>>> ();
	private ArrayList<ArrayList<ArrayList<Integer>>>  list_cells_colormap_as_linage = new ArrayList<ArrayList<ArrayList<Integer>>> ();
	private ArrayList<ArrayList<ArrayList<Double>>>  list_Distace2Root = new ArrayList<ArrayList<ArrayList<Double>>> ();
	private ArrayList<ArrayList<ArrayList<Double>>>  list_cells_displacement_rate = new ArrayList<ArrayList<ArrayList<Double>>> ();
	private ArrayList<ArrayList<ArrayList<Double>>>  list_cells_growth_rate = new ArrayList<ArrayList<ArrayList<Double>>> ();
	private ArrayList<ArrayList<ArrayList<Double>>>  list_cells_length = new ArrayList<ArrayList<ArrayList<Double>>> ();
	private ArrayList<ArrayList<ArrayList<int[]>>> Arraylist_TrackSegments = new ArrayList<ArrayList<ArrayList<int[]>>>();
	private double[] total_track_displacement;
	private ArrayList<int[]> list_treeNodes, list_cellCycleNodes, list_divisionTimesNodes, list_timePoints,list_mitoticEventNodes;
	ArrayList<Color> colorCell = new ArrayList<Color>();
	private boolean does_track_has_sorting_information;
	
	DataAnalisis(boolean does_track_has_sorting_information){
		//constructor
		this.does_track_has_sorting_information = does_track_has_sorting_information;
		
		if (this.does_track_has_sorting_information) {
			MenuBarGUI.smooth_tracking_segments = false;
			get_Individual_Tracks_with_Sorting_Information_FromGUI();
			
			//compute measure from each point to root position
			compute_distance2root();
			
			//compute cell displacement rate
			compute_cell_displacement_rate_from_sorted_track();
			
			compute_cells_length();
			compute_cells_growth_rate_from_sorted_track();
		}
		else
			get_Individual_Tracks_FromGUI();
		
		//compute mitotic event
		compute_mitosisCells();
		
		//compute division time
		
		
		//compute colormap as cell linage
		compute_cells_colormap_as_linage();
		
		//just smooth data if requested by user
		if (MenuBarGUI.smooth_tracking_segments)
			smooth_tracks();		
		
		//compute velocity y displacement cumulative
		compute_velocity_and_displacement_toInitialNucleus();
		
		//compute track segments
		compute_track_as_segments();
		
		

		
		convert_trackingToTreeNodes();
        //print_trackInformation();
	}
	public boolean tracking_has_sorting_information() {
		return does_track_has_sorting_information;
	}
	
	public ArrayList<int[]> get_trackingAsParentNodes(){
		return (list_treeNodes);
	}
	
	public ArrayList<int[]> get_cellcycleAsListArray(){
		return (list_cellCycleNodes);
	}	
	
	public  ArrayList<int[]>  get_timePointsAsListArray(){
		return (list_timePoints);
	}
	
	public ArrayList<int[]> get_DivisionTimesAsListArray() {    	
		return list_divisionTimesNodes ; 
	}	
	
	
	public ArrayList<int[]> get_MitoticEventAsListArray()  {
		return list_mitoticEventNodes;
	}
	public ArrayList<ArrayList<ArrayList<Member>>> get_individual_track(){
		return (individual_tracks);
	}

	public ArrayList<ArrayList<ArrayList<Double>>> get_Displacement_cumulative_to_initalNucleus() {
		return (list_Displacement_cumulative_to_initialNucleus);
	}	
	
	public double[] get_total_track_displacement() {
		return (total_track_displacement);
	}
	
	public ArrayList<ArrayList<ArrayList<Double>>> get_VelocityCells() {
		return (list_Velocity);
	}	
	
	public ArrayList<ArrayList<ArrayList<Integer>>> get_mitosisCells() {
		return (list_MitosisDetection);
	}
	
	public ArrayList<ArrayList<ArrayList<Double>>> get_distace_to_root_from_sorted_track() {
		return (list_Distace2Root);
	}
	
	public ArrayList<ArrayList<ArrayList<Double>>> get_distance_between_nuclei() {
		return (list_cells_length);
	}	
	

	
	public ArrayList<ArrayList<ArrayList<Double>>> get_cell_displacement_rate_from_sorted_track() {
		return (list_cells_displacement_rate);
	}
	
	public ArrayList<ArrayList<ArrayList<Double>>> get_cell_growth_rate_from_sorted_track() {
		return (list_cells_growth_rate);
	}
	
	public ArrayList<ArrayList<ArrayList<Integer>>> get_cells_colormap_as_linage() {
		return (list_cells_colormap_as_linage);
	}
	
	public ArrayList<ArrayList<ArrayList<int[]>>> get_tracks_as_segments(){
		return (Arraylist_TrackSegments);
	}
	
	public ArrayList<Color> get_listColor(){
		return (colorCell);
	}	
	
	private void convert_trackingToTreeNodes() {
		//Convert tracking to treeNodes, convert cellCycle to treeNodes, convert DivisionTimes to TreeNodes

		list_treeNodes = new ArrayList<int[]>();
		list_mitoticEventNodes = new ArrayList<int[]>();
		list_cellCycleNodes = new ArrayList<int[]>();
		list_divisionTimesNodes = new ArrayList<int[]>();
		list_timePoints = new ArrayList<int[]>();
		ArrayList<ArrayList<ArrayList<Integer>>> List_DivisionTimes = get_divisionTime();
		
    	//counting total number of cells for track_k		
    	for (ArrayList<ArrayList<Member>> track_k:individual_tracks) {
    		int n_cells = 0;
    		for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++)
    			n_cells = n_cells + track_k.get(tp).size();
    		
    		//adding a new list of array to save the tree
    		list_mitoticEventNodes.add(new int[n_cells]);
    		list_treeNodes.add(new int[n_cells]);
    		list_cellCycleNodes.add(new int[n_cells]);
    		list_divisionTimesNodes.add(new int[n_cells]);
    		list_timePoints.add(new int[n_cells]);
    	}    		
    	int count;
    	
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		count = 0;
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					list_treeNodes.get(n_track)[count] = individual_tracks.get(n_track).get(tp).get(i).parent;
					list_cellCycleNodes.get(n_track)[count] = individual_tracks.get(n_track).get(tp).get(i).cell_cycle;
					list_divisionTimesNodes.get(n_track)[count] = List_DivisionTimes.get(n_track).get(tp).get(i);
					list_mitoticEventNodes.get(n_track)[count] = list_MitosisDetection.get(n_track).get(tp).get(i);
					list_timePoints.get(n_track)[count] = tp;
					if (count!=individual_tracks.get(n_track).get(tp).get(i).ID) 
						throw new IllegalStateException("Tree is not correctly organized... Contact developer");
					count++;
				}
			}
    	}	
	}
	
	private void compute_track_as_segments() {
		
		System.out.println("Computing tracks as segments");
		//function that computes the cell position from an initial division to before division
		
		ArrayList<ArrayList<ArrayList<Boolean>>>  cell_alreadyProcessed = new ArrayList<ArrayList<ArrayList<Boolean>>> ();
		
		//initialize division times to -1
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		cell_alreadyProcessed.add(new ArrayList<ArrayList<Boolean>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				cell_alreadyProcessed.get(n_track).add(new ArrayList<Boolean> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					cell_alreadyProcessed.get(n_track).get(tp).add(false);
			}						
    	}		
		ArrayList<ArrayList<ArrayList<Integer>>>  list_mitotic_event = get_mitosisCells();
				
		int parent_id, initial_id, current_position, current_timepoint;
		boolean continue_track;
		
		//smooth for each individual track
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		Arraylist_TrackSegments.add( new ArrayList<ArrayList<int[]>>() );
    		//System.out.println("N_TRACK ---- " + n_track);
			//starting at the last terminal point    		
			for (int tp=ImageControler.get_ellipsoidList_nTimePoints()-1;tp>=0;tp--) {
				
				//for each cell
				for (int cell_position=0;cell_position<individual_tracks.get(n_track).get(tp).size();cell_position++) {
					
					//check if points has already been processed
					if (cell_alreadyProcessed.get(n_track).get(tp).get(cell_position)==false) {
						//System.out.println("\n \n New segment --");
						//this points has not been processed and therefore it is a terminal point from the tree
						Arraylist_TrackSegments.get(n_track).add(new ArrayList<int[]>());
						
						current_position = cell_position;
						current_timepoint = tp;
						
						if (individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent<0) {
							//add new point to the last track
							Arraylist_TrackSegments.get(n_track).get(Arraylist_TrackSegments.get(n_track).size()-1).add(new int[]{current_timepoint, current_position});
							cell_alreadyProcessed.get(n_track).get(current_timepoint).set(current_position, true);
							
						}else {
							continue_track = true;
							while (continue_track) {
								////System.out.println(current_timepoint + " " + individual_tracks.get(n_track).get(current_timepoint).get(current_position).ID);
								//add new point to the last track
								Arraylist_TrackSegments.get(n_track).get(Arraylist_TrackSegments.get(n_track).size()-1).add(new int[]{current_timepoint, current_position});
								cell_alreadyProcessed.get(n_track).get(current_timepoint).set(current_position, true);
								
								parent_id = individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent;
								initial_id = individual_tracks.get(n_track).get(current_timepoint-1).get(0).ID;
								
								//System.out.println("N_TRACK ---- " + n_track + " TP: " + current_timepoint + "  pos:" + current_position +" id: " + individual_tracks.get(n_track).get(current_timepoint).get(current_position).ID  + "parent: "+ parent_id);
								
								//getting the position of parent_id						
								current_position = parent_id-initial_id;
								//System.out.println(current_position);
								
								current_timepoint -= 1;
								if (current_timepoint>=0) {
									if (parent_id != individual_tracks.get(n_track).get(current_timepoint).get(current_position).ID)
										throw new IllegalStateException("Parent ID not localized correctly... Contact developer");
									else if (individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent<0 & list_mitotic_event.get(n_track).get(current_timepoint).get(current_position)==0){
										//we have a root point break track
										continue_track=false;
										
										//add root point to list of track segments
										Arraylist_TrackSegments.get(n_track).get(Arraylist_TrackSegments.get(n_track).size()-1).add(new int[]{current_timepoint, current_position});
										cell_alreadyProcessed.get(n_track).get(current_timepoint).set(current_position, true);
										//System.out.println(current_timepoint + " " + individual_tracks.get(n_track).get(current_timepoint).get(current_position).ID);
									}else if(list_mitotic_event.get(n_track).get(current_timepoint).get(current_position)==1) {
										continue_track=false;
									}
								}else {
									continue_track=false;
								}
								
							}
						}
											
					}
				}
			}
    	}
	}
	
	private void smooth_tracks(){
		System.out.println("Smoothing tracks ");
		ArrayList<ArrayList<ArrayList<int[]>>> track_segments = get_tracks_as_segments();
		
		double mean_posX, mean_posY, mean_posZ;
		int current_position, current_timepoint;
		
		for (int n_track=0; n_track<track_segments.size();n_track++) {
			
			
			//computing average track
			for (int s=0;s<track_segments.get(n_track).size();s++) {
				mean_posX = 0; mean_posY = 0;mean_posZ = 0;
				for (int i=0;i<track_segments.get(n_track).get(s).size();i++) {
					current_timepoint = track_segments.get(n_track).get(s).get(i)[0];
					current_position  = track_segments.get(n_track).get(s).get(i)[1];
					mean_posX = mean_posX + individual_tracks.get(n_track).get(current_timepoint).get(current_position).x;
					mean_posY = mean_posY + individual_tracks.get(n_track).get(current_timepoint).get(current_position).y;
					mean_posZ = mean_posZ + individual_tracks.get(n_track).get(current_timepoint).get(current_position).z;
				}
				mean_posX /= track_segments.get(n_track).get(s).size();
				mean_posY /= track_segments.get(n_track).get(s).size();
				mean_posZ /= track_segments.get(n_track).get(s).size();
				
				//setting track segment to average value
				for (int i =0; i<track_segments.get(n_track).get(s).size();i++) {
					current_timepoint = track_segments.get(n_track).get(s).get(i)[0];
					current_position  = track_segments.get(n_track).get(s).get(i)[1];
					individual_tracks.get(n_track).get(current_timepoint).get(current_position).x = mean_posX;
					individual_tracks.get(n_track).get(current_timepoint).get(current_position).y = mean_posY;
					individual_tracks.get(n_track).get(current_timepoint).get(current_position).z = mean_posZ;
				}				
			}	
		}
	}	
		
	
	private ArrayList<ArrayList<ArrayList<Integer>>> get_divisionTime(){
		ArrayList<ArrayList<ArrayList<Integer>>>  List_divisionTimes = new ArrayList<ArrayList<ArrayList<Integer>>> ();
		ArrayList<int[]> list_descendants = new ArrayList<int[]>();
		
		//initialize division times to -1
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		List_divisionTimes.add(new ArrayList<ArrayList<Integer>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				List_divisionTimes.get(n_track).add(new ArrayList<Integer> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					List_divisionTimes.get(n_track).get(tp).add(-1);
			}						
    	}		
		ArrayList<ArrayList<ArrayList<Integer>>>  list_mitotic_event = get_mitosisCells();
				
		int parent_id, initial_id, current_position, current_timepoint, current_divisiontime;
		boolean continue_track, calculate_division_time;
		
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		//List_divisionTimes.add(new ArrayList<ArrayList<Integer>> ());
			for (int tp=ImageControler.get_ellipsoidList_nTimePoints()-1;tp>0;tp--) {
				//List_divisionTimes.get(n_track).add(new ArrayList<Integer> ());
				for (int cell_position=0;cell_position<individual_tracks.get(n_track).get(tp).size();cell_position++) {
					calculate_division_time = true;
					if (List_divisionTimes.get(n_track).get(tp).get(cell_position)==-1) {
						
						//this points has not been computed time
						//getting childs
						list_descendants.clear();
						
						if (list_mitotic_event.get(n_track).get(tp).get(cell_position)==0) {
							//division time can not be calculated for points without childs since we dont have correct knowledgue of the total division time
							calculate_division_time = false;
						}
						
						
						continue_track = true;
						current_position = cell_position;
						current_timepoint = tp;	
						
						if (individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent<0) {
							//we have an isolated point does not compute cell division time
							continue_track = false;
							calculate_division_time = false;
						}
						
						
						
						while (continue_track) {
							list_descendants.add(new int[]{current_timepoint, current_position});
							
							parent_id  = individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent;
							initial_id = individual_tracks.get(n_track).get(current_timepoint-1).get(0).ID;
							//getting the position of parent_id
							
							
							//System.out.println(" ID = " + individual_tracks.get(n_track).get(current_timepoint).get(current_position).ID + " parent " + parent_id);
							
							current_position = parent_id-initial_id;
							current_timepoint -= 1;
							
							
							if (parent_id != individual_tracks.get(n_track).get(current_timepoint).get(current_position).ID)
								throw new IllegalStateException("Parent ID not localized correctly... Contact developer");
							if (individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent<0){
								//we have a root point break track
								continue_track=false;
								if (list_mitotic_event.get(n_track).get(current_timepoint).get(current_position)==0) {
									//we have a root point and there is not division
									calculate_division_time = false;
								}
							}else if(list_mitotic_event.get(n_track).get(current_timepoint).get(current_position)==1) {
								continue_track=false;
							}							
						}
						
						current_divisiontime=0;
						if (calculate_division_time)
							current_divisiontime = list_descendants.size();
							
						if (individual_tracks.get(n_track).get(current_timepoint).get(current_position).parent<0) {
							//if the last point was a root point add it to the list
							list_descendants.add(new int[]{current_timepoint, current_position});
						}
						
						for (int j =0; j<list_descendants.size();j++)
							List_divisionTimes.get(n_track).get(list_descendants.get(j)[0]).set(list_descendants.get(j)[1],current_divisiontime);
					}
				}
			}
    	}		
		return List_divisionTimes;
	}
	
	private ArrayList<ArrayList<ArrayList<Integer>>> compute_mitosisCells() {
    	//counting total number of cells in time point tp
		
		int count, id;
		//System.out.println("Mitosis detection");
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_MitosisDetection.add(new ArrayList<ArrayList<Integer>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_MitosisDetection.get(n_track).add(new ArrayList<Integer> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					id = individual_tracks.get(n_track).get(tp).get(i).ID;
					list_MitosisDetection.get(n_track).get(tp).add(0);
					if (tp<(ImageControler.get_ellipsoidList_nTimePoints()-1)) {
						count = 0;
						for (int it=0;it<individual_tracks.get(n_track).get(tp+1).size();it++) {
							//count how many childs have id
							if (individual_tracks.get(n_track).get(tp+1).get(it).parent==id)
								count++;							
						}
						if (count>1)
							list_MitosisDetection.get(n_track).get(tp).set(i, 1);	
					}
					//System.out.println(List_mitosisDetection.get(n_track).get(tp).get(i));
				}
			}
    	}
		return (list_MitosisDetection);    	
	}	
	
	private ArrayList<ArrayList<ArrayList<Double>>> compute_velocity_and_displacement_toInitialNucleus() {
    	
		total_track_displacement = new double[individual_tracks.size()];
		//initializing total_tree_length to zero
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		total_track_displacement[n_track] = 0;
    	}
		
		
		//initializing velocity to zero
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_Velocity.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_Velocity.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					list_Velocity.get(n_track).get(tp).add(0.0);
			}						
    	}	
    	
		//initializing displacement to zero
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_Displacement_cumulative_to_initialNucleus.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_Displacement_cumulative_to_initialNucleus.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++)
					list_Displacement_cumulative_to_initialNucleus.get(n_track).get(tp).add(0.0);
			}						
    	}    	
		
		int parent_id, initial_id, current_position;
		
		Double parent_x, parent_y, parent_z, cell_x, cell_y, cell_z, distance, delta_t = 1.0;
		//System.out.println("Mitosis detection");
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_Velocity.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_Velocity.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					parent_id = individual_tracks.get(n_track).get(tp).get(i).parent;
					if (parent_id>=0) {
						//we have a parent at time tp-1
						
						//get the position of the id
						initial_id = individual_tracks.get(n_track).get(tp-1).get(0).ID;
						current_position = parent_id-initial_id;
						
						if (parent_id != individual_tracks.get(n_track).get(tp-1).get(current_position).ID) 
							throw new IllegalStateException("Parent position not localized correctly... Contact developer");
						
						parent_x = individual_tracks.get(n_track).get(tp-1).get(current_position).x;
						parent_y = individual_tracks.get(n_track).get(tp-1).get(current_position).y;
						parent_z = individual_tracks.get(n_track).get(tp-1).get(current_position).z;
						
						cell_x = individual_tracks.get(n_track).get(tp).get(i).x;
						cell_y = individual_tracks.get(n_track).get(tp).get(i).y;
						cell_z = individual_tracks.get(n_track).get(tp).get(i).z;
						
						distance = Math.sqrt(Math.pow(parent_x - cell_x, 2) +
								Math.pow(parent_y - cell_y, 2) +
								Math.pow(parent_z - cell_z, 2));
						
						total_track_displacement[n_track] =  total_track_displacement[n_track] + distance;
						
						//updating the velocity assuming constant acquisition time.
						list_Velocity.get(n_track).get(tp).set(i,distance/delta_t);
						
						//calculating cumulative distance
						list_Displacement_cumulative_to_initialNucleus.get(n_track).get(tp).set(i,distance +   list_Displacement_cumulative_to_initialNucleus.get(n_track).get(tp-1).get(current_position));
						
					}					
				}
			}
    	}
		return (list_Velocity);    	
	}	
	
	private ArrayList<ArrayList<ArrayList<Double>>> compute_distance2root() {
    	//counting total number of cells in time point tp
		
		int sorted_ID_parent,ind_root,ind_link_conection;
		double distance, previous_link_distance_to_root;

    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_Distace2Root.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				ind_root = -1;
				list_Distace2Root.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					sorted_ID_parent = individual_tracks.get(n_track).get(tp).get(i).sort_ID_parent;
					if (sorted_ID_parent==-1) {
						if (ind_root!=-1)
							throw new IllegalStateException("Only a single root point allowed by track when creating individual tracks");
						if (i!=0)
							throw new IllegalStateException("Root points must be located at position 0");
						list_Distace2Root.get(n_track).get(tp).add(0.0);
						ind_root = individual_tracks.get(n_track).get(tp).get(i).sort_ID;
					}else {
						if ((individual_tracks.get(n_track).get(tp).get(i).sort_ID - ind_root)!=i)
							throw new IllegalStateException("Sort pos ID not localized correctly... Contact developer");
						
						ind_link_conection = individual_tracks.get(n_track).get(tp).get(i).sort_ID_parent - ind_root;
						
						if (individual_tracks.get(n_track).get(tp).get(ind_link_conection).sort_ID != individual_tracks.get(n_track).get(tp).get(i).sort_ID_parent)
							throw new IllegalStateException("Parent position not localized correctly... Contact developer");						
						
						distance = Math.sqrt(Math.pow(individual_tracks.get(n_track).get(tp).get(i).x -individual_tracks.get(n_track).get(tp).get(ind_link_conection).x, 2) +
								Math.pow(individual_tracks.get(n_track).get(tp).get(i).y -individual_tracks.get(n_track).get(tp).get(ind_link_conection).y, 2) +
								Math.pow(individual_tracks.get(n_track).get(tp).get(i).z -individual_tracks.get(n_track).get(tp).get(ind_link_conection).z, 2));
						
						previous_link_distance_to_root = list_Distace2Root.get(n_track).get(tp).get(ind_link_conection);
						list_Distace2Root.get(n_track).get(tp).add(distance+previous_link_distance_to_root);
					}
				}
			}
    	}
		return (list_Distace2Root);    	
	}
	
	private ArrayList<ArrayList<ArrayList<Double>>> compute_cell_displacement_rate_from_sorted_track() {
		System.out.println("Computing cell displacement rate from sorted tracks");
		
		int previous_position_ind;
		double displacement,cell_previous_distance2root,cell_current_distance2root;

		ArrayList<ArrayList<ArrayList<Double>>> cells_distance_2_root = get_distace_to_root_from_sorted_track();
		
		
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_cells_displacement_rate.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_cells_displacement_rate.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					list_cells_displacement_rate.get(n_track).get(tp).add(0.0);
					int parent = individual_tracks.get(n_track).get(tp).get(i).parent;
					if (parent==-1) {
						//we have a root point and there is not cell displacement from previous time point
						displacement = 0.0;
					}else {
						//there is a parent and we can compute cell displacement
						previous_position_ind = individual_tracks.get(n_track).get(tp).get(i).parent - individual_tracks.get(n_track).get(tp-1).get(0).ID;
						if (individual_tracks.get(n_track).get(tp-1).get(previous_position_ind).ID != individual_tracks.get(n_track).get(tp).get(i).parent) 
							throw new IllegalStateException("Parent not found at correct position... contact developer");
						cell_current_distance2root = cells_distance_2_root.get(n_track).get(tp).get(i);
						cell_previous_distance2root = cells_distance_2_root.get(n_track).get(tp-1).get(previous_position_ind);
						
						//System.out.println("C: "+cell_current_distance2root + " P: " + cell_previous_distance2root + "  T: " + tp);
						displacement = cell_current_distance2root - cell_previous_distance2root;
					}
					
					
					list_cells_displacement_rate.get(n_track).get(tp).set(i, displacement/(MenuBarGUI.delta_time[tp]+Double.MIN_VALUE));
				}
			}
    	}
		return (list_cells_displacement_rate);    	
	}	
	
	private ArrayList<ArrayList<ArrayList<Integer>>> compute_cells_colormap_as_linage(){
		
		ArrayList<ArrayList<ArrayList<Integer>>> constant_val = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for (int i=0;i<=255;i++) {
			constant_val.add(new ArrayList<ArrayList<Integer>>());
			for (int j=0;j<=255;j++) {
				constant_val.get(i).add(new ArrayList<Integer>());
				for (int k=0;k<=255;k++) {
					constant_val.get(i).get(j).add(-1);
				}
			}
		}
			
		int integer_color_val, count_colors = 0;
		Color color_val;
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_cells_colormap_as_linage.add(new ArrayList<ArrayList<Integer>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_cells_colormap_as_linage.get(n_track).add(new ArrayList<Integer> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					//System.out.println("getting colormap: ntrack: " + n_track + "  tp: "  + tp + "  index: "  + i);
					color_val = individual_tracks.get(n_track).get(tp).get(i).cell_color;
					integer_color_val = constant_val.get(color_val.getRed()).get(color_val.getGreen()).get(color_val.getBlue());
					
					if (integer_color_val==-1) {
						//new sphere color detected
						constant_val.get(color_val.getRed()).get(color_val.getGreen()).set(color_val.getBlue(),count_colors);
						integer_color_val = count_colors;
						count_colors++;
					}
					list_cells_colormap_as_linage.get(n_track).get(tp).add(integer_color_val);
				}
			}
    	}
		return (list_cells_colormap_as_linage); 
		
	}
	
	private ArrayList<ArrayList<ArrayList<Double>>> compute_cells_length(){
		
		int sorted_ID_parent,ind_root,ind_link_conection;
		double distance_to_neighbor_cell;

    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_cells_length.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				ind_root = -1;
				list_cells_length.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					sorted_ID_parent = individual_tracks.get(n_track).get(tp).get(i).sort_ID_parent;
					 
					if (sorted_ID_parent == -1) {
						//no neighbor cell
						if (ind_root!=-1)
							throw new IllegalStateException("Only a single root point allowed by track when creating individual tracks");
						if (i!=0)
							throw new IllegalStateException("Root points must be located at position 0");
						ind_root = individual_tracks.get(n_track).get(tp).get(i).sort_ID;
						list_cells_length.get(n_track).get(tp).add(0.0);
					}else {
						if ((individual_tracks.get(n_track).get(tp).get(i).sort_ID - ind_root)!=i)
							throw new IllegalStateException("Sort pos ID not localized correctly... Contact developer");
						
						ind_link_conection = individual_tracks.get(n_track).get(tp).get(i).sort_ID_parent - ind_root;
						
						if (individual_tracks.get(n_track).get(tp).get(ind_link_conection).sort_ID != individual_tracks.get(n_track).get(tp).get(i).sort_ID_parent)
							throw new IllegalStateException("Parent position not localized correctly... Contact developer");						
						
						distance_to_neighbor_cell = Math.sqrt(Math.pow(individual_tracks.get(n_track).get(tp).get(i).x -individual_tracks.get(n_track).get(tp).get(ind_link_conection).x, 2) +
								Math.pow(individual_tracks.get(n_track).get(tp).get(i).y -individual_tracks.get(n_track).get(tp).get(ind_link_conection).y, 2) +
								Math.pow(individual_tracks.get(n_track).get(tp).get(i).z -individual_tracks.get(n_track).get(tp).get(ind_link_conection).z, 2));
						
						list_cells_length.get(n_track).get(tp).add(distance_to_neighbor_cell);
					}
					
				}
			}
    	}
		return (list_cells_length); 
		
	}	
	
	private ArrayList<ArrayList<ArrayList<Double>>> compute_cells_growth_rate_from_sorted_track() {
		System.out.println("Computing cell growth rate from sorted tracks");
		
		int previous_position_ind;
		double growth_rate,cell_previous_growth_rate,cell_current_growth_rate;

		ArrayList<ArrayList<ArrayList<Double>>> cells_growth_distance = get_distance_between_nuclei();
		
		
    	for (int n_track=0; n_track<individual_tracks.size();n_track++) {
    		list_cells_growth_rate.add(new ArrayList<ArrayList<Double>> ());
			for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
				list_cells_growth_rate.get(n_track).add(new ArrayList<Double> ());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					list_cells_growth_rate.get(n_track).get(tp).add(0.0);
					int parent = individual_tracks.get(n_track).get(tp).get(i).parent;
					if (parent==-1) {
						//we have a root point and there is not cell displacement from previous time point
						growth_rate = 0.0;
					}else {
						//there is a parent and we can compute cell displacement
						previous_position_ind = individual_tracks.get(n_track).get(tp).get(i).parent - individual_tracks.get(n_track).get(tp-1).get(0).ID;
						if (individual_tracks.get(n_track).get(tp-1).get(previous_position_ind).ID != individual_tracks.get(n_track).get(tp).get(i).parent) 
							throw new IllegalStateException("Parent not found at correct position... contact developer");
						cell_current_growth_rate  = cells_growth_distance.get(n_track).get(tp).get(i);
						cell_previous_growth_rate = cells_growth_distance.get(n_track).get(tp-1).get(previous_position_ind);
						
						//System.out.println("C: "+cell_current_distance2root + " P: " + cell_previous_distance2root + "  T: " + tp);
						growth_rate = cell_current_growth_rate - cell_previous_growth_rate;
					}
					list_cells_growth_rate.get(n_track).get(tp).set(i, growth_rate/(MenuBarGUI.delta_time[tp]+Double.MIN_VALUE));
				}
			}
    	}
		return (list_cells_growth_rate);    	
	}	
	
	private void get_Individual_Tracks_with_Sorting_Information_FromGUI() {
		System.out.println("Getting individual tracks with sorting information");
		//just to keep track of the individual trees
		ArrayList<Integer> cell_Tree_ID = new ArrayList<Integer>();
		for (int i=1;i<1000000;i++)
			cell_Tree_ID.add(-1);
		
		individual_tracks.clear();
		colorCell.clear();
		
		int track_ID = 0;
		int tree_ID = 0;
		
		Sort_tracks d_sorting = new Sort_tracks();
		ArrayList<ArrayList<Ellipsoid>> array_sorted_spheres = new ArrayList<ArrayList<Ellipsoid>>();
		ArrayList<ArrayList<Integer>> array_sorted_spheres_tree_ID = new ArrayList<ArrayList<Integer>>();
		Ellipsoid ellipse;
		int parent_ellipse_ID;
		int parent_sorted;
		for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
			array_sorted_spheres_tree_ID.add(new ArrayList<Integer>());
			array_sorted_spheres.add(d_sorting.sort_spheres_by_sortingID(tp));
			
			for (int i=0; i<array_sorted_spheres.get(tp).size();i++){
				ellipse = array_sorted_spheres.get(tp).get(i);
				parent_sorted = ellipse.sort_ID_parent;
				
				if (ellipse.sort_ID!=i) {
					throw new IllegalStateException("Sort pos ID not localized correctly... Contact developer");
				}
				
				parent_ellipse_ID = (ellipse.getParent()==null)?-1:ellipse.getParent().ID;
				
        		if (parent_ellipse_ID==-1 && parent_sorted == -1) {
        			//create a new individual tree to save the tracking       			
        			individual_tracks.add(new ArrayList<ArrayList<Member>>());
        			colorCell.add(ellipse.getColor());
        			for (int tp_2=0; tp_2<ImageControler.get_ellipsoidList_nTimePoints();tp_2++)
        				individual_tracks.get(track_ID).add(new ArrayList<Member>());
        			tree_ID = track_ID;
        			track_ID = track_ID+1;  			
        		}
        		else{        			
        			if (parent_sorted==-1) {
        				//current cell is the root position and it has a parent, then check the tree for the parent
        				tree_ID =  cell_Tree_ID.get(parent_ellipse_ID);
        			}else {
        				//current cell is connected with another cell in current time point, then it must belong to the same tree as the connection
        				tree_ID =  cell_Tree_ID.get(array_sorted_spheres.get(tp).get(parent_sorted).ID);
        			}
        		}
        		
    			individual_tracks.get(tree_ID).get(tp).add(new Member((int) ellipse.ID, tp, ImageControler.voxel_size.pixelWidth*ellipse.getX(), ImageControler.voxel_size.pixelHeight*ellipse.getY(), ImageControler.voxel_size.pixelDepth*ellipse.getZ(), (int) ellipse.cell_cycle, parent_ellipse_ID, ellipse.sort_ID, ellipse.sort_ID_parent, ellipse.color));
    			//setting the tree id for current cell
    			cell_Tree_ID.set((int) ellipse.ID, tree_ID);        		
			}
		}
		tracking_updateIds();	
	}
	
	private void get_Individual_Tracks_FromGUI() {
		//just to keep track of the individual trees
		ArrayList<Integer> cell_tree_ID = new ArrayList<Integer>();
		for (int i=1;i<1000000;i++)
			cell_tree_ID.add(-1);
		
		individual_tracks.clear();
		colorCell.clear();
		
		
		int track_ID = 0;
		int tree_ID = 0;
		int parent;
		boolean create_new_individual_track = false;
        for (int tp=0; tp<ImageControler.get_ellipsoidList_nTimePoints();tp++){
        	for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
        		parent = (ellipse.getParent()==null)?-1:ellipse.getParent().ID;
        		if (parent>=0) {
        			//TreeID same as parent
        			create_new_individual_track = false;  			
        		}
        		else if (parent ==-1) {
        			//we have a new tree        			
        			create_new_individual_track = true;
        			
        		}else {
        			//negative parent and smaller than -1, then there is a cell with division at time 0
        			//System.out.println("analyzing node  " + parent);
        			create_new_individual_track= true;
        			outerloop:
        			for (int tree_id=0; tree_id <individual_tracks.size();tree_id++) {
        				for (int tp_2=0; tp_2<ImageControler.get_ellipsoidList_nTimePoints();tp_2++) {
        					for (int cell_id=0;cell_id<individual_tracks.get(tree_id).get(tp_2).size();cell_id++) {
        						//System.out.println("tree_id " +tree_id + " tp_2 " + tp_2+ " cell_id "+ cell_id + "ID " +individual_tracks.get(tree_id).get(tp_2).get(cell_id).ID);
        						if (individual_tracks.get(tree_id).get(tp_2).get(cell_id).parent==parent) {
        							tree_ID = cell_tree_ID.get(individual_tracks.get(tree_id).get(tp_2).get(cell_id).ID);
        							create_new_individual_track= false;
        							break outerloop;
        						}
        					}        						
        				}        					
        			}
        		}
        		if (create_new_individual_track) {
        			//we have a new tree        			
        			individual_tracks.add(new ArrayList<ArrayList<Member>>());
        			colorCell.add(ellipse.getColor());
        			for (int tp_2=0; tp_2<ImageControler.get_ellipsoidList_nTimePoints();tp_2++)
        				individual_tracks.get(track_ID).add(new ArrayList<Member>());
        			tree_ID = track_ID;
        			track_ID = track_ID+1;  			
        		}
        		else{        			
        			//TreeID same as parent
        			if (parent>0)
        				tree_ID =  cell_tree_ID.get(parent);        			
        		}  
        		
    			individual_tracks.get(tree_ID).get(tp).add(new Member((int) ellipse.ID, tp, ImageControler.voxel_size.pixelWidth*ellipse.getX(), ImageControler.voxel_size.pixelHeight*ellipse.getY(), ImageControler.voxel_size.pixelDepth*ellipse.getZ(), (int) ellipse.cell_cycle, (int) parent, -1, -1, ellipse.color));
    			cell_tree_ID.set((int) ellipse.ID, tree_ID);
        	}
        }
        tracking_updateIds();
	}
	
	public int[] get_track_initialTP() {
		int[] track_initialTP = new int[individual_tracks.size()];
		int timepoint;
		for (int tree_id=0; tree_id<individual_tracks.size();tree_id++) {
			timepoint=0;
			while (individual_tracks.get(tree_id).get(timepoint).size()==0)
				timepoint++;
			track_initialTP[tree_id]=timepoint;
		}
		return (track_initialTP);		
	}
	
	
	public void print_trackInformation() {
		int k=1;
		for (ArrayList<ArrayList<Member>> track_k:individual_tracks) {
			System.out.println("Printing new track " + k);
			for (int tp=0;tp<track_k.size();tp++)
				for (int i=0;i<track_k.get(tp).size();i++)
					System.out.println(track_k.get(tp).get(i).ID + " " + tp + " " + track_k.get(tp).get(i).parent);
			System.out.println("");
			k=k+1;
		}
		
	}
	

	
	private void tracking_updateIds() {
		int old_Id = -1;
		int cell_ID;
		
		//initialize updated sphere to false
		ArrayList<ArrayList<ArrayList<Boolean>>> track_updated = new ArrayList<ArrayList<ArrayList<Boolean>>>();
		for (int n_track=0; n_track<individual_tracks.size();n_track++) {
			track_updated.add(new ArrayList<ArrayList<Boolean>>());
			for (int tp=0;tp<individual_tracks.get(n_track).size();tp++) {
				track_updated.get(n_track).add(new ArrayList<Boolean>());
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					track_updated.get(n_track).get(tp).add(false);
				}
			}
		}
		
		for (int n_track=0; n_track<individual_tracks.size();n_track++) {
			cell_ID =0;
			for (int tp=0;tp<individual_tracks.get(n_track).size();tp++) {
				for (int i=0;i<individual_tracks.get(n_track).get(tp).size();i++) {
					old_Id = individual_tracks.get(n_track).get(tp).get(i).ID;
					individual_tracks.get(n_track).get(tp).get(i).ID =cell_ID;
					if (tp <(individual_tracks.get(n_track).size()-1)) {
						//search for cells connected to current ID
						for (int it=0;it<individual_tracks.get(n_track).get(tp+1).size();it++) {
							if (individual_tracks.get(n_track).get(tp+1).get(it).parent==old_Id & track_updated.get(n_track).get(tp+1).get(it)==false) {
								individual_tracks.get(n_track).get(tp+1).get(it).parent=cell_ID;						
								track_updated.get(n_track).get(tp+1).set(it,true);
							}
						}						
					}
					cell_ID++;
				}
			}
		}
	}
	
}
