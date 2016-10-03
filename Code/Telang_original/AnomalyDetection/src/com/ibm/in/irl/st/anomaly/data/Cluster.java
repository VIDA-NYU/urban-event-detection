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

/**
 * Cluster.java
 * <p/>
 * Purpose: Stores a 'cluster' of cells.
 */
public class Cluster {
	private HashSet<Integer> cells = new HashSet<Integer>();

	private int clusterID = 0;

	public Cluster(int clusterID) {
		this.setClusterID(clusterID);
	}

	/**
	 * @return the clusterID
	 */
	public int getClusterID() {
		return clusterID;
	}

	/**
	 * @param clusterID the clusterID to set
	 */
	private void setClusterID(int clusterID) {
		this.clusterID = clusterID;
	}

	/**
	 * addCell
	 * @param firstCellNeighbor Purpose: Adds a cell to the cluster
	 */
	public void addCells(Vector<Integer> neighbor2) {
		for (int neighbor : neighbor2) {
			addCell(neighbor);
		}
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param neighbor
	 */
	public void addCell(int neighbor) {
		cells.add(neighbor);
	}

	/**
	 * @return the cells
	 */
	public HashSet<Integer> getCells() {
		return cells;
	}

	/**
	 * @param cells the cells to set
	 */
	public void setCells(HashSet<Integer> cells) {
		this.cells = cells;
	}

	/**
	 * dumpClusters
	 * @param clusters Purpose: Dumps the vector of clusters found in a flat
	 * file
	 * @param cols
	 */
	public static void dumpClusters(Vector<Cluster> clusters, int rows, int cols, String dumpFile) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		for (Cluster cluster : clusters) {
			for (int cell : cluster.getCells()) {
				writer.write("(" + cell / (cols * rows) + "," + (cell / cols) % rows + "," + (cell % cols) + ") ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	/**
	 * dumpClusterValues
	 * @param clusters
	 * @param grid
	 * @param string Purpose: Dumps the values consumed by the cluster held in
	 * the grid
	 */
	public static void dumpClusterValues(Vector<Cluster> clusters, HashMap<Integer, Double> gridData, String dumpFile) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		for (Cluster cluster : clusters) {
			for (int cell : cluster.getCells()) {
				if (gridData.containsKey(cell)) {
					writer.write(gridData.get(cell) + " ");
				} else {
					writer.write(". ");
				}
			}
			writer.write("\n");
		}
		writer.close();
	}

	/**
	 * dumpClusterMap
	 * @param clusters
	 * @param gridClone
	 * @param string Purpose: Dumps the position of the clusters in the grid
	 */
	public static void dumpClusterMap(Vector<Cluster> clusters, GridData grid, String dumpFile) throws Exception {
		grid.getDataMatrix().clear();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		for (Cluster cluster : clusters) {
			for (int cell : cluster.getCells()) {
				grid.getDataMatrix().put(cell, cluster.getClusterID() + 0.0);
			}
		}
		writer.write(grid.printGrid());
		writer.close();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		Cluster clusterObj = (Cluster) obj;
		isEqual = (this.getClusterID() == clusterObj.getClusterID());
		return isEqual;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClusterID() + " : " + getCells());
		return sb.toString();
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @return
	 */
	public String printCells(int rows, int cols) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int cell : cells) {
			builder.append("(" + cell / (cols * rows) + "," + (cell / cols) % rows + "," + cell % cols + ") ");
		}
		builder.append("]");
		return builder.toString();
	}
}
