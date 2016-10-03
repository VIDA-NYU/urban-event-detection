/**
 * <p>
 * AnomalyDetection : ClusterAnomalyFinder.java <br>
 * <br>
 * @author Salil Joshi (saljoshi@in.ibm.com) <br>
 * <br>
 * <b>Created on: </b> Oct 8, 2013
 * </p>
 * Revision History:
 */
package com.ibm.in.irl.st.anomaly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Anomaly;
import com.ibm.in.irl.st.anomaly.data.Cluster;
import com.ibm.in.irl.st.anomaly.data.GridData;
import com.ibm.in.irl.st.anomaly.gini.GiniCalculator;
import com.ibm.in.irl.st.anomaly.parser.InputFileParser;
import com.ibm.in.irl.st.anomaly.stat.TestStatistic;

/**
 * <p>
 * <b>Purpose:</b> Treats the clusters as potential anomalies, and instead of
 * using LRT simply uses gini value of the cluster, and the size of the cluster
 * to rank the clusters as anomalies returns a sorted list of clusters based on
 * this criteria
 * <p/>
 */
public class ClusterAnomalyFinder {
	private final int topK = 500;
	private static final double THRESHOLD = 0.005;

	public void readClusters(String fileName, Vector<Cluster> clusters, int cols) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String line = "";
		String[] tokens = null;
		int rowNum = 0;
		int colNum = 0;
		int clusterID = 0;
		Cluster cluster = null;
		while ((line = reader.readLine()) != null) {
			tokens = line.split(" ");
			cluster = new Cluster(++clusterID);
			for (String token : tokens) {
				token = token.replaceAll("[^0-9]", " ").trim();
				rowNum = Integer.parseInt(token.split(" ")[1]);
				colNum = Integer.parseInt(token.split(" ")[2]);
				cluster.addCell(rowNum * cols + colNum);
			}
			clusters.add(cluster);
		}
		reader.close();
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param dataCells
	 * @param dataValues
	 * @param grid
	 */
	private void populateValues(HashSet<Integer> dataCells, Vector<Double> dataValues, GridData grid) {
		double dataValue = 0;
		HashMap<Integer, Double> dataMatrix = grid.getDataMatrix();
		dataValues.clear();
		for (int clusterCell : dataCells) {
			if (!dataMatrix.containsKey(clusterCell)) {
				continue;
			}
			dataValue = dataMatrix.get(clusterCell);
			if (dataValue == -1) {
				continue;
			}
			dataValues.add(dataValue);
		}
	}

	public void getGiniBasedAnomalies(Vector<Cluster> clusters, Vector<Anomaly> rankedAnomaly, GridData grid, String filePath) throws Exception {
		//Start Variable Declaration
		TreeMap<Double, Vector<Anomaly>> rankedMap = new TreeMap<Double, Vector<Anomaly>>(Collections.reverseOrder());
		TestStatistic t = new TestStatistic();
		double score = 0.0;
		double u = 0.0;
		GiniCalculator c = new GiniCalculator(THRESHOLD);
		Vector<Double> clusterValues = new Vector<Double>();
		Vector<Double> neighborValues = new Vector<Double>();
		HashSet<Integer> neighbors = new HashSet<Integer>();
		//End Variable Declaration
		for (Cluster cluster : clusters) {
			populateValues(cluster.getCells(), clusterValues, grid);
			t.findNeighbors(cluster, grid, neighbors, 3);
			populateValues(neighbors, neighborValues, grid);
			if (clusterValues.size() < 5) {
				continue;
			}
			clusterValues.addAll(neighborValues);
			u = getMeanValue(clusterValues);
			score = c.getGiniCoefficient(clusterValues, u);
			score = score + cluster.getCells().size() / 10000;

			if (!rankedMap.containsKey(score)) {
				rankedMap.put(score, new Vector<Anomaly>());
			}
			rankedMap.get(score).add(new Anomaly(cluster, neighbors, score));
		}

		for (double clusterScore : rankedMap.keySet()) {
			rankedAnomaly.addAll(rankedMap.get(clusterScore));
		}

		rankedAnomaly.setSize(topK);
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param clusterValues
	 * @return
	 */
	private double getMeanValue(Vector<Double> clusterValues) {
		//Start Variable Declaration
		double mean = 0.0;
		//End Variable Declaration
		if (clusterValues.size() == 0) {
			return mean;
		}
		for (double value : clusterValues) {
			mean += value;
		}
		mean = mean / clusterValues.size();
		return mean;
	}

	public static void main(String[] args) throws Exception {
		String filePath = "C:\\Work\\Project\\STD\\AnomalyDetection\\work\\experiment\\Ocean\\indian\\ClusterOnly";
		ClusterAnomalyFinder f = new ClusterAnomalyFinder();
		Vector<Cluster> clusters = new Vector<Cluster>();
		Vector<Anomaly> rankedAnomaly = new Vector<Anomaly>();
		//InputFileParser parser = new InputFileParser(filePath + "\\GridData.txt", 90, 180, 7500);
		InputFileParser parser = new InputFileParser(filePath + "\\input.txt", 90, 180, 7500);
		GridData grid = new GridData(1,1500, 3000);
		parser.fillGridData(grid);

		HashMap<Integer, Double> gridClone = new HashMap<Integer, Double>();
		gridClone.putAll(grid.getDataMatrix());
		GridData gridDataClone = new GridData(grid.getIntervals(), grid.getRows(), grid.getCols());
		gridDataClone.setDataMatrix(gridClone);

		f.readClusters(filePath + "\\ClusterCells.txt", clusters, grid.getCols());
		f.getGiniBasedAnomalies(clusters, rankedAnomaly, grid, filePath);

		Cluster.dumpClusters(clusters, grid.getRows(), grid.getCols(), filePath + File.separator + "ClusterCells_" + THRESHOLD + ".txt");
		Cluster.dumpClusterValues(clusters, gridClone, filePath + File.separator + "ClusterData_" + THRESHOLD + ".txt");
		Cluster.dumpClusterMap(clusters, grid, filePath + File.separator + "ClusterMap_" + THRESHOLD + ".txt");
		Anomaly.dumpAnomalies(rankedAnomaly, grid.getRows(), grid.getCols(), filePath + File.separator + "SortedAnomalyCells_" + THRESHOLD + ".txt");
		Anomaly.dumpAnomalyValues(rankedAnomaly, gridClone, filePath + File.separator + "SortedAnomalyData_" + THRESHOLD + ".txt");
	}
}
