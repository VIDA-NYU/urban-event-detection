# -*- coding: utf-8 -*-
"""
Created on Sat Aug 20 14:25:41 2016

@author: ferdinand

This file process the outputs of Telang code to get a single output file with the top k anomalies.
The anomalies in the final output are sorted by test score.
Information is added when reported anomalies: mean cell count of cluster / neighborhood; start/end time
Adapted to Oct. 2011, with a start and end day
"""

from operator import itemgetter
import pandas as pd
import numpy as np
import re
import sys

gridRes = int(sys.argv[1])
gini = float(sys.argv[2])
k = int(sys.argv[3])
startDay = int(sys.argv[4])
endDay = int(sys.argv[5])
inputFolder = sys.argv[6]
inputCoordFolder = sys.argv[7]


#gridRes = 500
#gini = 0.1
#k = 40
#startDay = 1
#endDay = 31
#inputFolder = '/home/ferdinand/Documents/NYU/Data/telang/xp_telang_70/'
#inputCoordFolder = '/home/ferdinand/Documents/NYU/Data/harish_grid_s_h/'


#Remote
#inputFolder = '~/telang/xp_telang_gridRes/'
#inputCoordFolder = ~/Data/harish_grid_h/

outputFile = inputFolder + 'Anomaly_' + str(gini) + '.txt'
outputFileSummary = inputFolder + 'Summary_' + str(gini) + '.txt'
a_cellsFile = inputFolder + "AnomalyCells_" + str(gini) + ".txt"
a_valsFile = inputFolder + "AnomalyData_" + str(gini) + ".txt"


tp = pd.read_csv(a_cellsFile, sep="\t", names=["cluster", "neighbors", "score"],iterator=True, chunksize=1000)
a_cells_orig = pd.concat(tp, ignore_index=True)

tp2 = pd.read_csv(a_valsFile, sep="\t", names=["cluster", "neighbors", "score"],iterator=True, chunksize=1000)
a_vals_orig= pd.concat(tp2, ignore_index=True)

a_cells = a_cells_orig
a_vals = a_vals_orig


a_cells = a_cells.sort(['score'], ascending=[0])
a_cells = a_cells.reset_index(drop=True)
a_cells = a_cells[0:k]
a_vals = a_vals.sort(['score'], ascending=[0])
a_vals = a_vals.reset_index(drop=True)
a_vals = a_vals[0:k]


#%%Adding the value to (interval,row,col)

def intervalToDatetime(interval, startD):    
    return '2011-10-' + str(startD+(interval/24)) + '_' + str(interval%24)

####Storing information as list

# Information on clusters
cluster_list=[]
clusters_timespan=[]
clusters_length = []
clusters_vals=[] # contains [sum,meanCell] for every cluster
clusters_ids = []
clusters_space_extension = [] # constains clusters_ids[i].unique()
clusters_teststatistic = []

c1 = a_cells['cluster'].str.split('\) ')
c2 = a_vals['cluster'].str.split(' ')

for i in range(k):
    c1.iloc[i].remove('')
    c2.iloc[i].remove('')
    c2.iloc[i] = map(float,c2.iloc[i])
    c2.iloc[i] = [ '%.1f' % elem for elem in c2.iloc[i]]
    c1.iloc[i] = [s + ',' for s in c1.iloc[i]]
    b1 = [a+b for a,b in zip(c1.iloc[i],c2.iloc[i])]
    b1 = [s + ')' for s in b1]
    cluster_ids = [('{:03}'.format(int(s.split(',')[1])) + '{:03}'.format(int(s.split(',')[2]))) for s in c1.iloc[i]]
    cluster_ids = list(set(cluster_ids)) ## Only takens unique ids
    cluster_list.append(b1)
    clusters_ids.append(cluster_ids)
    # Collecting start and end time information
    times = [int(re.split(',|\(',string)[1]) for string in b1]
    length = max(times) - min(times) +1
    clusters_length.append(length)
    startTime = intervalToDatetime(min(times),startDay)
    endTime = intervalToDatetime(max(times),startDay)
    clusters_timespan.append([startTime,endTime]) # Can be converted to MM-DD-HH
    #Collecting counts information
    counts = [float(re.split(',|\(|\)',string)[4]) for string in b1]
    clusters_vals.append([int(sum(counts)), "{0:.1f}".format(sum(counts)/len(counts)), len(counts)])
    clusters_teststatistic.append(a_cells.loc[i,'score'])
    
clusters_space_extension = [len(cluster) for cluster in clusters_ids]

#Information on neighbors
neighbors_list=[]
neighbors_vals=[] # contains [sum,meanCell] for every neighbors set

n1 = a_cells['neighbors'].str.split('\) ')
n2 = a_vals['neighbors'].str.split(' ')

