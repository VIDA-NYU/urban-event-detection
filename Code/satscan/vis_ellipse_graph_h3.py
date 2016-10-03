# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""

import pandas as pd
from operator import itemgetter
import numpy as np
import sys


outputRef = sys.argv[1]
coord_input = sys.argv[2]
k = int(sys.argv[3])

kml_filename = 'sit_' + outputRef + '_' + str(k) + 'c'

outputDirPath = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/sit_'+ outputRef +'/'
inputfile = '/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/iterative/sit_'+ outputRef+ '/summary_' + outputRef+ '.txt'

#Local regular satscan res file
#inputfile = "/home/ferdinand/Documents/NYU/satscan/xp_roads_elp/hourly/resg1110_h_elp_2k_none_3_ymdh"
#coord_input = "/home/ferdinand/Documents/NYU/satscan/xp_roads/coord_graph.csv"
#kml_filename = "graph_h_elp_2k_none_3"

#Local iterative satscan res file
#outputRef = '10k_3h'

#coord_input = "/home/ferdinand/Documents/NYU/satscan/xp_roads/coord_graph.csv"


#%% Extracting Clusters IDs
results = open(inputfile)

line = results.readline()
clusters_ids = []
clusters_timeframe = []
clusters_obscount = []
clusters_expcount = []
clusters_obsexpratio = []
clusters_teststatistic = []
cluster_index = 1
done = False

while (line[0:9] != 'Processor'): #until the end of the result file

    # Go to line "1. Location IDs include"
    while ((line[0] != str(cluster_index)) & (line[0:2] != str(cluster_index))):              
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
        cluster_ids = cluster_ids + ids_list
        line=results.readline()    
        line = line.replace(" ","")
        line = line.replace("\n","")  
        if (line[len(line)-1] == ','):
            line = line[:len(line)-1]     
        ids_list = line.split(',')
        
    while (line[0] != 'T'):       
        line = results.readline().replace(" ","")
    
    timeframe = line.split(':')[1]
    timeframe = timeframe.split('to')[0] + ' to ' + timeframe.split('to')[1]
    timeframe = timeframe.replace("/","-")
    clusters_timeframe.append(timeframe)   
    line = results.readline().replace(" ","")    
    
    # Get observed counts
    while (not('Number' in line)):       
        line = results.readline().replace(" ","")
        
    obscount = line.split(':')[1].replace(" ","")
    obscount = obscount.replace("\n","")
    clusters_obscount.append(obscount)        
    
    line = results.readline().replace(" ","")    

    # Get ratio obs/exp counts
    while (not('Observed' in line)):  
        line = results.readline().replace(" ","")
       
    obsexpratio = line.split(':')[1].replace(" ","")
    obsexpratio = obsexpratio.replace("\n","")
    clusters_obsexpratio.append(obsexpratio)  
    
    line = results.readline().replace(" ","")    
    
    # Get test statistic
    while (line[0:4] != 'Test'):       
        line = results.readline().replace(" ","")
        
    teststatistic = line.split(':')[1].replace(" ","")
    teststatistic = teststatistic.replace("\n","")
    clusters_teststatistic.append(teststatistic)
        
    cluster_index += 1
    clusters_ids.append(cluster_ids)

print "Clusters  IDs / Timeframe / Test statistc / Number of cases / Ratios extracted"

#%% Write summary file FOR ITERATIVE ONLY

def frameToTimespan(timeframe):
    time_frame = timeframe.replace('\n', '')
        
    start = (time_frame.split(' to ')[0]).split('-')[2]
    startDay = int(start.split('_')[0])
    startHour = int(start.split('_')[1])
    
    end = (time_frame.split(' to ')[1]).split('-')[2]
    endDay = int(end.split('_')[0])
    endHour = int(end.split('_')[1])
    
    return ((endDay-1)*24 + endHour) - (((startDay-1)*24) + startHour) +1
    
    
## Computing indicators
mean_size = 0
for i in range(k):
    mean_size += len(clusters_ids[i])
mean_size= mean_size / float(k)

mean_timespan = 0
for i in range(k):
    mean_timespan += frameToTimespan(clusters_timeframe[i])
mean_timespan= mean_timespan / float(k)

mean_teststatistic =0
for i in range(k):
    mean_teststatistic += float(clusters_teststatistic[i])
