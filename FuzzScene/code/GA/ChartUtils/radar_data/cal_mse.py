import pandas as pd
import csv
import sys
import os
import shutil
sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants

path = Constants.CARLA_RADAR_DATA_PATH


def calc_mse(yhat, label):  # used for loss cal, output float
    mse = 0.
    count = 0
    if len(yhat) != len(label):
        print("yhat and label have different lengths")
        return -1
    for i in range(len(yhat)):
        count += 1
        predicted_steering = yhat[i]
        steering = label[i]
        mse += (float(steering) - float(predicted_steering)) ** 2.
    print(count)
    return (mse / count)


def get_res(m):
    data_wb = []
    operator_data = [[[], []], [[], []], [[], []], [[], []], [[], []]]
    operator_mse = []

    p = path + 'radar' + str(m) + '.csv'
    with open(p, 'r') as f:
        f.seek(0)
        for i, line in enumerate(f):
            if i == 0:
                continue
            str_line = line.split(',')
            operator_data[int(str_line[1])][0].append(str_line[2])
            operator_data[int(str_line[1])][1].append(str_line[5])
    
    ori_mse = get_baseline_mse(m)
    for i in range(0, 5):
        if Constants.OPERATOR_FLAG[i] is True:
            mse = calc_mse(operator_data[i][0], operator_data[i][1])
        else:
            mse = ori_mse
        operator_mse.append(mse)
    data_wb.append([m, ori_mse, operator_mse[0], operator_mse[1], operator_mse[2], operator_mse[3], operator_mse[4]])

    mse_path = Constants.CARLA_RADAR_DATA_PATH + "mse.csv"
    if not os.path.exists(mse_path):
        shutil.copy(Constants.CARLA_RADAR_DATA_PATH + "mse_null.csv", mse_path)

    with open(mse_path, 'a+', encoding='utf-8') as f:
        csv_writer = csv.writer(f)
        for line in range(len(data_wb)):
            csv_writer.writerow(data_wb[line])


def get_baseline_mse(m):
    p = path + 'model' + str(m) + '_oriMSE.csv'
    df = pd.read_csv(p, encoding='utf-8', header=0)
    yhat = df["predict_steering_angle"]
    label = df["steering_angle_change"]
    return calc_mse(yhat=yhat, label=label)

for i in range(0, Constants.MODEL_NUM):
    get_res(Constants.MODEL_ARR[i])
