import os
import re
import sys
import argparse
import shutil

sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants

def clear_output():
    path = Constants.CARLA_RADAR_PNG_OUTPUT_PATH
    shutil.rmtree(path)  # 清空out图片
    os.mkdir(path)

    label_path = Constants.CARLA_RADAR_LABEL_OUTPUT_PATH
    file_list = os.listdir(label_path)
    for file in file_list:
        if re.match(r"label_test_\d+\.csv", file):
            os.remove(label_path + file)
    if os.path.exists(label_path + 'label_test.csv'):  # 清空label_test.csv
        os.remove(label_path + 'label_test.csv')
    shutil.copy(label_path + 'label_test_null.csv', label_path + 'label_test.csv')

    # 清空radarx.csv以及mse.csv
    csv_path = Constants.CARLA_RADAR_DATA_PATH
    file_list = os.listdir(csv_path)
    for file in file_list:
        if re.match(r"radar\d+\.csv", file) or re.match(r"mse\.csv", file):
            os.remove(csv_path + file)

    seed_pool_path = Constants.RADAR_SEED_POOL
    if os.path.exists(seed_pool_path):
        shutil.rmtree(seed_pool_path)  # 清空seed_pool
    shutil.copytree(Constants.RADAR_SEED_POOL_BAK, seed_pool_path)

def main():
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# Begin generate radar test data ")
    os.system("python radar_chart.py")
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# Generate radar test data done, Start split data ")
    os.system("python " + Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + "split.py")
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# Split data done, Start radar test ")
    # 检查目录Constants.CARLA_RADAR_LABEL_OUTPUT_PATH下有多少个划分出的label子文件，子文件具有label_test_xx.csv的格式
    # # 定义文件名的正则表达式
    file_pattern = r"label_test_\d+\.csv"
    # 统计匹配到的文件数量
    count = 0
    for filename in os.listdir(Constants.CARLA_RADAR_LABEL_OUTPUT_PATH):
        if re.match(file_pattern, filename):
            count += 1
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Found " + str(count) + " sub label files, Start radar test")
    for model_id in range(0, Constants.MODEL_NUM):
        for seq in range(0, count):
            os.system('python radar_error_test.py ' + str(Constants.MODEL_ARR[model_id]) + ' ' + str(seq))
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# Radar test done, Start cal mse ")
    os.system("python " + Constants.CARLA_RADAR_DATA_PATH + "cal_mse.py")
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# Cal mse done")
    print("# Operators Test Over")

if __name__ == "__main__":
    if Constants.CLEAR_OUTPUT:
        clear_output()
    main()
