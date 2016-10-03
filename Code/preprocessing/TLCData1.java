package hope.it.works;

import hope.it.works.MyIntList;
import hope.it.works.Utilities;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;

public class TLCData {
	
	
	class TaxiData {
	    long db_idx; // 8 bytes
	    double pick_x, pick_y; //8+8 bytes
	    double drop_x, drop_y; //8+8 bytes
	    long pickup_time; // 8 bytes
	    long dropoff_time; // 8 bytes
	    char []vendor = new char[4]; // 4 bytes
	    int duration; // 4 bytes
	    float miles; // 4 bytes
	    int fare; // 2 bytes
	    int surcharge; // 2 bytes
	    int mta_tax; // 2 bytes
	    int tip; // 2 bytes
	    int toll; // 2 bytes
	    int total; // 2 bytes
	    int  medallion_id; // 2 bytes
	    int license_id; // 2 bytes
	    char store_and_forward; // 1 byte
	    int payment_type; // 1 byte
	    int passengers; // 1 byte
	    int rate_code; // 1 byte
	};
	TaxiData taxi = new TaxiData();
	
	Calendar cal = Calendar.getInstance();
	long [] timeStamps;
	
	double radX = 0.0016;	
	double radY = 0.0008;
	double cutoff = 0.0004;
	double r2 = cutoff * cutoff;
	int np;
	
	GridIndex index;
	ArrayList<Path2D.Double> polygons = new ArrayList<>();
	ArrayList<Point2D.Double> nodes = new ArrayList<>();
	
	double [] pden, dden;
	// double [] pdur, ddur;
	// double [] pfare, dfare;
	// double [] pdist, ddist;
	
	private void addPolygon(int i, double x, double y) {
		Path2D polygon = new Path2D.Double();
		polygon.moveTo(x - radX, y - radY);
		polygon.lineTo(x + radX, y - radY);
		polygon.lineTo(x + radX, y + radY);
		polygon.lineTo(x - radX, y + radY);
		polygon.closePath();
		polygons.add((Path2D.Double) polygon);
		
		Point2D.Double pt = new Point2D.Double(x, y);
		nodes.add(pt);
	}
	
