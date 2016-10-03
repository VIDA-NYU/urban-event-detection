# -*- coding: utf-8 -*-
"""
Created on Thu Aug 18 18:44:58 2016

@author: ferdinand

We compare the raw data in order to make sure that the processing was well made to get Telang data
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab

inputSatscan = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/Casgraph1110_h.cas"
inputTelang50 = "/home/ferdinand/Documents/NYU/Data/telang/ct_pd_grid1110_h_50.csv"
inputTelang50t= "/home/ferdinand/Documents/NYU/Data/telang/ct_pd_grid111025-31_h_50_t.csv"

caseSatscan = pd.read_csv(inputSatscan, names=["node_id","count","datetime"], sep = " ",index_col=False)
countsTelang50 = pd.read_csv(inputTelang50, header = 0)
countsTelang50t = pd.read_csv(inputTelang50t, header = 0)

n_time = caseSatscan['datetime'].unique().size
countsTelang50['datetime'].unique().size

n_satscan_cells = caseSatscan.shape[0]/n_time
n_telang_cells = countsTelang50.shape[0]/n_time
n_telangt_cells = countsTelang50t.shape[0]/n_time

np.sum(countsTelang50['count'])
np.sum(caseSatscan['count'])
np.sum(countsTelang50t['count'])

np.mean(countsTelang50['count'])
np.mean(caseSatscan['count'])
np.mean(countsTelang50t['count'])

data = [caseSatscan['count'], countsTelang50t['count'], countsTelang50['count']]
plt.boxplot(caseSatscan['count'])
plt.boxplot(countsTelang50t['count'])
plt.boxplot(countsTelang50['count'])
plt.boxplot(data)

#Histogram plot
num_bins = 50
# the histogram of the data
x = countsTelang50t['count']
y = caseSatscan['count']
plt.hist(x, num_bins, normed=1, facecolor='green', alpha=0.5)
plt.hist(y, num_bins, normed=1, facecolor='blue', alpha=0.5)
# add a 'best fit' line
plt.xlabel('Counts')
plt.ylabel('Probability')
# Tweak spacing to prevent clipping of ylabel
plt.subplots_adjust(left=0.15)

#%% CHECK OF UNIFORMITY OF GRID CELLS --> OK
coord = pd.read_csv("/home/ferdinand/Documents/NYU/Data/harish_grid_s_h/gridCoordinates_180_s.txt", names=["longi","lati"], sep = " ")

coord1 = coord.iloc[1:,:]
maxlong=np.max(coord1['longi'])
minlong=np.min(coord1['longi'])
maxlat=np.max(coord1['lati'])
minlat=np.min(coord1['lati'])
deltaLati = maxlat - minlat
deltaLongi = maxlong - minlong
grid_x_width= longToMeter(deltaLongi,minlat)
grid_y_height=latToMeter(deltaLati)


xres = coord.iloc[0,0]
yres = coord.iloc[0,1]

longi_step = coord.loc[3,'longi'] - coord.loc[2,'longi']
lati_step = coord.loc[2+xres,'lati'] - coord.loc[2,'lati']

cell_x_width = longToMeter(longi_step,coord.loc[2,'lati'])
cell_y_width = latToMeter(lati_step)
































