/**
 * 
 */
package com.ibm.in.irl.st.anomaly;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Anomaly;
import com.ibm.in.irl.st.anomaly.data.Cluster;
import com.ibm.in.irl.st.anomaly.data.GridData;
import com.ibm.in.irl.st.anomaly.gini.GiniCalculator;
import com.ibm.in.irl.st.anomaly.parser.InputFileParser;
import com.ibm.in.irl.st.anomaly.stat.TestStatistic;

/**
 * AnomalyDetector.java
 * <p/>
 * Purpose: This class serves as the main class for finding out the
 * spatio-temporal anomalies. The entire process follows these steps:
 * <ol>
 * <li>The input data is read from a file and is stored in a {@link GridData}.</li>
 * <li>The {@link GridData} is divided into a series of {@link Cluster} by
 * calculating the gini index of neighborhood of cells in the grid and expanding
 * the clusters till the gini index of the cluster is below some threshold.</li>
 * <li>Each cluster is padded with some neighborhood and the likelihood test
 * statistic is applied to each against the neighborhood.</li>
 * <li>Clusters which satisfy the statistic are considered anomaly.</li>
 * </ol>
 * </p>
 */
public class AnomalyDetector {
	/**
	 * main
	 * @param args
	 * @throws Exception Purpose: This is the main method of the anomaly
	 * detector.
	 */
	public static void callMe(int i, int r, int c, double gini, int run) throws Exception {
		Vector<Cluster> clusters = new Vector<Cluster>(100); // TODO: WHY JUST 100?? ##
		Vector<Anomaly> anomalies = new Vector<Anomaly>(100); // TODO: WHY JUST 100?? ##
		//Vector<Anomaly> globalAnomalies = new Vector<Anomaly>();
		TestStatistic stat = new TestStatistic();

		//BEGIN: Variables/paths need to be set depending on the data
		String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "Ocean" + File.separator + "indian-rand";
		//For cab data:
		//String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "Cab";
		//For NA data:
		//String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "misc";
		String inputFile = filePath + File.separator + "input.txt";
		//inputFile = filePath + File.separator + "grid-input.txt";
		InputFileParser parser = new InputFileParser(inputFile, 90, 180, 7500);
		
		// ##### // Significance of paramaters 90, 180, 7500?? --> set parameters to read the grid well
		
		//System.setOut(new PrintStream(filePath + File.separator + "anamoly.log"));
		//For NA map:		InputFileParser parser = new InputFileParser(inputFile,-54.5,-205,5);
		//Declare a grid Size. The width of the grid is important for 1-D to 2-D conversion
		GridData grid = new GridData(i, r, c); //#####// intervals, rows, cols
		//For NA map:		GridData grid = new GridData(115,46);
		GiniCalculator giniCalc = new GiniCalculator(gini);
		//END: Variables/paths need to be set depending on the data

		//Parse the input file and fill up the grid values.
		//For storage efficiency, the grid is stored in a HashMap.
		System.out.println("Fetching the grid values... " + new Date());

		//Parser has 3 methods to handle the input depending on the file type
		//For list of grid cell values along with lat-long
		parser.fillGridData(grid); //#####// FUNCTION TO BE ADAPTED TO FIT GRID DATA

		//For list of point values along with lat-long
		//parser.fillPointData(grid);

		//For creating a randomized grid
		//parser.fileRandomData(grid);

		//For loading a matrix from the file
		//parser.loadGrid(grid," ");

		//Clone of the grid to be used after we update the original grid.
		//This is done only temporarily and can be avoided later.
		HashMap<Integer, Double> gridClone = new HashMap<Integer, Double>();
		gridClone.putAll(grid.getDataMatrix());
		GridData gridDataClone = new GridData(grid.getIntervals(), grid.getRows(), grid.getCols());
		gridDataClone.setDataMatrix(gridClone);
		File dir = new File(filePath + File.separator + run);
		if (!dir.isDirectory())
		dir.mkdir();
		//Store the grid for evaluation purpose.
		grid.dumpData(filePath + File.separator + run + File.separator +  "GridData.txt");

		Date start = new Date();
		//Find the clusters in the grid using gini coefficients.
		//System.out.println("Finding the clusters in the grid... " + new Date());
		//giniCalc.getClustersByExpansion(grid, clusters);
		giniCalc.getClustersByBestFirst(grid, clusters);
		//String clusterFile = filePath + File.separator + "cluster-cells.txt";
		//giniCalc.fillClusters(clusters, clusterFile, grid);

		double size = 0;
		for (Cluster cl : clusters) {
			size += cl.getCells().size();
		}
		size = size / clusters.size();
		System.out.println("Grid size: " + gridDataClone.getDataMatrix().size());
		System.out.println("Average Cluster size: " + size + " " + clusters.size());
		//Finally, find the anomalies among the detected clusters.
		//System.out.println("Finding the anomalies... " + new Date());

		stat.findLocalAnomalies(clusters, gridDataClone, anomalies);

		Date end = new Date();

		System.out.println("Time taken: " + ((double) end.getTime() - start.getTime()) / 1000);
		//Store the clusters for evaluation purpose.
		Cluster.dumpClusters(clusters, grid.getRows(), grid.getCols(), filePath + File.separator + run + File.separator + "ClusterCells_"+gini+".txt");
		Cluster.dumpClusterValues(clusters, gridClone, filePath + File.separator + run + File.separator + "ClusterData_"+gini+".txt");
		Cluster.dumpClusterMap(clusters, grid, filePath + File.separator + run + File.separator + "ClusterMap_"+gini+".txt");

		//stat.findGlobalAnomalies(clusters, gridDataClone, globalAnomalies);
		Anomaly.dumpAnomalies(anomalies, grid.getRows(), grid.getCols(), filePath + File.separator + run + File.separator + "AnomalyCells_"+gini+".txt");
		Anomaly.dumpAnomalyValues(anomalies, gridClone, filePath + File.separator + run + File.separator + "AnomalyData_"+gini+".txt");
		Anomaly.dumpAnomalyMap(anomalies, grid, filePath + File.separator + run + File.separator + "AnomalyMap_"+gini+".txt");

		/*
		 * Anomaly.dumpAnomalies(globalAnomalies, grid.getCols(), filePath +
		 * File.separator + "GlobalAnomalyCells.txt");
		 * Anomaly.dumpAnomalyValues(globalAnomalies, gridClone, filePath +
		 * File.separator + "GlobalAnomalyData.txt");
		 * Anomaly.dumpAnomalyMap(globalAnomalies, grid, filePath +
		 * File.separator + "GlobalAnomalyMap.txt");
		 */
		//System.out.println("Done at " + new Date());
	}

	public static void main(String[] args) throws Exception {
		int intervals = 1;
		int rows = 1500;
		int cols = 3000;
		//double[] ginis = { 1E-4, 1E-3, 5E-3, 1E-2 };
		double[] ginis = { 1E-3};
		for (int run= 10; run < 11; run++)
		for (double gini : ginis) {
			System.out.println(run);
			System.err.println(gini);
			callMe(intervals, rows, cols, gini,run);
			System.out.println();
		}
	}
}