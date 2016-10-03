/**
 * 
 */
package com.ibm.in.irl.st.anomaly.gini;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.in.irl.st.anomaly.data.Cluster;
import com.ibm.in.irl.st.anomaly.data.GridData;

/**
 * GiniCalculator.java
 * <p/>
 * Purpose: Calculates the gini coefficient for sample populations and also
 * divides the given grid data into a vector of {@link Cluster}.
 */
public class GiniCalculator {

	/**
	 * Threshold after which the cluster will not be expanded
	 */
	private double GINI_THRESHOLD = 0.0;
	/**
	 * Threshold beyond which a cluster will not be grown. This is necessary in
	 * order to control the size of anomalies.
	 */
	private final int CLUSTER_SIZE = 500;

	/**
	 * GiniCalculator
	 */
	public GiniCalculator(double threshold) {
		GINI_THRESHOLD = threshold;
	}

	/**
	 * getClustersByBestFirst
	 * @param grid - The grid from which the clusters will be drawn.
	 * @param clusters - The clusters which will be formed.
	 * @return Purpose: Returns the clusters from a grid by dividing them based
	 * on the gini coefficient which measures the intra-cluster cohesion.
	 * 
	 * ###FL### Clusters stops to grow when i.) reaches cluster size bound ii.) all possible gini indexes for extended cluster are beyond threshold
	 */
	public void getClustersByBestFirst(GridData grid, Vector<Cluster> clusters) {
		//HashSet<Integer> neighbors = new HashSet<Integer>();
		HashSet<Vector<Integer>> neighbors3D = new HashSet<Vector<Integer>>(1000);
		Vector<Integer> neighbor = null;
		int firstCell = 0;
		int clusterID = 1;
		List<Integer> l = new ArrayList<Integer>();
		l.addAll(grid.getDataMatrix().keySet());
		Collections.shuffle(l);
		while (l.size() != 0 && grid.getDataMatrix().size() != 0) {
			Cluster cluster = new Cluster(clusterID++);
			firstCell = l.get(0);
			cluster.addCell(firstCell);
			do {
				neighbors3D.clear();
				//findNeighbors(cluster, neighbors, grid);
				find3DNeighbors(cluster, neighbors3D, grid);
				//neighbor = getBestNeighbor(cluster.getCells(), neighbors, grid);
				neighbor = getBest3DNeighbor(cluster.getCells(), neighbors3D, grid);
				if (isValid(neighbor)) {
					//System.out.println(cluster.printCells(grid.getRows(), grid.getCols()) + " merged with " + printCellVector(neighbor, grid.getRows(), grid.getCols()));
					cluster.addCells(neighbor);
				}
				//} while (neighbor != -1);
			} while (isValid(neighbor) && cluster.getCells().size() < CLUSTER_SIZE);
			//Remove this cluster from the grid
			for (int clusterCell : cluster.getCells()) {
				l.remove(l.indexOf(clusterCell));
				grid.getDataMatrix().remove(clusterCell);
			}
			clusters.add(cluster);
			//System.out.println(clusters.size() + " " + cluster.getCells().size());
			//System.out.println(grid.getDataMatrix().size());
			//System.out.println(cluster.getCells().size());
		}
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param neighbor
	 * @param rows
	 * @param cols
	 * @return
	 */
	private String printCellVector(Vector<Integer> neighbor, int rows, int cols) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int cell : neighbor) {
			builder.append("(" + cell / (cols * rows) + "," + (cell / cols) % rows + "," + cell % cols + ") ");
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param neighbor
	 * @return
	 */
	private boolean isValid(Vector<Integer> neighbor) {
		//Start Variable Declaration
		boolean isValid = true;
		//End Variable Declaration

		if (neighbor == null) {
			return false;
		}
		for (int neighborCell : neighbor) {
			if (neighborCell == -1) {
				isValid = false;
				break;
			}
		}
		return isValid;
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param cluster
	 * @param neighbors3D
	 * @param grid
	 */
	private void find3DNeighbors(Cluster cluster, HashSet<Vector<Integer>> neighbors3D, GridData grid) {
		//Start Variable Declaration
		int clusterStart = 0;
		int clusterEnd = 0;
		int gridRows = grid.getRows();
		int gridCols = grid.getCols();
		int gridIntervals = grid.getIntervals();
		HashSet<Integer> startSurfaceCells = new HashSet<Integer>(2000);
		HashSet<Integer> surfaceNeighbors = new HashSet<Integer>(2000);
		int barNeighbor = 0;
		Vector<Integer> neighbor3D = new Vector<Integer>(2000);
		int neighbor3Dcell = 0;
		//End Variable Declaration

		//Given a cluster find its 'start' and 'end' as well 'surface'
		// ###FL### 'start' and 'end' refer to starting and ending datetime interval of the clusters
		clusterStart = getClusterEnd(cluster, gridRows, gridCols, 0);
		clusterEnd = getClusterEnd(cluster, gridRows, gridCols, 1);

		//the 'start' surface 
		// ###FL### --> cluster locations at starting time 
		for (int clusterCell : cluster.getCells()) {
			if (clusterCell < (clusterStart + 1) * gridRows * gridCols && clusterCell >= clusterStart * gridRows * gridCols) {
				startSurfaceCells.add(clusterCell);
			}
		}

		//the 'before-start' surface
		//###FL### adds the before-surface (cluster surface at start time - 1) cells if they are included in the grid and if their value is =/= -1
		// ###FL### only looks for persistent clusters
		if (clusterStart > 0) {
			for (int cell : startSurfaceCells) {
				neighbor3Dcell = cell - gridRows * gridCols;
				if (!grid.getDataMatrix().containsKey(neighbor3Dcell)) {
					break;
				}
				if (grid.getDataMatrix().get(neighbor3Dcell) != -1) {
					neighbor3D.add(neighbor3Dcell);
				} else {
					break;
				}
			}
		}
		if (neighbor3D.size() == startSurfaceCells.size()) {
			neighbors3D.add(neighbor3D);
		}
		neighbor3D = new Vector<Integer>();

		//the 'after-end' surface
		// ###FL### adds the after-end surface (cluster surface at end time + 1) cells if included in grid and value =/= -1
		if (clusterEnd < gridIntervals) {
			for (int cell : startSurfaceCells) {
				neighbor3Dcell = cell + (clusterEnd - clusterStart + 1) * gridRows * gridCols;
				if (!grid.getDataMatrix().containsKey(neighbor3Dcell)) {
					break;
				}
				if (grid.getDataMatrix().get(neighbor3Dcell) != -1) {
					neighbor3D.add(neighbor3Dcell);
				} else {
					break;
				}
			}
		}
		if (neighbor3D.size() == startSurfaceCells.size()) {
			neighbors3D.add(neighbor3D);
		}

		//add the 'bars' perpendicular to the surface
		// ###FL### one 'bar' is the hashset of cells at a single location between start and end time
		// add all the bars associated with surface neighbors of the start surface
		surfaceNeighbors = new HashSet<Integer>(2000);
		findSurfaceNeighbors(startSurfaceCells, surfaceNeighbors, grid);
		for (int neighbor : surfaceNeighbors) {
			neighbor3D = new Vector<Integer>();
			for (int interval = 0; interval <= (clusterEnd - clusterStart); interval++) {
				barNeighbor = neighbor + interval * gridRows * gridCols;
				if (!grid.getDataMatrix().containsKey(barNeighbor)) {
					break;
				}
				if (grid.getDataMatrix().get(barNeighbor) != -1) {
					neighbor3D.add(barNeighbor);
				} else {
					break;
				}
			}
			// only add the current 'bar' if all the cells of the bar are valid (in grid and =/= -1 value)
			if (neighbor3D.size() == (clusterEnd - clusterStart + 1)) {
				neighbors3D.add(neighbor3D);
			}
		}
	}

	/**
	 * getBest3DNeighbor
	 * @param cells
	 * @param neighbors3D
	 * @param grid
	 * @return</p> <p>
	 * <b>Purpose: ###FL### from the adjacent neighborhood 'neighbors3D' hashset, output the neighbor3D vector of cells which best increases cluster's gini index<b/>
	 * </p>
	 */
	private Vector<Integer> getBest3DNeighbor(HashSet<Integer> clusterCells, HashSet<Vector<Integer>> neighbors3D, GridData grid) {
		Vector<Double> dataValues = new Vector<Double>();
		double dataValue = 0.0;
		Vector<Integer> bestNeighbor = null;
		double giniCoeff = 0;
		double u = 0.0;
		double bestGini = 1;
		for (int clusterCell : clusterCells) {
			dataValue = grid.getDataMatrix().get(clusterCell);
			dataValues.add(dataValue);
			u += dataValue;
		}
		//Collections.sort(dataValues, Collections.reverseOrder());
		for (Vector<Integer> neighbor3D : neighbors3D) {
			if (clusterCells.containsAll(neighbor3D)) {
				continue;
			}
			giniCoeff = get3DGiniCoefficient(dataValues, u, neighbor3D, grid);
			if (giniCoeff < GINI_THRESHOLD && bestGini > giniCoeff) {
				bestNeighbor = new Vector<Integer>();
				bestNeighbor.addAll(neighbor3D);
				bestGini = giniCoeff;
			}
		}
		return bestNeighbor;
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param dataValues
	 * @param u
	 * @param neighbor3D
	 * @param grid
	 * @return
	 */
	private double get3DGiniCoefficient(Vector<Double> dataValues, double u, Vector<Integer> neighbor3D, GridData grid) {
		//Start Variable Declaration
		//The gini coefficient G
		double gini = 0.0;
		//Population size n
		int n = 0;
		double neighborValue = 0.0;
		Vector<Double> neighborValues = new Vector<Double>();
		//End Variable Declaration

		//Get the data point value for the neighbor
		for (int neighbor3DCell : neighbor3D) {
			if (!grid.getDataMatrix().containsKey(neighbor3DCell)) {
				return Double.MAX_VALUE;
			}
			neighborValue = grid.getDataMatrix().get(neighbor3DCell);
			neighborValues.add(neighborValue);
			u += neighborValue;
		}
		dataValues.addAll(neighborValues);
		Collections.sort(dataValues, Collections.reverseOrder());
		n = dataValues.size();

		//Find mean from the sum
		u = u / n;
		//Sort the data to get the ranks 

		//System.out.println(dataValues);
		//Calculate the gini coefficient using the above formula
		gini = getGiniCoefficient(dataValues, u);
		for (double value : neighborValues) {
			dataValues.remove(value);
		}
		return gini;
	}

	/**
	 * getClustersByExpansion
	 * @param grid - The grid from which the clusters will be drawn.
	 * @param clusters - The clusters which will be formed.
	 * @return Purpose: Returns the clusters from a grid by dividing them based
	 * on the gini coefficient which measures the intra-cluster cohesion.
	 */
	public void getClustersByExpansion(GridData grid, Vector<Cluster> clusters) {
		HashMap<Integer, Cluster> cellMap = new HashMap<Integer, Cluster>();
		TreeMap<Double, Vector<String>> giniValues = new TreeMap<Double, Vector<String>>();
		HashMap<String, Cluster> clusterMap = new HashMap<String, Cluster>();
		initClustersAndCellMap(grid, clusters, cellMap, clusterMap);
		for (Cluster cluster : clusters) {
			if (cluster.getCells().size() == 0) {
				continue;
			}
			getNeighborGini(cluster, grid, cellMap, giniValues);
		}
		expandClusters(clusters, grid, cellMap, giniValues, clusterMap);
	}

	/**
	 * expandClusters Purpose:
	 * @param cellMap
	 * @param grid
	 * @param clusters
	 * @param giniValues
	 */
	private void expandClusters(Vector<Cluster> clusters, GridData grid, HashMap<Integer, Cluster> cellMap, TreeMap<Double, Vector<String>> giniValues, HashMap<String, Cluster> clusterMap) {
		String clusterKey = null;
		HashSet<String> processed = new HashSet<String>();
		String firstClusterID = null;
		String secondClusterID = null;
		Cluster firstCluster = null;
		Cluster secondCluster = null;
		Cluster newCluster = null;
		int numClusters = clusters.size();
		boolean updates = true;
		double gini = 0;

		while (updates) {
			gini = giniValues.firstKey();
			if (gini > GINI_THRESHOLD) {
				break;
			}
			if (giniValues.get(gini).size() == 0) {
				giniValues.remove(gini);
				continue;
			}
			//System.out.println(gini + " " + giniValues.get(gini).size());
			clusterKey = giniValues.get(gini).elementAt(0);
			firstClusterID = clusterKey.split(" ")[0];
			secondClusterID = clusterKey.split(" ")[1];
			giniValues.get(gini).remove(clusterKey);
			if (processed.contains(firstClusterID) || processed.contains(secondClusterID)) {
				continue;
			}
			processed.add(firstClusterID);
			processed.add(secondClusterID);
			firstCluster = clusterMap.get(firstClusterID);
			secondCluster = clusterMap.get(secondClusterID);
			newCluster = new Cluster(numClusters);
			for (int cell : firstCluster.getCells()) {
				newCluster.addCell(cell);
				cellMap.put(cell, newCluster);
			}
			for (int cell : secondCluster.getCells()) {
				newCluster.addCell(cell);
				cellMap.put(cell, newCluster);
			}
			clusterMap.put(numClusters + "", newCluster);
			clusters.add(newCluster);
			clusters.remove(firstCluster);
			clusters.remove(secondCluster);

			getNeighborGini(newCluster, grid, cellMap, giniValues);
			numClusters++;
		}

	}

	/**
	 * initClustersAndCellMap
	 * @param grid
	 * @param clusters
	 * @param cellMap Purpose:
	 */
	private void initClustersAndCellMap(GridData grid, Vector<Cluster> clusters, HashMap<Integer, Cluster> cellMap, HashMap<String, Cluster> clusterMap) {
		Cluster dataCluster = null;
		int clusterID = 0;
		for (int cell : grid.getDataMatrix().keySet()) {
			dataCluster = new Cluster(clusterID);
			dataCluster.addCell(cell);
			clusters.add(dataCluster);
			clusterMap.put(clusterID + "", dataCluster);
			cellMap.put(cell, dataCluster);
			clusterID++;
		}
	}

	/**
	 * getNeighborGini Purpose:
	 */
	private void getNeighborGini(Cluster cluster, GridData grid, HashMap<Integer, Cluster> cellMap, TreeMap<Double, Vector<String>> giniValues) {
		String clusterKey = null;
		Vector<Integer> dataCells = new Vector<Integer>();
		Vector<Double> dataValues = new Vector<Double>();
		double gini = 0;
		double u = 0;
		for (Cluster neighbor : getNeighborClusters(cluster, grid, cellMap)) {
			if (neighbor.getCells().size() == 0) {
				continue;
			}
			if (cluster.equals(neighbor)) {
				continue;
			}
			clusterKey = cluster.getClusterID() + " " + neighbor.getClusterID();
			dataCells.clear();
			dataCells.addAll(cluster.getCells());
			dataCells.addAll(neighbor.getCells());
			u = populateAndGetMean(dataCells, dataValues, grid);
			Collections.sort(dataValues, Collections.reverseOrder());
			gini = getGiniCoefficient(dataValues, u);
			if (!giniValues.containsKey(gini)) {
				giniValues.put(gini, new Vector<String>());
			}
			giniValues.get(gini).add(0, clusterKey);
		}
	}

	/**
	 * populateAndGetMean
	 * @param dataCells
	 * @param dataValues
	 * @param grid
	 * @return Purpose:
	 */
	private double populateAndGetMean(Vector<Integer> dataCells, Vector<Double> dataValues, GridData grid) {
		double dataValue = 0;
		double u = 0;
		dataValues.clear();
		for (int clusterCell : dataCells) {
			dataValue = grid.getDataMatrix().get(clusterCell);
			dataValues.add(dataValue);
			u += dataValue;
		}
		return u / dataValues.size();
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

	/**
	 * getClusterEnd
	 * @param cluster
	 * @param i
	 * @return</p> <p>
	 * <b>Purpose: ###FL### Give the start (i=0) or end (i=1) datetime interval of the cluster <b/>
	 * </p>
	 */
	private int getClusterEnd(Cluster cluster, int rows, int cols, int i) {
		int endPoint = i == 0 ? Integer.MAX_VALUE : 0;
		int interval = 0;
		for (int cell : cluster.getCells()) {
			interval = cell / (rows * cols);
			if (i == 0) {
				if (endPoint > interval) {
					endPoint = interval;
				}
			} else {
				if (endPoint < interval) {
					endPoint = interval;
				}
			}
		}
		return endPoint;
	}

	/**
	 * getBestNeighbor
	 * @param clusterCells - The current set of cells within the cluster
	 * @param neighbors - The set of neighbors for the current cluster
	 * @param grid - The given grid
	 * @return Purpose: Figures out the best neighbor based on the gini
	 * coefficient calculated by combining each of the neighbors with the
	 * current cluster
	 */
	public int getBestNeighbor(HashSet<Integer> clusterCells, HashSet<Integer> neighbors, GridData grid) {
		Vector<Double> dataValues = new Vector<Double>();
		double dataValue = 0.0;
		int bestNeighbor = -1;
		double giniCoeff = 0;
		double u = 0.0;
		double bestGini = 1;
		for (int clusterCell : clusterCells) {
			dataValue = grid.getDataMatrix().get(clusterCell);
			dataValues.add(dataValue);
			u += dataValue;
		}
		Collections.sort(dataValues, Collections.reverseOrder());
		for (int neighbor : neighbors) {
			giniCoeff = getGiniCoefficient(dataValues, u, neighbor, grid);
			if (giniCoeff < GINI_THRESHOLD && bestGini > giniCoeff) {
				bestNeighbor = neighbor;
				bestGini = giniCoeff;
			}
		}
		return bestNeighbor;
	}

	/**
	 * getGiniCoefficient
	 * @param clusterCells - The current set of cells within the cluster
	 * @param neighbor - The current neighbor
	 * @param grid - The given grid
	 * @return Purpose: Calculates the gini coefficient of the given data points
	 * using the following formula by Angus Deaton:<br/>
	 * G = ((N+1)/(N-1)) - (2/(N * (N-1) * u)) * \sum_i^n(P_i * X_i)<br/>
	 * where,<br/>
	 * u is the mean of the population, P_i is the rank of item i, X_i is the
	 * value of item i, N is the size of the population
	 */
	private double getGiniCoefficient(Vector<Double> dataValues, double u, int neighbor, GridData grid) {
		//The gini coefficient G
		double gini = 0.0;
		//Population size n
		int n = 0;
		int neighborIndex = 0;

		double neighborValue = 0.0;

		//Get the data point value for the neighbor
		neighborValue = grid.getDataMatrix().get(neighbor);
		neighborIndex = addValue(neighborValue, dataValues);
		u += neighborValue;
		n = dataValues.size();

		//Find mean from the sum
		u = u / n;
		//Sort the data to get the ranks 

		//System.out.println(dataValues);
		//Calculate the gini coefficient using the above formula
		gini = getGiniCoefficient(dataValues, u);
		dataValues.remove(neighborIndex);
		return gini;
	}

	/**
	 * getGiniCoefficient
	 * @param dataValues
	 * @param u
	 * @return Purpose:
	 */
	public double getGiniCoefficient(Vector<Double> dataValues, double u) {
		double gini = 0.0;
		int n = dataValues.size();
		for (int i = 0; i < dataValues.size(); i++) {
			gini = gini + ((double) (i + 1) * dataValues.get(i));
		}
		gini = (2 / ((double) n * (n - 1) * u)) * gini;
		gini = ((n + 1) / (double) (n - 1)) - gini;
		return gini;
	}

	/**
	 * addValue
	 * @param neighborValue
	 * @param dataValues Purpose:
	 */
	private int addValue(double neighborValue, Vector<Double> dataValues) {
		int i = 0;
		while (i < dataValues.size()) {
			if (neighborValue > dataValues.elementAt(i)) {
				break;
			}
			i++;
		}
		dataValues.add(i, neighborValue);
		return i;
	}

	/**
	 * findNeighbors
	 * @param cluster - Current cluster for which the neighbors are being
	 * populated
	 * @param neighbors - The data structure which will hold the neighbors
	 * @param grid - The given grid Purpose:
	 */
	public void findNeighbors(Cluster cluster, HashSet<Integer> neighbors, GridData grid) {
		//Populate the neighbors for each cell within our cluster
		for (int cell : cluster.getCells()) {
			findNeighbors(cell, neighbors, grid);
		}

		//Remove the cells which are already in our cluster
		neighbors.removeAll(cluster.getCells());
	}

	/**
	 * findNeighbors
	 * @param cluster - Current cluster for which the neighbors are being
	 * populated
	 * @param neighbors - The data structure which will hold the neighbors
	 * @param grid - The given grid Purpose:
	 * ###FL### add all adjacent cells of the surface (fixed time step) to the neighbors hashset 
	 */
	public void findSurfaceNeighbors(HashSet<Integer> surface, HashSet<Integer> neighbors, GridData grid) {
		//Populate the neighbors for each cell within our cluster
		for (int cell : surface) {
			findSpatial3DNeighbors(cell, neighbors, grid);
		}

		//Remove the cells which are already in our cluster
		neighbors.removeAll(surface);
	}

	/**
	 * findNeighbors
	 * @param cell - Current cell out of the cluster
	 * @param neighbors - The data structure which will hold the neighbors
	 * @param grid - The given grid Purpose:
	 * ###FL### add to the hashset neighbors the 3x3 cells square around cell in the same time step (without cell)
	 */
	private void findSpatial3DNeighbors(int cell, HashSet<Integer> neighbors, GridData grid) {
		int row = 0;
		int col = 0;
		int interval = cell / (grid.getRows() * grid.getCols());
		int neighborCell = 0;
		cell = cell % (grid.getRows() * grid.getCols());
		row = cell / grid.getCols();
		col = cell % grid.getCols();
		for (int i = Math.max(0, row - 1); i <= Math.min(grid.getRows() - 1, row + 1); i++) {
			for (int j = Math.max(0, col - 1); j <= Math.min(grid.getCols() - 1, col + 1); j++) {
				if (i == row && j == col) {
					continue;
				}
				neighborCell = interval * grid.getRows() * grid.getCols() + i * grid.getCols() + j;
				if (!grid.getDataMatrix().containsKey(neighborCell)) {
					continue;
				}
				neighbors.add(neighborCell);
			}
		}
	}

	/**
	 * findNeighbors
	 * @param cell - Current cell out of the cluster
	 * @param neighbors - The data structure which will hold the neighbors
	 * @param grid - The given grid Purpose:
	 */
	private void findNeighbors(int cell, HashSet<Integer> neighbors, GridData grid) {
		/*
		 * int cols = grid.getCols(); int neighbor = 0; int[] neighborDiff = {
		 * -cols - 1, -cols, -cols + 1, -1, 1, cols - 1, cols, cols + 1 }; for
		 * (int diff : neighborDiff) { neighbor = cell + diff; if
		 * (!grid.getDataMatrix().containsKey(neighbor)) { continue; }
		 * neighbors.add(neighbor); }
		 */
		int row = 0;
		int col = 0;
		int neighborCell = 0;
		row = cell / grid.getCols();
		col = cell % grid.getCols();
		for (int i = Math.max(0, row - 1); i <= Math.min(grid.getRows() - 1, row + 1); i++) {
			for (int j = Math.max(0, col - 1); j <= Math.min(grid.getCols() - 1, col + 1); j++) {
				if (i == row && j == col) {
					continue;
				}
				neighborCell = i * grid.getCols() + j;
				if (!grid.getDataMatrix().containsKey(neighborCell)) {
					continue;
				}
				neighbors.add(neighborCell);
			}
		}
	}

	/**
	 * <p>
	 * <b>Purpose:</b>
	 * </p>
	 * @param clusters
	 * @param grid
	 * @param string
	 * @throws IOException
	 */
	public void fillClusters(Vector<Cluster> clusters, String fileName, GridData grid) throws IOException {
		//Start Variable Declaration
		Cluster cluster = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		int cell = 0;
		String line = "";
		int clusterID = 0;
		//End Variable Declaration
		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("")) {
				break;
			}
			cluster = new Cluster(clusterID);
			for (String cellCoord : line.split(" ")) {
				cellCoord = cellCoord.replaceAll("[\\(\\)]", "");
				cell = Integer.parseInt(cellCoord.split(",")[0]) * grid.getRows() * grid.getCols() + Integer.parseInt(cellCoord.split(",")[1]) * grid.getCols() + Integer.parseInt(cellCoord.split(",")[2]);
				cluster.addCell(cell);
			}
			clusters.add(cluster);
			clusterID++;
		}
		reader.close();
	}

}