mean_teststatistic = mean_teststatistic / float(k)

hl_ratio = 0
for i in range(k):
    if float(clusters_obsexpratio[i]) >=1:
        hl_ratio += 1
hl_ratio = hl_ratio/float(k)


## Writing summary file
summary = open(outputDirPath+'sum_' + outputRef + '_'+str(k)+'c.txt', 'w')

summary.write("Iterative SaTScan anomaly result file for " + outputRef + "\n")
summary.write("Number of clusters reported: " + str(k) + "\n")
summary.write("Space size Mean: " + str(mean_size) + "\n")
summary.write("Timespan Mean: " + str(mean_timespan) + "\n")
summary.write("Test statistic Mean: " + str(mean_teststatistic) + "\n")
summary.write("High counts anomaly / Total anomalies: " + str(hl_ratio) + '\n')
summary.write("\n\n")

summary.close()


#%% Get IDs locations

coord = pd.read_csv(coord_input, sep=",", header = 0, engine = 'python')
clusters_ids_location = []

# finding corresponding locations with loop - join would be better but ok not too heavy with loop
for i in range(len(clusters_ids)):
    cluster_ids_location = []
    for j in range(len(clusters_ids[i])):
        lati = coord[coord['node_id'] == int(clusters_ids[i][j])].iloc[0,coord.columns.get_loc('latitude')]
        longi = coord[coord['node_id'] == int(clusters_ids[i][j])].iloc[0,coord.columns.get_loc('longitude')]
        longilati = str(longi) + ',' + str(lati)
        longilatiPair = [longi,lati] 
        cluster_ids_location.append(longilatiPair)
    clusters_ids_location.append(cluster_ids_location)
    
print "Long/Lat of IDs obtained"
    
#%% Drawing polygon clusters

# ----- Calculate polygon bounds ---------

clusters_bounds = []

# Need to manually calculate them since there is no more grid
dlongi = abs(coord[coord['node_id'] == 1391].iloc[0,coord.columns.get_loc('longitude')] - coord[coord['node_id'] == 1392].iloc[0,coord.columns.get_loc('longitude')])/2
dlati = abs(coord[coord['node_id'] == 1392].iloc[0,coord.columns.get_loc('latitude')] - coord[coord['node_id'] == 2612].iloc[0,coord.columns.get_loc('latitude')])/2

for i in range(len(clusters_ids_location)):
    cluster_bounds = []
    
    # longitude extremes
    longisort = sorted(clusters_ids_location[i], key = itemgetter(0,1))
    j=0

    
    while (j<len(clusters_ids_location[i])):      
        longipoint = longisort[j][0]
        cluster_bounds.append([longisort[j][0],longisort[j][1]-dlati])
        
        while(longipoint == longisort[j][0]):
            j +=1
            if (j>=len(clusters_ids_location[i])):
                break
            
        cluster_bounds.append([longisort[j-1][0],longisort[j-1][1]+dlati])
    
    #latitude extremes
    latisort = sorted(clusters_ids_location[i], key = itemgetter(1,0))
    j=0
    
    while (j<len(clusters_ids_location[i])): 
        latipoint = latisort[j][1]
        cluster_bounds.append([latisort[j][0]-dlongi,latisort[j][1]])
        
        while(latipoint == latisort[j][1]):
            j +=1
            if (j>=len(clusters_ids_location[i])):
                break
            
        cluster_bounds.append([latisort[j-1][0]+dlongi,latisort[j-1][1]])       
    
    ## ---------- SORTING cluster_bounds by polar angle -----------
    
    cluster_bounds2 = np.array(cluster_bounds)
    
    # Calculate barycenter of points
    longi_bar = np.sum(cluster_bounds2, axis=0)[0]/len(cluster_bounds2)
    lati_bar = np.sum(cluster_bounds2, axis=0)[1]/len(cluster_bounds2)
    
    # Define angle calculating angle with origin = current barycenter
    def polar_angle(a):
        # be careful, the function depends on the value of predefined variables longi)bar and lati_bar
        angle = np.arctan((a[1]-lati_bar)/(a[0]-longi_bar))
        return(angle)
    
    #Calculate polar angles
    angles = np.apply_along_axis(polar_angle,1,cluster_bounds2)
    
    # add polar angles columns
    a = np.zeros((len(cluster_bounds2),3))   
    a[:,:-1] = cluster_bounds2
    a[:,2] = angles
    a = a[a[:,2].argsort()] #sort by angle
    cluster_bounds_sorted = a[:,:-1].tolist()
    
    clusters_bounds.append(cluster_bounds_sorted)
    
