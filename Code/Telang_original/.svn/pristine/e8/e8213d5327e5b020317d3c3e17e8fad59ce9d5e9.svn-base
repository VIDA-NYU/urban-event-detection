/**
 * <p>
 * AnomalyDetection : RankedAnomalyComparator.java <br>
 * <br>
 * @author Salil Joshi (saljoshi@in.ibm.com) <br>
 * <br>
 * <b>Created on: </b> Apr 16, 2014
 * </p>
 * Revision History:
 */
package com.ibm.in.irl.st.anomaly.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>
 * <b>Purpose:</b>Compares two sets of anomalies to figure out the overlap
 * <p/>
 */
public class RankedAnomalyAverageComparator {

	private int END_THRESHOLD = 10;
	private int BEGIN_THRESHOLD = 9;

	private void compareComposition(String baseDirectory) throws IOException {
		for (int i = 1; i <= 10; i++) {
			for (int j = 1; j <= i; j++) {
				System.out.print("\t");
			}
			for (int j = i + 1; j <= 10; j++) {
				compareComposition(baseDirectory + "/" + i + ".txt", baseDirectory + "/" + j + ".txt");
				System.out.print("\t");
			}
			System.out.println();
		}
	}

	private void compareComposition(String file1, String file2) throws IOException {
		List<String> cellList1 = new ArrayList<String>();
		List<String> cellList2 = new ArrayList<String>();
		populateCells(file1, cellList1);
		populateCells(file2, cellList2);
		cellList1.retainAll(cellList2);
		System.out.print(cellList1.size() * 1.0 / cellList2.size());
	}

	private void populateCells(String file, List<String> cellList) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = "";
		String[] tokens = null;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			tokens = line.split("\t");
			counter++;
			if (counter <= BEGIN_THRESHOLD) {
				continue;
			} else if (counter > END_THRESHOLD) {
				break;
			}
			if (tokens.length != 3) {
				continue;
			}
			cellList.addAll(Arrays.asList(tokens[0].split(" ")));
		}
		reader.close();
	}

	private void compareRanks(String baseDirectory) throws IOException {
		for (int i = 1; i <= 10; i++) {
			for (int j = i + 1; j <= 10; j++) {
				System.out.println(i + " --> " + j);
				compareRanks(baseDirectory + "/" + i + ".txt", baseDirectory + "/" + j + ".txt");
				System.out.println();
			}
		}
	}

	private void compareRanks(String file1, String file2) throws IOException {
		TreeMap<Integer, List<String>> cellMap = new TreeMap<Integer, List<String>>();
		HashMap<String, Integer> reverseMap = new HashMap<String, Integer>();
		populateCellMap(file1, cellMap);
		populateReverseMap(file2, reverseMap);
		double averageRank = 0.0;
		int size = 0;
		int missing = 0;
		for (int key : cellMap.keySet()) {
			averageRank = 0.0;
			size = 0;
			missing = 0;
			for (String cell : cellMap.get(key)) {
				if (reverseMap.containsKey(cell)) {
					averageRank += reverseMap.get(cell);
					size++;
				} else {
					missing++;
				}
			}
			if (size > 0) {
				averageRank /= size;
				System.out.println("Rank: " + key + ", changed to: " + averageRank + " (Missing: " + missing + ", Overall Size: " + size + ")");
			} else {
				System.out.println("Rank: " + key + " anomaly Not Found");
			}
		}
	}

	private void populateCellMap(String file, TreeMap<Integer, List<String>> cellMap) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = "";
		String[] tokens = null;
		int counter = 0;
		List<String> cellList = null;
		while ((line = reader.readLine()) != null) {
			counter++;
			tokens = line.split("\t");
			if (tokens.length != 3) {
				continue;
			}
			cellList = new ArrayList<String>();
			cellList.addAll(Arrays.asList(tokens[0].split(" ")));
			cellMap.put(counter, cellList);
		}
		reader.close();
	}

	private void populateReverseMap(String file, HashMap<String, Integer> reverseMap) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = "";
		String[] tokens = null;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			counter++;
			tokens = line.split("\t");
			if (tokens.length != 3) {
				continue;
			}
			for (String cell : tokens[0].split(" ")) {
				reverseMap.put(cell, counter);
			}
		}
		reader.close();
	}

	public static void main(String[] args) throws IOException {
		RankedAnomalyAverageComparator comp = new RankedAnomalyAverageComparator();
		String baseDirectory = "C:/Work/Project/STD/AnomalyDetection/work/experiment/Ocean/indian-rand";
		//comp.compareComposition(baseDirectory);
		comp.compareRanks(baseDirectory);
	}
}