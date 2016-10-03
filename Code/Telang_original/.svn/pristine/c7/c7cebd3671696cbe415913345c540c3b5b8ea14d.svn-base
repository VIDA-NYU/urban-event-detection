/**
 * 
 */
package com.ibm.in.irl.st.anomaly.data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.stat.TestStatistic;

/**
 * Anomaly.java<p/>
 *
 * Purpose: Stores the detected anomalies. Each anomaly stores the
 * cluster cells, the neighboring cells and the value of the test 
 * statistic which was calculated earlier.
 */
public class Anomaly {

	/**
	 * The cluster which is anomalous
	 */
	private Cluster cluster = null;

	/**
	 * The local neighbors of the anomalous cluster
	 */
	private HashSet<Integer> neighborCells = new HashSet<Integer>(1000);

	/**
	 * The test statistic value calculated using {@link TestStatistic}
	 */
	private double statValue = 0.0;

	/**
	 * Anomaly
	 * @param clusterID
	 * @param cells
	 * @param neighbors
	 * @param statValue2
	 */
	public Anomaly(Cluster cluster, HashSet<Integer> neighbors, double statValue) {
		this.setCluster(cluster);
		//this.getCluster().getCells().addAll(cluster.getCells());
		if (neighbors != null) {
			neighborCells.addAll(neighbors);
		}
		this.setStatValue(statValue);
	}

	/**
	 * @return the statValue
	 */
	public double getStatValue() {
		return statValue;
	}

	/**
	 * @param statValue the statValue to set
	 */
	public void setStatValue(double statValue) {
		this.statValue = statValue;
	}

	/**
	 * @return the neighborCells
	 */
	public HashSet<Integer> getNeighborCells() {
		return neighborCells;
	}

	/**
	 * @param neighborCells the neighborCells to set
	 */
	public void setNeighborCells(HashSet<Integer> neighborCells) {
		this.neighborCells = neighborCells;
	}

	/**
	 * dumpAnomalies
	 * @param anomalies
	 * @param cols
	 * @param string
	 * Purpose: Dumps the vector of anomalies found in a flat file
	 */
	public static void dumpAnomalies(Vector<Anomaly> anomalies, int rows, int cols, String dumpFile) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		for (Anomaly anomaly : anomalies) {
			if (anomaly == null || anomaly.getCluster() == null) {
				continue;
			}
			for (int cell : anomaly.getCluster().getCells()) {
				writer.write("(" + cell / (cols * rows) + "," + (cell / cols) % rows + "," + cell % cols + ") ");
			}
			writer.write("\t");
			for (int cell : anomaly.getNeighborCells()) {
				writer.write("(" + cell / (cols * rows) + "," + (cell / cols) % rows + "," + cell % cols + ") ");
			}
			writer.write("\t" + anomaly.getStatValue() + "\n");
		}
		writer.close();
	}

	/**
	 * dumpAnomalyValues
	 * @param clusters
	 * @param gridClone
	 * @param string
	 * Purpose: Dumps the values consumed by the anomalies held in the grid
	 */
	public static void dumpAnomalyValues(Vector<Anomaly> anomalies, HashMap<Integer, Double> gridData, String dumpFile) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		for (Anomaly anomaly : anomalies) {
			if (anomaly == null || anomaly.getCluster() == null) {
				continue;
			}
			for (int cell : anomaly.getCluster().getCells()) {
				if (gridData.containsKey(cell)) {
					writer.write(gridData.get(cell) + " ");
				} else {
					writer.write(". ");
				}
			}
			writer.write("\t");
			for (int cell : anomaly.getNeighborCells()) {
				if (gridData.containsKey(cell)) {
					writer.write(gridData.get(cell) + " ");
				} else {
					writer.write(". ");
				}
			}
			writer.write("\t" + anomaly.getStatValue() + "\n");
		}
		writer.close();
	}

	/**
	 * dumpAnomalyMap
	 * @param anomalies
	 * @param grid
	 * @param string
	 * Purpose: Dumps the position of the anomalies in the grid
	 */
	public static void dumpAnomalyMap(Vector<Anomaly> anomalies, GridData grid, String dumpFile) throws Exception {
		grid.getDataMatrix().clear();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		for (Anomaly anomaly : anomalies) {
			for (int cell : anomaly.getCluster().getCells()) {
				grid.getDataMatrix().put(cell, anomaly.getCluster().getClusterID() + 0.0);
			}
		}
		writer.write(grid.printGrid());
		writer.close();
	}

	/**
	 * @return the cluster
	 */
	private Cluster getCluster() {
		return cluster;
	}

	/**
	 * @param cluster the cluster to set
	 */
	private void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

}