for i in range(k):
    n1.iloc[i].remove('')
    n2.iloc[i].remove('')
    n2.iloc[i] = map(float,n2.iloc[i])
    n2.iloc[i] = [ '%.1f' % elem for elem in n2.iloc[i]]
    n1.iloc[i] = [s + ',' for s in n1.iloc[i]]
    b1 = [a+b for a,b in zip(n1.iloc[i],n2.iloc[i])]
    b1 = [s + ')' for s in b1]
    neighbors_list.append(b1)
    #Collection counts information
    counts = [float(re.split(',|\(|\)',string)[4]) for string in b1]
    neighbors_vals.append([int(sum(counts)), "{0:.1f}".format(sum(counts)/len(counts))])


#%% Write information on output text file
output = open(outputFile,'w')
for i in range(k):
    output.write(str(i+1) + '.')
    output.write('Timespan: ' + str(clusters_timespan[i][0]) + " to " + str(clusters_timespan[i][1]) + "\n") #End time included // SaTScan
    output.write('Size: ' + str(clusters_vals[i][2]) + "\n")    
    output.write("Score: " + str(a_cells.loc[i,'score']) + "\n")
    output.write("Sum cluster cell: " + str(clusters_vals[i][0]) + "\n")    
    output.write("Sum neighbors cell: " + str(neighbors_vals[i][0]) + "\n")
    output.write("Sum cluster / neighbor: " + str(float(clusters_vals[i][0])/float(neighbors_vals[i][0]))+ "\n")
    output.write("Mean cluster cell: " + str(clusters_vals[i][1])+ "\n")
    output.write("Mean neighbors cell: " + str(neighbors_vals[i][1])+ "\n")
    output.write("Mean cluster / neighbor: " + str(float(clusters_vals[i][1])/float(neighbors_vals[i][1]))+ "\n")
    output.write("Cluster: " + ' '.join(cluster_list[i])+ "\n")
    output.write("Neighbors: " + ' '.join(neighbors_list[i])+ "\n")
    output.write("\n") 
output.close()
### Indicators used   
# Cluster mean / Neighbors mean / Ratio
# Cluster counts / Neighbors counts / Ratio
# Cluster size

# Output txt without the list of the cells
output = open(outputFileSummary,'w')

output.write("Telang anomaly result file for - gridRes: " + str(gridRes) + " | Gini: " + str(gini) + "\n")
output.write("Number of clusters reported: " + str(k) + "\n")
output.write("Space size - Min / Mean / Max: " + str(min(clusters_space_extension)) + " / " + str(sum(clusters_space_extension)/float(len(clusters_space_extension))) + " / " + str(max(clusters_space_extension)) + "\n")
output.write("Timespan - Min / Mean / Max: " + str(min(clusters_length)) + " / " + str(sum(clusters_length)/float(len(clusters_length))) + " / " + str(max(clusters_length))+ "\n")
output.write("Test statistic - Min / Mean / Max: " + str(min(clusters_teststatistic)) + " / " + str(sum(clusters_teststatistic)/len(clusters_teststatistic)) + " / " + str(max(clusters_teststatistic)) + "\n")
output.write("High counts anomaly / Total anomalies: " + str(float(sum([(float(clusters_vals[i][1])/float(neighbors_vals[i][1]) >= 1) for i in range(k)]))/k) + '\n')
output.write("\n\n")



for i in range(k):
    output.write(str(i+1) + '.')
    output.write('Timespan: ' + str(clusters_timespan[i][0]) + " to " + str(clusters_timespan[i][1]) + "\n") #End time included // SaTScan
    output.write('Space size: ' + str(clusters_space_extension[i]) + "\n")    
    output.write("Score: " + str(a_cells.loc[i,'score']) + "\n")
    output.write("Sum cluster cell: " + str(clusters_vals[i][0]) + "\n")    
    output.write("Sum neighbors cell: " + str(neighbors_vals[i][0]) + "\n")
    output.write("Sum cluster / neighbor: " + str(float(clusters_vals[i][0])/float(neighbors_vals[i][0]))+ "\n")
    output.write("Mean cluster cell: " + str(clusters_vals[i][1])+ "\n")
    output.write("Mean neighbors cell: " + str(neighbors_vals[i][1])+ "\n")
    output.write("Mean cluster / neighbor: " + str(float(clusters_vals[i][1])/float(neighbors_vals[i][1]))+ "\n")
    output.write("\n") 
output.close()

#%% Collecting necessary information for visualization

coordInputFile = inputCoordFolder + 'coord_grid_'+str(gridRes)+'.csv'
coord = pd.read_csv(coordInputFile, header = 0,dtype=str)        
    

#%% Get IDS locations

clusters_ids_location = []

for cluster in clusters_ids:
    cluster_ids_loc = []    
    for ID in cluster:
        idlong = float(coord.loc[coord['cell_id'] == ID,'longitude'].item())
        idlat = float(coord.loc[coord['cell_id'] == ID,'latitude'].item())
        cluster_ids_loc.append([idlong,idlat])
    clusters_ids_location.append(cluster_ids_loc)
        


#%% Drawing polygon clusters

# ----- Calculate polygon bounds ---------

clusters_bounds = []

