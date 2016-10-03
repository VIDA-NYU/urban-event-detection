# -*- coding: utf-8 -*-
"""
This file converts the result file of SaTSCan with generic datetime into another one with yy-mm-dd-hh format
"""

import sys

inputfile = sys.argv[1]
outputfile = sys.argv[2]

#inputfile = "/home/ferdinand/Documents/NYU/satscan/xp_roads/resg1110_h_01"
#outputfile = "/home/ferdinand/Documents/NYU/satscan/xp_roads/resg1110_h_01_ymdh"

# Conversion function from t_generic (int) to yyyy-mm-dd-hh (string)
def generic_to_ymdh(t_generic):
    y = 2011
    m = 10
    h = t_generic % 24
    d = (((t_generic - h)/24) %  31) + 1
    ymdh = str(y) + '/' + str(m) + '/' + str(d) + '-' + str(h)
    return ymdh


# Processing wriing file
res = open(inputfile)
res_ymdh = open(outputfile,"w")

line = res.readline()
print line
done = False

cluster_index = 1

while (line[0:9] != 'Processor'): #untill the end of the result file
    
    while (not('Time frame' in line)): 
        res_ymdh.write(line)        
        line = res.readline()
        if (line[0:9] == 'Processor'):
            done = True
            break    
    
    if (done):
        break
    
    timeframe = line.replace(' ','').replace('\n','').split(':')[1].split('to')  
    timeframe = generic_to_ymdh(int(timeframe[0])) + ' to ' + generic_to_ymdh(int(timeframe[1]))
    
    if (cluster_index != 10):    
        newline     = "  Time frame............: " + timeframe + "\n"
    else:
        newline     = "   Time frame............: " + timeframe + "\n"
    res_ymdh.write(newline) # replacing line with ymdh time format

    cluster_index += 1
    
    line = res.readline()

res_ymdh.write(line)
    
print "Time frames converted from generic to yyyy-mm-dd-hh"

6
