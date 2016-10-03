# -*- coding: utf-8 -*-
"""
Created on Fri Aug 12 18:13:14 2016

@author: ferdinand

Input: parameter file & data
Output: i) res txt file with all clusters' information

Adapted to HOURLY data for first easy computation
Results files are assumed to indicate clusters timespans in time_interval generic unit (0 to 743)

"""

import sys
from subprocess import call
import fileinput
import pandas as pd
import numpy as np

n_clusters_reported = int(sys.argv[1])
inputParamFile = sys.argv[2]
outputDirPath = sys.argv[3]
resName = sys.argv[4]

#inputParamFile = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/param1110_h_elp_' + str(maxSize/1000) + 'k_none_'+str(maxTemp)+'h_1c.prm'
#outputDirPath = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/' + it_maxS_maxT/
#resName = res_1110_h_2k_none_7h_1c
resFile = outputDirPath + resName
inputCaseFile = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/Casgraph1110_h.cas"
outputFile = outputDirPath + "globalRes_h.txt"
tmp_outputFile = outputDirPath + "secondaryRes_h.txt"

#%% UpdateCounts function

def intervalToDatetime(interval):
    return '2011-10-' + str((interval/24)+1) + '_' + str(interval % 24)

def updateCounts(cluster_loc_ids, cluster_time_ids, inputCaseFileNew):
    counts = pd.read_csv(inputCaseFileNew, names=["node_id","count","time_interval"], sep = " ",index_col=False)  
    counts['time_interval'] = pd.to_numeric(counts['time_interval'])
    
    # Now implement calculation
    c_SpTp = np.sum(counts.loc[~counts['node_id'].isin(cluster_loc_ids) & ~counts['time_interval'].isin(cluster_time_ids), 'count'])
    
    for i in cluster_loc_ids:
        for t in cluster_time_ids:
            c_iTp= np.sum(counts.loc[(counts['node_id'] == i) & (~counts['time_interval'].isin(cluster_time_ids)), 'count'])
            c_Spt= np.sum(counts.loc[(~counts['node_id'].isin(cluster_loc_ids)) & (counts['time_interval'] == t),'count'])
            
            #c_iT= np.sum(counts.loc[(counts['node_id'] == i), 'count'])
            #c_St= np.sum(counts.loc[(counts['day']==t),'count'])
            #c_ST = np.sum(counts['count'])  
            #baseline_old = (c_iT*c_St)/ c_ST
            
            newCount = (c_iTp*c_Spt)/ c_SpTp
            rowIndex = counts[(counts['node_id'] == i) & (counts['time_interval']==t)].index[0]        
                    
            #print "Node_id = " + str(i) + " / time_interval = " + str(t)
            #print "c_iTp = " + str(c_iTp)
            #print "c_Spt = " + str(c_Spt)
            #print "old Count = " + str(counts.iloc[rowIndex]['count'])
            #print "old baseline = " + str(baseline_old)
            #print "newCount = " + str(newCount)
            #print "\n"              
            
            counts = counts.set_value(rowIndex,'count',newCount)
    
    # Write to csv
    counts.to_csv(inputCaseFileNew, header = False, sep = " ", index = False)

#%% -------- Main loop -------

output = open(outputFile,"w")
tmp_output = open(tmp_outputFile,"w")
cluster_rank = 1

call(['cp',inputCaseFile, outputDirPath])
inputCaseFileNew = outputDirPath + 'Casgraph1110_h.cas'

while(cluster_rank <= n_clusters_reported):
    
    cluster_loc_ids = []   
    
    
    print "SaTScan iteration number: " + str(cluster_rank)
    # ./SaTScanBatch64 + inputParamFile
    call(["/home/ferdinand/apps/SaTScan/SaTScanBatch64", inputParamFile])
    currentRes = open(resFile) # open res file at resPath    
    
    # 1.) Collecting cluster information
    if (cluster_rank == 1):
        ## At the first pass of SaTScan, we create the global output file
        ## TODO : Translate time intervals to datetime
        for line in currentRes:
            output.write(line) 
            
        output.close()
         
    # Collecting cluster info even for first cluster          
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
            # TODO : Verify that result files comes with time indicated in time_interval generic unit
            # To be adapted if result file processed
            cluster_startint = int(line[0])
            cluster_endint = int(line[1]) ## End interval is included in cluster timespan
               
    cluster_time_ids = range(cluster_startint,cluster_endint+1)
    cluster_loc_ids = map(int, cluster_loc_ids)
            
    if (cluster_rank != 1):
        tmp_output.write("\n")
    
    # 2.) Updating clusters location values in raw data at /xp_roads/Casgraph1110_d.cas
    updateCounts(cluster_loc_ids, cluster_time_ids, inputCaseFileNew)    
    
    cluster_rank += 1 

tmp_output.close()
    # replace counts of cluster locations ids in data

#%% ---------- Insert secondary cluster info in global output file -----------
print "Building global output file"
tmp_output = open(tmp_outputFile)

# Concatenating all results files
for line in fileinput.input(outputFile, inplace = 1):            
    if (line[0:5] == "Note:"):
        for tmp_line in tmp_output:
            print tmp_line.replace("\n","")
    print line.replace("\n","") 

# Converting time intervals to datetimes
for line in fileinput.input(outputFile, inplace = 1):            
    line1 = line
    if (line[2:12] == "Time frame"): # collect cluster timespan to update data      
        line1 = line1.split(":")[1]
        line1 = line1.replace(" ", "")
        line1 = line1.split("to")
        # TODO : Verify that result files comes with time indicated in time_interval generic unit
        # To be adapted if result file processed
        cluster_startint = int(line1[0])
        cluster_endint = int(line1[1])
        line1 = '  Time frame............: ' + intervalToDatetime(cluster_startint) + ' to ' + intervalToDatetime(cluster_endint) + '\n'
    print line1.replace("\n","") 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
