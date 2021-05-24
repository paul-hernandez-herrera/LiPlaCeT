import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;

class CreateandDisplayGUI implements  WindowListener, KeyListener{
	static Dimension screenSize = new Dimension();
	static FileInfo infoImage=null;
	JFrame frame_LiPlaCeT;
	
	private ImagePlus[] image_xy;
	
	private ImageControler panel_t_XY=null;
	PanelControlFeatures control = null;
	
    CreateandDisplayGUI(final ImagePlus image)  {
    	
        
    	frame_LiPlaCeT = new JFrame("Live Plant Cell Tracking (LiPlaCeT)");
        frame_LiPlaCeT.addWindowListener(this);
        frame_LiPlaCeT.setResizable(true);
        frame_LiPlaCeT.addKeyListener(this);
        frame_LiPlaCeT.setFocusable(true); // needed for KeyListener to work
        
        //screenSize =  get_ScreenResolution();
        screenSize =  Toolkit.getDefaultToolkit().getScreenSize();
        
        int[] image_size = image.getDimensions();
        Calibration cal = image.getCalibration(); 
        infoImage = image.getOriginalFileInfo(); 
        //System.out.println(image);
        //split channels
        if (image_size[2]==2)
        	//We have two channel image
        	image_xy = ChannelSplitter.split(image);
        else if (image_size[2]==1) {
        	//we have one channel image
        	image_xy = new ImagePlus[2];
        	image_xy[0] = image;
        	image_xy[1] = image;
        }       	
        
        image.hide();
                
        //Variable shared across the 4 panels
        ImageControler.timeLapseImage = image_xy;
        
        //Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        
        int height_panelXY = image_size[1];
        int height_panelXZ = (int)(image_size[3]*cal.pixelDepth/cal.pixelHeight);
        int width_panel = image_size[0];
        
        double displayPanelHeight = height_panelXZ + height_panelXY;
        double displayPanelWidth = 2*width_panel;
        System.out.println("Screen size:" + screenSize.width + " " + screenSize.height);
        double scale_ = 0.8*screenSize.width/displayPanelWidth;
        if (scale_>(0.8*screenSize.height/displayPanelHeight))
        	scale_ = (0.8*screenSize.height/displayPanelHeight);
        
        
        //Panel to display XZ images
        JPanel panelXZ = new JPanel(new GridLayout(1, 2,10,0));
        panelXZ.setPreferredSize(new Dimension((int)(scale_*displayPanelWidth), (int)(scale_*height_panelXZ)));
        //panelXZ.setMaximumSize(new Dimension((int)(scale_*displayPanelWidth), (int)(10000)));
        
        ImageControler panel_t_XZ = new ImageControler(0, new Point(1,0));
        ImageControler panel_t_next_XZ = new ImageControler(1, new Point(1,1));  
        panel_t_next_XZ.set_move_syncronized_slice(false);
        panelXZ.add(panel_t_XZ);        
        panelXZ.add(panel_t_next_XZ);         
        //adding panel XZ
        mainPanel.add(panelXZ);
        
        //Panel to display XY images
        JPanel panelImagesXY = new JPanel(new GridLayout(1, 2,10,0));
        panelImagesXY.setPreferredSize(new Dimension((int)(scale_*displayPanelWidth), (int)(scale_*height_panelXY)));
        //panelImagesXY.setMaximumSize(new Dimension((int)(scale_*displayPanelWidth), (int)(10000)));
        System.out.println((int)(scale_*displayPanelWidth) + " " + (int)(scale_*height_panelXY));
        panel_t_XY = new ImageControler(0, new Point(0,0));
		ImageControler panel_t_next_XY = new ImageControler(1, new Point(0,1));	
        
		panel_t_next_XY.set_move_syncronized_slice(false);
        
        panelImagesXY.add(panel_t_XY);        
        panelImagesXY.add(panel_t_next_XY);
        
        
        
        ImageControler[][] allDisplayPanels= new ImageControler [2][2];
        allDisplayPanels[0][0] = panel_t_XY;
        allDisplayPanels[0][1] = panel_t_next_XY;
        allDisplayPanels[1][0] = panel_t_XZ;
        allDisplayPanels[1][1] = panel_t_next_XZ;
        
        panel_t_XY.set_all_display_panels(allDisplayPanels);
        panel_t_next_XY.set_all_display_panels(allDisplayPanels);
        panel_t_XZ.set_all_display_panels(allDisplayPanels);
        panel_t_next_XZ.set_all_display_panels(allDisplayPanels);
        
        mainPanel.add(panelImagesXY);
        
        control = new PanelControlFeatures(allDisplayPanels);   
        control.setPreferredSize(new Dimension((int)(scale_*displayPanelWidth), (int)(100)));
        mainPanel.add(control);
        
        

        MenuBarGUI customMenuBar = new MenuBarGUI();
        customMenuBar.set_panelXY(panel_t_XY);

        frame_LiPlaCeT.setJMenuBar(customMenuBar.getmenuBar());
        frame_LiPlaCeT.add(mainPanel);       
        frame_LiPlaCeT.pack();
        frame_LiPlaCeT.setVisible(true);
        
        panelImagesXY.setPreferredSize(new Dimension((int)(scale_*displayPanelWidth), (int)(scale_*height_panelXY)));
        panelXZ.setPreferredSize(new Dimension((int)(scale_*displayPanelWidth), (int)(scale_*height_panelXZ)));
        control.setPreferredSize(new Dimension((int)(scale_*displayPanelWidth), (int)(100)));
        //System.out.println(panelImagesXY.getBounds());
        
        
        for (int col=0;col<=1;col++){
	        allDisplayPanels[0][col].distorsion_screen();
	        int ypos = (int)((image_xy[0].getHeight()/2)*allDisplayPanels[0][col].scaleScreenHeight);
	        int xpos = (int)((image_xy[0].getWidth()/2)*allDisplayPanels[0][col].scaleScreenWidth);
	        allDisplayPanels[0][col].lineMovingSlice_pos.y = ypos;
	        allDisplayPanels[0][col].lineMovingSlice_pos.x = xpos;
	        if (allDisplayPanels[1][col].panel_pos_Row==1)
	        	allDisplayPanels[1][col].set_currentSlice(image_xy[0].getWidth()/2);
	        if (allDisplayPanels[1][col].panel_pos_Row==2)
	        	allDisplayPanels[1][col].set_currentSlice(image_xy[0].getHeight()/2);
        }
        ImageControler.repaintPanels() ;    
        
        //System.out.println("Image Calibration: "+ ImageControler.voxel_size.pixelWidth + " " + ImageControler.voxel_size.pixelHeight + " " + ImageControler.voxel_size.pixelDepth + " micrometers");
    }
    
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}



	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		ImagePlus OriginalImage = RGBStackMerge.mergeChannels(image_xy, false);
		OriginalImage.setCalibration(image_xy[0].getCalibration());
		OriginalImage.setOpenAsHyperStack(true);
		OriginalImage.show();
	}



	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
		//System.out.println("Pressing KEY: " + e.getKeyChar());
		panel_t_XY.keyPressed(e);
		control.keyPressed(e);	
		frame_LiPlaCeT.requestFocus();
		
		//System.out.println("");
		//System.out.println("");		
	}



	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}	
}