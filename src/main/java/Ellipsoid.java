import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("serial")
class Ellipsoid implements Serializable{
	private Ellipsoid parent = null;
	private ArrayList<Ellipsoid> children = null;
	private double x,y,z,a,b,c;
	boolean selected;
	int tp;
	static int count=0;
	public int ID, cell_cycle=0, sort_ID=-1, sort_ID_parent=-1;
	
	Color color;
	Random rand = new Random();
	public Ellipsoid(int TP, double x_,double y_, double z_, double a_, double b_, double c_){
		x = x_;
		y = y_;
		z = z_;
		a = a_;
		b = b_;
		c = c_;
		tp = TP;
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		while ((r>0.9 & g>0.9 & g>0.9)|(r<0.1 & g<0.1 & g<0.1)|(r+b<g)) {
			//avoiding black, white and green colors
			r = rand.nextFloat();
			g = rand.nextFloat();
			b = rand.nextFloat();
		}
		color = new Color(r, g, b);
		this.children = new ArrayList<Ellipsoid> ();
		count++;
		ID= count;
	}
	
	public void setParent(Ellipsoid parent) {
		
		if (this.getParent() !=null) {
			//remove this ellipsoid from the list of children from parent
			this.getParent().getChildren().remove(this.getParent());
		}
		
		//set new parent
		this.parent = parent;	
		
		if (parent!=null) {
			//add to the list of child of new parent
			if (!parent.getChildren().contains(this))
				parent.addChild(this);	
			
			//setting color to all descendants same as parent
			parent.setColor(parent.color);			
		}
	
	}
	
	public Ellipsoid getParent(){
		return parent;
	}
	
	
	public void update_values( double posX, double posY, double posZ, double width1, double height1, double depth1) {
		x = posX;
		y = posY;
		z = posZ;
		a=  width1;
		b = height1;
		c = depth1;		
	}
	
	public void setTP(int TP){
		tp= TP;
	}
	
	public void clear_children_list() {
		this.children = new ArrayList<Ellipsoid> ();
	}
	public ArrayList<Ellipsoid> getChildren(){
		return children;
	}
	
	public int getTP(){
		return tp;
	}

	public void set_position(double x_,double y_, double z_){
		x = x_;
		y = y_;
		z = z_;		
	}
	public double getX(){
		return (x);
	}
	
	public double getY(){
		return (y);
	}
	
	public double getZ(){
		return (z);
	}
	
	
	public double getWidth(){
		return (a);
	}
	
	public double getHeight(){
		return (b);
	}		
	
	public double getDepth(){
		return (c);
	}
	
	public void setSelected(boolean sel){
		selected = sel;
	}
	
	public boolean getSelected(){
		return(selected);
	}	

	public void addChild(Ellipsoid child){
		//remove child from list of parent children
		child.parent.getChildren().remove(child);
		
		//set new parent
		child.parent = this;
		
		//add to the list of child of new parent
		if (!children.contains(child))
			children.add(child);		
		
		//set ellipse color to the same as parent
		child.setColor(this.color);		
	}
	
	
	public void setColor(Color col_){
		color = col_;
		
		//add color of parent to all descendants
		for (int i=0;i<children.size();i++){
			children.get(i).setColor(col_);
		}
	}
	
	public Color getColor(){
		return (selected==true?Color.GREEN:color);
	}
	
	
	public void setAxis(double a_, double b_, double c_){
		a = a_;
		b = b_;
		c = c_;
	}
	
	public boolean contains(double x1, double y1, double z1){	
		return (Math.pow((x-x1)/a, 2) + Math.pow((y-y1)/b, 2) + Math.pow((z-z1)/c, 2) <1.0);
	}
	
    public static Ellipsoid copy(Ellipsoid orig) {
    	Ellipsoid obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            obj = (Ellipsoid) in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }
	
	
}
