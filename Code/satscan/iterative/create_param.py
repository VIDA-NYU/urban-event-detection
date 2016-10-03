# -*- coding: utf-8 -*-
"""
Created on Fri Aug 12 18:28:05 2016

@author: ferdinand
"""

import sys



#for maxSize in [5000,10000,25000]:

maxSize = 2000
for maxTemp in [3,7,24]:

    inputFile = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/parameters/param1110_h_elp_none.prm'
    outputFile = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/parameters/param1110_h_elp_' + str(maxSize/1000) + 'k_none_'+str(maxTemp)+'h_1c.prm'       
    
    
    inputf = open(inputFile)
    outputf = open(outputFile,'w')
    
    for line in inputf: 
    
        if (line[0:len('ResultsFile=')] == 'ResultsFile='):    
            outputf.write(line.replace('TBD','res1110_h_elp_'+ str(maxSize/1000) + 'k_none_'+str(maxTemp)+'h_1c'))
        
        elif (line[0:len('MaxSpatialSizeInDistanceFromCenter=')] == 'MaxSpatialSizeInDistanceFromCenter='):
            outputf.write(line.replace('TBD',str(maxSize)))
            
        elif (line[0:len('MaxTemporalSize=')] == 'MaxTemporalSize='):
            outputf.write(line.replace('TBD',str(maxTemp)))
            
        else:
            outputf.write(line)
            
    outputf.close()
