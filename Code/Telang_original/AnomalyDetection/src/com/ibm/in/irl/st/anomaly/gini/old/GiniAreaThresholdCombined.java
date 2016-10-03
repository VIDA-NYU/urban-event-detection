//  Created by Ranjana Rajendran.
//  Copyright (c) 2012 __IBM IRL__. All rights reserved.

package com.ibm.in.irl.st.anomaly.gini.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class GiniAreaThresholdCombined {
	
	static Double[][] geo=new Double[1000000][3];
	
	//static Double[][] geoseattle = new Double[1000000][3];
	
	static Double[][] grid;
	
	static Integer[][] ngrid;
	
	static Integer[][] ginied;
	
	static Integer[][] whichginiarea;
	
	static Integer idgini;
	
	static Double rightincrement, upincrement, left, right, top,bottom,mintemp, tempshift, bottomline, leftline;
	
	static int n,sum,px, numpoints, nrows,ncols;
	
	static String infilepath, outfilepath;
	
	static ArrayList<Integer> members;
	
	public static CopyOnWriteArrayList<String> ginineighbors = new CopyOnWriteArrayList();
	
	public static CopyOnWriteArrayList<Double> ginindices = new CopyOnWriteArrayList();
	
	public static CopyOnWriteArrayList<Integer> bestginineighbor = new CopyOnWriteArrayList();
	
	public static CopyOnWriteArrayList<Double> bestginiofneighbor = new CopyOnWriteArrayList();
	
	static CopyOnWriteArrayList<Double> bestginiaverage = new CopyOnWriteArrayList();
	
	static CopyOnWriteArrayList<Double> bestpreviousgini = new CopyOnWriteArrayList();
	
	public static Double bestginiofneighbordouble[];
	
	public static Double bestpreviousginidouble[];
	
	public static Double ginindicesarray[];
	
	public static BufferedWriter out8;
	
	public static void main(String args[]) throws IOException{
		
		infilepath = args[0];
		outfilepath = args[1];
		
		FileInputStream fstream = new FileInputStream(infilepath+File.separator+"tempNA.txt");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		String linesXs = File.separator + "ablineXs.txt";
		String linesYs = File.separator + "ablineYs.txt";
		
		File newfile = new File(outfilepath+linesXs);
		boolean exist = newfile.createNewFile();
		
		while(!exist){
			
			boolean success= newfile.delete();
			if(success)
				exist = newfile.createNewFile();
		}
		
		FileWriter fstream1 = new FileWriter(newfile);
		BufferedWriter lineXwriter = new BufferedWriter(fstream1);
		
		File newfile1 = new File(outfilepath+linesYs);
		boolean exist1 = newfile1.createNewFile();
		
		while(!exist1){
			
			boolean success= newfile1.delete();
			if(success)
				exist1 = newfile1.createNewFile();
		}
		
		FileWriter fstream2 = new FileWriter(newfile1);
		BufferedWriter lineYwriter = new BufferedWriter(fstream2);	
		
		File newfile8 = new File(outfilepath+"/ginicellscombinedoveriterations.txt");
		boolean exist8 = newfile8.createNewFile();
		
		while(!exist8){
			
			boolean success= newfile8.delete();
			if(success)
				exist8 = newfile8.createNewFile();
		}
		
		FileWriter fstream8 = new FileWriter(newfile8);
	    out8 = new BufferedWriter(fstream8);	
		
		String currentline;
		Integer i=0,j=0,p=0;	
		
		left=4185.0;right=-5000.0;top=0.0;bottom=5000.0;
		
		mintemp =0.0;
		
		while((currentline=br.readLine())!=null){
			
			currentline.trim();
			
			String current[]=currentline.split("\\s+");
			
			Double longitude = Double.parseDouble(current[3]);
			Double latitude = Double.parseDouble(current[4]);
			
			geo[i][0]=Double.parseDouble(current[3]);
			geo[i][1]=Double.parseDouble(current[4]);
			geo[i][2]=Double.parseDouble(current[2]);
			
			if(geo[i][2] < mintemp)
				mintemp = geo[i][2].doubleValue();
			
			if(geo[i][0] > right.doubleValue())
				right = geo[i][0];
			if(geo[i][0] < left.doubleValue())
				left = geo[i][0];
			if(geo[i][1] > top.doubleValue())
				top = geo[i][1];
			if(geo[i][1] < bottom.doubleValue())
				bottom = geo[i][1];
			
			if(i>1 && geo[i][0].equals(geo[i-1][0]))
				upincrement = Math.abs(geo[i][1]-geo[i-1][1]);
			else if (i > 1)
				rightincrement = Math.abs(geo[i][0]-geo[i-1][0]);
			
			i++;

		}	
		
		if(mintemp < 0)
			tempshift = Math.abs(mintemp);
		else tempshift = 0.0;
		
		numpoints = i;
		
		System.out.println("Left:"+left+"	Right:"+right+"	Top:"+top+"	Bottom:"+bottom);
		
		System.out.println("Rightincrement:"+rightincrement+" Upincrement:"+upincrement);
		
		if(left > right || bottom > top){
			
			System.out.println("Error in left, right, bottom and top values");
			return;
		}
		else
			System.out.println("Left "+left+" Right "+right+" Top "+top+" Bottom "+bottom);
		
		nrows = 0;
		ncols = 0;
		
		i = 0;
		
		Double rightlimit = left + i * rightincrement;
		ncols++;
		
		for (j = 1; rightlimit <= right; j++){
			
			ncols ++;
			rightlimit = left + j * rightincrement;
			
		}
		
		nrows++;
		Double toplimit = bottom + i * upincrement;
		
		for (j = 1; toplimit <= top; j++){
			
			nrows++;
			toplimit = bottom + j * upincrement;
			
		}
		
		System.out.println("Total number of points: "+numpoints);
		
		System.out.println("Nrows:"+nrows+" NCols"+ncols);
		
		leftline = left - rightincrement / 2;
		
		lineXwriter.write(leftline + "\n");
		
		for(i = 1; i <= ncols; i++){
			
			Double leftlinenew = leftline + i * rightincrement;
			
			lineXwriter.write(leftlinenew+"\n");
			
		}
		
		bottomline = bottom - upincrement / 2 ;
		
		lineYwriter.write(bottomline + "\n");
		
		for(i=0;i <= nrows; i++){
			
			Double bottomlinenew = bottomline + i * upincrement;
			
			lineYwriter.write(bottomlinenew + "\n");
		}
		
		lineXwriter.close();
		lineYwriter.close();
		
		Double radius,giniindex=0.0,variance=0.0;
		
		Integer count =0,ncount=0,maxmembers=0;
		
		ginineighbors.clear();
		ginindices.clear();
		ginied = new Integer[nrows][ncols];
		whichginiarea = new Integer[nrows][ncols];
		grid = new Double[nrows][ncols];
		ngrid = new Integer[nrows][ncols];
		ginied = new Integer[nrows][ncols];
		
		for(j=0; j < nrows; j++){
			
			for(p=0;p < ncols;p++){
				
					grid[j][p]= - 5000.0;
					ngrid[j][p]=0;
			}
		}
		
		for(j=0;j<nrows;j++){
			
			for(p=0;p<ncols;p++){	
				
				findgridvalues(j,p);
				Integer arraynum = j*ncols+p;
				ginied[j][p]=1;
				
				//if(!(grid[j][p].equals(-5000.0))){
					//Integer arraynum = j*ncols+p;
					ginineighbors.add(arraynum.toString());
					//ginied[j][p]=1;
					ginindices.add(0.0);
					whichginiarea[j][p]=arraynum;
				//}
				//whichginiarea[j][p]=arraynum;
			}			
		}
		
		Integer n=0;
		for(j=0; j < nrows; j++){
			
			for(p=0;p < ncols;p++){
				
					if(ngrid[j][p]!=0)
						n++;
			}
		}	
		
		if(n==0)
			System.out.println("Grid not being set");
		else
			System.out.println("Ngrid points:"+n);

		Integer iteration=0;
	
		Double mingini=2.0,maxgini=0.0;
				
		do{
			ListIterator<String> it = ginineighbors.listIterator();
			
			ListIterator<Double> itgini = ginindices.listIterator();
						
			ListIterator<Double> itbestginiofneighbor1 = bestginiofneighbor.listIterator();
			
			Integer ginineighborsize = ginineighbors.size();
			
			bestpreviousgini.clear();
			
			while(itbestginiofneighbor1.hasPrevious()){
				itbestginiofneighbor1.previous();
			}
			
			while(itbestginiofneighbor1.hasNext()){
				Double gini2= itbestginiofneighbor1.next();
				bestpreviousgini.add(gini2);
			}
			
			mingini = 2.0;
			iteration++;
				
			bestginineighbor.clear();
			bestginiofneighbor.clear();
			bestginiaverage.clear();
			//ginindices.clear();
		
			while(it.hasPrevious()){
			
				it.previous();
			}
		
			while(it.hasNext()){
				bestginineighbor.add(0);
				bestginiofneighbor.add(2.0);
				bestginiaverage.add(0.0);
				//ginindices.add(0.0);
				Integer index = it.nextIndex();
				String cells[] = it.next().split(",");
				
				Integer size = cells.length;
				
				for(int s = 0; s < size; s++){
					
					Integer arraynum = Integer.parseInt(cells[s]);
					Integer rownum = (arraynum) / ncols;
					
					Integer colnum = arraynum - rownum * ncols;
					
					
					whichginiarea[rownum][colnum]=index;
				      

					
				}
			}
			
			while(it.hasPrevious()){
				
				it.previous();
			}
			
			while(itgini.hasPrevious()){
				
				itgini.previous();
				
			}
			
			Integer towhichtoaddto=0;
		
			while(it.hasNext()){
				
				towhichtoaddto = it.nextIndex();
				String element = it.next().toString();
			
				String cells[] = element.split(",");
			
				Integer indices[] = new Integer[cells.length];
			
				Integer rownum,colnum;
				Double value;
			
				Set<String> nearestneighbors = new HashSet();
			
				nearestneighbors.clear();
			
				int q;
			
				for(q = 0; q < cells.length;q++){
					indices[q]=Integer.parseInt(cells[q]);
				
					nearestneighbors.add(cells[q]);
				
					rownum = (indices[q]) / ncols;
				
					colnum = indices[q] - rownum * ncols;
					
					String arrayindex;
					Integer arraynum;
					
				    if(rownum < nrows-1){
				 //   	if(ginied[rownum+1][colnum] > 0 )
				    	if(whichginiarea[rownum+1][colnum].equals(4238) && ginineighbors.size()==4227)
				    		System.out.println("");
				    	String	gots = ginineighbors.get(whichginiarea[rownum+1][colnum]);
				    	//if(gots.length() == ginied[rownum+1][colnum])
				    	//if(!(element.equals(gots)) && ngrid[rownum+1][colnum].intValue() > 0 )
				    	if(!(element.equals(gots)))
				    		nearestneighbors.add(gots);
				    }
					
				    //if(colnum < 50-1){
				    if(colnum < ncols -1 ){
				    	
				    	if(whichginiarea[rownum][colnum+1].equals(2701) && ginineighbors.size()==2696)
				    		System.out.println("");
				    	String gots = ginineighbors.get(whichginiarea[rownum][colnum+1]);
				    	//if(gots.length() == ginied[rownum][colnum+1])
				    	//if(!(element.equals(gots)) && ngrid[rownum][colnum+1].intValue() > 0)
				    	if(!(element.equals(gots)))
				    		nearestneighbors.add(gots);
				    }
					
					//if(rownum > 29 ){
				    if(rownum > 0){
				    	String gots = ginineighbors.get(whichginiarea[rownum-1][colnum]);
				    	//if(gots.length() == ginied[rownum-1][colnum])
				    	//if(!(element.equals(gots)) && ngrid[rownum-1][colnum].intValue() > 0)
				    	if(!(element.equals(gots)))
				    		nearestneighbors.add(gots);
					}
				
					if(colnum > 0 ){
				    	String gots = ginineighbors.get(whichginiarea[rownum][colnum-1]);
				    	//if(gots.length() == ginied[rownum][colnum-1])
				    	//if(!(element.equals(gots)) && ngrid[rownum][colnum-1].intValue() > 0)
				    	if(!(element.equals(gots)))
				    		nearestneighbors.add(gots);
					}	
				}
			
				for (q=0;q < cells.length; q++){
				
					nearestneighbors.remove(cells[q]);
				
				}
			
				Double sum=0.0,px=0.0;
			
				Integer  bestindex=0,num=0;
			
				Double bestgini=2.0, bestginaverage=0.0;
			
				Iterator<String> itr = nearestneighbors.iterator();
			
				while(itr.hasNext()){
				
					sum=0.0;
					px=0.0;
					num=0;
					
					ArrayList<Double> evalset = new ArrayList();
					
					evalset.clear();
					
					String indexstring = itr.next();
					

					
					Integer index = ginineighbors.indexOf(indexstring);
				
					String indexstrings[] = indexstring.split(",");
					
					for(int s=0; s < indexstrings.length ; s++){
						
						Integer index1 = Integer.parseInt(indexstrings[s]);
						
						Integer rownum1 = index1 / ncols;
						
						Integer colnum1 = index1 - rownum1 * ncols;
				
						sum += grid[rownum1][colnum1].doubleValue() ;
						
						num += 1;

						evalset.add(grid[rownum1][colnum1].doubleValue());	

					}	
			
					for (q=0;q<cells.length ; q++){
				
						rownum = (Integer.parseInt(cells[q])) / ncols;
				
						colnum = Integer.parseInt(cells[q]) - rownum * ncols;
				
						num += 1;
				
						sum += grid[rownum][colnum].doubleValue();
					
						evalset.add(grid[rownum][colnum].doubleValue());
				
					}
				
					Collections.sort(evalset);
					
					Integer evalsize=evalset.size();
				
					for(q=0;q<evalsize;q++){
					
						px += (evalsize.doubleValue() -q)*evalset.get(q);
					}
				
					Double u = sum / num;
				
					Double neighborratio=0.0,ginivalue=3.0;
				
					if ( u != -5000.0 && u > 0.0 && num > 1){
					
						neighborratio = (num.doubleValue() + 1)/(num.doubleValue()-1);
					
						ginivalue = neighborratio - ((2 * px)/(num*(num-1)*u));
						
						if(ginivalue < 0){
							ginivalue = 0.0;
						}
					
						if(ginivalue < bestgini){
						
							bestgini = ginivalue;
						
							bestindex = index;
							
							bestginaverage = u;
						}
						
					
					}
			
				}
				
				bestginineighbor.set(towhichtoaddto,bestindex);
				bestginiofneighbor.set(towhichtoaddto,bestgini);
				bestginiaverage.set(towhichtoaddto,bestginaverage);
			
			}
			
			ListIterator<Double> itbestginiofneighbor2 = bestginiofneighbor.listIterator();
		
			while(itbestginiofneighbor2.hasPrevious()){
				itbestginiofneighbor2.previous();
			}
			
			ListIterator<Integer> itbestginineighbor = bestginineighbor.listIterator();
			
			while(itbestginineighbor.hasPrevious())
				itbestginineighbor.previous();
			
			Integer minginiindex=0,minclusterindex=0,count3=0;
		
			while(itbestginiofneighbor2.hasNext()){
			
				Double ginival = itbestginiofneighbor2.next();
				Integer ginivalindex = itbestginineighbor.next();
			
				if( ginival < mingini.doubleValue()){
				
					mingini = ginival;
					minginiindex = ginivalindex;
					minclusterindex=count3;
				
				}
				
				count3++;
				
			}
			
			System.out.println("Iteration: "+iteration+" ; miniginivalue: "+mingini.doubleValue());
			
			findtopk(iteration);
			
			if(mingini.doubleValue() <= 0.05){
				
				mergebelowthreshold(iteration);
	
			}
		
		}while(mingini.doubleValue() <= 0.05);
		
		System.out.println("Finished with gini area construction");
		
		File newfile5 = new File(outfilepath+"/climateginiarearectanglesthreshold");
		boolean exist5 = newfile5.createNewFile();
		
		while(!exist5){
			
			boolean success= newfile5.delete();
			if(success)
				exist5 = newfile5.createNewFile();
		}
		
		FileWriter fstream5 = new FileWriter(newfile5);
		BufferedWriter out5 = new BufferedWriter(fstream5);
		
		ListIterator<String> it = ginineighbors.listIterator();
		
		ListIterator<Double> itgini = ginindices.listIterator();
		
		if(ginineighbors.size()!=ginindices.size()){
			System.out.println("Error - in algorithm");
			return;
		}
		
		while(it.hasPrevious()){
			
			it.previous();
		}
		
		while(itgini.hasPrevious()){
			
			itgini.previous();
		}
		
		Integer count1=0,biggest=0,countgini=0;
		
		for(i=0;i < nrows; i++)
			for(j=0;j< ncols; j++)
				if(!ginied[i][j].equals(1))
					countgini++;
		
		if(countgini==0)
			System.out.println("ginied not set:"+ginied[nrows-2][ncols-2]);
		
		Integer countsingleginiareas =0;
		
		while(it.hasNext()){
			
			System.out.println("Gini area number :"+it.nextIndex() + " written");
			
			String cells[] = it.next().split(",");
			
			Double gini = itgini.next();
			
			if(cells.length==1)
				countsingleginiareas++;
			
				for (int q=0; q < cells.length;q++){
				
					Integer index = Integer.parseInt(cells[q]);
				
					Integer rownum = index / ncols;
				
					Integer colnum = index - rownum * ncols;
				
					System.out.println("("+rownum+","+colnum+")");
				
					Double leftx= leftline+(colnum)*rightincrement;
					Double lefty = bottomline+(rownum)*upincrement;
					Double rightx = leftline+(colnum+1)*rightincrement;
					Double righty = bottomline + (rownum+1)*upincrement;
					
					if(grid[rownum][colnum].doubleValue() < 0.0 && q > 0){
						System.out.println("Standalone cell in bigger gini area: "+grid[rownum][colnum].doubleValue());
					}
					out5.write(leftx.toString()+"	"+lefty.toString()+"	"+rightx.toString()+"	"+righty.toString()+"	"+gini.toString()+"	"+grid[rownum][colnum].toString()+"	"+ index.toString()+"	" +rownum.toString()+"	"+colnum.toString()+"	"+ngrid[rownum][colnum].toString()+"	"+ginied[rownum][colnum].toString()+"	"+count1.toString()+"\n");
				
				}
				
				out5.write("\n");
			//}
			
			
			
			if(biggest < cells.length)
				biggest = cells.length;
			
			
			count1++;
			
		}
		
		out5.close();
		
		File newfile20 = new File(outfilepath+"/ginithresholdNAstatistics.txt");
		boolean exist20 = newfile20.createNewFile();
		
		while(!exist20){
			
			boolean success= newfile20.delete();
			if(success)
				exist20 = newfile20.createNewFile();
		}
		
		FileWriter fstream20 = new FileWriter(newfile20);
		BufferedWriter out20 = new BufferedWriter(fstream20);
		
		out20.write("Statistics of combining all gini areas with gini index below threshold during each iteration over NA\n\n");
		
		String mingini20 = "mingini:"+mingini;
		out20.write(mingini20+"\n");
		System.out.println("mingini:"+mingini);
		
		String iterations20 = "Number of iterations:"+ iteration;
		out20.write(iterations20+"\n");
		System.out.println("Number of iterations:"+ iteration);
		
		String numginiareas20 = "Number of gini areas: "+ginineighbors.size();
		out20.write(numginiareas20+"\n");
		System.out.println("Number of gini areas: "+ginineighbors.size());
		
		String sincellginiareas20 = "Single cell gini areas :"+ countsingleginiareas;
		out20.write(sincellginiareas20+"\n");
		System.out.println("Single cell gini areas :"+ countsingleginiareas);
		
		String biggestginicluster20 = "Biggest gini cluster has size: "+biggest;
		out20.write(biggestginicluster20+"\n");
		System.out.println("Biggest gini cluster has size: "+biggest);
		
		out20.close();
		
		while(it.hasPrevious()){
			it.previous();
		}
		
		String giniareaarraystring[] = ginineighbors.toArray(new String[ginineighbors.size()]);
		
		bestginiofneighbordouble = bestginiofneighbor.toArray(new Double[bestginiofneighbor.size()]);
		
		bestpreviousginidouble = bestpreviousgini.toArray(new Double[bestpreviousgini.size()]);
		
		ginindicesarray = ginindices.toArray(new Double[ginindices.size()]);
		
		if(ginineighbors.size()!=bestginiofneighbor.size() || bestginiofneighbor.size()!= bestpreviousgini.size()){
			System.out.println("Arrays not of equal size");
		}
		
		File newfile6 = new File(outfilepath+"/giniareastrings.txt");
		boolean exist6 = newfile6.createNewFile();
		
		while(!exist6){
			
			boolean success= newfile6.delete();
			if(success)
				exist6 = newfile6.createNewFile();
		}
		
		FileWriter fstream6 = new FileWriter(newfile6);
		BufferedWriter out6 = new BufferedWriter(fstream6);
		
		
		while(it.hasNext()){
			out6.write(it.next()+"\n");
		}
		
		out6.close();
		
		Arrays.sort(giniareaarraystring, new GiniComparator31());
	
		File newfile61 = new File(outfilepath+"/giniareastringsdiff.txt");
		boolean exist61 = newfile61.createNewFile();
		
		while(!exist61){
			
			boolean success= newfile61.delete();
			if(success)
				exist61 = newfile61.createNewFile();
		}
		
		FileWriter fstream61 = new FileWriter(newfile61);
		BufferedWriter out61 = new BufferedWriter(fstream61);
		
		
		for(int h=0;h < giniareaarraystring.length;h++){
			out61.write(giniareaarraystring[h]+"\n");
		}
		
		out61.close();
		
		Arrays.sort(giniareaarraystring, new GiniComparatorgini31());
	
		
		File newfile7 = new File(outfilepath+"/giniareastringsgini.txt");
		boolean exist7 = newfile7.createNewFile();
		
		while(!exist7){
			
			boolean success= newfile7.delete();
			if(success)
				exist7 = newfile7.createNewFile();
		}
		
		FileWriter fstream7 = new FileWriter(newfile7);
		BufferedWriter out7 = new BufferedWriter(fstream7);
		
		
		for(int h=0;h < giniareaarraystring.length;h++){
			out7.write(giniareaarraystring[h]+"\n");
		}
		
		Arrays.sort(giniareaarraystring, new GiniComparatorbyneighbor());
		
		File newfile9 = new File(outfilepath+"/giniareastringsabswithneighbors.txt");
		boolean exist9 = newfile9.createNewFile();
		
		while(!exist9){
			
			boolean success= newfile9.delete();
			if(success)
				exist9 = newfile9.createNewFile();
		}
		
		FileWriter fstream9 = new FileWriter(newfile9);
		BufferedWriter out9 = new BufferedWriter(fstream9);
		
		
		for(int h=0;h < giniareaarraystring.length;h++){
			out9.write(giniareaarraystring[h]+"\n");
		}
		
		Arrays.sort(giniareaarraystring, new GiniComparatorbyneighboravg());
		
		File newfile10 = new File(outfilepath+File.separator + "giniareastringswithneighborsavg.txt");
		boolean exist10 = newfile10.createNewFile();
		
		while(!exist10){
			
			boolean success= newfile10.delete();
			if(success)
				exist10 = newfile10.createNewFile();
		}
		
		FileWriter fstream10 = new FileWriter(newfile10);
		BufferedWriter out10 = new BufferedWriter(fstream10);
		
		
		for(int h=0;h < giniareaarraystring.length;h++){
			out10.write(giniareaarraystring[h]+"\n");
		}
		
		out7.close();
		out8.close();
		out9.close();
		out10.close();
	
	}
	
	static void findgridvalues(Integer i, Integer j){
		
		Double longitude = left.doubleValue() + j * rightincrement.doubleValue();
		Double latitude = bottom.doubleValue() + i * upincrement.doubleValue();
		
		//System.out.println(numpoints);
		
		for(int k=0;k < numpoints;k++){
			if(geo[k][0].equals(longitude) && geo[k][1].equals(latitude)){
				grid[i][j]=geo[k][2].doubleValue() + tempshift + 1.0;
				ngrid[i][j]=1;
				System.out.println(geo[k][2].toString());
			}
		}
		
	}
	

	static void findlocalneighborsingrid(Double left,Double right,Double bottom,Double top){
		
		Integer j;
		
		for(j=0;j<n;j++){
			
			Double X = geo[j][0];
			Double Y = geo[j][1];
			Double speed1 = geo[j][2];
			
			if(X >= left && X < right && Y >= bottom && Y < top){
				
					members.add(j);

			}
			
		}	
	}
	
	public static void mergebelowthreshold(int iteration) throws IOException{
		
		System.out.println("Merging started, iteration  "+ iteration);
		
		Double threshold = 0.05;
				
		//ArrayList<Giniareainfo> giniareas = new ArrayList<Giniareainfo>();
		
		ListIterator<String> itginineighbors = ginineighbors.listIterator();
		
		ListIterator<Double> itbestginiofneighbor = bestginiofneighbor.listIterator();
		
		ListIterator<Integer> itbestginineighbor = bestginineighbor.listIterator();
		
		ListIterator<Double> itginindices = ginindices.listIterator();
		
		while(itginineighbors.hasPrevious())
			itginineighbors.previous();
		
		while(itbestginiofneighbor.hasPrevious())
			itbestginiofneighbor.previous();
		
		while(itbestginineighbor.hasPrevious())
			itbestginineighbor.previous();
		
		while(itginindices.hasPrevious())
			itginindices.previous();
		
		while(itginineighbors.hasNext()){
			
			Integer index = itginineighbors.nextIndex();
			
			String giniareastring = itginineighbors.next();
			
			giniareastring = ginineighbors.get(index);
			
			String cells[] = giniareastring.split(",");
			
			for(int i = 0 ; i < cells.length; i++){
				Integer arraynum = Integer.parseInt(cells[i]);
				Integer rownum = arraynum / ncols;
				Integer colnum = arraynum - rownum *ncols;
				whichginiarea[rownum][colnum]=index;
			}
		}
		
		while(itginineighbors.hasPrevious())
			itginineighbors.previous();
		
	
		while(itginineighbors.hasNext()){
			
			Integer ginineighborsize = ginineighbors.size();
			
			Integer giniareaindex = itginineighbors.nextIndex();
			
			System.out.println("Indexes:"+giniareaindex+","+ginineighborsize);
			
			if(giniareaindex.equals(5463) || giniareaindex.equals(5360) || giniareaindex.equals(5615))
				System.out.println("");
			
			String giniareastring = itginineighbors.next();
			
			giniareastring = ginineighbors.get(giniareaindex);
			
			if(iteration == 1 && giniareastring.equals("1118") )
				System.out.println("");
			
			System.out.println("Giniareastring: "+giniareastring.toString());
		
			
			Double giniexpected = bestginiofneighbor.get(giniareaindex);
			
			if(giniexpected.doubleValue() <= threshold.doubleValue()){
			
				Integer bestneighbor1 = bestginineighbor.get(giniareaindex);
		
				
				String bestneighbor = ginineighbors.get(bestneighbor1);
				
				String bestactualneighbor = bestneighbor;
				
				Integer giniareaid = bestneighbor1;
				
				String cellsneighbors[] = bestactualneighbor.split(",");
				
				Double sum=0.0;
				Double px=0.0;
				Integer num=0;
				
				ArrayList<Double> evalset = new ArrayList();
				
				evalset.clear();
				
				////////
				for(int s=0; s < cellsneighbors.length ; s++){
					
					Integer index1 = Integer.parseInt(cellsneighbors[s]);
					
					Integer rownum1 = index1 / ncols;
					
					Integer colnum1 = index1 - rownum1 * ncols;
				
					sum += grid[rownum1][colnum1].doubleValue() ;
					
					num += 1;

					evalset.add(grid[rownum1][colnum1].doubleValue());	
				}	
				
				int sizeofneighbor = cellsneighbors.length; 
				
				String giniareacells[] = giniareastring.split(",");
		
				for (int q=0;q<giniareacells.length ; q++){
			
					Integer rownum3 = (Integer.parseInt(giniareacells[q])) / ncols;
			
					Integer colnum3 = Integer.parseInt(giniareacells[q]) - rownum3 * ncols;
			
					num += 1;
			
					sum += grid[rownum3][colnum3].doubleValue();
				
					evalset.add(grid[rownum3][colnum3].doubleValue());

				}
			
				Collections.sort(evalset);
		
				
				Integer evalsize=evalset.size();
			
				for(int q=0;q<evalsize;q++){
				
					px += (evalsize.doubleValue() -q)*evalset.get(q);
				}
			
				Double u = sum / num;
	
			
				Double neighborratio=0.0,ginivalue=3.0;
			
				if ( u != -5000.0 && u > 0.0 && num > 1){
				
					neighborratio = (num.doubleValue() + 1)/(num.doubleValue()-1);
				
					ginivalue = neighborratio - ((2 * px)/(num*(num-1)*u));
		
					
					if(ginivalue < 0){
						ginivalue = 0.0;
					}
				
				}
				/////////
				
				if(ginivalue.doubleValue() <= threshold.doubleValue()){
					
					System.out.println("For iteration "+iteration+" : "+num+" cells are combined");
					System.out.println("Minimum value in combined cells in iteration "+iteration+" is "+ evalset.get(0));
					if(evalset.get(0).doubleValue() == -5000.0){
						System.out.println("-5000.0 cell combined");
						return;
					}
					
					String neighbor1 = ginineighbors.get(giniareaid);
					
					String neighbor2 = ginineighbors.get(giniareaindex);
					
					//String neighbor3 = neighbor2;
					
					neighbor2 = neighbor2 + "," +neighbor1;
					
					//ginineighbors.add(index, element)
					
					ginineighbors.set(giniareaindex, neighbor2);
					
					ginindices.set(giniareaindex,ginivalue);
					
					//bestginineighbor.set(giniareaindex, bestginineighbor.get(giniareaid));
			
					ginineighbors.remove(giniareaid.intValue());
					Double obj = ginindices.remove(giniareaid.intValue());
					System.out.println(obj.toString()+" removed");
					obj =bestginiofneighbor.remove(giniareaid.intValue());
					System.out.println(obj.toString()+" removed");
					Integer obji = bestginineighbor.remove(giniareaid.intValue());
					System.out.println(obji.toString()+" removed");
					
					/*Now we should delete the entry for neighbor1. Before that for all ginineighbor Strings between giniareaid and ginineighborsize,
					 * the giniareaid in whichginiarea array should be reduced by one*/
					
					for(int index = 0; index < ginineighbors.size() ; index++){
						
						String neighbor3 = ginineighbors.get(index);
						String[] neighbor3cells = neighbor3.split(",");
						for(int c=0; c < neighbor3cells.length;c++){
							Integer arraynum = Integer.parseInt(neighbor3cells[c]);
					    	Integer rownum4 = arraynum / ncols;
					    	
					    	Integer colnum4 = arraynum - rownum4 * ncols;
					    	
					    	whichginiarea[rownum4][colnum4]= index;
						}			
					}
					
					/*Before we delete the entry for neighbor1, the index of bestginineighbors which points to strings after it
					 * has to be decreased by 1.*/
					
					ListIterator<Integer> itbestginineighbor1 =bestginineighbor.listIterator();
					
					while(itbestginineighbor1.hasPrevious())
						itbestginineighbor1.previous();
					
					while(itbestginineighbor1.hasNext()){
						Integer indexofginiarea = itbestginineighbor1.nextIndex();
						Integer bestneighborid = itbestginineighbor1.next();
						if(bestneighborid.intValue() > giniareaid){
							bestneighborid = bestneighborid - 1;
						}
						else if(bestneighborid.equals(giniareaid)){
							if(giniareaid.intValue() < giniareaindex.intValue()){
								bestneighborid = giniareaindex.intValue() -1 ;
							}
							else
								bestneighborid = giniareaindex.intValue();
						}
						bestginineighbor.set(indexofginiarea, bestneighborid);
					}
					
					int ginineighborindex1;
					if(giniareaid.intValue() < giniareaindex.intValue())
						ginineighborindex1 = giniareaindex - 1;
					else
						ginineighborindex1 = giniareaindex;
					
					itginineighbors=ginineighbors.listIterator();
					
					
					int i=0;
					
					while(i <= ginineighborindex1){
						itginineighbors.next();
						i++;
					}
					
					itbestginineighbor = bestginineighbor.listIterator();
					
					System.out.println("Ginineighbors resized: "+ginineighbors.size() + "Neighbor1:"+ neighbor1+" Neighbor 2:"+neighbor2);
					
				    String neighbor2cells[] = neighbor2.split(",");
				    
				    Integer indices[] = new Integer[neighbor2cells.length];
				    
				    Integer rownum5,colnum5;
				    
				    Integer cellsize = neighbor2cells.length;
				    
				    for(int q=0;q< neighbor2cells.length;q++){
				    	
				    	indices[q]=Integer.parseInt(neighbor2cells[q]);
				    	
				    	rownum5 = indices[q] / ncols;
				    	
				    	colnum5 = indices[q] - rownum5 * ncols;
				    	
				    	//Integer cellsize = cells.length;
				    	
				    	ginied[rownum5][colnum5]=cellsize.intValue();
				    	
				    	//if(colnum > 50 && ginied[rownum][colnum]>1){
				    	if(ginied[rownum5][colnum5] > 1){
				    		System.out.println("["+rownum5+"]"+"["+colnum5+"]"+ " ginied");
				    	}
				    	
				    	
				    }
				    
				    System.out.println(ginivalue.doubleValue());
				    
				    System.out.println(ginineighbors.size()+","+ginindices.size()+","+bestginiofneighbor.size()+","+bestginineighbor.size());
				    
				    System.out.println(iteration+" Number iteration");
					}
					else{
						System.out.println("Gini expected below threshold but Gini value of combined expected above threshold: "+ginivalue.toString());
						//System.out.println("Gini neighbor size was :"+ ginineighborsize);
					}				
			}
			else{
				System.out.println("Gini expected :"+giniexpected);
				//break;
			}
			
		}
		
		for(int index = 0; index < ginineighbors.size() ; index++){
			
			String neighbor3 = ginineighbors.get(index);
			String[] neighbor3cells = neighbor3.split(",");
			for(int c=0; c < neighbor3cells.length;c++){
				Integer arraynum = Integer.parseInt(neighbor3cells[c]);
		    	Integer rownum4 = arraynum / ncols;
		    	
		    	Integer colnum4 = arraynum - rownum4 * ncols;
		    	
		    	whichginiarea[rownum4][colnum4]= index;
		    	
		    	if(index == 4238)
		    		System.out.println("");
			}			
		}
		
	}
	
	public static void findtopk(int itr) throws IOException{
		
		Integer[] giniareaindices = new Integer[ginineighbors.size()];
		
		for(int i=0; i < ginineighbors.size();i++){
			
			giniareaindices[i]=i;
			
		}
		
		File newfile7 = new File(outfilepath+"/sortedginicouplesof"+itr+"iter.txt");
		boolean exist7 = newfile7.createNewFile();
		
		while(!exist7){
			
			boolean success= newfile7.delete();
			if(success)
				exist7 = newfile7.createNewFile();
		}
		
		FileWriter fstream7 = new FileWriter(newfile7);
		BufferedWriter out7 = new BufferedWriter(fstream7);
		
		Arrays.sort(giniareaindices, new GiniComparatorforeachiter1());
		
		//ArrayList<String> topkpairs = new ArrayList<String>();
		
		for(int i=0; i < ginineighbors.size();i++){
			
			String giniareastring = ginineighbors.get(giniareaindices[i]);
			
			Double giniexpected = bestginiofneighbor.get(giniareaindices[i]);
			
			Double giniindex = ginindices.get(giniareaindices[i]);
			
			String[] cells = giniareastring.split(",");
			
			for(int m=0; m< cells.length;m++){
				
				Integer index = Integer.parseInt(cells[m]);
				
				Integer rownum = index / ncols;
			
				Integer colnum = index - rownum * ncols;
			
				//System.out.println("("+rownum+","+colnum+")");
			
				Double leftx= leftline+(colnum)*rightincrement;
				Double lefty = bottomline+(rownum)*upincrement;
				Double rightx = leftline+(colnum+1)*rightincrement;
				Double righty = bottomline + (rownum+1)*upincrement;
				
				out7.write(itr+"	"+i+"	"+1+"	"+giniexpected+"	"+giniindex.doubleValue()+"		"+grid[rownum][colnum]+"	"+rownum+"	"+colnum+"	"+index.intValue()+"	"+leftx+"	"+lefty+"	"+rightx+"	"+righty+"\n");
				
				if(giniexpected.doubleValue() <= 0.05)
					out8.write(itr+"	"+i+"	"+1+"	"+giniexpected+"	"+giniindex.doubleValue()+"		"+grid[rownum][colnum]+"	"+rownum+"	"+colnum+"	"+index.intValue()+"	"+leftx+"	"+lefty+"	"+rightx+"	"+righty+"\n");

				
			}	
			
			String bestneighbor = ginineighbors.get(bestginineighbor.get(giniareaindices[i]));
			
			String[] cellsn = bestneighbor.split(",");
			
			for(int k=0;k < cellsn.length; k++){
			
				Integer index = Integer.parseInt(cellsn[k]);
			
				Integer rownum = index / ncols;
		
				Integer colnum = index - rownum * ncols;
			
				Double leftx= leftline+(colnum)*rightincrement;
				Double lefty = bottomline+(rownum)*upincrement;
				Double rightx = leftline+(colnum+1)*rightincrement;
				Double righty = bottomline + (rownum+1)*upincrement;
			
				out7.write(itr+"	"+i+"	"+2+"	"+giniexpected+"	"+giniindex.doubleValue()+"		"+grid[rownum][colnum]+"	"+rownum+"	"+colnum+"	"+index.intValue()+"	"+leftx+"	"+lefty+"	"+rightx+"	"+righty+"\n");
			
				if(giniexpected.doubleValue() <= 0.05)
					out8.write(itr+"	"+i+"	"+2+"	"+giniexpected+"	"+giniindex.doubleValue()+"		"+grid[rownum][colnum]+"	"+rownum+"	"+colnum+"	"+index.intValue()+"	"+leftx+"	"+lefty+"	"+rightx+"	"+righty+"\n");

			}
			
			out7.write("\n");
			out8.write("\n");
		}	
		
		out7.close();
	}	
}

