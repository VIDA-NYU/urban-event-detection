# -*- coding: utf-8 -*-
"""
Created on Fri Aug 12 18:28:05 2016

@author: ferdinand
"""

import pandas as pd
import numpy as np

# Replace lines corresponding to ST cluster in raw data
def updateCounts(cluster_loc_ids, cluster_time_ids, inputCaseFile):
    counts = pd.read_csv(inputCaseFile, names=["node_id","count","datetime"], sep = " ",index_col=False)  
    counts['day'] = pd.to_numeric(counts['datetime'].str.split('-').str.get(2))
    
    # Now implement calculation
    c_SpTp = np.sum(counts.loc[~counts['node_id'].isin(cluster_loc_ids) & ~counts['day'].isin(cluster_time_ids), 'count'])
    
    for i in cluster_loc_ids:
        for t in cluster_time_ids:
            c_iTp= np.sum(counts.loc[(counts['node_id'] == i) & (~counts['day'].isin(cluster_time_ids)), 'count'])
            c_Spt= np.sum(counts.loc[(~counts['node_id'].isin(cluster_loc_ids)) & (counts['day']==t),'count'])
            
            c_iT= np.sum(counts.loc[(counts['node_id'] == i), 'count'])
            c_St= np.sum(counts.loc[(counts['day']==t),'count'])
            c_ST = np.sum(counts['count'])       
            
            newCount = (c_iTp*c_Spt)/ c_SpTp
            baseline_old = (c_iT*c_St)/ c_ST
            rowIndex = counts[(counts['node_id'] == i) & (counts['day']==t)].index[0]        
                    
            print "Node_id = " + str(i) + " / time_interval = " + str(t)
            print "c_iTp = " + str(c_iTp)
            print "c_Spt = " + str(c_Spt)
            print "old Count = " + str(counts.iloc[rowIndex]['count'])
            print "old baseline = " + str(baseline_old)
            print "newCount = " + str(newCount)
            print "\n"              
            
            counts = counts.set_value(rowIndex,'count',newCount)
    
    # Write to csv
    del counts['day']
    counts.to_csv(inputCaseFile, header = False, sep = " ", index = False)
