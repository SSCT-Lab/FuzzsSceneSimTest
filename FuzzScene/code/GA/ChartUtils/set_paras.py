import sys, os, shutil
sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants

operator_flag = sys.argv[1]
radar_rand_time = sys.argv[2]
seed_num = sys.argv[3]
model_num = sys.argv[4]
model_arr = sys.argv[5]
clear_output = sys.argv[6]

print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "operator_flag: ", operator_flag, "radar_rand_time: ", radar_rand_time, "seed_num: ", seed_num, "model_num: ", model_num, "model_arr: ", model_arr, "clear_output: ", clear_output)

f = open(Constants.CONSTANTS_PATH, "r+")
new = []
for line in f:
    new.append(line)
f.close()
new[1] = 'OPERATOR_FLAG = ' + operator_flag + '\n'
new[2] = 'RADAR_RAND_TIME = ' + radar_rand_time + '\n'
new[3] = 'SEED_NUM = ' + seed_num + '\n'
new[4] = 'MODEL_NUM = ' + model_num + '\n'
new[5] = 'MODEL_ARR = ' + model_arr + '\n'
new[6] = 'CLEAR_OUTPUT = ' + clear_output + '\n'
f = open(Constants.CONSTANTS_PATH, "w+")
for n in new:
    f.write(n)
f.close()