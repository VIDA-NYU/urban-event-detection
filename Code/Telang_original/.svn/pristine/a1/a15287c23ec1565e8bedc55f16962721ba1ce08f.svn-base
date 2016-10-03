/**
 * <p>
 * AnomalyDetection : SATScanAnomalyDetector.java <br>
 * <br>
 * @author Salil Joshi (saljoshi@in.ibm.com) <br>
 * <br>
 * <b>Created on: </b> May 21, 2013
 * </p>
 * Revision History:
 */
package com.ibm.in.irl.st.anomaly;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Anomaly;
import com.ibm.in.irl.st.anomaly.data.GridData;
import com.ibm.in.irl.st.anomaly.parser.InputFileParser;
import com.ibm.in.irl.st.anomaly.sat.SATSampleGenerator;

/**
 * <p>
 * <b>Purpose:</b>
 * <p/>
 */
public class SATScanAnomalyDetector {
	public static void main(String args[]) throws Exception {
		//Vector<Cluster> clusters = new Vector<Cluster>(20000);
		Vector<Anomaly> anomalies = new Vector<Anomaly>(10000);
		//Vector<Anomaly> globalAnomalies = new Vector<Anomaly>();
	//	TestStatistic stat = new TestStatistic();
		SATSampleGenerator satGen = new SATSampleGenerator();
		//BEGIN: Variables/paths need to be set depending on the data
		String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "Ocean" + File.separator + "SAT";
		//For cab data:
		//String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "Cab";
		//For NA data:
		//String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "misc";
		String inputFile = filePath + File.separator + "input.txt";
		//inputFile = filePath + File.separator + "grid-input.txt";
		InputFileParser parser = new InputFileParser(inputFile, 90, 180, 7500);
		//System.setOut(new PrintStream(filePath + File.separator + "anamoly.log"));
		//For NA map:		InputFileParser parser = new InputFileParser(inputFile,-54.5,-205,5);
		//Declare a grid Size. The width of the grid is important for 1-D to 2-D conversion
		GridData grid = new GridData(1, 1500, 3000);
		parser.fillGridData(grid);

		//For list of point values along with lat-long
		//parser.fillPointData(grid);

		//For creating a randomized grid
		//parser.fileRandomData(grid);

		//For loading a matrix from the file
		//parser.loadGrid(grid, " ");
		System.out.println("Data Loaded at " + new Date());
		HashMap<Integer, Double> gridClone = new HashMap<Integer, Double>();
		gridClone.putAll(grid.getDataMatrix());
		GridData gridDataClone = new GridData(grid.getIntervals(), grid.getRows(), grid.getCols());
		gridDataClone.setDataMatrix(gridClone);
		//stat.findLocalSATAnomalies(clusters, grid, anomalies);
		satGen.generateCircles(grid, anomalies);
		//stat.findGlobalAnomalies(clusters, gridDataClone, globalAnomalies);

		Anomaly.dumpAnomalies(anomalies, grid.getRows(), grid.getCols(), filePath + File.separator + "LocalAnomalyCells.txt");
		Anomaly.dumpAnomalyValues(anomalies, gridClone, filePath + File.separator + "LocalAnomalyData.txt");
		Anomaly.dumpAnomalyMap(anomalies, grid, filePath + File.separator + "LocalAnomalyMap.txt");

		System.out.println("Done at " + new Date());

	}
}
