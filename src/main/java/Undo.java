import java.util.ArrayList;

public class Undo {
	//static variables to share the previous states of tracking
    public static ArrayList<ArrayList<ArrayList<Ellipsoid>>> undoList = new ArrayList<ArrayList<ArrayList<Ellipsoid>>>();
    public static ArrayList<Integer> undoList_tp =  new ArrayList<Integer>();
    
    
    public static void add_State_to_Undo_List(int t, int n_timePoints){
    	if (undoList.size()>10){
    		undoList.remove(0);
    		undoList_tp.remove(0);
    		
    	}
		undoList.add(duplicateCurrentState_for_TimePoints(t,n_timePoints));
		undoList_tp.add(t);
    }
    
    public static void undoAction(){
    	if (undoList.size()>0){
    		
    		int undo_tp = undoList_tp.get(undoList.size()-1);
    		int n_timePointsUndo = undoList.get(undoList.size()-1).size();
    		

    		for (int tp=undo_tp;tp<undo_tp+n_timePointsUndo;tp++) {
    			//we are updating two time points per undo action. Since undo can affect up to two time points. For example, when creating a mitotic event
    			
    			if (tp<ImageControler.ellipsoidList.size()) {
    				//setting data to previous state
    				ImageControler.ellipsoidList.set(tp,undoList.get(undoList.size()-1).get(tp-undo_tp)) ;
    				
    				//update parent and childs id of time t-1
    	    		if (tp-1>=0) {
    	    			for (Ellipsoid ellipsoid_parent:ImageControler.ellipsoidList.get(tp-1)) {
    	    				ellipsoid_parent.clear_children_list();
    	    				for (Ellipsoid ellipsoid_from_undo:ImageControler.ellipsoidList.get(tp)) {
    	    					if (ellipsoid_from_undo.getParent()!=null) {
	    	    					if (ellipsoid_parent.ID == ellipsoid_from_undo.getParent().ID)
	    	    						ellipsoid_from_undo.setParent(ellipsoid_parent);
    	    					}
    	    				}    				
    	    			}
    	    		}
    	
    	    		//update parent and childs id of time t-1
    	    		if (tp+1<ImageControler.ellipsoidList.size()) {
    	    			for (Ellipsoid ellipsoid_from_undo:ImageControler.ellipsoidList.get(tp)) {
    	    				ellipsoid_from_undo.clear_children_list();
    	    				for (Ellipsoid ellipsoid_from_childs:ImageControler.ellipsoidList.get(tp+1)) {
    	    					if (ellipsoid_from_childs.getParent()!=null) {
	    	    					if (ellipsoid_from_undo.ID == ellipsoid_from_childs.getParent().ID)
	    	    						ellipsoid_from_childs.setParent(ellipsoid_from_undo);
    	    					}
    	    				}    				
    	    			}
    	    		}
    			}
    		}
    		//removing undo from list
    		undoList.remove(undoList.size()-1);
    		undoList_tp.remove(undoList_tp.size()-1);
    		ImageControler.repaintPanels();        	
    	}   	
    }
    
	public static ArrayList<ArrayList<Ellipsoid>> duplicateCurrentState_for_TimePoints(int t, int n_timePoints){
		//just to unselect any sphere selected
		for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[0]){
			ellipse.setSelected(false);
		}
		for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[1]){
			ellipse.setSelected(false);
		}
		
		ArrayList<ArrayList<Ellipsoid>> tempList = new ArrayList<ArrayList<Ellipsoid>>();
		for (int tp=t;tp<t+n_timePoints;tp++){
			tempList.add( new ArrayList<Ellipsoid> ());
			if (tp<ImageControler.ellipsoidList.size()) {
				for (Ellipsoid ellipsoid_target:ImageControler.ellipsoidList.get(tp)){
					tempList.get(tp-t).add(Ellipsoid.copy(ellipsoid_target));
				}				
			}
		}
		
		//return to correct value selected spheres
		for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[0]){
			ellipse.setSelected(true);
		}
		for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[1]){
			ellipse.setSelected(true);
		}
		return tempList;
    }

}
