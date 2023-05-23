# fit_funcs.py
import numpy as np
import math
from generate_carla import *


def fuzzscene_f1(x, pop_seed_name, error_number, choose_time, list_er, domain_flags, data_collection_para):
    d = np.loadtxt("diversity_count.txt")  # load diversity and entropy count metrix
    e = np.loadtxt("entropy.txt")

    diversity_sum = 0.0
    seed_str = pop_seed_name.split("_")[3]
    seed_number = int(seed_str[0])
    iterate = int(pop_seed_name.split("_")[1])

    if error_number is None:    # exclude seed already after ga_err_test
        error_number, div, list_er = ga_sim(x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7], pop_seed_name, domain_flags,
                                            data_collection_para)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", error_number, div)
        
        for i in range(len(list_er)):   # cal diversity metrix
            if iterate != 0:
                d[seed_number - 1, int(list_er[i])] = d[seed_number - 1, int(list_er[i])] * 0.5
        
        if data_collection_para[0]:  # update err metrix for entropy
            for i in range(len(list_er)):
                e[seed_number - 1, int(list_er[i])] += 1
            np.savetxt("entropy.txt", e)

    for i in range(len(list_er)):
        diversity_sum = d[seed_number - 1, int(list_er[i])] + diversity_sum

    np.savetxt("diversity_count.txt", d)

    # diversity_total = diversity_sum / 125.0 + math.exp(-choose_time)
    diversity_total = math.exp(-choose_time)
    err = error_number / 125.0

    return [err, diversity_total], error_number, list_er


def get_zdt(domain_flags):
    f_funcs = fuzzscene_f1
    ori_domain = [[0, 255], [0, 255], [0, 255], [1, 12], [8, 16], [0, 200], [0, 100], [30, 100]]
    # 
    domain = ori_domain
    for i in range(len(domain_flags)):
        if not domain_flags[i]:
            if i == 0:
                domain[0] = [0, 0]
                domain[1] = [0, 0]
                domain[2] = [255, 255]
            elif i == 1:
                domain[3] = [3, 3]
                domain[4] = [12, 12]
            else:
                domain[i] = domain[i]
    return f_funcs, domain


def cal_entropy(metrix):
    sum_err = metrix.sum()
    entropy = 0.0
    if sum_err == 0:
        return entropy
    for i in np.nditer(metrix):
        if i != 0:
            p = i / sum_err
            entropy += -1 * (p * math.log2(p))
    return entropy


def cal_fitness(R):
    b = np.loadtxt("entropy.txt")
    ori_entropy = cal_entropy(b)
    # 6为seed_num
    temp_metrix = np.random.random_integers(1, 1, size=(6, 125))
    flag = (b == temp_metrix).all()     # is origin 24 seeds or not
    for r in R:
        b = np.loadtxt("entropy.txt")
        ori_entropy = cal_entropy(b)
        pop_seed_name = r.pop_seed_name
        seed_str = pop_seed_name.split("_")[3]
        seed_number = int(seed_str[0])
        list_er = r.list_er
        entropy = ori_entropy
        if not flag:    # exclude first 24 seeds which entropy
            for i in range(len(list_er)):
                b[seed_number - 1, int(list_er[i])] += 1
            entropy = cal_entropy(b)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", r.f)
        if len(r.f) < 3:
            r.f.append(entropy - ori_entropy)
        else:
            r.f[2] = entropy - ori_entropy
        with open('r_list.csv', 'a+', encoding='utf-8') as el:  # save entropy
            cw = csv.writer(el)
            cw.writerow([r.pop_seed_name, r.dna, r.f, entropy - ori_entropy])

