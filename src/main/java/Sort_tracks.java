import java.util.ArrayList;

public class Sort_tracks {
	public static boolean draw_text_horizontal = false;
	public void sort_tracks(int t) {
		if (t==0 & !does_tracking_have_sorting_information_at_time(0)) {
			//sort for both times t=0 and t=1
			System.out.println("Sorting time 0");
        	for (int i = 0; i<ImageControler.ellipsoidList.get(0).size();i++){
        		ImageControler.ellipsoidList.get(0).get(i).sort_ID = i;
        		ImageControler.ellipsoidList.get(0).get(i).sort_ID_parent = i-1;
        	}
		}
		
		create_new_sorting(t+1);	
	}
	
	public ArrayList<Ellipsoid> sort_spheres_by_sortingID(int t) {
		ArrayList<Ellipsoid> array_sorted_spheres_by_sortID = new ArrayList<Ellipsoid>();
		if (ImageControler.ellipsoidList.get(t).size()>0) {
			array_sorted_spheres_by_sortID.add(ImageControler.ellipsoidList.get(t).get(0));
			int current_id_sorting,list_id_sorting;
			boolean current_value_is_minimum;
			for (int i=1;i<ImageControler.ellipsoidList.get(t).size();i++) {
				current_value_is_minimum=true;
				current_id_sorting = ImageControler.ellipsoidList.get(t).get(i).sort_ID;
				
				for (int j=array_sorted_spheres_by_sortID.size()-1;j>=0;j--) {
					list_id_sorting = array_sorted_spheres_by_sortID.get(j).sort_ID;
					if (current_id_sorting>list_id_sorting) {
						array_sorted_spheres_by_sortID.add(j+1,ImageControler.ellipsoidList.get(t).get(i));
						current_value_is_minimum = false;
						break;
					}					
				}
				if (current_value_is_minimum)
					array_sorted_spheres_by_sortID.add(0,ImageControler.ellipsoidList.get(t).get(i));
			}	
		}
		return (array_sorted_spheres_by_sortID);
	}

	public ArrayList<ArrayList<Integer>>  get_child_list(ArrayList<Ellipsoid> array_sorted_spheres, int t) {
		
		ArrayList<ArrayList<Integer>>  child_pos = new ArrayList<ArrayList<Integer>> ();
		for (int i=0; i<array_sorted_spheres.size();i++) {
			System.out.println("Sorted spheres " + array_sorted_spheres.get(i).sort_ID);
			child_pos.add(new ArrayList<Integer>());
		}
			
		
		for (int i=0;i<ImageControler.ellipsoidList.get(t+1).size();i++) {
			Ellipsoid parent_ellipse = ImageControler.ellipsoidList.get(t+1).get(i).getParent();
			if (parent_ellipse!=null) {
				int pos = array_sorted_spheres.indexOf(parent_ellipse);
				if (pos>=0)
					child_pos.get(pos).add(i);				
			}
		}
		return child_pos;
	}
	
	
	public void create_new_sorting(int t) {
		
		ArrayList<Ellipsoid> array_sorted_spheres_previousTime = sort_spheres_by_sortingID(t-1);
		
		ArrayList<ArrayList<Integer>>  child_pos = get_child_list(array_sorted_spheres_previousTime,t-1);
		
		ArrayList<Integer> sphere_connection_pos = new ArrayList<Integer>();
		
		//creating sorting for time t
		int count = 0, parent_sort_ID_previous_t, sphere_pos, parent_sort_ID_current_t;
		for (int i=0;i<child_pos.size();i++) {
			parent_sort_ID_previous_t = array_sorted_spheres_previousTime.get(i).sort_ID_parent;
			for (int j=0;j<child_pos.get(i).size();j++) {
				sphere_pos = child_pos.get(i).get(j);
				
				ImageControler.ellipsoidList.get(t).get(sphere_pos).sort_ID = count;
				if (j==0) {	
					if (parent_sort_ID_previous_t<0)
						parent_sort_ID_current_t = -1;
					else
						parent_sort_ID_current_t = sphere_connection_pos.get(parent_sort_ID_previous_t);
				}else {
					parent_sort_ID_current_t = count-1;
				}
				ImageControler.ellipsoidList.get(t).get(sphere_pos).sort_ID_parent = parent_sort_ID_current_t;
				count++;
			}
			sphere_connection_pos.add(count-1);
		}
	}
	
	private boolean does_tracking_have_sorting_information_at_time(int tp) {
		boolean tracking_sorting_information = false;
		int total_spheres=0;
		int total_spheres_sorted = 0;
    	
		for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
    		total_spheres++;
    		if (ellipse.sort_ID!=-1)
    			total_spheres_sorted++;        		
    	}
        	
		if ((total_spheres_sorted/total_spheres)>0.5)
			tracking_sorting_information=true;
		return (tracking_sorting_information);
	}	
	
	public boolean does_tracking_have_sorting_information() {
		boolean tracking_sorting_information = false;
		int total_spheres=0;
		int total_spheres_sorted = 0;
    	
		for (int tp=0;tp<ImageControler.get_ellipsoidList_nTimePoints();tp++) {
			for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(tp)){
	    		total_spheres++;
	    		if (ellipse.sort_ID!=-1)
	    			total_spheres_sorted++;        		
	    	}		
		}

        if (total_spheres>0) {
    		if ((total_spheres_sorted/total_spheres)>0.5)
    			tracking_sorting_information=true;        	
        }

		return (tracking_sorting_information);
	}
		
}
