import java.io.File;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;


public class LiPlaCeT_GUI implements PlugIn{
	
	public static void main(String arg[]) {
		new LiPlaCeT_GUI().run("");
	}	

	ImagePlus image;
	public void run(String arg) {
		// TODO Auto-generated method stub
		
		image = WindowManager.getCurrentImage();
		if (image == null) {
			//I am debugging .... 
			//image = new ImagePlus(Paths.get("/home/paul/Projects/Yamel/2019/time lapse xal1 tiff/hyperstack_tp_0-2.tif").toString());
			
			//image = new ImagePlus(Paths.get("C:\\Users\\andre\\eclipse-workspace\\CellTrackingRaiz_\\Find_004_Pos025_S001-TP1_3-C2.tif").toString());
			
			//String file_path = Paths.get("C:\\Users\\jalip\\Documents\\eclipse-workspace\\LiPlaCeT\\DATA\\02\\PRL9_HS_drift corrected_Rot_Smooth_Cropped-TP_1_8.tif").toString();
			String file_path = Paths.get("C:\\Users\\jalip\\Documents\\eclipse-workspace\\LiPlaCeT\\DATA\\01\\Find_004_Pos025_S001.tif").toString();
			//String file_path = Paths.get("C:\\Users\\jalip\\Documents\\eclipse-workspace\\HottC_\\DATA\\03\\xal1_tp_0_21_8bits_Composite.tif").toString();
			//String file_path = Paths.get("C:\\Users\\jalip\\Documents\\Proyectos\\Raiz\\Yamel\\wild_type\\WT P1P1 time lapse hyperstack.tif").toString();
			File file = new File(file_path);
			if(file.exists() && file.isFile()) { 
				image = new ImagePlus(file_path);
				new CreateandDisplayGUI(image);	
			}else {
				JOptionPane.showMessageDialog(new JFrame(),
					    "LiPlaCeT plugin requires an hyperstack to run.");
			}			
		}else {
			//we have an image in the fiji windows
			//create a new GUI
			new CreateandDisplayGUI(image);
			
		}		 
	}
}