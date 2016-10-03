# -*- coding: utf-8 -*-
"""
Created on Mon Aug 22 12:02:55 2016

@author: ferdinand
"""

import pandas as pd

gridRes = 300
inputInBoundsFile = '/home/ferdinand/Documents/NYU/Data/harish_grid_h/inBounds_'+str(gridRes)+'.txt'
inputCoordFile = '/home/ferdinand/Documents/NYU/Data/harish_grid_h/coord_grid_'+str(gridRes)+'.csv'
outputKmlFile = '/home/ferdinand/Documents/NYU/Data/harish_grid_h/grid_'+str(gridRes)+'.kml'

coord = open(inputCoordFile)
inBounds = pd.read_csv(inputInBoundsFile, names=['longitude','latitude','inManhattan'])
inBounds = inBounds[inBounds['inManhattan'] == True]
inBounds = inBounds.reset_index(drop=True)

#Skipping first useless line
coord.readline()
line = coord.readline()

#%% Writing kml file

kml = open(outputKmlFile, "w")


# Header of kml file
kml.write("""<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>

	<Style id="high-rate-placemark"><IconStyle><Icon><href>https://maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href><scale>0.25</scale></Icon></IconStyle></Style>
	<Style id="low-rate-placemark"><IconStyle><Icon><href>https://maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href><scale>0.25</scale></Icon></IconStyle></Style>

	<name>Grid_"""+str(gridRes)+"""</name>
 
""")
   
# Raw Grid points
kml.write("""<Folder><name>Raw Grid Points</name><description></description>\n""")      

while (line!= ''):
    longi = float(line.split(',')[1])
    lati = float(line.split(',')[2])
    line = coord.readline()
    kml.write("			<Placemark><name></name><visibility>0</visibility><description></description><styleUrl>#low-rate-placemark</styleUrl><Point><coordinates>"+str(longi) + ',' +str(lati) + ",0</coordinates></Point></Placemark>\n") 
kml.write("	</Folder>\n")

## Processed grid
kml.write("""<Folder><name>Processed Grid Points</name><description></description>\n""")
for i in range(inBounds.shape[0]):
    longi = float(inBounds.loc[i,'longitude'].item())
    lati = float(inBounds.loc[i,'latitude'].item())
    kml.write("			<Placemark><name></name><visibility>0</visibility><description></description><styleUrl>#low-rate-placemark</styleUrl><Point><coordinates>"+str(longi) + ',' +str(lati) + ",0</coordinates></Point></Placemark>\n") 
kml.write("	</Folder>\n")

## Footer of kml file

kml.write("""
</Document>
</kml>""")

kml.close()

print "KML file written"