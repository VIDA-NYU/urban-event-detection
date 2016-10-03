/**
 * 
 */
package com.ibm.in.irl.st.anomaly.stat;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Anomaly;
import com.ibm.in.irl.st.anomaly.data.Cluster;
import com.ibm.in.irl.st.anomaly.data.GridData;
import com.ibm.in.irl.st.anomaly.sat.SATSampleGenerator;

/**
 * TestStatistic.java
 * <p/>
 * Purpose: Carries out the test of significance for each region (cluster) in
 * order to identify anomalies. Currently, the likelihood ratio test is
 * implemented.
 */
public class TestStatistic {

	/**
	 * The field holds the threshold for accepting the hypothesis. The value is
	 * derived from chi-square critical region.
	 */
	private static final double STAT_THRESHOLD = 3.84;

	/**
	 * The field holds the baseline b_c for each cell.
	 */
	private static final double CELL_BASELINE = 200;

	/**
	 * findAnomalies
	 * @param clusters
	 * @param grid
	 * @param anomalies Purpose: Figures out the local anomalies in a grid by
	 * iterating over all the clusters by finding out the clusters which are
	 * 'significantly' different than their local neighbors.
	 */
	public void findLocalAnomalies(Vector<Cluster> clusters, GridData grid, Vector<Anomaly> anomalies) {
		HashSet<Integer> neighbors = new HashSet<Integer>(1000);
		Vector<Double> clusterValues = new Vector<Double>(1000);
		Vector<Double> neighborValues = new Vector<Double>(1000);
		Anomaly anomaly = null;
		int padValue = 2;
		double statValue = 0.0;
		for (Cluster cluster : clusters) {
			findNeighbors(cluster, grid, neighbors, padValue);
			getClusterValues(cluster.getCells(), grid, clusterValues);
			getClusterValues(neighbors, grid, neighborValues);
			if (neighborValues.size() == 0 || clusterValues.size() == 0) {
				continue;
			}
			statValue = getLocalStat(clusterValues, neighborValues);
			if (Math.abs(statValue) >= STAT_THRESHOLD) {
				anomaly = new Anomaly(cluster, neighbors, statValue);
				anomalies.add(anomaly);
			}
		}

	}

	/**
	 * findGlobalAnomalies
	 * @param clusters
	 * @param gridDataClone
	 * @param globalAnomalies Purpose: Figures out the global anomalies in a
	 * grid by iterating over all the clusters by finding out the clusters which
	 * are 'significantly' different than the rest of the grid.
	 */
	public void findGlobalAnomalies(Vector<Cluster> clusters, GridData grid, Vector<Anomaly> globalAnomalies) {
		HashSet<Integer> complements = new HashSet<Integer>();
		Vector<Double> clusterValues = new Vector<Double>();
		Vector<Double> complementValues = new Vector<Double>();
		Anomaly anomaly = null;
		double globalLL = 0.0;
		double lambdaG = 0.0;
		double statValue = 0.0;
		double wholeSum = 0.0;

		HashMap<Integer, Double> gridData = grid.getDataMatrix();
		for (int key : gridData.keySet()) {
			wholeSum += gridData.get(key);
		}
		lambdaG = wholeSum / (CELL_BASELINE * gridData.size());
		globalLL = wholeSum * (Math.log(lambdaG) - 1);

		for (Cluster cluster : clusters) {
			complements.clear();
			complements.addAll(gridData.keySet());
			complements.removeAll(cluster.getCells());
			getClusterValues(cluster.getCells(), grid, clusterValues);
			getClusterValues(complements, grid, complementValues);
			if (complementValues.size() == 0 || clusterValues.size() == 0) {
				continue;
			}
			statValue = getGlobalStat(clusterValues, complementValues, globalLL);
			if (statValue >= STAT_THRESHOLD) {
				anomaly = new Anomaly(cluster, null, statValue);
				globalAnomalies.add(anomaly);
			}
		}
	}

	/**
	 * getGlobalStat
	 * @param clusterValues
	 * @param complementValues
	 * @param globalLL
	 * @return Purpose: Calculates the likelihood ratio test statistic value by
	 * comparing a cluster with its complement in the grid.
	 */
	private double getGlobalStat(Vector<Double> clusterValues, Vector<Double> complementValues, double globalLL) {
		//MLE of Region (cluster)
		double regionSum = 0.0;
		double lambdaR = 0.0;
		//MLE of Complement of region (neighbors)
		double complementSum = 0.0;
		double lambdaC = 0.0;
		//The LRT stat value
		double statValue = 0.0;

		for (double clusterValue : clusterValues) {
			regionSum += clusterValue;
		}
		for (double complementValue : complementValues) {
			complementSum += complementValue;
		}

		lambdaR = regionSum / (CELL_BASELINE * clusterValues.size());
		lambdaC = complementSum / (CELL_BASELINE * complementValues.size());

		statValue = 2 * ((regionSum * Math.log(lambdaR) - regionSum) + (complementSum * Math.log(lambdaC) - complementSum) - globalLL);
		return statValue;
	}

