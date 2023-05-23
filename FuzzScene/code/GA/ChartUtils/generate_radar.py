from re import A
from turtle import color
from xml.dom.minidom import parse
import xml.dom.minidom
import random
import json
import os
import sys
from numpy import var
from pyrsistent import v

from regex import F
from scipy import rand
# from radar_error_test import *

# day_of_month = [0,31,29,31,30,31,30,31,31,30,31,30,31]
model_name = "4"


# Function vehicleRandomization
#
# Parameters:
#   name:           指定的ScenarioObject名称
#   car_type:       boolean值，true时随机car类型，false时不发生随机
#   car_color:      list[boolean]，决定三种颜色RGB是否发生随机
#   var_map:        dict 存储变量值

def vehicleRandomization(name, red, green, blue, var_map, e):
    scenario_obj = e.getElementsByTagName("ScenarioObject")
    for obj in scenario_obj:
        if obj.getAttribute("name") == name:

            v = obj.getElementsByTagName('Vehicle')[0]  # 获取带随机ScenarioObject代码块
            org_car_name = v.getAttribute("name")
            # variable["org_car_name"] = org_car_name

            p_1 = v.getElementsByTagName('Properties')[0]  # Properties层
            p_2 = p_1.getElementsByTagName('Property')

            for p in p_2:
                if p.getAttribute("name") == "color":
                    org_color = p.getAttribute("value")
                    # print("车辆原颜色为", org_color, '\n')
                    color_list = org_color.split(",")
                    var_map["org_R"] = color_list[0]
                    var_map["org_G"] = color_list[1]
                    var_map["org_B"] = color_list[2]

                    final_color = ""
                    c1 = str(red)
                    var_map["rand_R"] = c1
                    final_color += c1

                    final_color += ","
                    c2 = str(green)
                    var_map["rand_G"] = c2
                    final_color += c2

                    final_color += ","
                    c3 = str(blue)
                    var_map["rand_B"] = c3
                    final_color += c3

                    p.setAttribute("value", final_color)
                    # print("车辆颜色随机为", final_color, '\n')


# Function envRandomization
#
# Parameters:
#   is_time:               boolean值，true时随机日期类型，false时不发生随机
#   is_sun:                boolean值，true时随机intensity，false时不发生随机
#   is_precipitation:      boolean值，true时随机天气类型，在rain和dry中间取值
#   is_fog:                boolean值，true时随机fog的visual_range以及intensity
#   var_map:               dict 存储变量值

