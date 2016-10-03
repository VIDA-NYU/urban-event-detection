# -*- coding: utf-8 -*-
"""
Created on Wed Aug 10 14:13:37 2016

@author: ferdinand

This script transform harish grid multiple txt files to input for Telang processing code

the outputted file is a txt file RRRCCC / time_interval / datetime / count

"""
import sys
import pandas as pd

#gridRes = 50
gridRes = sys.argv[1]
#dirPath = sys.argv[2]


dirPath = '/home/ferdinand/Documents/NYU/Data/harish_grid_s_h/'


#%% ------------- Write HARISH_GRID_HOUR coordinates file -----------------
coord_input = open(dirPath + "gridCoordinates_" +str(gridRes) + "_s.txt")
coord_output = open(dirPath + "coord_grid_"+ str(gridRes) + "_s.csv", "w")
coord_output.write("cell_id,longitude,latitude\n")

# Output file has format: RRRCCC / longitude / latitude
# row index RRR increases with latitude
# column index CCC increases with longitude

nxny = coord_input.readline().split(" ")

nx = int(nxny[0])
ny = int(nxny[1])

## SPATIAL ROW ORDER -- CAUTION DO NOT GET CONFUSED
# i designates the index of spatial ROWS --> increases with latitude
# j designates the index of spatial COLUMNS --> increases with longitude

for i in range(ny):
    for j in range(nx):
        cell_id = '{:03}'.format(i) + '{:03}'.format(j)        
        longilati = coord_input.readline().split(" ")
        coord_output.write(str(cell_id) + "," + longilati[0] + "," + longilati[1])

coord_output.close()
print "Coord file written"

#%%-------------- Write HARISH_GRID_HOUR pickups counts file -----------------------

nhours=24
ndays=31

# ------ hourly aggregation -------
ct_p_grid1110_h = open(dirPath + "ct_p_grid1110_h_"+str(gridRes)+"_s.csv", "w")
ct_p_grid1110_h.write("cell_id,time_interval,datetime,count\n")

# Output file has format: RRRCCC / time_interval / datetime / count
# time_interval: index of the hour with Oct 1st 2011 midnight has index 0, Oct 1st 2011 1am has index 1...
# datetime in format YYYY-MM-DD-HH

# Maybe computational complexity could be cut by rounding the floats of density? Do NOT round to closest integer because values are around 0 - 10

time_interval = 0

for d in range(1,ndays+1):
    
    for h in range(nhours):
        
        datetime = '2011-10-' + '{:02}'.format(d) + '-' + '{:02}'.format(h)
        count = open(dirPath + 'pick-density_'+str(gridRes)+ '/density-2011-10-' + str(d) + '-' + str(h) + '.txt')      
        
        # SPATIAL ROW MAJOR ORDER
        for i in range(ny):
            for j in range(nx):
                cell_id = '{:03}'.format(i) + '{:03}'.format(j)        
                ct = float(count.readline().replace("\n",""))
                ct_p_grid1110_h.write(str(cell_id) + ',' + str(time_interval) + ',' + datetime + ',' + str(ct) +'\n')
        
        time_interval+=1
        
ct_p_grid1110_h.close()

print "Pickups count file written"

#%%-------------- Write HARISH_GRID_HOUR drop-offs counts file -----------------------

nhours=24
ndays=31

# ------ hourly aggregation -------
ct_d_grid1110_h = open(dirPath + "ct_d_grid1110_h_"+str(gridRes)+"_s.csv", "w")
ct_d_grid1110_h.write("cell_id,time_interval,datetime,count\n")

# Output file has format: RRRCCC / time_interval / datetime / count
# time_interval: index of the hour with Oct 1st 2011 midnight has index 0, Oct 1st 2011 1am has index 1...
# datetime in format YYYY-MM-DD-HH

# Maybe computational complexity could be cut by rounding the floats of density? Do NOT round to closest integer because values are around 0 - 10

time_interval = 0

for d in range(1,ndays+1):
    
    for h in range(nhours):
        
        datetime = '2011-10-' + '{:02}'.format(d) + '-' + '{:02}'.format(h)
        count = open(dirPath + 'drop-density_'+str(gridRes)+ '/density-2011-10-' + str(d) + '-' + str(h) + '.txt')      
        
        # SPATIAL ROW MAJOR ORDER
        for i in range(ny):
            for j in range(nx):
                cell_id = '{:03}'.format(i) + '{:03}'.format(j)        
                ct = float(count.readline().replace("\n",""))
                ct_d_grid1110_h.write(str(cell_id) + ',' + str(time_interval) + ',' + datetime + ',' + str(ct) +'\n')
        
        time_interval+=1
        
ct_d_grid1110_h.close()

print "Drop-offs count file written"

#%%-------------- Write HARISH_GRID_HOUR pickups+drop-offs counts file---------------

ct_p_grid1110_h= pd.read_csv(dirPath + "ct_p_grid1110_h_"+str(gridRes)+"_s.csv",header = 0)
ct_d_grid1110_h= pd.read_csv(dirPath + "ct_d_grid1110_h_"+str(gridRes)+"_s.csv", header = 0)

ct_pd_grid1110_h = ct_p_grid1110_h
ct_pd_grid1110_h['cell_id'] = pd.Series(['{:06}'.format(val) for val in ct_p_grid1110_h['cell_id']], index = ct_pd_grid1110_h.index)
ct_pd_grid1110_h['count'] = ct_p_grid1110_h['count'] + ct_d_grid1110_h['count']

ct_pd_grid1110_h.to_csv(dirPath + "ct_pd_grid1110_h_"+str(gridRes)+"_s.csv", sep=',',header=True, index=False)

print "Pickups + Drop-offs count file written"

