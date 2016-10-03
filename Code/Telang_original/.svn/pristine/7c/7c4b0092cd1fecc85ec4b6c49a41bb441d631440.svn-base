/**
 * <p>
 * AnomalyDetection : SATSampleGenerator.java <br>
 * <br>
 * @author Salil Joshi (saljoshi@in.ibm.com) <br>
 * <br>
 * <b>Created on: </b> May 21, 2013
 * </p>
 * Revision History:
 */
package com.ibm.in.irl.st.anomaly.sat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Anomaly;
import com.ibm.in.irl.st.anomaly.data.Cluster;
import com.ibm.in.irl.st.anomaly.data.GridData;
import com.ibm.in.irl.st.anomaly.stat.TestStatistic;

/**
 * <p>
 * <b>Purpose:</b>
 * <p/>
 */
public class SATSampleGenerator {

	final static int PADDING = 3;
	private static final double STAT_THRESHOLD = 6.63;

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param grid
	 * @param clusters
	 * @param grid
	 * @param neighbors
	 */
	public void generateCircles(GridData grid, Vector<Anomaly> anomalies) {
		//Start Variable Declaration
		long randX = 0;
		long radius = 0;
		long randY = 0;
		HashMap<Integer, Double> gridData = grid.getDataMatrix();
		HashSet<Integer> clusterCells = null;
		HashSet<Integer> neighborCells = null;
		Vector<Double> clusterValues = new Vector<Double>(200);
		Vector<Double> neighborValues = new Vector<Double>(200);
		TestStatistic stat = new TestStatistic();
		HashSet<Integer> doneCells = new HashSet<Integer>(100000);
		Cluster cluster = null;
		//Cluster neighbor = null;
		//TreeMap<Double, Vector<Anomaly>> topAnomalies = new TreeMap<Double, Vector<Anomaly>>(Collections.reverseOrder());
		double statValue = 0.0;
		int cell = 0;
		int size = 0;
		boolean overlap = false;
		//End Variable Declaration
		for (int i = 0; i < 100000; i++) {
			cluster = new Cluster(i + 1);
			if (i % 1000 == 0) {
				System.out.println(i);
			}
			//neighbor = new Cluster(i + 1);
			clusterValues = new Vector<Double>();
			neighborValues = new Vector<Double>();
			randX = Math.round(Math.random() * grid.getRows());
			randY = Math.round(Math.random() * grid.getCols());
			radius = Math.round(Math.random() * Math.min(grid.getCols(), grid.getRows()) / 200);
			clusterCells = new HashSet<Integer>(200);
			neighborCells = new HashSet<Integer>(200);
			overlap = false;
			for (long x = Math.max(0, randX - radius - PADDING); x < Math.min(grid.getRows(), randX + radius + PADDING); x++) {
				if (overlap) {
					break;
				}
				for (long y = Math.max(0, randY - radius - PADDING); y < Math.min(grid.getCols(), randY + radius + PADDING); y++) {
					if (((x - randX) * (x - randX) + (y - randY) * (y - randY)) < ((radius + PADDING) * (radius + PADDING))) {
						cell = (int) (x * grid.getCols() + y);
						if (!gridData.containsKey(cell)) {
							continue;
						}

						if (doneCells.contains(cell)) {
							overlap = true;
							break;
						}

						if (((x - randX) * (x - randX) + (y - randY) * (y - randY)) < (radius * radius)) {
							clusterCells.add(cell);
							clusterValues.add(gridData.get(cell));
						} else {
							neighborCells.add(cell);
							neighborValues.add(gridData.get(cell));
						}
					}
				}
			}
			if (clusterCells.size() < 5 || neighborCells.size() < 5 || overlap) {
				i--;
				continue;
			}
			statValue = stat.getLocalStat(clusterValues, neighborValues);
			cluster.setCells(clusterCells);
			if (Math.abs(statValue) >= STAT_THRESHOLD) {
				anomalies.add(new Anomaly(cluster, neighborCells, statValue));
				/*if (!topAnomalies.containsKey(Math.abs(statValue))) {
					topAnomalies.put(Math.abs(statValue), new Vector<Anomaly>());
				}
				topAnomalies.get(Math.abs(statValue)).add(new Anomaly(cluster, neighborCells, statValue));
				if (size == 100) {
					topAnomalies.pollLastEntry();
				} else {
					size++;
				}*/
				doneCells.addAll(clusterCells);
			}
			/*
			 * cluster.addCells(clusterCells); neighbor.addCells(neighborCells);
			 * clusters.add(cluster); neighbors.add(neighbor);
			 */
		}
	/*	for (double key : topAnomalies.keySet()) {
			anomalies.addAll(topAnomalies.get(key));
			
			 * if (anomalies.size() > 100) { anomalies.setSize(100); break; }
			 
		}*/
	}
}
