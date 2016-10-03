/**
 * <p>
 * AnomalyDetection : HAC.java <br>
 * <br>
 * @author Salil Joshi (saljoshi@in.ibm.com) <br>
 * <br>
 * <b>Created on: </b> Sep 24, 2013
 * </p>
 * Revision History:
 */
package com.ibm.in.irl.st.anomaly;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Anomaly;
import com.ibm.in.irl.st.anomaly.data.Cluster;
import com.ibm.in.irl.st.anomaly.data.GridData;
import com.ibm.in.irl.st.anomaly.parser.InputFileParser;
import com.ibm.in.irl.st.anomaly.stat.TestStatistic;

/**
 * <p>
 * <b>Purpose:</b> Implements hierarchical agglomerative clustering to cluster
 * the given grid data. Ranks the clusters based on the cluster score against
 * the neighbors and size. Returns the ranked clusters based on this criterion.
 * <p/>
 */
public class HAC {
	private static final double THRESHOLD = 1.0;
	private static final int TOP = 500;

	public static void main(String[] args) throws Exception {
		String filePath = "C:" + File.separator + "Work" + File.separator + "Project" + File.separator + "STD" + File.separator + "AnomalyDetection" + File.separator + "work" + File.separator + "experiment" + File.separator + "Ocean\\indian" + File.separator + "HAC";
		String inputFile = filePath + File.separator + "input.txt";
		String outputFile = filePath + File.separator + "output_" + THRESHOLD + ".txt";
		System.setOut(new PrintStream(outputFile));
		InputFileParser parser = new InputFileParser(inputFile, 90, 180, 7500);
		GridData grid = new GridData(1, 1500, 3000);
		HashMap<Integer, Cluster> cellMap = new HashMap<Integer, Cluster>();
		Vector<Cluster> clusters = new Vector<Cluster>();
		Vector<Anomaly> rankedAnomaly = new Vector<Anomaly>();

		HAC hac = new HAC();
		parser.fillGridData(grid);
		System.out.println("Loaded data");
		grid.dumpData(filePath + File.separator + "GridData.txt");

		HashMap<Integer, Double> gridClone = new HashMap<Integer, Double>();
		gridClone.putAll(grid.getDataMatrix());
		GridData gridDataClone = new GridData(grid.getIntervals(), grid.getRows(), grid.getCols());
		gridDataClone.setDataMatrix(gridClone);

		hac.initClustersAndCellMap(grid, clusters, cellMap);
		System.out.println("Initialized clusters");
		hac.doClustering(grid, clusters, cellMap);

		hac.getRankedClusters(clusters, rankedAnomaly, grid, TOP);
		Cluster.dumpClusters(clusters, grid.getRows(), grid.getCols(), filePath + File.separator + "ClusterCells_" + THRESHOLD + ".txt");
		Cluster.dumpClusterValues(clusters, gridClone, filePath + File.separator + "ClusterData_" + THRESHOLD + ".txt");
		Cluster.dumpClusterMap(clusters, grid, filePath + File.separator + "ClusterMap_" + THRESHOLD + ".txt");
		Anomaly.dumpAnomalies(rankedAnomaly, grid.getRows(), grid.getCols(), filePath + File.separator + "SortedAnomalyCells_" + THRESHOLD + ".txt");
		Anomaly.dumpAnomalyValues(rankedAnomaly, gridClone, filePath + File.separator + "SortedAnomalyData_" + THRESHOLD + ".txt");
		//Cluster.dumpClusterMap(rankedClusters, grid, filePath + File.separator + "ClusterMap_"+THRESHOLD+".txt");
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param clusters
	 * @param rankedAnomaly
	 * @param topK
	 */
	private void getRankedClusters(Vector<Cluster> clusters, Vector<Anomaly> rankedAnomaly, GridData grid, int topK) {
		//Start Variable Declaration
		TreeMap<Double, Vector<Anomaly>> rankedMap = new TreeMap<Double, Vector<Anomaly>>(Collections.reverseOrder());
		double score = 0.0;
		Vector<Double> clusterValues = new Vector<Double>();
		Vector<Double> neighborValues = new Vector<Double>();
		TestStatistic t = new TestStatistic();
		HashSet<Integer> neighbors = new HashSet<Integer>();
		//End Variable Declaration

		for (Cluster cluster : clusters) {
			if (cluster.getCells().size() < 5) {
				continue;
			}
			populateValues(cluster.getCells(), clusterValues, grid);
			t.findNeighbors(cluster, grid, neighbors, 3);
			populateValues(neighbors, neighborValues, grid);
			if (neighbors.size() < 5) {
				continue;
			}
			//Both the scores are more or less in the same range, so ignoring the normalization
			score = getLinkScore(clusterValues, neighborValues);
			score = score + cluster.getCells().size();

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
	 * @param grid
	 * @param clusterMap
	 * @param cellMap
	 * @param clusters
	 * @throws Exception
	 */
	private void doClustering(GridData grid, Vector<Cluster> clusters, HashMap<Integer, Cluster> cellMap) throws Exception {
		//Start Variable Declaration
		TreeMap<Double, HashMap<Cluster, Cluster>> scoreMap = new TreeMap<Double, HashMap<Cluster, Cluster>>();
		int numClusters = clusters.size();
		//End Variable Declaration

		getNeighborScores(grid, clusters, cellMap, scoreMap);

		while (clusters.size() > 1) {
			if (scoreMap.size() == 0) {
				break;
			}
			HashMap<Cluster, Cluster> firstMap = scoreMap.firstEntry().getValue();
			double score = scoreMap.firstEntry().getKey();

			if (score > THRESHOLD) {
				break;
			}
			if (firstMap.size() == 0) {
				scoreMap.remove(score);
				continue;
			}
			Cluster firstCluster = firstMap.keySet().iterator().next();
			if (firstMap.get(firstCluster) == null) {
				firstMap.remove(firstCluster);
				continue;
			}

			if (!clusters.contains(firstCluster)) {
				firstMap.remove(firstCluster);
				continue;
			}

			Cluster firstNeighbor = firstMap.get(firstCluster);
			firstMap.remove(firstCluster);

			if (!clusters.contains(firstNeighbor)) {
				continue;
			}

			numClusters++;
			//System.out.println(firstCluster + " " + firstNeighbor + " " + score + " " + clusters.size());
			mergeClusters(firstCluster, firstNeighbor, cellMap, clusters, grid, scoreMap, numClusters);
		}
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param firstCluster
	 * @param firstNeighbor
	 * @param cellMap
	 * @param clusters
	 * @param grid
	 * @param scoreMap
	 * @param newClusterID
	 */
	private void mergeClusters(Cluster firstCluster, Cluster firstNeighbor, HashMap<Integer, Cluster> cellMap, Vector<Cluster> clusters, GridData grid, TreeMap<Double, HashMap<Cluster, Cluster>> scoreMap, int newClusterID) {
		//Start Variable Declaration
		Cluster newCluster = new Cluster(newClusterID);
		//End Variable Declaration
		newCluster.getCells().addAll(firstCluster.getCells());
		newCluster.getCells().addAll(firstNeighbor.getCells());
		for (int cell : newCluster.getCells()) {
			cellMap.put(cell, newCluster);
		}
		clusters.remove(firstCluster);
		clusters.remove(firstNeighbor);
		clusters.add(newCluster);

		HashSet<Cluster> neighbors = getNeighborClusters(newCluster, grid, cellMap);
		if (neighbors == null) {
			return;
		}
		for (Cluster neighbor : neighbors) {
			double score = getLinkScore(newCluster, neighbor, grid);
			if (!scoreMap.containsKey(score)) {
				scoreMap.put(score, new HashMap<Cluster, Cluster>());
			}
			scoreMap.get(score).put(newCluster, neighbor);
		}

	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param grid
	 * @param clusters
	 * @param cellMap
	 * @param scoreMap
	 */
	private void getNeighborScores(GridData grid, Vector<Cluster> clusters, HashMap<Integer, Cluster> cellMap, TreeMap<Double, HashMap<Cluster, Cluster>> scoreMap) {
		//Start Variable Declaration
		HashSet<Cluster> neighbors = null;
		double score = 0.0;
		//End Variable Declaration

		for (Cluster cluster : clusters) {
			neighbors = getNeighborClusters(cluster, grid, cellMap);
			if (neighbors == null) {
				continue;
			}
			for (Cluster neighbor : neighbors) {
				score = getLinkScore(cluster, neighbor, grid);
				if (!scoreMap.containsKey(score)) {
					scoreMap.put(score, new HashMap<Cluster, Cluster>());
				}
				scoreMap.get(score).put(cluster, neighbor);
			}
		}
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param cluster
	 * @param neighbor
	 * @return
	 */
	private double getLinkScore(Cluster cluster, Cluster neighbor, GridData grid) {
		//Start Variable Declaration
		Vector<Double> clusterValues = new Vector<Double>();
		Vector<Double> neighborValues = new Vector<Double>();
		//End Variable Declaration

		populateValues(cluster.getCells(), clusterValues, grid);
		populateValues(neighbor.getCells(), neighborValues, grid);

		return getLinkScore(clusterValues, neighborValues);
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param clusterValues
	 * @param neighborValues
	 * @return
	 */
	private double getLinkScore(Vector<Double> clusterValues, Vector<Double> neighborValues) {
		//Start Variable Declaration
		int clusterSize = clusterValues.size();
		int neighborSize = neighborValues.size();
		double distance = 0.0;
		//End Variable Declaration

		if (clusterSize == 0 || neighborSize == 0) {
			return THRESHOLD + 1;
		}
		for (double clusterValue : clusterValues) {
			for (double neighborValue : neighborValues) {
				distance += Math.abs(clusterValue - neighborValue);
			}
		}
		return distance / (clusterSize * neighborSize);
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
		dataValues.clear();
		for (int clusterCell : dataCells) {
			dataValue = grid.getDataMatrix().get(clusterCell);
			if (dataValue == -1) {
				continue;
			}
			dataValues.add(dataValue);
		}
	}

	/**
	 * initClustersAndCellMap
	 * @param grid
	 * @param clusters
	 * @param cellMap Purpose:
	 */
	private void initClustersAndCellMap(GridData grid, Vector<Cluster> clusters, HashMap<Integer, Cluster> cellMap) {
		Cluster dataCluster = null;
		int clusterID = 0;
		for (int cell : grid.getDataMatrix().keySet()) {
			if (cell > (grid.getCols() * grid.getRows())) {
				continue;
			}
			dataCluster = new Cluster(clusterID);
			dataCluster.addCell(cell);
			clusters.add(dataCluster);
			cellMap.put(cell, dataCluster);
			clusterID++;
		}
	}

	/**
	 * getNeighborClusters
	 * @param cluster1
	 * @return Purpose:
	 */
	private HashSet<Cluster> getNeighborClusters(Cluster cluster, GridData grid, HashMap<Integer, Cluster> cellMap) {
		HashSet<Cluster> neighborClusters = new HashSet<Cluster>();
		HashSet<Integer> neighbors = new HashSet<Integer>();
		int row = 0;
		int col = 0;
		int gridRows = grid.getRows();
		int gridCols = grid.getCols();
		int neighborCell = 0;

		for (int cell : cluster.getCells()) {
			row = (cell % (gridRows * gridCols)) / gridCols;
			col = cell % gridCols;
			for (int i = Math.max(0, row - 1); i <= Math.min(gridRows - 1, row + 1); i++) {
				for (int j = Math.max(0, col - 1); j <= Math.min(gridCols - 1, col + 1); j++) {
					if (i == row && j == col) {
						continue;
					}
					neighborCell = i * gridCols + j;
					if (!grid.getDataMatrix().containsKey(neighborCell)) {
						continue;
					}
					neighbors.add(neighborCell);
				}
			}

		}
		neighbors.removeAll(cluster.getCells());
		for (int neighbor : neighbors) {
			if (cellMap.containsKey(neighbor)) {
				neighborClusters.add(cellMap.get(neighbor));
			}
		}
		return neighborClusters;
	}
}