	public void setupGraph() {
		// TODO: instead of reading graph from file, create grid cells and add center of grid cells as polygons
		try {
			BufferedReader reader = new BufferedReader(new FileReader("cleanFilteredGraph.txt"));
			String [] s = Utilities.getLine(reader);
			int nv = Integer.parseInt(s[0]);
			
			for(int i = 0;i < nv;i ++) {
				s = Utilities.getLine(reader);
				double x = Double.parseDouble(s[1]);
				double y = Double.parseDouble(s[0]);
				addPolygon(i,x,y);
			}
			reader.close();
			
			index = new GridIndex(4096, 4096);
			index.buildGrid(polygons);
			
			np = nodes.size();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/*
	 * longMin, longMax, latMin, latMax indicate the spatial bounds of the grid
	 * gridRes indicates the spatial resolution of the grid in meters
	 */
	public void setupGrid(double longMin, double longMax, double latMin, double latMax, double gridRes, String dirPath) {
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
			int polygon_id = 0;
			
			double latDiff = (latMax - latMin) / yres;
			double lonDiff = (longMax - longMin) / xres;
			PrintStream pr = new PrintStream(dirPath + "gridCoordinates_" + ((int)gridRes) + ".txt");
			pr.println(xres + " " + yres);
			for(int j = 0; j < yres; j ++) {
				// lat
				double y = latMin + (j + 0.5) * latDiff;
				for(int i = 0; i < xres; i ++) {
					// lon
					double x = longMin + (i + 0.5) * lonDiff;
					// polygon_id is row major
					addPolygon(polygon_id,x,y);
					polygon_id ++;
					
					// location (i,j)
					pr.println(Utilities.roundDouble(x, 6) + " " + Utilities.roundDouble(y, 6));
				}
			}
			pr.close();
			index = new GridIndex(1024, 1024);
			index.buildGrid(polygons);
			
			np = nodes.size();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Polygons have been setup");
	}

	
	public void setupTime() {
		try {
			// For now computing hourly intervals for October 2011
			cal.set(2011, 9,1,0,0,0);
			System.out.println(cal.getTimeInMillis());
			ArrayList<Long> times = new ArrayList<>();
			while(cal.get(Calendar.YEAR) == 2011 && cal.get(Calendar.MONTH) == 9) {
				times.add(cal.getTimeInMillis()/1000);
				cal.add(Calendar.HOUR, 1);
			}
			times.add(cal.getTimeInMillis()/1000);
			System.out.println(cal.getTimeZone());
			timeStamps = new long[times.size()];
			for(int i = 0;i < timeStamps.length;i ++) {
				timeStamps[i] = times.get(i);
			}
			System.out.println(timeStamps.length + " " + timeStamps[0] + " " + timeStamps[timeStamps.length - 1]);
			int nt = timeStamps.length - 1;
			nt = nt * nodes.size();
			
			// pickup density
			pden = new double[nt];
			// drop-off density
			dden = new double[nt];
			
			// pdur = new double[nt];
			// ddur = new double[nt];
			
			// pdist = new double[nt];
			// ddist = new double[nt];
			
			// pfare = new double[nt];
			// dfare = new double[nt];
			System.out.println("Size of arrays: " + nt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readData(String file) {
		long i = 0;
		try {
			File f = new File(file); 
			long len = f.length();
			long noRecords = len / 88;
			System.out.println("no. of records = " + noRecords);
			byte [] b = new byte[88];
			BufferedInputStream ip = new BufferedInputStream(new FileInputStream(f));
			ByteBuffer buf = ByteBuffer.wrap(b);
			buf.order(ByteOrder.LITTLE_ENDIAN);
//			noRecords = 20000;
			long start = 0;
			for(i = 0;i < start;i ++) {
				int read = ip.read(b);
				if(read != 88) {
					System.err.println("Error while reading!!");
					System.exit(0);
				}
				if(i % 100000 == 0) {
					System.out.print("\r Skipping " + i + " records ...");
				}
			}
			int ct = 0;
			long prev = -1;
			boolean flag = false;
			double val =-1;
			for(i = start;i < noRecords;i ++) {
				if(i % 100000 == 0) {
					System.out.println(i + " of " + noRecords);
				}
				int read = ip.read(b);
				if(read != 88) {
					System.err.println("Error while reading!!");
					System.exit(0);
				}
				buf.rewind();
				parseData(buf);
				if(prev > taxi.pickup_time) {
					Utilities.er("not sorted!!!!");
				}
				prev = taxi.pickup_time;
			    if(!ignore()) {
			    	processData();
					if(flag && val > pden[0]) {
						Utilities.er("Problem here!!! " + ct);
					}
			    	ct ++;
//			    } else {
//			    	if(!flag && taxi.pickup_time > timeStamps[10] + 10000) {
//			    		flag = true;
//			    		val = pden[0];
//			    		System.err.println("----------------------- " + val + " " + ct);
//			    	}
			    }
			}
			ip.close();
			System.out.println("processed " + ct + " trips");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Completed till :" + i);
			System.out.println(i + " " + taxi.pick_x + " " + taxi.pick_y + " " + taxi.drop_x + " " + taxi.drop_y + " " + taxi.pickup_time + " " + taxi.dropoff_time + " " + new String(taxi.vendor));
		}
	}
	
	private void processData() {
		int ct = 0;
		// pickup
		{
			int tin = getTime(taxi.pickup_time, true);
			if(tin != -1) {
				ct++;
				double x = taxi.pick_x;
				double y = taxi.pick_y;
				MyIntList polys = index.getRegions(x, y);
				for (int i = 0; i < polys.length; i++) {
					int pin = polys.array[i];
					Point2D.Double pt = nodes.get(pin);
					double d2 = pt.distanceSq(x, y);
					double fac = Math.exp(-1 * (d2 / r2));
					
					int in = tin * np + pin;
					pden[in] += fac;
					// pdur[in] += fac * taxi.duration;
					// pfare[in] += fac * taxi.fare;
					// pdist[in] += fac * taxi.miles;
				}
			}
		}
		// dropoff
		{
			 int tin = getTime(taxi.dropoff_time, false);
			 if(tin != -1) {
			 	ct++;
			 	double x = taxi.drop_x;
			 	double y = taxi.drop_y;
			 	MyIntList polys = index.getRegions(x, y);
			 	for (int i = 0; i < polys.length; i++) {
			 		int pin = polys.array[i];
			 		Point2D.Double pt = nodes.get(pin);
			 		double d2 = pt.distanceSq(x, y);
			 		double fac = Math.exp(-1 * (d2 / r2));

			 		int in = tin * np + pin;
			 		dden[in] += fac;
//			 		ddur[in] += fac * taxi.duration;
//			 		dfare[in] += fac * taxi.fare;
//			 		ddist[in] += fac * taxi.miles;
			 	}
			 }
		}
		
		if(ct == 0) {
			Utilities.er("cannot be outside time period!!");
		}
	}

	int timeIn = -1;;
	private int getTime(long t, boolean use) {
		int st = 0;
		int en = timeStamps.length - 1;
		if(timeStamps[st] > t || timeStamps[en] <= t) {
			return -1;
		}
		
		while(st < en) {
			int m = (st + en) / 2;
			if(timeStamps[m] <= t && timeStamps[m + 1] > t) {
				return m;
			}
			if(timeStamps[m] < t) {
				st = m;
			} else {
				en =  m; 
			}
		}
//		st = 0;
//		if(timeIn != -1) {
//			st = timeIn; 
//		}
//		for(int i = st;i < en;i ++) {
//			if (timeStamps[i] <= t && t < timeStamps[i + 1]) {
//				if(use) {
//					timeIn = i;
//				}
//				return i;
//			}
//		}
		return -1;
	}

	private boolean ignore() {
	    if((taxi.pickup_time >= timeStamps[0] && taxi.pickup_time < timeStamps[timeStamps.length - 1]) || (taxi.dropoff_time >= timeStamps[0] && taxi.dropoff_time < timeStamps[timeStamps.length - 1])) {
			if(taxi.pick_x < -180 || taxi.pick_x > 180 || taxi.drop_x < -180 || taxi.drop_x > 180) {
				return true;
			}
			if(taxi.pick_y < -90 || taxi.pick_y > 90 || taxi.drop_y < -90 || taxi.drop_y > 90) {
				return true;
			}
			return false;
	    } else {
	    	return true;
	    }
	}

	void parseData(ByteBuffer buf) {
	    taxi.db_idx = buf.getLong(); // 8 bytes
	    taxi.pick_x = buf.getDouble();
	    taxi.pick_y = buf.getDouble(); //8+8 bytes
	    taxi.drop_x = buf.getDouble();
	    taxi.drop_y = buf.getDouble(); //8+8 bytes
	    taxi.pickup_time = buf.getLong(); // 8 bytes
	    taxi.dropoff_time = buf.getLong(); // 8 bytes
	    taxi.vendor[0] = (char) buf.get(); // 1 bytes
	    taxi.vendor[1] = (char) buf.get(); // 1 bytes
	    taxi.vendor[2] = (char) buf.get(); // 1 bytes
	    taxi.vendor[3] = (char) buf.get(); // 1 bytes
	    taxi.duration = buf.getInt(); // 4 bytes
	    taxi.miles = buf.getFloat(); // 4 bytes
	    taxi.fare = buf.getShort(); // 2 bytes
	    taxi.surcharge = buf.getShort();; // 2 bytes
	    taxi.mta_tax = buf.getShort();; // 2 bytes
	    taxi.tip = buf.getShort();; // 2 bytes
	    taxi.toll = buf.getShort();; // 2 bytes
	    taxi.total = buf.getShort();; // 2 bytes
	    taxi.medallion_id = buf.getShort();; // 2 bytes
	    taxi.license_id = buf.getShort();; // 2 bytes
	    taxi.store_and_forward = (char) buf.get(); // 1 bit
	    taxi.payment_type = buf.get(); // 2 bits
	    taxi.passengers = buf.get(); // 1 byte
	    taxi.rate_code = buf.get(); // 1 byte
	}

	public void writeFunctions(String dirPath, int gridRes) {
		try {
			new File(dirPath + "pick-density_" + ((int)gridRes)).mkdirs();
			new File(dirPath + "drop-density_" + ((int)gridRes)).mkdir();
			
			// new File("pick-dist").mkdir();
			// new File("drop-dist").mkdir();
			
			// new File("pick-fare").mkdir();
			// new File("drop-fare").mkdir();
			
			// new File("pick-duration").mkdir();
			// new File("drop-duration").mkdir();

			cal.set(2011, 9,1,0,0,0); // start datetime set to 2011_10_01_00
//			cal.set(2011,0,1,0,0,0);
			int ct = 0;
			for(int t = 0;t < timeStamps.length - 1;t ++) {
				int yy = cal.get(Calendar.YEAR);
				int mm = cal.get(Calendar.MONTH) + 1;
				int dd = cal.get(Calendar.DATE);
				int hh = cal.get(Calendar.HOUR_OF_DAY);
				String ts = "-" + yy + "-" + mm + "-" + dd + "-" + hh + ".txt";
				System.out.println("Writing " + ts);
				String pfol = "pick-";
				String dfol = "drop-";
				
				PrintStream pden = new PrintStream(dirPath + pfol + "density_" + ((int)gridRes)+ "/density" + ts);
				PrintStream dden = new PrintStream(dirPath + dfol + "density_" + ((int)gridRes)+ "/density" + ts);
				
				// PrintStream pdist = new PrintStream(pfol + "dist/dist" + ts);
				// PrintStream ddist = new PrintStream(dfol + "dist/dist" + ts);
				
				// PrintStream pfare = new PrintStream(pfol + "fare/fare" + ts);
				// PrintStream dfare = new PrintStream(dfol + "fare/fare" + ts);
				
				// PrintStream pdur = new PrintStream(pfol + "duration/duration" + ts);
				// PrintStream ddur = new PrintStream(dfol + "duration/duration" + ts);
				for(int i = 0;i < np;i ++) {
					pden.println(this.pden[ct]);
					dden.println(this.dden[ct]);
					
					// if(this.pden[ct] != 0) {
					// 	this.pdist[ct] /= this.pden[ct];
					// 	this.pfare[ct] /= this.pden[ct];
					// 	this.pdur[ct] /= this.pden[ct];
					// } else {
					// 	if(this.pdist[ct] != 0 || this.pdur[ct] != 0 || this.pfare[ct] != 0) {
					// 		Utilities.er("pick not possible: " + ct);
					// 	}
					// }
					// if(this.dden[ct] != 0) {
					// 	this.ddist[ct] /= this.dden[ct];
					// 	this.dfare[ct] /= this.dden[ct];
					// 	this.ddur[ct] /= this.dden[ct];
					// } else {
					// 	if(this.ddist[ct] != 0 || this.ddur[ct] != 0 || this.dfare[ct] != 0) {
					// 		Utilities.er("drop not possible: " + ct);
					// 	}
					// }
					// pdist.println(this.pdist[ct]);
					// ddist.println(this.ddist[ct]);
					
					// pdur.println(this.pdur[ct]);
					// ddur.println(this.ddur[ct]);
					
					// pfare.println(this.pfare[ct]);
					// dfare.println(this.dfare[ct]);
					
					ct ++;
				}
				
				pden.close();
				dden.close();
				// pdist.close();
				// ddist.close();
				// pdur.close();
				// ddur.close();
				// pfare.close();
				// dfare.close();
				cal.add(Calendar.HOUR, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		
		int gridRes = 100; // 100m grid resolution
		String dataDirPath = "/home/ferdinand/Documents/NYU/Data/harish_grid_h/";
		
		TLCData t = new TLCData();
//		t.readData("/local_scratch/harish/rawdata/data_new.bin");
		long st = System.currentTimeMillis();
//		t.setupGraph();
		t.setupGrid(-74.0278, -73.9437, 40.7003, 40.7904, gridRes, dataDirPath); // right bounds
		t.setupTime();
		t.readData("/home/ferdinand/Documents/NYU/Data/raw/unif_trips_88.bin");
//		t.readData("/home/harishd/Desktop/Projects/GPU-DB/data/unif_trips_88.bin");
		t.writeFunctions(dataDirPath, gridRes);
		long en = System.currentTimeMillis();
		System.out.println("Time Taken: " + (en - st));
	}
}
