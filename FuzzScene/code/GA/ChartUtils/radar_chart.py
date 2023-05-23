import numpy as np
import random
import math
import copy
import os
import sys

sys.path.append("/home/vangogh/software/FuzzScene/code/")
from generate_radar import *
import Constants

domain = [[0, 255], [0, 255], [0, 255], [1, 12], [8, 16], [30, 100], [0, 100], [0, 200]]
ori_data = [[0, 0, 255], [3, 12], 85, 0, 200]


def radar_sim(color1, color2, color3, time1, time2, sun, rain, fog, seed_name):  # generate seed files, run simulations and collect data
    mutation_name = seed_name.split("_")

    complete_mutation_name = "seed_0_0_" + mutation_name[3]
    parse_path = "radar_seed_pool/" + complete_mutation_name
    DOMTree = xml.dom.minidom.parse(parse_path)
    ele = DOMTree.documentElement
    e = ele.getElementsByTagName("Entities")[0]
    s = ele.getElementsByTagName("Storyboard")[0]

    xml_path = 'radar_seed_pool/' + seed_name
    variable = {"name": "origin & random parameters"}
    rand_para = [color1, color2, color3, time1, time2, sun, rain, fog]
    Simulation(rand_para, variable, e, s)
    # print(variable)
    writeBack(xml_path, DOMTree)
    os.system("bash ./ga_sim.sh " + seed_name)
    # data_collection_para = str(data_collection_para[0]) + ',' + str(data_collection_para[1]) + ',' + str(
    #    data_collection_para[2])
    # os.system("python radar_error_test.py " + model_name + " 1 " + seed_name + " " + data_collection_para)


def rand_para(operator_num):  # get random value of specified operator
    if operator_num == 0:
        color = []
        for i in range(0, 3):
            color.append(random.randint(0, 255))
        return color
    if operator_num == 1:
        date = []
        date.append(random.randint(1, 12))
        date.append(random.randint(8, 16))
        return date
    else:
        return random.randint(domain[operator_num + 3][0], domain[operator_num + 3][1])

for operator in range(0, 5):
    if Constants.OPERATOR_FLAG[operator] is True:
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# operator: ", operator)
        for random_time in range(1, Constants.RADAR_RAND_TIME + 1):
            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# random_time: ", random_time)
            for scene in range(1, Constants.SEED_NUM + 1):
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# scene: ", scene)
                pop_seed_name = 'seed_' + str(operator) + '_' + str(random_time) + '_' + str(scene) + '.xosc'
                rand_data = copy.deepcopy(ori_data)
                rand_data[operator] = rand_para(operator)
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "pop_seed_name: ", pop_seed_name)
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "rand_data", rand_data)
                radar_sim(rand_data[0][0], rand_data[0][1], rand_data[0][2], rand_data[1][0], rand_data[1][1], rand_data[2], rand_data[3],
                        rand_data[4], pop_seed_name)
