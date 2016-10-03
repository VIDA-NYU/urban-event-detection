/**
 * 
 */
package com.ibm.in.irl.st.anomaly.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.ibm.in.irl.st.anomaly.data.GridData;

/**
 * InputFileParser.java
 * <p/>
 * Purpose: Parses the input file and stores the data values in a
 * {@link GridData} structure.
 */
public class InputFileParser {
	/**
	 * The field stored the reader object
	 */
	private BufferedReader reader = null;

	/**
	 * The file to be parsed
	 */
	private String fileName = null;

	/**
	 * The field states whether the variables are initialized
	 */
	private boolean initialized = false;

	/**
	 * The gini is calculated on positive values. This field stores the bias
	 * value to be added to each entry
	 */
	private double VALUE_BIAS = 0.0;

	/**
	 * The field stored the value to be adjusted in order to fit the row number
	 * as matrix index
	 */
	private double ROW_BIAS = 0.0;

	/**
	 * The field stored the value to be adjusted in order to fit the column
	 * number as matrix index
	 */
	private double COL_BIAS = 0.0;

	public void testy(){
		System.out.println("This is a test !");
	}
	
	public InputFileParser(String fileName, double rowBias, double colBias, double valueBias) throws Exception {
		System.out.println("Initializing biases to 0 and reading input file")
		this.fileName = fileName;
		VALUE_BIAS = valueBias;
		ROW_BIAS = rowBias;
		COL_BIAS = colBias;
		initialize(fileName);
	}

	private void initialize(String fileName) throws Exception {
		//####// Reads the input file
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		System.out.println("inputFile has been read by initialize function")
		initialized = true;
	}

	public void close() throws Exception {
		if (reader != null) {
			reader.close();
		}
	}

	/**
	 * getGridData
	 * @return Purpose: Populates the grid given the input data. This method
	 * will change if the input data format is also changed.
	 * 
	 * ##### TO BE ADAPTED
	 * 
	 */
	public void fillGridData(GridData grid) throws Exception {
		
		System.out.println("Starting to fill grid");
		
		if (!initialized) {
			initialize(fileName);
		}
		String line = reader.readLine(); // skip the header line
		String[] tokens = null;
		int interval = 0;
		int row = 0;
		int col = 0;
		double count = 0.0;
		
		System.out.println("Beginning reading of " + filename);
		while ((line = reader.readLine()) != null) {
			tokens = line.split(",");
			//Each line contains the cell_id=RRRCCC, time_interval, datetime, count
			interval = Integer.parseInt(tokens[1]);
			row = Integer.parseInt(tokens[0].substring(0,3));
			col = Integer.parseInt(tokens[0].substring(3,6));
			count = Double.parseDouble(tokens[3]) + VALUE_BIAS;
			
			grid.setValue(interval, row, col, count);
			System.out.println("Cell ("+interval+ ", "+row+", "+ col+ ")  -->  " + count);
		}
		
		System.out.println("Grid filled !")
	}

	/*	*//**
	 * fileRandomData
	 * @param grid Purpose:
	 */
	/*
	 * public void fileRandomData(GridData grid) { int cols = grid.getCols();
	 * int rows = grid.getRows(); double value = 0; for (int i = 0; i < rows;
	 * i++) { for (int j = 0; j < cols; j++) { value = Math.random() * 100;
	 * grid.setValue(i, j, value); } } }
	 *//**
	 * loadGrid
	 * @param grid Purpose:
	 */

	public void loadGrid(GridData grid, String fieldSeparator) throws Exception {
		if (!initialized) {
			initialize(fileName);
		}
		String line = null;
		String[] tokens = null;
		int row = 0;
		int col = 0;
		double value = 0;
		while ((line = reader.readLine()) != null) {
			tokens = line.split(fieldSeparator);
			for (col = 0; col < tokens.length; col++) {
				value = Double.parseDouble(tokens[col]);
				if (value != -1) {
					grid.setValue(row / grid.getRows(), row % grid.getRows(), col, value);
				}
			}
			row++;
		}
	}
	/**
	 * fillPointData
	 * @param grid</p>
	 * <p>
	 * <b>Purpose:<b/>
	 * </p>
	 * @throws Exception
	 */
	/*
	 * public void fillPointData(GridData grid) throws Exception { if
	 * (!initialized) { initialize(fileName); } String line = null; String[]
	 * tokens = null; int row = 0; int col = 0; while ((line =
	 * reader.readLine()) != null) { System.out.println(line); tokens =
	 * line.split(","); if (tokens.length < 3) { continue; } row = (int)
	 * ((Double.parseDouble(tokens[1]) + ROW_BIAS) * 100); col = (int)
	 * ((Double.parseDouble(tokens[2]) + COL_BIAS) * 100);
	 * System.out.println(row + " " + col); //id =
	 * Double.parseDouble(tokens[0]); grid.addValue(row, col); } }
	 */
}
