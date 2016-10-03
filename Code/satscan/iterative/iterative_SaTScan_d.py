# -*- coding: utf-8 -*-
"""
Created on Fri Aug 12 18:13:14 2016

@author: ferdinand

Input: parameter file & data
Output: i) res txt file with all clusters' information

Adapted to daily data for first easy computation
TODO: Adapt later to hourly precision

"""

import sys
from subprocess import call
import fileinput
import pandas as pd
import numpy as np

n_clusters_reported = int(sys.argv[1])
#n_clusters_reported = 3

inputParamPath = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/paramg1110_d_elp_2k_none_4d_1c.2.prm"
inputCaseFile = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/Casgraph1110_d.cas"
resFile = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/resg1110_d_elp_2k_none_4d_1c"
outputFile = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/globalRes.txt"
tmp_outputFile = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/secondaryRes.txt"

#%% UpdateCounts function

def updateCounts(cluster_loc_ids, cluster_time_ids, inputCaseFile):
    counts = pd.read_csv(inputCaseFile, names=["node_id","count","datetime"], sep = " ",index_col=False)  
    counts['day'] = pd.to_numeric(counts['datetime'].str.split('-').str.get(2))
    
    # Now implement calculation
    c_SpTp = np.sum(counts.loc[~counts['node_id'].isin(cluster_loc_ids) & ~counts['day'].isin(cluster_time_ids), 'count'])
    
    for i in cluster_loc_ids:
        for t in cluster_time_ids:
            c_iTp= np.sum(counts.loc[(counts['node_id'] == i) & (~counts['day'].isin(cluster_time_ids)), 'count'])
            c_Spt= np.sum(counts.loc[(~counts['node_id'].isin(cluster_loc_ids)) & (counts['day']==t),'count'])
            
            #c_iT= np.sum(counts.loc[(counts['node_id'] == i), 'count'])
            #c_St= np.sum(counts.loc[(counts['day']==t),'count'])
            #c_ST = np.sum(counts['count'])  
            #baseline_old = (c_iT*c_St)/ c_ST
            
            newCount = (c_iTp*c_Spt)/ c_SpTp
            rowIndex = counts[(counts['node_id'] == i) & (counts['day']==t)].index[0]        
                    
            #print "Node_id = " + str(i) + " / time_interval = " + str(t)
            #print "c_iTp = " + str(c_iTp)
            #print "c_Spt = " + str(c_Spt)
            #print "old Count = " + str(counts.iloc[rowIndex]['count'])
            #print "old baseline = " + str(baseline_old)
            #print "newCount = " + str(newCount)
            #print "\n"              
            
            counts = counts.set_value(rowIndex,'count',newCount)
    
    # Write to csv
    del counts['day']
    counts.to_csv(inputCaseFile, header = False, sep = " ", index = False)

#%% -------- Main loop -------

output = open(outputFile,"w")
tmp_output = open(tmp_outputFile,"w")
cluster_rank = 1

while(cluster_rank <= n_clusters_reported):
    
    cluster_loc_ids = []    
    
    print "SaTScan iteration number: " + str(cluster_rank)
    # ./SaTScanBatch64 + inputParamPath
    call(["/home/ferdinand/apps/SaTScan/SaTScanBatch64", inputParamPath])
    currentRes = open(resFile) # open res file at resPath    
    
    # 1.) Collecting cluster information
    if (cluster_rank == 1):
        ## At the first pass of SaTScan, we create the global output file
        for line in currentRes:
            output.write(line) 
        output.close()
         
    # TODO: Collect cluster info even for first cluster          
    currentRes = open(resFile) # open res file at resPath     
    line = currentRes.readline()        
    while(line[0:10]!= "1.Location"):
        line = currentRes.readline()
    
    if (cluster_rank != 1):
        tmp_output.write(line.replace("1.Loc", str(cluster_rank)+".Loc"))
    
    
    # Adding cluster locations id to current list cluster_loc_ids
    # i.) Processing first line of location ids list
    line = line.split(':')[1]  #kick the "Location IDs included"
    line = line.replace(" ","")
    line = line.replace("\n","")
    ids_list = line.split(',')
    if ('' in ids_list):
        ids_list.remove('')        
    
    # ii.) Processing the location_ids lines
    while (line[0] != 'C'):        # iterate until the Coordinates line
        cluster_loc_ids = cluster_loc_ids + ids_list
        line=currentRes.readline()  
        
        if (cluster_rank != 1):        
            tmp_output.write(line) # Adding unprocessed line to secondary cluster output file
        
        line = line.replace(" ","")
        line = line.replace("\n","")  
        if (line[len(line)-1] == ','):
            line = line[:len(line)-1]     
        ids_list = line.split(',')        
    
    
    # collect cluster information
    while(line[2:6]!="Test"):        
        line = currentRes.readline()
        
        if (cluster_rank != 1):        
            tmp_output.write(line)
        
        if (line[2:6] == "Time"): # collect cluster timespan to update data
            line = line.split(":")[1]
            line = line.replace(" ", "")
            line = line.split("to")
            cluster_startday = int(line[0].split("/")[2])
            cluster_endday = int(line[1].split("/")[2]) ## End day is included in cluster timespan
        
    cluster_time_ids = range(cluster_startday,cluster_endday+1)
    cluster_loc_ids = map(int, cluster_loc_ids)
            
    if (cluster_rank != 1):
        tmp_output.write("\n")
    
    # 2.) Updating clusters location values in raw data at /xp_roads/Casgraph1110_d.cas
    updateCounts(cluster_loc_ids, cluster_time_ids, inputCaseFile)    
    
    cluster_rank += 1 

tmp_output.close()
    # replace counts of cluster locations ids in data

#%% ---------- Insert secondary cluster info in global output file -----------
print "Building global output file"
tmp_output = open(tmp_outputFile)

for line in fileinput.input(outputFile, inplace = 1):        
    if (line[0:5] == "Note:"):
        for tmp_line in tmp_output:
            print tmp_line.replace("\n","")
    print line.replace("\n","") 
