Readme satscan

Includes all results of satscan experiments. xp_roads_elp means that SaTScan was run on the dataset where 1 point = 1 road intersection in Manhattan, with the cluster shape set to Ellipse.

Data files:
- Casgraph1110_d/h: density files of Oct 2011, with day/hour time precision
- coord_graph_utm.csv: coordinates files
- Coordgraph1110_utm.geo: coord file adapted to satscan

/iterative: contains the results of iterative satscan experiments
- parameters: all satscan parameters files used
- sit_2k_3h_low: Satscan ITerative results / spatial bound on semi-minor axis = 2k units / time bound for cluster extension = 3h / only low density anomalies looked for (optional)
	- summary_2k_3h_40c.txt: outcome detail for the top 40 clusters
	- sum_2k_3h_40c.txt: aggregated values (time span, mean value…) over all top 40 clusters
	- .kml files used to visualize on Google Earth