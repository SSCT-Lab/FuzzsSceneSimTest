import sys
import os
import re
import pandas as pd

sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants


operator_flag = sys.argv[1]
radar_rand_time = sys.argv[2]
seed_num = sys.argv[3]
model_num = sys.argv[4]
clear_output = sys.argv[5]

print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] " + "operator_flag: " + operator_flag + ", radar_rand_time: " + radar_rand_time + ", seed_num: " + seed_num + ", model_num: " + model_num + ", clear_output: " + clear_output)

# if os.path.exists('./error_count.csv'):  # clear ./error_count.csv
#     os.remove('./error_count.csv')
# shutil.copy('./error_count_null.csv', './error_count.csv')

f = open(Constants.CONSTANTS_PATH, "r+")
new = []
for line in f:
    new.append(line)
f.close()
new[1] = 'OPERATOR_FLAG = ' + operator_flag + '\n'
new[2] = 'RADAR_RAND_TIME = ' + radar_rand_time + '\n'
new[3] = 'SEED_NUM = ' + seed_num + '\n'
new[4] = 'MODEL_NUM = ' + model_num + '\n'
new[5] = 'CLEAR_OUTPUT = ' + clear_output + '\n'
f = open(Constants.CONSTANTS_PATH, "w+")
for n in new:
    f.write(n)
f.close()