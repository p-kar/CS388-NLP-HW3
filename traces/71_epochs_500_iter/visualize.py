#!/usr/bin/python

import matplotlib.pyplot as plt
import time
import numpy as np
from scipy.interpolate import spline
from scipy.signal import savgol_filter

# Open the data file for reading lines
files = ['stats_random.csv', 'stats_length.csv', 'stats_raw.csv', 'stats_margin.csv']
colors = ['blue', 'orange', 'green', 'red']
labels = ['0', '0', '20k', '40k', '60k', '80k', '100k']

fig, ax = plt.subplots()

for f, c in zip(files, colors):
    datafile = open(f, 'r')
    sepfile = datafile.read().split('\n')
    datafile.close()

    # Local variables
    x = []
    y = []

    # Iterate through the lines and parse them
    for datapair in sepfile[1:-1]:
        if datapair:
            xypair = datapair.split(', ')
            if xypair[2] == 'NaN':
                continue
            x.append(int(xypair[1]))
            y.append(float(xypair[2]))

    x_sm = np.array(x)
    y_sm = np.array(y)
    y_smooth = savgol_filter(y, 9, 3)

    # ax.plot(x_sm, y_smooth, c, linewidth=1, label=f[6:-4])
    ax.plot(x_sm, y_sm, c, linewidth=1, label=f[6:-4])


ax.set_xticklabels(labels)
ax.legend()
plt.title('ALDP - 71 Epochs, 100 Iterations per Epoch')
plt.xlabel('Number of training words')
plt.ylabel('Label Attachment Score (LAS)')
plt.show()
