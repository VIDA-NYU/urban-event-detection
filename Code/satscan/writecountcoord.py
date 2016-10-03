# -*- coding: utf-8 -*-
import sys
import pandas as pd
import numpy as np

inputnb = sys.argv[1]

# Can be automatized
taxi = open('/home/ferdinand/Documents/NYU/Data/Fabio/' + inputnb + '/taxi2_DAY_1.scalars')
ncells = taxi.readline().split(',') 
nx = int(ncells[0].replace("\n",""))
ny = int(ncells[1])
ndays = 31

#%% ------------- Write GRAPH coordinates file -----------------
coord_input = open("/home/ferdinand/Documents/NYU/Data/harish/coord_graph.txt")
coord_output = open("/home/ferdinand/Documents/NYU/satscan/xp_roads/coord_graph.csv", "w")
coord_output.write("node_id,latitude,longitude\n")

nodeid = 0
n_nodes = int(coord_input.readline().split(" ")[0])

for i in range(n_nodes):
    latilongi = coord_input.readline().split(" ")
    coord_output.write(str(nodeid) + "," + latilongi[0] + "," + latilongi[1])
    nodeid += 1

coord_output.close()
line = coord_input.readline()


#%%-------------- Write GRAPH counts file -----------------------

nhours=24
ndays=31

# ------ hourly aggregation -------
ctgraph1110_h = open("/home/ferdinand/Documents/NYU/satscan/xp_roads/ctgraph1110_h.csv", "w")
ctgraph1110_h.write("node_id,count,datetime\n")

for d in range(1,ndays+1):
    
    for h in range(nhours):
        
        datetime = '2011-10-' + '{:02}'.format(d) + '-' + '{:02}'.format(h)
        count = open('/home/ferdinand/Documents/NYU/Data/harish/oct-2011/scalar-2011-10-' + str(d) + '-' + str(h) + '.txt')
        node_id = 0        
        
        for i in range(n_nodes):
            ct = str(int(round(float(count.readline().replace("\n","")))))
            ctgraph1110_h.write(str(node_id) + ',' + ct + ',' + datetime +'\n')
            node_id +=1

ctgraph1110_h.close()

# ------ time precision and aggregation ----------

ctg1110_h = pd.read_csv("/home/ferdinand/Documents/NYU/satscan/xp_roads/ctgraph1110_h.csv", sep = ',', header = 0)

# processing date and time
ctg1110_h['date'] = ctg1110_h['datetime'].str[:10]
ctg_datetime = ctg1110_h['datetime'].str.split('-')
ctg1110_h['year'] = pd.to_numeric(ctg_datetime.str.get(0))
ctg1110_h['month'] = pd.to_numeric(ctg_datetime.str.get(1))
ctg1110_h['day'] = pd.to_numeric(ctg_datetime.str.get(2))
ctg1110_h['hour'] = pd.to_numeric(ctg_datetime.str.get(3))


# DAY - aggregating per day and node_id
ctg1110_d = ctg1110_h.groupby(['node_id','date','year','month','day'], as_index=False).sum()
ctg1110_d = ctg1110_d[['node_id','count','date','year','month','day']]
ctg1110_d = ctg1110_d.sort(['year','month','day','node_id'])


# writing in counts file
ctg1110_d.to_csv('/home/ferdinand/Documents/NYU/satscan/xp_roads/ctgraph1110_d.csv', sep=',',header=True,columns=['node_id','count','date'], index=False)

### -- HOUR - generic time setting for hour time precision --

# y0 = 2011
# m0 = 10

# Simple version with only October - Watch for irregular number of days each month if you do the whole year !!
# Here we just skip years and month

ctg1110_h['t_generic'] = (ctg1110_h['day']-1)*24 + ctg1110_h['hour']

# writing in counts file
ctg1110_h.to_csv('/home/ferdinand/Documents/NYU/satscan/xp_roads/ctgraph1110_h.csv', sep=',',header=True,columns=['node_id','count','t_generic','datetime'], index=False)

# Reverse formulas
reverse_hour = int(ctg1110_h['generic'] % 24)
reverse_day = int((((ctg1110_h['generic'] - reverse_hour)/24) % 31) + 1)

# Generic time bounds
ctg1110_h['t_generic'][0]  #min
ctg1110_h['t_generic'][len(ctg1110_h)-1]  #max

#%%-------------- Write GRID coordinates file ------------------

# Write file
coordinates = open("/home/ferdinand/Documents/NYU/Data/satscan/experiments2b/"+ inputnb + "/coordinates" + inputnb + ".csv", "w")
coordinates.write("cell_id, latitude, longitude \n")

taxi2 = open('/home/ferdinand/Documents/NYU/Data/Fabio/'+inputnb+'/taxi2_DAY_1.scalars')
taxi2.readline()

grid_coord = taxi2.readline().split(',') 
tl_lati = float(grid_coord[2])
tl_longi = float(grid_coord[1])
br_lati = float(grid_coord[0])
br_longi = float(grid_coord[3])

lati = tl_lati
longi = tl_longi
lati_step = -abs(tl_lati - br_lati)/ny
longi_step = abs(br_longi - tl_longi)/nx

# we take the coordinates of the top left corner of each grid cell
for j in range(0,ny):
        for i in range(0,nx):
            coordinates.write('{:03}'.format(j) + '{:03}'.format(i) + ', '
                                    + str(lati) + ', ' + str(longi) + '\n')
            longi += longi_step
        longi = tl_longi
        lati += lati_step
        

coordinates.close()

#%%--------------- Write GRID counts file ------------------

cases = 0

# Write file
counts1110 = open("/home/ferdinand/Documents/NYU/Data/satscan/experiments2b/"+inputnb+"/counts1110_"+inputnb+".csv", "w")
counts1110.write("cell_id, count, date \n")

for k in range(1,ndays+1):
    date = '2011/10/'+ '{:02}'.format(k)    
    taxi = open('/home/ferdinand/Documents/NYU/Data/Fabio/' + inputnb + '/taxi2_DAY_'+ str(k) + '.scalars')

    for m in range(0,3):
        taxi.readline()  

    for j in range(0,ny):
        for i in range(0,nx):
            count = int(float(taxi.readline().replace("\n","")))   
            counts1110.write('{:03}'.format(j) + '{:03}'.format(i) + ', ' 
            + str(count) + ', ' + date + '\n')
            cases += count

counts1110.close()
print count

#%% Extracting max/min lat/long for GRAPH data

coord_graph = pd.read_csv("/home/ferdinand/Documents/NYU/satscan/xp_roads/coord_graph.csv", sep = ',', header =0)