	/**
	 * getLocalStat
	 * @param clusterValues - list of sample points from the first model
	 * @param neighborValues - list of sample points from the second model
	 * @return Purpose: Calculates the test statistic value given the two
	 * vectors of values coming from respective models. The models which are
	 * 'significantly' different are identified as potential anomalies.
	 * <p/>
	 * Currently, the likelihood ratio statistic is implemented.
	 */
	public double getLocalStat(Vector<Double> clusterValues, Vector<Double> neighborValues) {
		//MLE of Region (cluster)
		double regionSum = 0.0;
		double lambdaR = 0.0;
		//MLE of Complement of region (neighbors)
		double complementSum = 0.0;
		double lambdaC = 0.0;
		//MLE of the whole region
		double wholeSum = 0.0;
		double lambdaG = 0.0;
		//The LRT stat value
		double statValue = 0.0;

		for (double clusterValue : clusterValues) {
			regionSum = regionSum + clusterValue;
		}
		for (double neighborValue : neighborValues) {
			complementSum = complementSum + neighborValue;
		}

		wholeSum = regionSum + complementSum;

		lambdaR = regionSum / (CELL_BASELINE * clusterValues.size());
		lambdaC = complementSum / (CELL_BASELINE * neighborValues.size());
		lambdaG = wholeSum / (CELL_BASELINE * (neighborValues.size() + clusterValues.size()));
		statValue = 2 * ((regionSum * Math.log(lambdaR)) + (complementSum * Math.log(lambdaC)) - (wholeSum * Math.log(lambdaG)));
		//System.out.println(clusterValues + " " + neighborValues + " " + statValue);
		return statValue;
	}

	/**
	 * getClusterValues
	 * @param cells
	 * @param grid
	 * @param values Purpose: Returns the values from the grid consumed by a
	 * cluster
	 */
	private void getClusterValues(Set<Integer> cells, GridData grid, Vector<Double> values) {
		values.clear();
		for (int cell : cells) {
			if (grid.getDataMatrix().containsKey(cell)) {
				values.add(grid.getDataMatrix().get(cell));
			}
		}
	}

	/**
	 * findNeighbors
	 * @param cluster
	 * @param grid
	 * @param neighbors
	 * @param padValue Purpose: Populates the list of neighboring cells given a
	 * cluster
	 */
	public void findNeighbors(Cluster cluster, GridData grid, HashSet<Integer> neighbors, int padValue) {
		neighbors.clear();
		int row = 0;
		int col = 0;
		int interval = 0;
		int rows = grid.getRows();
		int cols = grid.getCols();
		int intervals = grid.getIntervals();
		int neighborCell = 0;
		for (int cell : cluster.getCells()) {
			interval = cell / (cols * rows);
			cell = cell - interval * (cols * rows);
			row = cell / cols;
			col = cell % cols;
			for (int h = Math.max(0, interval - padValue); h <= Math.min(intervals - 1, interval + padValue); h++) {
				for (int i = Math.max(0, row - padValue); i <= Math.min(rows - 1, row + padValue); i++) {
					for (int j = Math.max(0, col - padValue); j <= Math.min(cols - 1, col + padValue); j++) {
						if (h == interval && i == row && j == col) {
							continue;
						}
						neighborCell = h * rows * cols + i * cols + j;
						if (!grid.getDataMatrix().containsKey(neighborCell)) {
							continue;
						}
						neighbors.add(neighborCell);
					}
				}
			}
		}
		neighbors.removeAll(cluster.getCells());
	}

	public static void main(String args[]) {
		TestStatistic stat = new TestStatistic();
		Vector<Double> vals = new Vector<Double>();
		int numPoints = 5;
		for (int i = 0; i < numPoints; i++) {
			vals.add((double) numPoints - i - 1);
		}
		Vector<Double> vals2 = new Vector<Double>();
		for (int i = 0; i < numPoints; i++) {
			vals2.add((double) i);
		}
		vals2.add(10.0);
		double result = stat.getLocalStat(vals2, vals);
		System.out.println(numPoints + " " + result);
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param clusters
	 * @param grid
	 * @param anomalies
	 */
	/*
	 * public void findLocalSATAnomalies(Vector<Cluster> clusters, GridData
	 * grid, Vector<Anomaly> anomalies) { //Start Variable Declaration
	 * //SATSampleGenerator satGen = new SATSampleGenerator(); Vector<Cluster>
	 * neighbors = new Vector<Cluster>(); Vector<Double> clusterValues = new
	 * Vector<Double>(); Vector<Double> neighborValues = new Vector<Double>();
	 * Anomaly anomaly = null; double statValue = 0.0; //End Variable
	 * Declaration //satGen.generateCircles(clusters, grid, neighbors);
	 * System.out.println("Circles generated at " + new Date()); for (int i = 0;
	 * i < clusters.size(); i++) { getClusterValues(clusters.get(i).getCells(),
	 * grid, clusterValues); getClusterValues(neighbors.get(i).getCells(), grid,
	 * neighborValues); statValue = getLocalStat(clusterValues, neighborValues);
	 * if (Math.abs(statValue) >= STAT_THRESHOLD) { anomaly = new
	 * Anomaly(clusters.get(i), neighbors.get(i).getCells(), statValue);
	 * anomalies.add(anomaly); } } }
	 */
}