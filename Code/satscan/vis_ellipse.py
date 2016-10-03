# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import pandas as pd
from operator import itemgetter
import sys

resolution = sys.argv[1]
inputfile = sys.argv[2]

kml_filename = resolution + "elp"


#%% Extracting Clusters IDs
results = open(inputfile)

line = results.readline()
clusters_ids = []
cluster_index = 1
done = False

while (line[0:9] != 'Processor'): #untill the end of the result file

    # Go to line "1. Location IDs include"
    while (line[0] != str(cluster_index)):        
        print line        
        line = results.readline()
        if (line[0:9] == 'Processor'):
            done = True
            break

    if (done):
        break
    
    # Process first line
    cluster_ids = []           # temporary cluster_ids
    line = line.split(':')[1]  #kick the "Location IDs included"
    line = line.replace(" ","")
    line = line.replace("\n","")
    ids_list = line.split(',')
    if ('' in ids_list):
        ids_list.remove('')
    
    # Add ids to temporary cluster_ids
    while (line[0] != 'C'):       
        print line[0]
        cluster_ids = cluster_ids + ids_list
        line=results.readline()    
        line = line.replace(" ","")
        line = line.replace("\n","")  
        if (line[len(line)-1] == ','):
            line = line[:len(line)-1]     
        ids_list = line.split(',')
        
    cluster_index += 1
    clusters_ids.append(cluster_ids)

print "Clusters  IDs extracted"

#%% Get IDs locations

def formatting(test):
    return '{:06}'.format(int(test['cell_id']))

coord = pd.read_csv("/home/ferdinand/Documents/NYU/Data/satscan/experiments_elp/"+resolution+"/coordinates"+resolution+".csv", sep=", ", header = 0, engine = 'python')
coord['cell_id'] = coord.apply(formatting, axis=1)
clusters_ids_location = []

coord[coord['cell_id'] == '000001'].iloc[0,coord.columns.get_loc('latitude')]

# finding corresponding locations with loop - join would be better but ok not too heavy with loop
for i in range(len(clusters_ids)):
    cluster_ids_location = []
    for j in range(len(clusters_ids[i])):
        lati = coord[coord['cell_id'] == clusters_ids[i][j]].iloc[0,coord.columns.get_loc('latitude')]
        longi = coord[coord['cell_id'] == clusters_ids[i][j]].iloc[0,coord.columns.get_loc('longitude')]
        longilati = str(longi) + ',' + str(lati)
        longilatiPair = [longi,lati] 
        cluster_ids_location.append(longilatiPair)
    clusters_ids_location.append(cluster_ids_location)
    
#%% Drawing polygon clusters

clusters_bounds = []

dlongi = abs(coord[coord['cell_id'] == '000000'].iloc[0,coord.columns.get_loc('longitude')] - coord[coord['cell_id'] == '000001'].iloc[0,coord.columns.get_loc('longitude')])
dlati = abs(coord[coord['cell_id'] == '000000'].iloc[0,coord.columns.get_loc('latitude')] - coord[coord['cell_id'] == '001000'].iloc[0,coord.columns.get_loc('latitude')])
i=0
for i in range(len(clusters_ids_location)):
    cluster_bounds = []
    
    # longitude extremes
    longisort = sorted(clusters_ids_location[i], key = itemgetter(0,1))
    j=0
    print "cluster_size" + str(i) + ' is ' + str(len(clusters_ids_location[i]))
    
    while (j<len(clusters_ids_location[i])):
        print 'j = ' + str(j)        
        longipoint = longisort[j][0]
        cluster_bounds.append([longisort[j][0],longisort[j][1]-dlati])
        
        while(longipoint == longisort[j][0]):
            j +=1
            print "j++ to j = " + str(j)
            print " j / cluster_size: " + str(j) + " / " + str(len(clusters_ids_location[i]))
            print (j>=len(clusters_ids_location[i]))
            if (j>=len(clusters_ids_location[i])):
                print "break"
                break
            
        cluster_bounds.append([longisort[j-1][0],longisort[j-1][1]+dlati])
    
    #latitude extremes
    latisort = sorted(clusters_ids_location[i], key = itemgetter(1,0))
    j=0
    print "cluster_size" + str(i) + ' is ' + str(len(clusters_ids_location[i]))
    
    while (j<len(clusters_ids_location[i])):
        print 'j = ' + str(j)   
        latipoint = latisort[j][1]
        cluster_bounds.append([latisort[j][0]-dlongi,latisort[j][1]])
        
        while(latipoint == latisort[j][1]):
            j +=1
            if (j>=len(clusters_ids_location[i])):
                break
            
        cluster_bounds.append([latisort[j-1][0]+dlongi,latisort[j-1][1]])            
    
    clusters_bounds.append(cluster_bounds)
    
