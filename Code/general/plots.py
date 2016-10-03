# -*- coding: utf-8 -*-
"""
Spyder Editor

We plot the test score functions of SpaceTime permutation model and Telang
"""
    
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter
import matplotlib.pyplot as plt
import numpy as np


# Defining function
def sat(u,c,c_tot):
    return ((c/u)^c) * ((c_tot - c)/(c_tot - u))^(c_tot - c)


# Plot satscan score
fig = plt.figure()
ax = fig.gca(projection='3d')
x = np.arange(0.5, 5, 0.1) # c
y = np.arange(1, 5, 0.1) # u
cx_tot = np.full((1, len(x)), 1000)
cy_tot = np.full((1, len(y)), 1000)
x, y = np.meshgrid(x, y)
cx_tot, cy_tot = np.meshgrid(cx_tot, cy_tot)


a = np.power((x/y.astype(float)),x)
b = np.power((cx_tot - x) / (cy_tot - y).astype(float), cx_tot - x)
score = a*b





surf = ax.plot_surface(x, y, score, rstride=1, cstride=1, cmap=cm.coolwarm,
                       linewidth=0, antialiased=False)
ax.set_zlim(0, 30)

ax.zaxis.set_major_locator(LinearLocator(10))
#ax.zaxis.set_major_formatter(FormatStrFormatter('%.02f'))

#fig.colorbar(surf, shrink=0.5, aspect=5)
fig.colorbar(surf, aspect=5)

plt.show()