class GiniComparatorforeachiter1 implements Comparator<Integer> {
	
	public int compare(Integer s, Integer t){
		
		Double giniofs = GiniAreaThresholdCombined.bestginiofneighbor.get(s);
		Double ginioft = GiniAreaThresholdCombined.bestginiofneighbor.get(t);
		Double diff = giniofs.doubleValue() - ginioft.doubleValue();
	  	if(!giniofs.equals(ginioft))
	          return giniofs.compareTo(ginioft);
	      else
	          return 0;
	}
	
}

class GiniComparator31 implements Comparator<String> {  // Sorts(decreasing) according to difference in gini when added to best neighbor
	
	public int compare(String s, String t) {
		
		Integer indexs = GiniAreaThresholdCombined.ginineighbors.indexOf(s);
		Integer indext = GiniAreaThresholdCombined.ginineighbors.indexOf(t);
		
		Double ginidiffs = (GiniAreaThresholdCombined.bestginiofneighbordouble[indexs] - GiniAreaThresholdCombined.ginindicesarray[indexs]);
		Double ginidifft = (GiniAreaThresholdCombined.bestginiofneighbordouble[indext] - GiniAreaThresholdCombined.ginindicesarray[indext]);
		
		Double diff = (ginidifft-ginidiffs) * 10000;
		
		if(!ginidiffs.equals(ginidifft)){
			
			//System.out.println(diff.toString());
	        return diff.intValue();}
	    else
	        return 0;
	}
}



