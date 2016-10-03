# -*- coding: utf-8 -*-
"""
Created on Sat Aug 20 18:49:48 2016

@author: ferdinand

This file converts Anomaly_GiniIndex to kml file
"""

from operator import itemgetter
gini = 0.1

#%% Parameters


#TODO: def writeKml(inputf,gini,gridRes):
    
inputFileKml = '/home/ferdinand/Documents/NYU/Data/telang/xp_telang_' + str(gridRes) + '_s_t/Anomaly_' + str(gini)
kml_filename = 'T_' + str(gini)
    
kml = open(inputFileKml + ".kml", "w")

#%% Get IDS locations

clusters_ids_location = []

ID = '002006'

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
dlongi = float(coord.loc[coord['cell_id']== '003004','longitude'].item()) - float(coord.loc[coord['cell_id']== '003003','longitude'].item())/2
dlati = float(coord.loc[coord['cell_id']== '004004','latitude'].item()) - float(coord.loc[coord['cell_id']== '003004','latitude'].item())/2

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
    
    print "  ------------------  Writing file " + str(i) + "-------------"    
    
    kml.write("""	<Style id="cluster-""" + str(i) + """-style"><IconStyle><Icon></Icon></IconStyle><LabelStyle><scale>1.0</scale></LabelStyle><LineStyle><color>3fff0000</color></LineStyle><PolyStyle><color>40ff0000</color></PolyStyle><BalloonStyle><text><![CDATA[<b>$[snippet]</b><br/><table border="0"><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Time frame</th><td style="white-space:nowrap;">$[Time frame]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Number of cases</th><td style="white-space:nowrap;">$[Number of cases]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Test statistic</th><td style="white-space:nowrap;">$[Test statistic]</td></tr><tr><th style="text-align:left;white-space:nowrap;padding-right:5px;">Observed / expected</th><td style="white-space:nowrap;">$[Observed / expected]</td></tr></table>]]></text></BalloonStyle></Style>
	<StyleMap id="cluster-""" + str(i) + """-stylemap"><Pair><key>normal</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair><Pair><key>highlight</key><styleUrl>#cluster-""" + str(i) + """-style</styleUrl></Pair></StyleMap>
    """)
    
    # Clusters Polygons
    
    ## get datetime in local variables
    beg_dt = clusters_timespan[i-1][0]
    end_dt = clusters_timespan[i-1][1]
    
    kml.write("""	<Placemark>
		<name>""" + str(i) + """</name>
		<snippet>SaTScan Cluster #""" + str(i) + """</snippet>
		<visibility>1</visibility>
		<TimeSpan><begin>""" + beg_dt.split('_')[0] + """T""" + '{:02}'.format(int(beg_dt.split('_')[1])) + """:59:00Z</begin><end>""" + end_dt.split('_')[0] + """T""" + '{:02}'.format(int(end_dt.split('_')[1])) + """:23:00Z</end></TimeSpan>
		<styleUrl>#cluster-""" + str(i) + """-stylemap</styleUrl>
		<ExtendedData><Data name="Time frame"><value>""" + clusters_timespan[i-1][0] + ' to ' + clusters_timespan[i-1][1] +"""</value></Data><Data name="Mean cluster cells count"><value>""" + clusters_vals[i-1][1] + """</value></Data><Data name="Test statistic"><value>""" + str(a_cells.loc[i-1,'score']) + """</value></Data><Data name="Mean cluster / Mean neighbors"><value>""" + str(float(clusters_vals[i-1][1])/float(neighbors_vals[i-1][1])) + """</value></Data></ExtendedData>
		<MultiGeometry>
			<Polygon><outerBoundaryIs><LinearRing><tessellate>1</tessellate><coordinates>""")
    
    # TODO: Think of a way to respresent the cluster with more that just the points           
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
    


def createSquare(center, start, end, mean_cluster, mean_neighbors, score):
    kml.write("""	<Placemark>
		<name>""" + str(i) + """</name>
		<snippet>Telang Cluster #""" + str(i) + """</snippet>
		<visibility>1</visibility>
		<TimeSpan><begin>""" + startday + """T""" + startHour + """:59:00Z</begin><end>""" + endDay + """T""" + endHour + """:23:00Z</end></TimeSpan>
		<styleUrl>#cluster-""" + str(i) + """-stylemap</styleUrl>
		<ExtendedData><Data name="Time frame"><value>""" + start + ' to ' + end +"""</value></Data><Data name="Mean cluster cells count"><value>""" + mean_cluster + """</value></Data><Data name="Test statistic"><value>""" + score + """</value></Data><Data name="Mean cluster / Mean neighbors"><value>""" + mean_cluster/mean_neighbors + """</value></Data></ExtendedData>
		<MultiGeometry>
			<Polygon><outerBoundaryIs><LinearRing><tessellate>1</tessellate><coordinates>""")
    
    # TODO: Think of a way to respresent the cluster with more that just the points           
    # LOOP WRITING BOUNDARY Polygon
    #for j in range(len(clusters_bounds[i-1])):
    #    longilati500 = str(clusters_bounds[i-1][j][0]) + ',' + str(clusters_bounds[i-1][j][1]) + ',500 '        
    #    kml.write(longilati500)
                   
               
    kml.write("""</coordinates></LinearRing></outerBoundaryIs></Polygon>
			<Point><extrude>1</extrude><altitudeMode>relativeToGround</altitudeMode><coordinates>-73.9857,40.7331,0</coordinates></Point>
		</MultiGeometry>
	</Placemark>  
    """)



















    
    
    
    
