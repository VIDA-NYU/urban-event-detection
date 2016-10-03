Readme Code

This folder contains the code used for the experiments. Here is a succinct description of the folders.

general:
- tools used for plotting, converting coordinates from lat long to meters

preprocessing: code used to convert raw .bin data to grid data with density filter
- harish_data_h_50.jar: executable to compute grid data with 50mx50m resolution
- src: source code for preprocessing
- TLCData0/1.java: file to substitute in the src code to adapt it to our experiment

satscan:
- data processing specific to SaTScan
- visualization scripts specific to SaTScan
- /iterative
	- data processing & visualization for the iterative version of satscan

telang:
- AnomalyDetector: file to replace in the original Telang code to run the experiments on taxi data
- output_processing: processes the output of Telang to a clearer output
- preprocessing scripts for telang experiments
	- generating density files, coordinates files, applying a threshold on the data
	- vizualisation script
- .jar files are the executable jar for experiments