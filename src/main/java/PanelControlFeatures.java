import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class PanelControlFeatures extends JPanel implements ActionListener,ChangeListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static boolean displayEllipse = true, displayLabels = true;
	public static boolean[] display_Channel = new boolean[2];
	
	public static Color[] channelColors = new Color[2];
	public static float opacityToDisplayEllipse=100;
	
	public static boolean flagCellDivision=false, flagMovingCells = false, flagChangeCellCycle=false, flagUpdatingZpos=false, flagCreateConnection=false;
			
	JButton next,previous,copy_data,play,updateZpos,buttonSelectDaughters, buttonConfirmDaughters,buttonCycleNumber,buttonMoveCells,buttonSelectConnection,buttonConfirmConnection;
	ArrayList<JButton> list_buttons_to_activate = new ArrayList<JButton>();
	BufferedImage outImage = null,tempImage = null;
	JCheckBox checkBoxdisplayChannel1, checkBoxdisplayChannel2, checkBoxdisplayEllipse,checkBoxdisplayLabels;
	JRadioButton displayXZ,displayYZ;
	JSlider ellipseOpacitySlider, timeSlider;
	int NChannels;
	int[][] shift_visualization;
	ImageControler[][] all_panels;
	boolean continue_timer = true;
	Timer myTimer;
	
	public PanelControlFeatures(ImageControler[][] panel_){		
		
		//CONSTRUCTORS
		all_panels = panel_;
		this.setLayout(new GridLayout(1,1,10,2));
		//GRIDBAGLAYOUT
		
		//Creating a radio button to select which projection to display on top (XZ or YZ)
		displayXZ = new JRadioButton("XZ", true);
		displayYZ = new JRadioButton("YZ");
		displayXZ.addActionListener(this);
		displayYZ.addActionListener(this);
		
		display_Channel[0] = true;
		display_Channel[1] = true;
		
        //Default colors for channels
		channelColors[0] = Color.green;
		channelColors[1] = Color.red;			
		
		
	    //Group the radio buttons.
	    ButtonGroup group = new ButtonGroup(); 
	    group.add(displayXZ);
	    group.add(displayYZ);
	      
        JPanel groupDisplay = new JPanel();
        groupDisplay.setBorder(BorderFactory.createTitledBorder("Ortho-view"));
        groupDisplay.add(displayXZ);
        groupDisplay.add(displayYZ);
        
        add(groupDisplay);

        String buttonText = "";

		
        //creating cell_division button
		//twoLines = "Cell\nDivision";  
		//cell_division = new JButton("<html>" + twoLines.replaceAll("\\n", "<br>") + "</html>");
        //add action Listener
        //cell_division.addActionListener(this);
        //add(cell_division);
        
        buttonText = "Select\nDaughters";
        buttonSelectDaughters = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        buttonSelectDaughters.addActionListener(this);
        
        buttonText = "Confirm\nDaughters";
        buttonConfirmDaughters = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        buttonConfirmDaughters.addActionListener(this);
        buttonConfirmDaughters.setEnabled(false);
        
        JPanel groupSetDivision = new JPanel();
        groupSetDivision.setLayout(new GridLayout(2, 1));     
        groupSetDivision.add(buttonSelectDaughters);
        groupSetDivision.add(buttonConfirmDaughters);
        add(groupSetDivision);       
        
        buttonText = "Cell\nCycle #";
        buttonCycleNumber = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        buttonCycleNumber.addActionListener(this);
        
        buttonText = "Move lineage\n position";
        buttonMoveCells = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        buttonMoveCells.addActionListener(this);
        JPanel group2 = new JPanel();
        group2.setLayout(new GridLayout(3, 1));     
        group2.add(buttonCycleNumber);
        group2.add(buttonMoveCells);
              
        
        //creating move spheres button
		//twoLines = "Move\nSpheres";  
		//move_cells = new JButton("<html>" + twoLines.replaceAll("\\n", "<br>") + "</html>");
        //add action Listener
		//move_cells.addActionListener(this);
        //add(move_cells);   

        //creating update Zposition button
        buttonText = "Correct sphere\n Z position";  
		updateZpos = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        //add action Listener
		updateZpos.addActionListener(this);
		group2.add(updateZpos);
        add(group2);   
        
        
        buttonText = "Select\nConnection";
        buttonSelectConnection = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        buttonSelectConnection.addActionListener(this);
        
        buttonText = "Confirm\n Connection";
        buttonConfirmConnection = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
        buttonConfirmConnection.setEnabled(false);
        buttonConfirmConnection.addActionListener(this);
        JPanel groupSetOrdering = new JPanel();
        groupSetOrdering.setLayout(new GridLayout(2, 1));  
        groupSetOrdering.setBorder(BorderFactory.createTitledBorder("Modify Ordering"));
        groupSetOrdering.add(buttonSelectConnection);
        groupSetOrdering.add(buttonConfirmConnection); 
        add(groupSetOrdering);         
        
		NChannels = all_panels[0][0].getNChannels();
		
		
		//creating JCheckBoxes for differents features
		checkBoxdisplayChannel1 = new JCheckBox("Ch 1");
		checkBoxdisplayChannel2 = new JCheckBox("Ch 2");
        checkBoxdisplayEllipse = new JCheckBox("Spheres");
        checkBoxdisplayLabels = new JCheckBox("Labels");
        checkBoxdisplayChannel1.setSelected(true);
        checkBoxdisplayChannel2.setSelected(true);
        checkBoxdisplayEllipse.setSelected(true);
        checkBoxdisplayLabels.setSelected(true);
        
        //Creating the listener for each option
        checkBoxdisplayChannel1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	display_Channel[0] = (e.getStateChange()==1?true:false);
            	transferFocusBackward();
            	ImageControler.repaintPanels();
            	
             }           
          });
        
        checkBoxdisplayChannel2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {     
            	display_Channel[1] = (e.getStateChange()==1?true:false);
            	transferFocusBackward();
            	ImageControler.repaintPanels();
            	
             }           
          });  
        
        checkBoxdisplayEllipse.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	displayEllipse = (e.getStateChange()==1?true:false);
            	transferFocusBackward();
            	ImageControler.repaintPanels();
             }           
          });
        
        checkBoxdisplayLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	displayLabels = (e.getStateChange()==1?true:false);
            	for (int i=0;i<2;i++){
            		for (int j=0;j<2;j++){
            			ImageControler.allDisplayPanels[i][j].updateLabel();	        			
            		}
            	}   
            	transferFocusBackward();
            	ImageControler.repaintPanels();
             }           
          });
        
        if (NChannels ==1){
        	//Disable visualization of channel 2 if there is only one channel
        	checkBoxdisplayChannel2.setVisible(false);        	
        }
        	
		//Creating group to display options
        JPanel groupDisplayOptions = new JPanel();
        groupDisplayOptions.setLayout(new GridLayout(2, 2));
        groupDisplayOptions.setBorder(BorderFactory.createTitledBorder("Display"));
        groupDisplayOptions.add(checkBoxdisplayChannel1);
        groupDisplayOptions.add(checkBoxdisplayChannel2);
        groupDisplayOptions.add(checkBoxdisplayEllipse);
        groupDisplayOptions.add(checkBoxdisplayLabels);
        add(groupDisplayOptions);

        
        JPanel groupSlider = new JPanel();
        groupSlider.setLayout(new GridLayout(1, 1));
        groupSlider.setBorder(BorderFactory.createTitledBorder("Sphere Opacity"));
        ellipseOpacitySlider = new JSlider(JSlider.HORIZONTAL,
                0, 100, 100);
        ellipseOpacitySlider.setMinorTickSpacing(4);     
        ellipseOpacitySlider.setMajorTickSpacing(20);
        ellipseOpacitySlider.setPaintTicks(true);
        ellipseOpacitySlider.setPaintLabels(true);
        ellipseOpacitySlider.addChangeListener(this);
        groupSlider.add(ellipseOpacitySlider);
        
        add(groupSlider);
      
        

		//creating next button
        next = new JButton("Next time");
        if (ImageControler.get_stack_nTimePoints()==2)
        	next.setEnabled(false);
        
        //add action Listener
        next.addActionListener(this);	
        
        //creating previous button
        previous = new JButton("Previous time");
        if (all_panels[0][0].get_currentTime()==0)
        	previous.setEnabled(false);        
        //add action Listener
        previous.addActionListener(this);        
        
        
        //creating previous button
        play = new JButton("Play");
        play.addActionListener(this); 
        
        JPanel groupTimeSlider = new JPanel();
        groupTimeSlider.setLayout(new GridLayout(2, 1));
        groupTimeSlider.setBorder(BorderFactory.createTitledBorder("Time slider"));
        timeSlider = new JSlider(JSlider.HORIZONTAL,
                0, ImageControler.get_stack_nTimePoints()-2, 0);
        timeSlider.setMinorTickSpacing(1);     
        timeSlider.setMajorTickSpacing(2);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.addChangeListener(this);
        groupTimeSlider.add(timeSlider);
        groupTimeSlider.add(play);
        add(groupTimeSlider);
        
        //Creating group to control time
        JPanel groupControlTime = new JPanel();
        groupControlTime.setLayout(new GridLayout(2, 1));
        groupControlTime.setBorder(BorderFactory.createTitledBorder("Control time"));        
        groupControlTime.add(previous);
        groupControlTime.add(next);
        add(groupControlTime);   
        
		//Creating buttons to copy ellipsoids and create a cell division
        buttonText = "Copy\nSpheres";
		copy_data = new JButton("<html>" + buttonText.replaceAll("\\n", "<br>") + "</html>");
		copy_data.addActionListener(this);
		add(copy_data);        
               
        shift_visualization = new int[3][ImageControler.get_stack_nTimePoints()];
        
        for (int i=0;i<2;i++){
        	for (int j=0;j<ImageControler.get_stack_nTimePoints();j++){
        		shift_visualization[i][j] = 0;
        	}        	
        }    
        
        list_buttons_to_activate.add(buttonSelectDaughters);
        list_buttons_to_activate.add(buttonCycleNumber);
        list_buttons_to_activate.add(buttonMoveCells);
        list_buttons_to_activate.add(updateZpos);
        list_buttons_to_activate.add(buttonSelectConnection);
        
	}

	public void move_time_next(){
		if (all_panels[0][1].get_currentTime()==(ImageControler.get_stack_nTimePoints()-1)){
			//we are displaying the last time return to time zero
			for (int i=0;i<2;i++){			
				all_panels[i][0].set_currentTime(0);
				all_panels[i][0].set_currentSlice(all_panels[i][1].get_slice());
			
				all_panels[i][1].set_currentTime(1);
				all_panels[i][1].set_currentSlice(all_panels[i][0].get_slice()+shift_visualization[i][all_panels[i][0].get_currentTime()]);				
			}
			previous.setEnabled(false);
			next.setEnabled(true);
			timeSlider.setValue(0);
		}else {
			//we just increase one time
			for (int i=0;i<2;i++){
				shift_visualization[i][all_panels[i][0].get_currentTime()] = all_panels[i][1].get_slice() - all_panels[i][0].get_slice();
			
				all_panels[i][0].set_currentTime(all_panels[i][0].get_currentTime() +1);
				all_panels[i][0].set_currentSlice(all_panels[i][1].get_slice());
			
				all_panels[i][1].set_currentTime(all_panels[i][1].get_currentTime() +1);
				all_panels[i][1].set_currentSlice(all_panels[i][0].get_slice()+shift_visualization[i][all_panels[i][0].get_currentTime()]);
			}
			if (all_panels[0][1].get_currentTime()==(ImageControler.get_stack_nTimePoints()-1))
				next.setEnabled(false);		
			previous.setEnabled(true);				
			timeSlider.setValue(timeSlider.getValue()+1);
		}
		

	}
	
	public void move_time_previous(){
		for (int i=0;i<2;i++){
			all_panels[i][1].set_currentTime(all_panels[i][1].get_currentTime() -1);
			all_panels[i][1].set_currentSlice(all_panels[i][0].get_slice());		
			
			all_panels[i][0].set_currentTime(all_panels[i][1].get_currentTime() -1);
			all_panels[i][0].set_currentSlice(all_panels[i][1].get_slice()-shift_visualization[i][all_panels[i][0].get_currentTime()]);
		}
		
		if (all_panels[0][0].get_currentTime()==0)
			previous.setEnabled(false);
		next.setEnabled(true);
		timeSlider.setValue(timeSlider.getValue()-1);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource()==next){
			//if (ImageControler.ellipsoidList.get(all_panels[0][1].get_currentTime()).size()==0)
			//	function_copy_data();
			//else
			move_time_next();	

		}
		
		if (e.getSource()==previous){
			move_time_previous();		
		}
	
		
		if (e.getSource()==play){
			
		   ActionListener taskPerformer = new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					move_time_next();
				}
			    };	
				    
			if (continue_timer){
				play.setText("Stop");
				myTimer = new Timer((int)(1000.0/MenuBarGUI.FPS),taskPerformer);
				myTimer.start();
			}
			else{
				play.setText("Play");
				myTimer.stop();
			}
				
			
			if (continue_timer)
				continue_timer=false;
			else
				continue_timer = true;				
		}

		
		if (e.getSource()==copy_data){
			function_copy_data();
		}
		
		if (e.getSource()==buttonSelectDaughters & check_buttons_are_not_selected(e)){
			flagCellDivision = (flagCellDivision==true?false:true);

			if (flagCellDivision){
				buttonSelectDaughters.setBackground(Color.GREEN);
				buttonConfirmDaughters.setEnabled(true);
				
			}else {
				clear_selected_ellipsoids();
				buttonSelectDaughters.setBackground(null);
				buttonConfirmDaughters.setEnabled(false);
			}
		}
		if (e.getSource()==buttonConfirmDaughters) {
			//cell division has to be created
			
			System.out.println(ImageControler.ellipsoid_selected_Panel[0].size());
			if (ImageControler.ellipsoid_selected_Panel[0].size()>0){
				//we have created a new division			
				Undo.add_State_to_Undo_List(all_panels[0][0].get_currentTime(),2);	
				
				if (ImageControler.ellipsoid_selected_Panel[0].size()<=1) {
					for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[1]){
						ellipse.setParent(ImageControler.ellipsoid_selected_Panel[0].get(0));
						if (ImageControler.ellipsoid_selected_Panel[1].size()>1)
							ellipse.cell_cycle=ImageControler.ellipsoid_selected_Panel[0].get(0).cell_cycle+1;
						else
							ellipse.cell_cycle=ImageControler.ellipsoid_selected_Panel[0].get(0).cell_cycle;
					}
				}else {
					Ellipsoid parent_ellipsoid_TEMP = new Ellipsoid(0,0,0,0,0,0,0);
					parent_ellipsoid_TEMP.ID = -parent_ellipsoid_TEMP.ID;
					Color new_ellipseColor = null;
					if (ImageControler.ellipsoid_selected_Panel.length>0) {
						new_ellipseColor = ImageControler.ellipsoid_selected_Panel[0].get(0).color;						
					}
					parent_ellipsoid_TEMP.setColor(new_ellipseColor);
					for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[0]){
						ellipse.setParent(parent_ellipsoid_TEMP);
						ellipse.cell_cycle+=1;
					}						
				}								
			}		
			
			clear_selected_ellipsoids();
			buttonConfirmDaughters.setEnabled(false);
			buttonSelectDaughters.setBackground(null);		
			flagCellDivision = false;
			
		}
		if (e.getSource()==buttonMoveCells & check_buttons_are_not_selected(e)){
			flagMovingCells = (flagMovingCells==true?false:true);

			if (flagMovingCells){
				buttonMoveCells.setBackground(Color.GREEN);
			}
			else{
				clear_selected_ellipsoids();				
				buttonMoveCells.setBackground(null);
				
			}
		}
		if (e.getSource()==buttonCycleNumber & check_buttons_are_not_selected(e)){
			flagChangeCellCycle = (flagChangeCellCycle==true?false:true);

			if (flagChangeCellCycle){
				buttonCycleNumber.setBackground(Color.GREEN);
			}else {
				clear_selected_ellipsoids();				
				buttonCycleNumber.setBackground(null);
			}
		}
		if (e.getSource()==updateZpos & check_buttons_are_not_selected(e)){
			flagUpdatingZpos = (flagUpdatingZpos==true?false:true);

			if (flagUpdatingZpos){
				updateZpos.setBackground(Color.GREEN);
			}
			else{
				clear_selected_ellipsoids();
				updateZpos.setBackground(null);
			}
		}		
		if (e.getSource() == buttonSelectConnection & check_buttons_are_not_selected(e)) {
			flagCreateConnection = (flagCreateConnection==true?false:true);

			if (flagCreateConnection){
				buttonSelectConnection.setBackground(Color.GREEN);
				buttonConfirmConnection.setEnabled(true);
				
			}else {
				clear_selected_ellipsoids();
				buttonSelectConnection.setBackground(null);
				buttonConfirmConnection.setEnabled(false);
			}			
			
		}
		if (e.getSource()==buttonConfirmConnection) {
			//a new connection has been set
			
			if (ImageControler.ellipsoid_selected_Panel[1].size()==2){
				//we have created a new connection	
				Ellipsoid ellipsoid1 = ImageControler.ellipsoid_selected_Panel[1].get(0);
				Ellipsoid ellipsoid2 = ImageControler.ellipsoid_selected_Panel[1].get(1);
				if (ellipsoid2.sort_ID < ellipsoid1.sort_ID & ellipsoid2.sort_ID>=0) {
					ellipsoid1.sort_ID_parent = ellipsoid2.sort_ID;
				}					
				else {
					int ellipsoid2_sort_ID_OLD = ellipsoid2.sort_ID;
					int ellipsoid2_sort_ID_NEW = ellipsoid1.sort_ID;
					int t = all_panels[0][1].get_currentTime();
					if (ellipsoid1.sort_ID+1 == ellipsoid2.sort_ID) {
						//just invert IDS
						ellipsoid1.sort_ID = ellipsoid2.sort_ID;
						ellipsoid2.sort_ID_parent = ellipsoid1.sort_ID_parent;
						ellipsoid1.sort_ID_parent = ellipsoid1.sort_ID-1;
						ellipsoid2.sort_ID = ellipsoid1.sort_ID-1;				
					}else {
						//update ids and all the ids above current id
						for (Ellipsoid ellipse:ImageControler.ellipsoidList.get(t)) {
							if (ellipse.sort_ID>=ellipsoid2_sort_ID_NEW) {
								ellipse.sort_ID+=1;
							}
							if (ellipse.sort_ID_parent==ellipsoid2_sort_ID_OLD && ellipsoid2_sort_ID_OLD>=0) {
								ellipse.sort_ID_parent= ellipsoid2_sort_ID_NEW;
							}else if(ellipse.sort_ID_parent>=ellipsoid2_sort_ID_NEW ) {
								ellipse.sort_ID_parent+=1;
							}
						}
						ellipsoid2.sort_ID = ellipsoid2_sort_ID_NEW;
						ellipsoid1.sort_ID_parent = ellipsoid2.sort_ID;
					}
				}
			}else if (ImageControler.ellipsoid_selected_Panel[0].size()==1) {
				//selected sphere correspond to the tip and has not connection
				Ellipsoid ellipsoid_tip = ImageControler.ellipsoid_selected_Panel[0].get(0);
				
				ellipsoid_tip.sort_ID_parent = -1;
			}
			
			clear_selected_ellipsoids();
			buttonSelectConnection.setBackground(null);
			buttonConfirmConnection.setEnabled(false);
			flagCreateConnection = false;			
		}
		
		if (e.getSource() == displayXZ){
			all_panels[1][0].updatePanel(new Point(1,0),all_panels[1][0].get_currentTime());
			all_panels[1][0].set_currentSlice((int)(all_panels[0][0].lineMovingSlice_pos.y/all_panels[0][0].scaleScreenHeight));
			
			
			all_panels[1][1].updatePanel(new Point(1,1),all_panels[1][1].get_currentTime());
			all_panels[1][1].set_currentSlice((int)(all_panels[0][1].lineMovingSlice_pos.y/all_panels[0][1].scaleScreenHeight));
			all_panels[1][0].set_labelOrthogonalView("XZ");
			all_panels[1][1].set_labelOrthogonalView("XZ");
	        ImageControler.repaintPanels() ;
		}
		if (e.getSource() == displayYZ){
			all_panels[1][0].updatePanel(new Point(2,0),all_panels[1][0].get_currentTime());
			all_panels[1][0].set_currentSlice((int)(all_panels[0][0].lineMovingSlice_pos.x/all_panels[0][0].scaleScreenWidth));
			
			all_panels[1][1].updatePanel(new Point(2,1),all_panels[1][1].get_currentTime());
			all_panels[1][1].set_currentSlice((int)(all_panels[0][1].lineMovingSlice_pos.x/all_panels[0][1].scaleScreenWidth));
			
			all_panels[1][0].set_labelOrthogonalView("YZ");
			all_panels[1][1].set_labelOrthogonalView("YZ");
			ImageControler.repaintPanels();
		}
		
		this.transferFocusBackward();
	}
	
	public boolean check_buttons_are_not_selected(ActionEvent e) {
		boolean buttons_are_not_selected = true;
		for (JButton control_button:list_buttons_to_activate) {
			if (control_button!=e.getSource() & control_button.getBackground()==Color.GREEN) {
				buttons_are_not_selected = false;
			}
		}
		return (buttons_are_not_selected);
	}
	
	public void clear_selected_ellipsoids() {
		for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[0]){
			ellipse.setSelected(false);
		}
		for (Ellipsoid ellipse:ImageControler.ellipsoid_selected_Panel[1]){
			ellipse.setSelected(false);
		}	
		ImageControler.ellipsoid_selected_Panel[0].clear();
		ImageControler.ellipsoid_selected_Panel[1].clear();	
		ImageControler.repaintPanels();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
        JSlider source = (JSlider)e.getSource();
        if (source == timeSlider){
        	int timeSliderValue = (int)source.getValue();
        	if (timeSliderValue>all_panels[0][0].get_currentTime()){
        		for (int i=0;i<timeSliderValue-all_panels[0][0].get_currentTime();i++)
        			move_time_next();
        			
        	}else if (timeSliderValue<all_panels[0][0].get_currentTime()){
        		for (int i=0;i<all_panels[0][0].get_currentTime()-timeSliderValue;i++)
        			move_time_previous();
        	} 	
        }
        else if (source == ellipseOpacitySlider){
        	opacityToDisplayEllipse =(float)source.getValue();
        	ImageControler.repaintPanels();
        }
		
        //all_panels[0][0].requestFocus();
        this.transferFocusBackward();
	}	
	
	public void function_copy_data() {
		
		
		//time to copy the data
		int next_t = all_panels[0][1].get_currentTime();
		
		//just to know how many timepoints to add to the undoQueue
		int add_n_time_points_toQueue=0;
		for (int tp = next_t;tp<ImageControler.ellipsoidList.size();tp++) {
			if (ImageControler.ellipsoidList.get(tp).size()>0) {
				add_n_time_points_toQueue+=1;
			}else {
				break;
			}
		}
		Undo.add_State_to_Undo_List(next_t,add_n_time_points_toQueue);	
		
		//Copy array list -- getting the xy projection for copying the data
		ArrayList<Ellipsoid> source_List = ImageControler.ellipsoidList.get(next_t-1);

		//clear ellipsoids from next time point to end
		for (int tp = next_t;tp<ImageControler.ellipsoidList.size();tp++)
			ImageControler.ellipsoidList.get(tp).clear();

		
		for (Ellipsoid ellipsoid:source_List){
			all_panels[0][1].add_ellipsoid(next_t, ellipsoid.getX(), ellipsoid.getY(), ellipsoid.getZ(), ellipsoid.getHeight(), ellipsoid.getWidth(), ellipsoid.getDepth());
			ImageControler.ellipsoidList.get(next_t).get(ImageControler.ellipsoidList.get(next_t).size()-1).setParent(ellipsoid);;
			ImageControler.ellipsoidList.get(next_t).get(ImageControler.ellipsoidList.get(next_t).size()-1).cell_cycle =ellipsoid.cell_cycle;
		}
		
    	ImageControler.repaintPanels();
    	        		
	}
	
	public void keyPressed(KeyEvent arg0) {
		//getting the key value
		int key = arg0.getKeyCode();
		
		if (key == KeyEvent.VK_N){
			if (next.isEnabled())
				move_time_next();
			
		}else if (key == KeyEvent.VK_P){
			if (previous.isEnabled())
				move_time_previous();			
		}else if (key == KeyEvent.VK_C){
			function_copy_data();
		}
		
	}

}