# Need to manually calculate them since the computation is approximate
dlongi = (float(coord.loc[coord['cell_id']== '003004','longitude'].item()) - float(coord.loc[coord['cell_id']== '003003','longitude'].item()))/2
dlati = (float(coord.loc[coord['cell_id']== '004004','latitude'].item()) - float(coord.loc[coord['cell_id']== '003004','latitude'].item()))/2

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

#%% Writing kml file


#TODO: def writeKml(inputf,gini,gridRes):
    
inputFileKml = inputFolder + 'Anomaly_' + str(gini)
kml_filename = 'T_' + str(gridRes) + '_' + str(gini)
    
kml = open(inputFileKml + ".kml", "w")


#%% Writing kml
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
    
    print "  ------------------  Writing cluster " + str(i) + "-------------"    
    
    kml.write("""	<Style id="cluster-""" + str(i) + """-style"><IconStyle><Icon></Icon></IconStyle><LabelStyle><scale>1.0</scale></LabelStyle><LineStyle><color>3fff0000</color></LineStyle><PolyStyle><color>40ff0000</color></PolyStyle><BalloonStyle><text><![CDATA[<b>$[snippet]</b><br/><table border="0"><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Time frame</th><td style="white-space:nowrap;">$[Time frame]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Mean density</th><td style="white-space:nowrap;">$[Mean density]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Test statistic</th><td style="white-space:nowrap;">$[Test statistic]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Mean (clust / neighb)</th><td style="white-space:nowrap;">$[Mean (clust / neighb)]</td></tr></table>]]></text></BalloonStyle></Style>
	<StyleMap id="cluster-""" + str(i) + """-stylemap"><Pair><key>normal</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair><Pair><key>highlight</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair></StyleMap>
    """)
    
    # Clusters Polygons
    
    ## get datetime in local variables
    beg_dt = clusters_timespan[i-1][0]
    end_dt = clusters_timespan[i-1][1]

    ## Get cluster barycenter
    barlongi = 0
    barlati = 0
    for j in range(len(clusters_bounds[i-1])):
        barlongi += float(clusters_bounds[i-1][j][0])
        barlati += float(clusters_bounds[i-1][j][1])
    barlongi = barlongi / len(clusters_bounds[i-1])
    barlati = barlati / len(clusters_bounds[i-1]) 
    
    kml.write("""	<Placemark>
		<name>""" + str(i) + """</name>
		<snippet>Telang Cluster #""" + str(i) + """</snippet>
		<visibility>1</visibility>
		<TimeSpan><begin>""" + beg_dt.split('_')[0] + """T""" + '{:02}'.format(int(beg_dt.split('_')[1])) + """:59:00Z</begin><end>""" + end_dt.split('_')[0] + """T""" + '{:02}'.format(int(end_dt.split('_')[1])) + """:23:00Z</end></TimeSpan>
		<styleUrl>#cluster-""" + str(i) + """-stylemap</styleUrl>
		<ExtendedData><Data name="Time frame"><value>""" + clusters_timespan[i-1][0] + ' to ' + clusters_timespan[i-1][1] +"""</value></Data><Data name="Mean density"><value>""" + clusters_vals[i-1][1] + """</value></Data><Data name="Test statistic"><value>""" + str(int(a_cells.loc[i-1,'score'])) + """</value></Data><Data name="Mean (clust / neighb)"><value>""" + "{0:.2f}".format(float(clusters_vals[i-1][1])/float(neighbors_vals[i-1][1])) + """</value></Data></ExtendedData>
		<MultiGeometry>
			<Polygon><outerBoundaryIs><LinearRing><tessellate>1</tessellate><coordinates>""")
    
    # TODO: Think of a way to respresent the cluster with more that just the points           
    # LOOP WRITING BOUNDARY Polygon
    for j in range(len(clusters_bounds[i-1])):
        longilati500 = str(clusters_bounds[i-1][j][0]) + ',' + str(clusters_bounds[i-1][j][1]) + ',500 '        
        kml.write(longilati500)
                   
               
    kml.write("""</coordinates></LinearRing></outerBoundaryIs></Polygon>
			<Point><extrude>1</extrude><altitudeMode>relativeToGround</altitudeMode><coordinates>""" + str(barlongi) + ',' + str(barlati) + ',0' + """</coordinates></Point>
		</MultiGeometry>
	</Placemark>  
    """)    # WHAT IS ALTITUDE??? Nevermind
    
    
    # Clusters IDs location folders
    kml.write("""<Folder><name>Cluster """ + str(i) + """ Locations</name><description></description>\n""")      
    
    for j in range(len(clusters_ids[i-1])):
        ID = clusters_ids[i-1][j]
        id_long = coord.loc[coord['cell_id'] == ID,'longitude'].item()
        id_lat = coord.loc[coord['cell_id'] == ID,'latitude'].item()
        kml.write("			<Placemark><name>""</name><visibility>0</visibility><description></description><styleUrl>#low-rate-placemark</styleUrl><Point><coordinates>"+id_long + ',' +id_lat + ",0</coordinates></Point></Placemark>\n") 
    kml.write("	</Folder>\n")

kml.write("""
</Document>
</kml>""")

kml.close()

print "KML file written"