print "Polygon bounds calculated and sorted"
   
st = '2011/10/03-9 to 2011/10/04-16'
st.split(' to ')[1].split('-')[0]

#%% Writing kml file

kml = open(inputfile.replace('.txt','') + "_" + str(k)+"c.kml", "w")


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
    
    print "  ------------------  Writing file " + str(i) + "-------------"    
    
    kml.write("""	<Style id="cluster-""" + str(i) + """-style"><IconStyle><Icon></Icon></IconStyle><LabelStyle><scale>1.0</scale></LabelStyle><LineStyle><color>3fff0000</color></LineStyle><PolyStyle><color>40ff0000</color></PolyStyle><BalloonStyle><text><![CDATA[<b>$[snippet]</b><br/><table border="0"><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Time frame</th><td style="white-space:nowrap;">$[Time frame]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Number of cases</th><td style="white-space:nowrap;">$[Number of cases]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Test statistic</th><td style="white-space:nowrap;">$[Test statistic]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Observed / expected</th><td style="white-space:nowrap;">$[Observed / expected]</td></tr></table>]]></text></BalloonStyle></Style>
	<StyleMap id="cluster-""" + str(i) + """-stylemap"><Pair><key>normal</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair><Pair><key>highlight</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair></StyleMap>
    """)
    
    # Clusters Polygons
    
    ## get datetime in local variables
    beg_dt = clusters_timeframe[i-1].split(' to ')[0]
    end_dt = clusters_timeframe[i-1].split(' to ')[1]
    
    kml.write("""	<Placemark>
		<name>""" + str(i) + """</name>
		<snippet>SaTScan Cluster #""" + str(i) + """</snippet>
		<visibility>1</visibility>
		<TimeSpan><begin>""" + beg_dt.split('-')[0] + """T""" + '{:02}'.format(int(beg_dt.split('-')[1])) + """:00:00Z</begin><end>""" + end_dt.split('-')[0] + """T""" + '{:02}'.format(int(end_dt.split('-')[1])) + """:00:00Z</end></TimeSpan>
		<styleUrl>#cluster-""" + str(i) + """-stylemap</styleUrl>
		<ExtendedData><Data name="Time frame"><value>""" + clusters_timeframe[i-1] +"""</value></Data><Data name="Number of cases"><value>""" + clusters_obscount[i-1] + """</value></Data><Data name="Test statistic"><value>""" + clusters_teststatistic[i-1] + """</value></Data><Data name="Observed / expected"><value>""" + clusters_obsexpratio[i-1] + """</value></Data></ExtendedData>
		<MultiGeometry>
			<Polygon><outerBoundaryIs><LinearRing><tessellate>1</tessellate><coordinates>""")
               
    # LOOP WRITING BOUNDARY Polygon
    for j in range(len(clusters_bounds[i-1])):
        longilati500 = str(clusters_bounds[i-1][j][0]) + ',' + str(clusters_bounds[i-1][j][1]) + ',500 '        
        kml.write(longilati500)
                   
               
    kml.write("""</coordinates></LinearRing></outerBoundaryIs></Polygon>
			<Point><extrude>1</extrude><altitudeMode>relativeToGround</altitudeMode><coordinates>-73.9857,40.7331,0</coordinates></Point>
		</MultiGeometry>
	</Placemark>  
    """)    # WHAT IS ALTITUDE??? Nevermind
    
    
    # Clusters IDs location folders
    kml.write("""<Folder><name>Cluster """ + str(i) + """ Locations</name><description></description>\n""")      
    
    for j in range(len(clusters_ids[i-1])):
        kml.write("			<Placemark><name></name><visibility>0</visibility><description></description><styleUrl>#low-rate-placemark</styleUrl><Point><coordinates>"+str(clusters_ids_location[i-1][j][0]) + ',' +str(clusters_ids_location[i-1][j][1]) + ",0</coordinates></Point></Placemark>\n") 
    kml.write("	</Folder>\n")

kml.write("""
</Document>
</kml>""")

kml.close()

print "KML file written"
