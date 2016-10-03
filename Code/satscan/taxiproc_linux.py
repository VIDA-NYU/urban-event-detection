# -*- coding: utf-8 -*-

import pandas as pd

# Importing data
taxi = pd.read_csv('/home/ferdinand/Documents/NYU/Data/raw/taxi1110b.csv', header = 0)
print "taxi imported"
taxi_header = pd.read_csv('/home/ferdinand/Documents/NYU/Data/raw/taxi.header', header = None, usecols=[0,5,1,2])
#print "taxi_headers imported"
taxi_header = taxi_header.iloc[0,[0,3,1,2]]
taxi.columns = taxi_header

#taxib = taxi
#print taxib.iloc[0:20,:]
#taxi.iloc[0:5,:]

# Converting time in %Y-%m-%d %H:%M:%S format
taxib['pickup_time'] = pd.to_datetime(taxib['pickup_time'],unit='s')
index = pd.DatetimeIndex(taxib['pickup_time'])

# Select a day and an area -- Useless??
taxib = taxib.loc[((index.year == 2011) & (index.month == 10)) 
    & (taxib['pick_x'] >= -74.021029) & (taxib['pick_x'] <= -73.929364)
    & (taxib['pick_y'] >= 40.720566) & (taxib['pick_y'] <= 40.787866)]

# Select 500 rows --> USELESSa
taxi500b = taxib.iloc[0:500,:]
taxi500b.to_csv('/home/vgc/flegros/Data/taxi500b.csv', header = True)

#%% Find area: max and min lat/long covered on raw data
minlongiraw = taxi.loc[:,"pick_x"].min()
maxlongiraw = taxi.loc[:,"pick_x"].max()
minlatiraw = taxi.loc[:,"pick_y"].min()
maxlatiraw = taxi.loc[:,"pick_y"].max()

# Loading grided csv files: coordinates1500, coordinates800...

coord1500 = pd.read_csv('/home/ferdinand/Documents/NYU/Data/satscan/experiments/1500/Coordinates1500.geo', sep=' ', header = None)

minlongi1500 = coord1500.iloc[:,2].min()
maxlongi1500= coord1500.iloc[:,2].max()
minlati1500 = coord1500.iloc[:,1].min()
maxlati1500 = coord1500.iloc[:,1].max()

# Coordinates 800
coord800 = pd.read_csv('/home/ferdinand/Documents/NYU/Data/satscan/experiments/800/Coordinates800.geo', sep=' ', header = None)

minlongi800 = coord800.iloc[:,2].min()
maxlongi800= coord800.iloc[:,2].max()
minlati800 = coord800.iloc[:,1].min()
maxlati800 = coord800.iloc[:,1].max()

# Coordinates 200
coord200 = pd.read_csv('/home/ferdinand/Documents/NYU/Data/satscan/experiments/200/Coordinates200.geo', sep=' ', header = None)

minlongi200 = coord200.iloc[:,2].min()
maxlongi200= coord200.iloc[:,2].max()
minlati200 = coord200.iloc[:,1].min()
maxlati200 = coord200.iloc[:,1].max()

# Coordinates 10
coord10 = pd.read_csv('/home/ferdinand/Documents/NYU/Data/satscan/experiments/10/Coordinates10.geo', sep=' ', header = None)

minlongi10 = coord10.iloc[:,2].min()
maxlongi10= coord10.iloc[:,2].max()
minlati10 = coord10.iloc[:,1].min()
maxlati10 = coord10.iloc[:,1].max()


   