#%% Writing kml file

kml = open(inputfile + ".kml", "w")


# Header of kml file
kml.write("""<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>

	<Style id="high-rate-placemark"><IconStyle><Icon><href>https://maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href><scale>0.25</scale></Icon></IconStyle></Style>
	<Style id="low-rate-placemark"><IconStyle><Icon><href>https://maps.google.com/mapfiles/kml/shapes/placemark_circle.png</href><scale>0.25</scale></Icon></IconStyle></Style>

	<name>"""+kml_filename+"""</name>
 
""")

# Clusters writing
for i in range(1,len(clusters_ids)+1):
    
    kml.write("""	<Style id="cluster-""" + str(i) + """-style"><IconStyle><Icon></Icon></IconStyle><LabelStyle><scale>1.0</scale></LabelStyle><LineStyle><color>ffff0000</color></LineStyle><PolyStyle><color>40ff0000</color></PolyStyle><BalloonStyle><text><![CDATA[<b>$[snippet]</b><br/><table border="0"><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Time frame</th><td style="white-space:nowrap;">$[Time frame]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Number of cases</th><td style="white-space:nowrap;">$[Number of cases]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Expected cases</th><td style="white-space:nowrap;">$[Expected cases]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Observed / expected</th><td style="white-space:nowrap;">$[Observed / expected]</td></tr></table>]]></text></BalloonStyle></Style>
	<StyleMap id="cluster-""" + str(i) + """-stylemap"><Pair><key>normal</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair><Pair><key>highlight</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair></StyleMap>
    """)
    
    # Clusters Polygons
    
    kml.write("""	<Placemark>
		<name>""" + str(i) + """</name>
		<snippet>SaTScan Cluster #""" + str(i) + """</snippet>
		<visibility>1</visibility>
		<TimeSpan><begin>DATETIME / 2011-10-21T00:00:00Z</begin><end>DATETIME / 2011-10-30T23:59:59Z</end></TimeSpan>
		<styleUrl>#cluster-""" + str(i) + """-stylemap</styleUrl>
		<ExtendedData><Data name="Time frame"><value>DATE / 2011/10/21 to DATE / 2011/10/30</value></Data><Data name="Number of cases"><value>18332</value></Data><Data name="Expected cases"><value>TBC / 20887.47</value></Data><Data name="Observed / expected"><value>TBC / 0.88</value></Data></ExtendedData>
		<MultiGeometry>
			<Polygon><outerBoundaryIs><LinearRing><extrude>1</extrude><tessellate>1</tessellate><coordinates>""")
               
    # LOOP WRITING BOUNDARY
    for j in range(len(clusters_bounds[i-1])):
        longilati500 = str(clusters_bounds[i-1][j][0]) + ',' + str(clusters_bounds[i-1][j][1]) + ',500 '        
        kml.write(longilati500)
                   
               
    kml.write("""</coordinates></LinearRing></outerBoundaryIs></Polygon>
			<Point><extrude>1</extrude><altitudeMode>relativeToGround</altitudeMode><coordinates>-73.9857,40.7331,0</coordinates></Point>
		</MultiGeometry>
	</Placemark>  
    """)    # WHAT IS ALTITUDE???
    
    
    # Clusters IDs location folders
    kml.write("""<Folder><name>Cluster """ + str(i) + """ Locations</name><description></description>\n""")      
    
    for j in range(len(clusters_ids[i-1])):
        kml.write("			<Placemark><name>"+clusters_ids[i-1][j]+"</name><visibility>0</visibility><description></description><styleUrl>#low-rate-placemark</styleUrl><Point><coordinates>"+str(clusters_ids_location[i-1][j][0]) + ',' +str(clusters_ids_location[i-1][j][1]) + ",0</coordinates></Point></Placemark>\n") 
    kml.write("	</Folder>\n")

kml.write("""
</Document>
</kml>""")

kml.close()
