# -*- coding: utf-8 -*-
"""
Created on Tue Aug 16 16:26:02 2016

@author: ferdinand
"""

import sys
import pandas as pd
import numpy as np

gridRes = sys.argv[1]
threshold = float(sys.argv[2])
startDay = int(sys.argv[3])
endDay = int(sys.argv[4]) # included
dirInputPath = sys.argv[5]
dirOutputPath = sys.argv[6]
sml = sys.argv[7]

#gridRes = 200
#startDay = 25
#endDay = 31
#Local
#dirInputPath = '/home/ferdinand/Documents/NYU/Data/telang/'
#dirOutputPath = dirPath
#Remote
#dirInputPath = dirOutputPath = '~/telang/'

s = ""
if (sml == 'small'):
	s = "_s"

#%% THRESHOLD ON MONTHLY COUNT VALUE

counts_orig = pd.read_csv(dirInputPath + "ct_pd_grid1110_h_" + str(gridRes) + s + ".csv", sep = ",",index_col=False,dtype=str, header = 0)     
counts= counts_orig

print "Csv read"

counts['count'] = pd.to_numeric(counts['count'], errors='ignore')

counts1 = counts.groupby(['cell_id'], as_index=False) 
counts1 = counts1['count'].aggregate(np.sum)

counts2 = counts1
counts1['count'] = counts1['count'] / np.mean(counts1['count'])
counts2 = counts2[counts1['count'] > threshold]
keep_cells = counts2['cell_id'].tolist()

print "Counts aggregated"

counts_kept = counts[counts['cell_id'].isin(keep_cells)]

print "Aggregated counts written"

#%% THRESHOLD ON DATES - TAKE HALLOWEEN PARADE WEEK --> 25 to 31

day = counts_kept['datetime'].str.split('-').str.get(2).astype(int)
print "Days computed"
counts_kept = counts_kept[(day >= startDay) & (day <= endDay)]

# Set time_intervals from 0
counts_kept['time_interval'] = pd.to_numeric(counts['time_interval'])
counts_kept['time_interval'] = counts_kept['time_interval'] - (startDay-1)*24

counts_kept.to_csv(dirOutputPath + "ct_pd_grid1110" + '{:02}'.format(startDay) + '-' + '{:02}'.format(endDay) + "_h_"+str(gridRes)+s+"_t.csv", sep=',',header=True, index=False)

print "Threshold & Date bounded file written"
