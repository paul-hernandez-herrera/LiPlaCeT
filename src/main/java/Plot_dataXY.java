import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Plot_dataXY {
	ArrayList<double[]> X; 
	ArrayList<double []> Y; 
	ArrayList<String> label;
	String outputName;
	Plot_dataXY(ArrayList< double[]> X, ArrayList<double []> Y, ArrayList<String> label, String outputName){
		this.X = X;
		this.Y = Y;
		this.label = label;
		this.outputName = outputName;
		plot_data();
	}
	
	private void plot_data()  {
		
		final XYSeriesCollection dataset = new XYSeriesCollection( );
		
		XYSeries [] data = new XYSeries [label.size()];
		for (int i=0;i<label.size();i++) {
			data[i] = new XYSeries( label.get(i) );
			for (int j=0; j<X.get(i).length;j++) {
				data[i].add(X.get(i)[j], Y.get(i)[j]);
			}
			dataset.addSeries( data[i]);
		}
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				"", 
				"x-axis",
				"y-axis", 
				dataset,
				PlotOrientation.VERTICAL, 
				true, true, false);		
		
	      int width = 640;   /* Width of the image */
	      int height = 480;  /* Height of the image */ 
	      File XYChart = new File("C:\\Users\\jalip\\Documents\\Proyectos\\Raiz\\Yamel\\wild_type\\Plots", outputName + ".png" ); 
	      try {
			ChartUtilities.saveChartAsJPEG( XYChart, xylineChart, width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