class GiniComparatorgini31 implements Comparator<String> { // Sorts according to gini 
	
public int compare(String s, String t) {
	
	Integer indexs = GiniAreaThresholdCombined.ginineighbors.indexOf(s);
	Integer indext = GiniAreaThresholdCombined.ginineighbors.indexOf(t);
	
	Double ginis = GiniAreaThresholdCombined.ginindicesarray[indexs];
	Double ginit = GiniAreaThresholdCombined.ginindicesarray[indext];
	
	Integer diff = (int)Math.floor((ginis-ginit)*1000000000);
	
	if(!ginis.equals(ginit))
        return diff;
    else
        return 0;
}
}

class GiniComparatorbyneighbor implements Comparator<String> { // Sorts according to sum of absolute difference between mean of giniarea and each of its neighbors
	
public int compare(String s, String t) {
	
	String cells[] = s.split(",");
	String cellt[] = t.split(",");
	
	Set<Integer> nearestneighbors = new HashSet();
	
	nearestneighbors.clear();
	
	Double means = 0.0;
	
	for(int i =0; i < cells.length; i++){
		
		Integer arrayindex = Integer.parseInt(cells[i]);
		
		nearestneighbors.add(arrayindex);
		
		Integer rownum = arrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = arrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		means += GiniAreaThresholdCombined.grid[rownum][colnum];
		
	    if(rownum < GiniAreaThresholdCombined.nrows-1){
	    	if(GiniAreaThresholdCombined.grid[rownum+1][colnum] >= 0.0){
	    	Integer narrayindex = (rownum + 1) * GiniAreaThresholdCombined.ncols + colnum;
	    	nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(colnum < GiniAreaThresholdCombined.ncols -1 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum+1] >= 0.0){
	    	Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum+1;
	    	nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(rownum > 0){
	    	if(GiniAreaThresholdCombined.grid[rownum-1][colnum] >= 0.0){
	    	Integer narrayindex = (rownum - 1) * GiniAreaThresholdCombined.ncols + colnum;
	    	nearestneighbors.add(narrayindex);
	    	}
		}
	
		if(colnum > 0 ){
			if(GiniAreaThresholdCombined.grid[rownum][colnum-1] >= 0.0){
	    	Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum - 1;
	    	nearestneighbors.add(narrayindex);
			}
		}	
		
	}
	
	means = means / cells.length;
	
	for(int i=0; i < cells.length; i++){
		Integer arrayindex = Integer.parseInt(cells[i]);
		nearestneighbors.remove(arrayindex);
	}
	
	Double sumabs = 0.0;
	
	Iterator<Integer> it = nearestneighbors.iterator();
	
	while(it.hasNext()){
		Integer narrayindex = it.next();
		
		Integer rownum = narrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = narrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		sumabs = Math.abs(GiniAreaThresholdCombined.grid[rownum][colnum] - means);
		
	}
	
	
	nearestneighbors.clear();
	
	Double meant = 0.0;
	
	for(int i =0; i < cellt.length; i++){
		
		Integer arrayindex = Integer.parseInt(cellt[i]);
		
		nearestneighbors.add(arrayindex);
		
		Integer rownum = arrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = arrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		meant += GiniAreaThresholdCombined.grid[rownum][colnum];
		
	    if(rownum < GiniAreaThresholdCombined.nrows-1){
	    	if(GiniAreaThresholdCombined.grid[rownum+1][colnum] >= 0.0){

	    	Integer narrayindex = (rownum + 1) * GiniAreaThresholdCombined.ncols + colnum;
	    	nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(colnum < GiniAreaThresholdCombined.ncols -1 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum+1] >= 0.0){

	    	Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum+1;
	    	nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(rownum > 0){
	    	if(GiniAreaThresholdCombined.grid[rownum-1][colnum] >= 0.0){

	    	Integer narrayindex = (rownum - 1) * GiniAreaThresholdCombined.ncols + colnum;
	    	nearestneighbors.add(narrayindex);
	    	}
		}
	
		if(colnum > 0 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum-1] >= 0.0){

	    	Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum - 1;
	    	nearestneighbors.add(narrayindex);
	    	}
		}	
		
	}
	
	meant = meant / cellt.length;
	
	for(int i=0; i < cellt.length; i++){
		Integer arrayindex = Integer.parseInt(cellt[i]);
		nearestneighbors.remove(arrayindex);
	}
	
	Double sumabt = 0.0;
	
	it = nearestneighbors.iterator();
	
	while(it.hasNext()){
		Integer narrayindex = it.next();
		
		Integer rownum = narrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = narrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		sumabt = Math.abs(GiniAreaThresholdCombined.grid[rownum][colnum] - meant);
		
	}
	
	
	Integer diff = (int)Math.floor((sumabt-sumabs)*1000000000);
	
	if(!sumabs.equals(sumabt))
        return diff;
    else
        return 0;
}
}


class GiniComparatorbyneighboravg implements Comparator<String> { // Sorts according to diff between mean inside and outside
	
public int compare(String s, String t) {
	
	String cells[] = s.split(",");
	String cellt[] = t.split(",");
	
	Set<Integer> nearestneighbors = new HashSet();
	
	nearestneighbors.clear();
	
	Double means = 0.0;
	
	for(int i =0; i < cells.length; i++){
		
		Integer arrayindex = Integer.parseInt(cells[i]);
		
		nearestneighbors.add(arrayindex);
		
		Integer rownum = arrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = arrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		means += GiniAreaThresholdCombined.grid[rownum][colnum];
		
	    if(rownum < GiniAreaThresholdCombined.nrows-1){
	    	
	    	if(GiniAreaThresholdCombined.grid[rownum+1][colnum] >= 0.0){
	    		Integer narrayindex = (rownum + 1) * GiniAreaThresholdCombined.ncols + colnum;
	    		nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(colnum < GiniAreaThresholdCombined.ncols -1 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum+1] >= 0.0){
	    		Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum+1;
	    		nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(rownum > 0){
	    	if(GiniAreaThresholdCombined.grid[rownum-1][colnum] >= 0.0){

	    		Integer narrayindex = (rownum - 1) * GiniAreaThresholdCombined.ncols + colnum;
	    		nearestneighbors.add(narrayindex);
	    	}
		}
	
		if(colnum > 0 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum-1] >= 0.0){

	    		Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum - 1;
	    		nearestneighbors.add(narrayindex);
	    	}
		}	
		
	}
	
	means = means / cells.length;
	
	for(int i=0; i < cells.length; i++){
		Integer arrayindex = Integer.parseInt(cells[i]);
		nearestneighbors.remove(arrayindex);
	}
	
	Double meanneighbos = 0.0;
	
	Integer cs=0;
	
	Iterator<Integer> it = nearestneighbors.iterator();
	
	while(it.hasNext()){
		Integer narrayindex = it.next();
		
		cs++;
		
		Integer rownum = narrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = narrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		meanneighbos += GiniAreaThresholdCombined.grid[rownum][colnum];
		
	}
	
	Double diffs = Math.abs(means.doubleValue() - meanneighbos.doubleValue()/cs);
	
	
	nearestneighbors.clear();
	
	Double meant = 0.0;
	
	for(int i =0; i < cellt.length; i++){
		
		Integer arrayindex = Integer.parseInt(cellt[i]);
		
		nearestneighbors.add(arrayindex);
		
		Integer rownum = arrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = arrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		meant += GiniAreaThresholdCombined.grid[rownum][colnum];
		
	    if(rownum < GiniAreaThresholdCombined.nrows-1){
	    	if(GiniAreaThresholdCombined.grid[rownum+1][colnum] >= 0.0){

	    	Integer narrayindex = (rownum + 1) * GiniAreaThresholdCombined.ncols + colnum;
	    	nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(colnum < GiniAreaThresholdCombined.ncols -1 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum+1] >= 0.0){

	    	Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum+1;
	    	nearestneighbors.add(narrayindex);
	    	}
	    }
		
	    if(rownum > 0){
	    	if(GiniAreaThresholdCombined.grid[rownum-1][colnum] >= 0.0){

	    	Integer narrayindex = (rownum - 1) * GiniAreaThresholdCombined.ncols + colnum;
	    	nearestneighbors.add(narrayindex);
	    	}
		}
	
		if(colnum > 0 ){
	    	if(GiniAreaThresholdCombined.grid[rownum][colnum-1] >= 0.0){

	    	Integer narrayindex = (rownum) * GiniAreaThresholdCombined.ncols + colnum - 1;
	    	nearestneighbors.add(narrayindex);
	    	}
		}	
		
	}
	
	meant = meant / cellt.length;
	
	for(int i=0; i < cellt.length; i++){
		Integer arrayindex = Integer.parseInt(cellt[i]);
		nearestneighbors.remove(arrayindex);
	}
	
	Double meanneighbot = 0.0;
	
	Integer ct=0;
	
	it = nearestneighbors.iterator();
	
	while(it.hasNext()){
		Integer narrayindex = it.next();
		
		ct++;
		
		Integer rownum = narrayindex / GiniAreaThresholdCombined.ncols;
		
		Integer colnum = narrayindex - rownum * GiniAreaThresholdCombined.ncols;
		
		meanneighbot += GiniAreaThresholdCombined.grid[rownum][colnum];
		
	}
	
	Double difft = meant - meanneighbot/ct ;
	
	
	Integer diff = (int)Math.floor((difft-diffs)*1000000000);
	
	if(!diffs.equals(difft))
        return diff;
    else
        return 0;
}
}



class GiniAreaComparator implements Comparator<Giniareainfo> { 
	
public int compare(Giniareainfo s, Giniareainfo t) {
	
	//Integer indexs = GiniAreaThresholdCombined.ginineighbors.indexOf(s);
	//Integer indext = GiniAreaThresholdCombined.ginineighbors.indexOf(t);
	
	Double ginis = s.bestginiofneighbor;
	Double ginit = t.bestginiofneighbor;
	
	Integer diff = (int)Math.floor((ginis-ginit)*1000000000);
	
	if(!ginis.equals(ginit.doubleValue()))
        return diff;
    else
        return 0;
	}
}



class Giniareainfo {
	
	String giniareastring;
	Double bestginiofneighbor;
	Integer bestginineighbor;
	Double ginindex;
	
	public Giniareainfo(String giniareastring1, Integer bestginineighbor1,
			Double bestginiofneighbor1, Double giniindices1) {
		// TODO Auto-generated constructor stub
		giniareastring = giniareastring1;
		bestginiofneighbor = bestginiofneighbor1;
		bestginineighbor = bestginineighbor1;
		ginindex = giniindices1;
	}
}

