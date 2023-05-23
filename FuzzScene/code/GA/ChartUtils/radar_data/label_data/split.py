import pandas as pd
import os
import sys
sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants

# 读取csv文件
df = pd.read_csv(Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + "label_test.csv")

# 获取文件总行数
row_num = len(df)

# 确定每个小文件要包含的数据量
step = 1250
index = 0

for start in range(0, row_num, step):
    stop = start + step
    filename = Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + "label_test_"+str(index)+".csv"
    d = df[start: stop]
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Saving file : " + filename + ", data size : " + str(len(d)))
    d.to_csv(filename, index=None)
    index += 1

# 输出如下
# Saving file : ./small_0-500.csv, data size : 500
# Saving file : ./small_500-1000.csv, data size : 500
