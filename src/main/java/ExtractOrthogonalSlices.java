import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

class ExtractOrthogonalSlices{
	//class to select orthogonal views of the stack
	ImagePlus[] Image;
	ImageStack[] sliceXY,sliceXZ, sliceYZ;
	ImagePlus[] ipsliceXY,ipsliceXZ, ipsliceYZ;	
	BufferedImage[] bsliceXY,bsliceXZ, bsliceYZ;
	ImageProcessor ipSliceXY;
	ColorModel cm;
	WritableRaster raster;
	public static double zoom=1.0;
	public static int roi_corner_posX=0, roi_corner_posY=0;
	int[] dim;
	public ExtractOrthogonalSlices(ImagePlus[] image_){
		Image = image_;
		dim = image_[0].getDimensions();
		sliceXY = new ImageStack[Image.length]; bsliceXY = new BufferedImage[Image.length];ipsliceXY = new ImagePlus[Image.length];
		sliceXZ = new ImageStack[Image.length]; bsliceXZ = new BufferedImage[Image.length];ipsliceXZ = new ImagePlus[Image.length];
		sliceYZ = new ImageStack[Image.length]; bsliceYZ = new BufferedImage[Image.length];ipsliceYZ = new ImagePlus[Image.length];
		
		
		for (int i=0;i<Image.length;i++){
			sliceXY[i] = ImageStack.create(dim[0], dim[1], 1, 32);
			ipsliceXY[i] = new ImagePlus(null,sliceXY[i]);
			sliceXZ[i] = ImageStack.create(dim[0], dim[3], 1, 32);
			ipsliceXZ[i] = new ImagePlus(null,sliceXZ[i]);
			sliceYZ[i] = ImageStack.create(dim[1], dim[3], 1, 32);
			ipsliceYZ[i] = new ImagePlus(null,sliceYZ[i]);
		}
	}
	public void update_zoom_info(double new_zoom, int posX, int posY) {
		//Updating current zoom and getting upper_left corner position to crop
		zoom = new_zoom;
		
		roi_corner_posX = posX - (int) (dim[0]/(2*zoom));
		roi_corner_posY = posY - (int) (dim[1]/(2*zoom));
		
		//Cheking boundary conditions x-axis
		if (roi_corner_posX<0) {
			//zoomed image out of boundary in x_axis
			roi_corner_posX = 0;
		}
		else if ((roi_corner_posX + (int) (dim[0]/zoom))>dim[0]) {
			//zoomed image out of boundary in x_axis
			roi_corner_posX = dim[0]-(int) (dim[0]/(zoom))-1;
		}
				
		//Cheking boundary conditions y-axis
		if (roi_corner_posY<0) {
			//zoomed image out of boundary in y_axis
			roi_corner_posY = 0;
		}
		else if ((roi_corner_posY + (int) (dim[1]/zoom))>dim[1]) {
			//zoomed image out of boundary in y_axis
			roi_corner_posY = dim[1]-(int) (dim[1]/(zoom))-1;
		}		
		
		if (zoom==1.0) {
				roi_corner_posX = 0;
				roi_corner_posY = 0;
		}
	}
	
	public BufferedImage[] getSliceXY(int index){		
		for (int channel=0;channel<Image.length;channel++){
			//getting complete full image
			bsliceXY[channel] = Image[channel].getStack().getProcessor(index).getBufferedImage();
			
			//creating zoom image
			if (zoom!=1){
				bsliceXY[channel] = bsliceXY[channel].getSubimage(roi_corner_posX, roi_corner_posY, (int) (dim[0]/zoom),  (int) (dim[1]/zoom));
				cm = bsliceXY[channel].getColorModel();
				raster = bsliceXY[channel].copyData(bsliceXY[channel].getRaster().createCompatibleWritableRaster());
				bsliceXY[channel] = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
			}		
		}
		return (bsliceXY);
	}
	
	public BufferedImage[] getSliceXZ(int index){
		int timePoint = (int) Math.floor((index-1)/dim[1]);
		index = index%dim[1];		
		for (int z=0; z<dim[3];z++)	{	
			for (int x=0; x<dim[0];x++){
				for (int channel=0;channel<Image.length;channel++){ 
					sliceXZ[channel].setVoxel(x, dim[3]-1-z, 0, Image[channel].getStack().getVoxel(x, index,timePoint*dim[3] + z));
				}
			}
		}	

		for (int channel=0;channel<Image.length;channel++){
			//getting complete full image
			ipsliceXZ[channel].setStack(sliceXZ[channel]);
			//bsliceXZ[channel] = ipsliceXZ[channel].getStack().getProcessor(1).getBufferedImage();
			bsliceXZ[channel] = sliceXZ[channel].getProcessor(1).getBufferedImage();
			//creating zoom image
			if (zoom!=1){
				bsliceXZ[channel] = bsliceXZ[channel].getSubimage(roi_corner_posX, 0, (int) (dim[0]/zoom),  dim[3]);
				cm = bsliceXZ[channel].getColorModel();
				raster = bsliceXZ[channel].copyData(bsliceXZ[channel].getRaster().createCompatibleWritableRaster());
				bsliceXZ[channel] = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
			}			
		}
		return (bsliceXZ);
	}
	
	public BufferedImage[] getSliceYZ(int index){
		int timePoint = (int) Math.floor((index-1)/dim[0]);
		index = index%dim[0];	
		for (int y=0; y<dim[1];y++){
			for (int z=0; z<dim[3];z++){
				for (int channel=0;channel<Image.length;channel++){
					//sliceYZ[channel].setVoxel(y, z, 0, Image[channel].getStack().getVoxel(index, y ,timePoint*dim[3] + z));
					sliceYZ[channel].setVoxel(y, dim[3]-1-z, 0, Image[channel].getStack().getVoxel(index, y ,timePoint*dim[3] + z));
				}
			}
		}
		
		for (int channel=0;channel<Image.length;channel++){
			ipsliceYZ[channel].setStack(sliceYZ[channel]);
			bsliceYZ[channel] = ipsliceYZ[channel].getStack().getProcessor(1).getBufferedImage();
			
			//creating zoom image
			if (zoom!=1){
				bsliceYZ[channel] = bsliceYZ[channel].getSubimage(roi_corner_posY, 0, (int) (dim[1]/zoom),  dim[3]);
				cm = bsliceYZ[channel].getColorModel();
				raster = bsliceYZ[channel].copyData(bsliceYZ[channel].getRaster().createCompatibleWritableRaster());
				bsliceYZ[channel] = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
			}				
		}	
		return (bsliceYZ);
	}		
}