def envRandomization(mut_date, mut_time, mut_fog, mut_precipitation, mut_sun_intensity, var_map, s):  # 环境随机代码
    env = s.getElementsByTagName('Init')[0].getElementsByTagName('Actions')[0].getElementsByTagName('GlobalAction')[
        0].getElementsByTagName('EnvironmentAction')[0].getElementsByTagName('Environment')[0]
    time_of_day = env.getElementsByTagName("TimeOfDay")[0]
    weather = env.getElementsByTagName("Weather")[0]

    sun = weather.getElementsByTagName("Sun")[0]
    fog = weather.getElementsByTagName("Fog")[0]
    p = weather.getElementsByTagName("Precipitation")[0]

    # 原始时间数据读取
    datetime = time_of_day.getAttribute("dateTime")
    var_map["org_time"] = datetime
    # 原始太阳照度读取
    org_s_intensity = sun.getAttribute("intensity")
    var_map["org_sun_intensity"] = org_s_intensity
    # 原始雾天数据读取
    fog_para = fog.getAttribute("visualRange").split("\\")
    if (len(fog_para) == 1):
        org_visual_range = fog_para[0]
        # org_f_intensity = 100
    # else:
    #     org_visual_range = fog_para[0]
    #     org_f_intensity = fog_para[1]
    var_map["org_visual_range"] = org_visual_range
    # var_map["org_fog_intensity"] = org_f_intensity
    # 原始降水数据读取
    org_precipitation = p.getAttribute("precipitationType")
    org_p_intensity = p.getAttribute("intensity")
    var_map["org_precipitation"] = org_precipitation
    var_map["org_precipitation_intensity"] = org_p_intensity
    # 日期
    time_of_day.setAttribute("animation", "true")
    date = str(datetime.split("T")[0])
    rand_month = mut_date
    ori_day = date[8:]
    if (rand_month < 10):  # format
        rand_month = "0" + str(rand_month)
    # if(rand_day<10):                       #format
    #     rand_day = "0" + str(rand_day)
    rand_date = "2020-" + str(rand_month) + "-" + str(ori_day)
    var_map["rand_date"] = rand_date
    # 时间
    time_of_day.setAttribute("animation", "true")
    date = rand_date
    rand_hour = mut_time
    if (rand_hour < 10):  # format
        rand_hour = "0" + str(rand_hour)
    rand_time = date + "T" + str(rand_hour) + ":00:00"
    time_of_day.setAttribute("dateTime", rand_time)
    var_map["rand_time"] = rand_time

    # 照度
    rand_s_intensity = round((mut_sun_intensity / 100.0), 2)
    sun.setAttribute("intensity", str(rand_s_intensity))
    var_map["rand_sun_intensity"] = rand_s_intensity
    # 降水
    rand_p_intensity = round((mut_precipitation / 100.0), 2)
    p.setAttribute("intensity", str(rand_p_intensity))
    var_map["rand_precipitation_intensity"] = rand_p_intensity
    # 雾天及雾浓度

    rand_fog = str(mut_fog)  # 雾天
    var_map["rand_fog"] = rand_fog
    fog_para = rand_fog
    fog.setAttribute("visualRange", fog_para)
    var_map["visualRange"] = fog_para


def Simulation(rand_para, var_map, e, s):
    print(rand_para)
    vehicleRandomization("adversary", rand_para[0], rand_para[1], rand_para[2], var_map, e)
    envRandomization(rand_para[3], rand_para[4], rand_para[5], rand_para[6], rand_para[7], var_map, s)


def writeBack(xml_path, DOMTree):
    # 写回源文件使属性修改生效
    fp = open(xml_path, 'w+', encoding='utf-8')
    DOMTree.writexml(fp, indent='', addindent='', newl='', encoding='utf-8')
    fp.close()


def ga_sim(color1, color2, color3, time1, time2, fog, rain, sun, seed_name, data_collection_para):
    mutation_name = seed_name.split("_")

    complete_mutation_name = "seed_0_0_" + mutation_name[3]
    parse_path = "../seed_pool/" + complete_mutation_name

    # 使用minidom解析器打开 XML 文档
    DOMTree = xml.dom.minidom.parse(parse_path)
    #   Linux下改一下路径

    ele = DOMTree.documentElement
    e = ele.getElementsByTagName("Entities")[0]
    s = ele.getElementsByTagName("Storyboard")[0]

    xml_path = '../seed_pool/' + seed_name
    variable = {"name": "origin & random parameters"}
    rand_para = [color1, color2, color3, time1, time2, fog, rain, sun]
    Simulation(rand_para, variable, e, s)
    # print(variable)
    writeBack(xml_path, DOMTree)
    os.system("bash ./ga_sim.sh " + seed_name)
    data_collection_para = str(data_collection_para[0])+','+str(data_collection_para[1])+','+str(data_collection_para[2])
    os.system("python ga_error_test.py " + model_name + " 1 " + seed_name + " " + data_collection_para)

    error = 0
    div = 0
    list_er = []

    with open('list.txt', 'r') as p:
        for line in p:
            list_er.append(eval(line))
            # print(type(eval(line)))

    with open('./error_count.csv', 'r') as f:
        rows = len(f.readlines()) - 1
        f.seek(0)
        for i, line in enumerate(f):
            if i == 0:
                continue
            if line.split(',')[0] == seed_name:
                error = int(line.split(',')[1])
                div = int(line.split(',')[2])

    return error, div, list_er
