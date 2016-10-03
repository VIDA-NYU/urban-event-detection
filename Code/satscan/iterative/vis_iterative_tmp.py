# -*- coding: utf-8 -*-
"""
Created on Wed Aug 24 18:15:34 2016

@author: ferdinand
Outputs
i.) Global results file // telang
ii.) kml file to display clusters
"""

import pandas as pd
import numpy as np
import re
import sys

k = int(sys.argv[1])
refOutput = sys.argv[2]
inputFolder = sys.argv[3]
inputCoordFolder = sys.argv[4]

#Remote
#inputFolder = ~/satscan/iterative/sit_2k_3h/
#refOutput = 2k_3h
#inputCoordFolder = ~/satscan/iterative/???

#Local
#k = 10
#inputFolder = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/sit_2k_3h/'
#inputCoordFoler = '/home/ferdinand/Documents/NYU/satscan/xp_roads/coord_graph.csv'
#refOutput = '2k_3h'

#%% BUILD GLOBAL RES FILE 

outputFileSummary = inputFolder + 'summary_' + refOutput + '.txt'
res = open(inputFolder + 'res')
outputRes = open(outputFileSummary,'w')

line = res.readline()
print line

#Writing header with initial res --> manually check the last line
for j in range(19):
    outputRes.write(line)
    line = res.readline()
    print "Writing header"
    
print "\n"
print "\n"

# writing res_i contents
for i in range(1,k+1):
    print "Processing res_" + str(i)
    res_tmp = open(inputFolder + 'res_' + str(i))
    for line_tmp in res_tmp:
        outputRes.write(line_tmp)
    outputRes.write('\n')
    print "res_" + str(i) + "written"

# Going to footer in initial res
#while(line[0:len('Note: As')] !='Note: As'):
#    line = res.readline()
#    print "Going to footer"

print "\n"
print "\n"


# Writing footer
#while (line != ''):
#    outputRes.write(line)
#    line = res.readline()

#Writing footer --> adapted to vis_ellipse_graph_h2.py
outputRes.write('Processor :')

res.close()
outputRes.close()

    





#%% BUILD KML FILE
