import os
from generate_carla import *
from sampling_predict import *
import pandas as pd

model_name = "1"


def sample_simulate(color1, color2, color3, time1, time2, fog, rain, sun, seed_name, domain_flags):
    mutation_name = seed_name.split("_")

    complete_mutation_name = "seed_0_0_" + mutation_name[3]
    parse_path = "../seed_pool/" + complete_mutation_name
    seed_number = mutation_name[3][0]
    # 使用minidom解析器打开 XML 文档
    DOMTree = xml.dom.minidom.parse(parse_path)
    #   Linux下改一下路径
    ele = DOMTree.documentElement
    e = ele.getElementsByTagName("Entities")[0]
    s = ele.getElementsByTagName("Storyboard")[0]
    
    xml_path = '../seed_pool/' + seed_name
    variable = {"name": "origin & random parameters"}
    rand_para = [color1, color2, color3, time1, time2, fog, rain, sun]
    Simulation(rand_para, variable, e, s, domain_flags)
    # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", variable)
    writeBack(xml_path, DOMTree)
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "\n#############  BEFORE  RUNNING!  ##############\n")
    os.system("bash ./ga_sim.sh " + seed_name)
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "\n#############  AFTER  RUNNING!  ##############\n")
    sample_predict = prenum(model_name, seed_number)
    # os.system("python ga_error_test.py "+model_name+" 1 "+seed_name)
    # error=0
    # with open('./error_count.csv', 'r') as f:
    #     rows = len(f.readlines()) - 1
    #     f.seek(0)
    #     for i, line in enumerate(f):
    #         if i == 0:
    #             continue
    #         if line.split(',')[0]==seed_name:
    #             error= int(line.split(',')[1])

    test_dataset_path = '../scenario_runner-0.9.13/_out/label_test.csv'  # clear csv of each seed of sampling
    df = pd.read_csv(test_dataset_path)
    df.head(2)
    df = df.drop(df.index[0:250])
    df.to_csv(test_dataset_path, index=False, sep=',', encoding="utf-8")

    # path = '../scenario_runner-0.9.13/_out/'
    # shutil.rmtree(path + 'center')  # 清空out
    # os.mkdir(path + 'center')   

    return sample_predict
