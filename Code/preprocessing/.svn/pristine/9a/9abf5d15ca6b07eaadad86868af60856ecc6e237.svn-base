# -*- coding: utf-8 -*-
"""
Created on Fri Aug 26 08:56:29 2016

@author: ferdinand
"""

import pandas as pd
from array import array
import numpy as np

gridRes = 50
h = 23

pick = pd.read_csv('/home/ferdinand/Documents/NYU/Data/harish_grid_h/pick-density_'+str(gridRes)+'/density-2011-10-31-'+str(h)+'.txt', names=['count'])
drop = pick = pd.read_csv('/home/ferdinand/Documents/NYU/Data/harish_grid_h/drop-density_'+str(gridRes)+'/density-2011-10-31-'+str(h)+'.txt', names=['count'])

pickdrop = open('/home/ferdinand/Documents/NYU/Data/harish_grid_h/pickdrop_50-density_2011-10-31-'+str(h)+'.txt','w')
#pickdrop_bin = open('/home/ferdinand/Documents/NYU/Data/harish_grid_h/pickdrop_50-density_2011-10-31-'+str(h),'wb')
#pickdrop_np_bin = open('/home/ferdinand/Documents/NYU/Data/harish_grid_h/pickdrop-np_50-density_2011-10-31-'+str(h),'wb')

#Txt
for i in range(pick.shape[0]):
   pickdrop.write(str(pick['count'][i] + drop['count'][i]) + '\n')
pickdrop.close()

len((pick['count']+drop['count']).tolist())

#Binary
#pd_list = (pick['count']+drop['count']).tolist()
#pd_array = array('d',pd_list)
#pd_array.tofile(pickdrop_bin)

#Numpy binary
#pd_np = np.array(pick['count']+drop['count'])
#pd_np.dtype
#pd_np.tofile(pickdrop_np_bin)
