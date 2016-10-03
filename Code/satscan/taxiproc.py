# -*- coding: utf-8 -*-

import pandas as pd
import csv
import datetime

# Importing data
taxi = pd.read_csv('/media/ferdinand/F85C9BA65C9B5E66/Users/Ferdinand/Documents/NYU/Data/taxi1110.csv', header = 0)
print "taxi imported"
taxi_header = pd.read_csv('/media/ferdinand/F85C9BA65C9B5E66/Users/Ferdinand/Documents/NYU/Data/taxi.header', header = None)
print "taxi_headers imported"
taxi_header = taxi_header.iloc[0,:]
taxi.columns = taxi_header
taxib = taxi
print taxib.iloc[0:20,:]
taxi.iloc[0:5,:]

# Converting time in %Y-%m-%d %H:%M:%S format
taxib['pickup_time'] = pd.to_datetime(taxib['pickup_time'],unit='s')
index = pd.DatetimeIndex(taxib['pickup_time'])

# Select a day and an area
taxib = taxib.loc[((index.year == 2011) & (index.month <= 10)) 
    & (taxib['pick_x'] >= -74.021029) & (taxib['pick_x'] <= -73.929364)
    & (taxib['pick_y'] >= 40.720566) & (taxib['pick_y'] <= 40.787866)]

# Select 500 rows
taxi500b = taxib.iloc[0:500,:]
taxi500b.to_csv('/home/vgc/flegros/Data/taxi500b.csv', header = True)