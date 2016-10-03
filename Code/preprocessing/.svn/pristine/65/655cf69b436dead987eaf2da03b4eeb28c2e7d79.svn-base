package hope.it.works;

import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


// singleton class
public class TestBound {
	
	private static TestBound obj;
	
	public static TestBound getInstance() {
		if(obj == null) {
			obj = new TestBound();
		}
		return obj;
	}
	
	
	GridIndex gin;
	
	
	private TestBound() {
		// read neighborhood file
		readManhattanNeighborhoods("/home/ferdinand/Documents/NYU/Data/neighborhoods.txt");
	}
	
	public boolean isInside(double x, double y) {
		// assumes x = longitude, x = latitude
		return (gin.getRegion(x, y) != -1);
	}
	
	void readManhattanNeighborhoods(String nfile) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(nfile));
			int n = Integer.parseInt(buf.readLine());
			System.out.println("Reading " + n + " polygons from file");
			ArrayList<Path2D.Double> polygons = new ArrayList<>();
			for(int i = 0;i < n;i ++) {
				int np = Integer.parseInt(buf.readLine());
				for(int j = 0;j < np;j ++) {
					int ps = Integer.parseInt(buf.readLine());
					
	                Path2D polygon = new Path2D.Double();
					for(int k = 0;k < ps;k ++) {
						String [] line = Utilities.getLine(buf);
						double lat = Double.parseDouble(line[0]);
						double lon = Double.parseDouble(line[1]);
						if(k == 0) {
			                polygon.moveTo(lon, lat);
						} else {
							polygon.lineTo(lon, lat);
						}
					}
	                polygon.closePath();
	                polygons.add((Path2D.Double) polygon);
				}
			}
			buf.close();
			System.out.println("Building index");
			gin = new GridIndex(128,128);
			gin.buildGrid(polygons);
			System.out.println("Finished init");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void kickCells(double longMin, double longMax, double latMin, double latMax, double gridRes) throws FileNotFoundException, UnsupportedEncodingException {
		
		PrintWriter inBounds = new PrintWriter("/home/ferdinand/Documents/NYU/Data/harish_grid_h/inBounds_" + (int)(gridRes) + ".txt", "UTF-8");
		TestBound test = TestBound.getInstance();
		// Create grid cells and add center of grid cells as polygons
		try {
			
			double clat = (latMax + latMin) / 2;
			double clon = (longMax + longMin) / 2;
			
			double gres = Utilities.getGroundResolution(clat, clon);
			double [] worldmin = Utilities.geo2world(latMin, longMin);
			double [] worldmax = Utilities.geo2world(latMax, longMax);
			
			// in meters
			double xwid = Math.abs(worldmax[0] - worldmin[0]) * gres;
			double ywid = Math.abs(worldmax[1] - worldmin[1]) * gres;
			
			int xres = (int) Math.ceil(xwid / gridRes); 
			int yres = (int) Math.ceil(ywid / gridRes);
			
			System.out.println("grid resolution: " + xres + " " + yres);
			
			double latDiff = (latMax - latMin) / yres;
			double lonDiff = (longMax - longMin) / xres;
			for(int j = 0; j < yres; j ++) {
				// lat
				double y = latMin + (j + 0.5) * latDiff;
				double ymin = latMin + j* latDiff;
				double ymax = latMin + (j+1)* latDiff;
				
				for(int i = 0; i < xres; i ++) {
					// lon
					double x = longMin + (i + 0.5) * lonDiff;
					double xmin = longMin + i * lonDiff;
					double xmax = longMin + (i+1)* lonDiff;
					
					
					boolean inManhattan = test.isInside(xmin, ymin) && test.isInside(xmin, ymax) && test.isInside(xmax, ymax) && test.isInside(xmax, ymin);
					
					inBounds.println(x + "," + y + "," + inManhattan);
					System.out.println(x + "," + y + "," + inManhattan);
				}
			}
			inBounds.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String [] args) throws FileNotFoundException, UnsupportedEncodingException {
		kickCells(-74.0278, -73.9437, 40.7003, 40.7904, 150);
	}

}

