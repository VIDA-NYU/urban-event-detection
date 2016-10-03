/**
 * 
 */
package com.ibm.in.irl.st.anomaly.data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * GridData.java<p/>
 *
 * Purpose: Holds the grid cell values using a HashMap. 
 * The hashmap is useful in removing the grid sparsity.
 */
public class GridData {
	private int intervals = 0;
	private int rows = 0;
	private int cols = 0;

	private HashMap<Integer, Double> dataMatrix = null;

	public GridData(int intervals, int rows, int cols) {
		this.setIntervals(intervals);
		this.setRows(rows);
		this.setCols(cols);
		dataMatrix = new HashMap<Integer, Double>();
	}

	/**
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	private void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * @return the cols
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * @param cols the cols to set
	 */
	private void setCols(int cols) {
		this.cols = cols;
	}

	/**
	 * @return the dataMatrix
	 */
	public HashMap<Integer, Double> getDataMatrix() {
		return dataMatrix;
	}

	/**
	 * @param dataMatrix the dataMatrix to set
	 */
	public void setDataMatrix(HashMap<Integer, Double> dataMatrix) {
		this.dataMatrix.clear();
		this.dataMatrix.putAll(dataMatrix);
	}

	/**
	 * setValue
	 * @param row
	 * @param col
	 * @param value
	 * Purpose: 
	 */
	public void setValue(int interval, int row, int col, double value) {
		if (interval >= intervals) {
			return;
		}
		if (row >= rows) {
			return;
		}
		if (col >= cols) {
			return;
		}
		this.dataMatrix.put(interval * rows * cols + row * cols + col, value);
	}

	/**
	 * printGrid
	 * @return
	 * Purpose: Returns a string containing the entire grid
	 */
	public String printGrid() {
		StringBuilder builder = new StringBuilder();
		for (int h = 0; h < intervals; h++) {
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					if (dataMatrix.containsKey(h * rows * cols + i * cols + j)) {
						builder.append(dataMatrix.get(h * rows * cols + i * cols + j) + " ");
					} else {
						builder.append("-1 ");
					}
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	/**
	 * dumpData
	 * @param dumpFile - The file in which the grid is to be dumped
	 * Purpose: Dumps the grid data in a flat file
	 */
	public void dumpData(String dumpFile) throws Exception {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile)));
		writer.write(printGrid());
		writer.close();
	}

	/**
	 * addValue
	 * @param row
	 * @param col</p>
	 * <p><b>Purpose:<b/>
	 * </p> 
	 */
	public void addValue(int interval, int row, int col) {
		if (interval >= intervals) {
			return;
		}
		if (row >= rows) {
			return;
		}
		if (col >= cols) {
			return;
		}
		int value = interval * rows * cols + row * cols + col;
		if (!this.dataMatrix.containsKey(value)) {
			this.dataMatrix.put(value, 0.0);
		}
		this.dataMatrix.put(value, this.dataMatrix.get(value) + 1);
	}

	/**
	 * @return the intervals
	 */
	public int getIntervals() {
		return intervals;
	}

	/**
	 * @param intervals the intervals to set
	 */
	private void setIntervals(int intervals) {
		this.intervals = intervals;
	}
}
