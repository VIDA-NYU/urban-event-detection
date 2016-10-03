# -*- coding: utf-8 -*-
"""
Created on Fri Aug 19 13:34:26 2016

@author: ferdinand

Converts lat long to meters
"""

import math

R_earth = 6371000

# Convert a delta degree longitude to meters
def latToMeter(deg):
    return(2*math.pi*R_earth*deg/360)

# Convert a delta degree latitude to meters
# lat should be indicated in degrees
def longToMeter(deg,lat):
    return(2*math.pi*R_earth*math.cos(lat*math.pi/180)*deg/360)
    

deltaLong = 74.027209-74.026027
deltaLat = 40.701643 - 40.700748


deltaX250 = longToMeter(deltaLong, 40.703627)
deltaY250 = latToMeter(deltaLat)

deltaX100 = longToMeter(deltaLong, 40.700748)
deltaY100 = latToMeter(deltaLat)

theta4 = math.atan(deltaX/deltaY) #output in rad
theta4 = theta4*180/math.pi