# -*- coding: utf-8 -*-
"""
Created on Fri Jun 10 12:33:30 2016

@author: ferdinand
"""

import pandas as pd
import numpy as np
import sys

infile = sys.argv[1]
outfile = sys.argv[2]

#infile = '/home/ferdinand/Documents/NYU/Data/satscan/experiments2b/1000/coordinates1000.csv'
#outfile = '/home/ferdinand/Documents/NYU/Data/satscan/experiments2b_ellipses/1000/coordinates1000_utm.csv'

#%% Function definition

def formatting(test):
    return '{:06}'.format(int(test['cell_id']))

def latlong_to_xutm( coordinates ):
   longi = coordinates['longitude']  
   x=((longi+180)/360.0*256)*np.exp2(WORLD_ZOOM_LEVEL)
   return x
   
def latlong_to_yutm(coordinates):
   lati = coordinates['latitude']
   if (lati == 90.0):
       y = 256
   elif (lati == -90.0):
       y=0
   else:
       y = (np.pi - np.arctanh(np.sin(lati*np.pi/180)))/np.pi*128   
   y= y*np.exp2(WORLD_ZOOM_LEVEL)
   return y
   
#%% Converting coordinates from lat/long to utm

coord = pd.read_csv(infile, sep=', ', header = 0, engine = 'python')

coord['cell_id'] = coord.apply(formatting, axis=1)

WORLD_ZOOM_LEVEL = 22 #?? no unit?

coord['x_utm'] = coord.apply(latlong_to_xutm, axis=1)
coord['y_utm'] = coord.apply(latlong_to_yutm, axis=1)

coord = coord.drop(['latitude','longitude'],1)

coord.to_csv(outfile, header = True)

