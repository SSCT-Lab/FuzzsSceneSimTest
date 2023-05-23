# ga.py
import numpy as np
import random
import fit_funcs
from sampling import *


class Individual:
    def __init__(self, dna, pop_seed_name):
        self.dna = dna
        self.dna_len = len(dna)
        self.pop_seed_name = pop_seed_name
        self.f = []
        self.choose_time = 0
        self.error_number = None
        self.list_er = []

    def __lt__(self, other):
        if self.f[0] > other.f[0]:
            return True

        return False

    def get_fitness(self, F, domain_flags, data_collection_para):
        # len_F = len(F)
        # fitness = np.zeros(shape=len_F)
        # for i in range(len_F):
        fitness, error_number, list_er = F(self.dna, self.pop_seed_name, self.error_number, self.choose_time,
                                           self.list_er, domain_flags, data_collection_para)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", type(fitness))
        return fitness, error_number, list_er

    # def cross_newObj(self, p1, p2,iterate_time):
    #     # 不能修改之前的，需要生成一个新的对象
    #     p1_dna = p1.dna.copy()
    #     p2_dna = p2.dna.copy()
    #     cut_i = np.random.randint(1, self.dna_len - 1)
    #     temp = p1_dna[cut_i:].copy()
    #     p1_dna[cut_i:] = p2_dna[cut_i:]
    #     p2_dna[cut_i:] = temp

    #     cross_name=p1.pop_seed_name.split("_")
    #     p1_name="seed_"+iterate_time+"_1_"+cross_name[3]
    #     cross_name=p2.pop_seed_name.split("_")
    #     p2_name="seed_"+iterate_time+"_2_"+cross_name[3]
    #     q1 = individual(p1_dna,p1_name)
    #     q2 = individual(p2_dna,p2_name)
    #     return q1, q2

    def cross(self, p1, p2):
        cut_i = np.random.randint(1, self.dna_len - 1)
        temp = p1[cut_i:].copy()
        p1[cut_i:] = p2[cut_i:]
        p2[cut_i:] = temp
        return p1, p2

    # def mutation(self, domain):
    #     point = np.random.randint(0, self.dna_len)
    #     low = domain[point][0]
    #     high = domain[point][1]
    #     self.dna[point] = random.randint(low,high)
    #     return self.dna

    # 实数编码,SBX交叉
    def cross_newObj(self, domain, p1, p2, iterate_time, m, n):
        eta_c = 10
        pcross_real = 1.0  # ori : 0.9
        # 如果随机概率大于交叉概率则不进行交叉操作
        p1_dna = p1.dna.copy()
        p2_dna = p2.dna.copy()

        if random.random() <= pcross_real:
            # 对两个个体执行SBX交叉操作
            for j in range(self.dna_len):
                # 判断是否对某自变量交叉
                if random.random() > 0.5:
                    continue
                # 如果两个体某自变量相等则不操作
                if p1_dna[j] == p2_dna[j]:
                    continue
                # 对某自变量交叉
                y1 = min(p1_dna[j], p2_dna[j])
                y2 = max(p1_dna[j], p2_dna[j])
                ylow = domain[j][0]
                yup = domain[j][1]
                r = random.random()
                beta = 1.0 + (2.0 * (y1 - ylow) / (y2 - y1))
                alpha = 2.0 - beta ** (-(eta_c + 1.0))
                if r <= (1.0 / alpha):
                    betaq = (r * alpha) ** (1.0 / (eta_c + 1.0))
                else:
                    betaq = (1.0 / (2.0 - r * alpha)) ** (1.0 / (eta_c + 1.0))
                child1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1))

                beta = 1.0 + (2.0 * (yup - y2) / (y2 - y1))
                alpha = 2.0 - beta ** (-(eta_c + 1.0))
                if r <= (1.0 / alpha):
                    betaq = (r * alpha) ** (1.0 / (eta_c + 1.0))
                else:
                    betaq = (1.0 / (2.0 - r * alpha)) ** (1.0 / (eta_c + 1.0))
                child2 = 0.5 * ((y1 + y2) - betaq * (y2 - y1))

                if str(child1) == 'nan':
                    child1 = 0
                if str(child2) == 'nan':
                    child2 = 0
                child1 = min(max(child1, ylow), yup)
                child2 = min(max(child2, ylow), yup)
                p1_dna[j] = child1
                p2_dna[j] = child2

        cross_name = p1.pop_seed_name.split("_")
        p1_name = "seed_" + iterate_time + "_" + str(m) + "_" + cross_name[3]
        cross_name = p2.pop_seed_name.split("_")
        p2_name = "seed_" + iterate_time + "_" + str(n) + "_" + cross_name[3]
        q1 = Individual(p1_dna, p1_name)
        q2 = Individual(p2_dna, p2_name)

        return q1, q2

    def mutation(self, domain, pm):
        eta_m = 5
        for j in range(self.dna_len):
            r = random.random()
            # 对个体某变量进行变异
            if r <= pm:
                y = self.dna[j]
                ylow = domain[j][0]
                yup = domain[j][1]
                delta1 = 1.0 * (y - ylow) / (yup - ylow)
                delta2 = 1.0 * (yup - y) / (yup - ylow)
                # delta=min(delta1, delta2)
                r = random.random()
                mut_pow = 1.0 / (eta_m + 1.0)
                if r <= 0.5:
                    xy = 1.0 - delta1
                    val = 2.0 * r + (1.0 - 2.0 * r) * (xy ** (eta_m + 1.0))
                    deltaq = val ** mut_pow - 1.0
                else:
                    xy = 1.0 - delta2
                    val = 2.0 * (1.0 - r) + 2.0 * (r - 0.5) * (xy ** (eta_m + 1.0))
                    deltaq = 1.0 - val ** mut_pow
                y = y + deltaq * (yup - ylow)
                y = min(yup, max(y, ylow))
                self.dna[j] = y
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", self.dna)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "77777777777777777777777777777")


class ga:
    def __init__(self, pri_queue_size, pop_size, dna_size, pc, pm, f_funcs, domain, domain_flags):
        self.pri_queue_size = pri_queue_size
        self.pop_size = pop_size
        self.dna_size = dna_size
        # self.f_size = len(f_funcs)
        self.pc = pc
        self.pm = pm
        self.f_funcs = f_funcs
        self.domain = domain
        self.domain_flags = domain_flags

    def ini_pop(self, if_sampling):
        rseed = 8.35
        random.seed(rseed)
        if not if_sampling:
            # 24x8 随机参数矩阵  初始种群24x可变参数8
            P_dna = np.random.random_integers(0, 0, size=(self.pop_size, self.dna_size))
            for i in range(self.pop_size):
                for j in range(len(self.domain)):
                    item = self.domain[j]
                    P_dna[i, j] = random.randint(item[0], item[1])
            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", P_dna)
            P = []  # 初始种群P\
            Seed_ini = []
            for j in range(self.pop_size):
                seed = "seed_0_" + str(j + 1) + "_" + str(int(j / 4) + 1) + ".xosc"
                Seed_ini.append(seed)
            for m in range(self.pop_size):
                P.append(Individual(P_dna[m], Seed_ini[m]))
        else:
            # 72x8
            P_dna = np.random.random_integers(0, 0, size=(3 * self.pop_size, self.dna_size))
            # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", P_dna)
            for i in range(3 * self.pop_size):
                for j in range(len(self.domain)):
                    item = self.domain[j]
                    P_dna[i, j] = random.randint(item[0], item[1])
            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", P_dna)
            P = []  # 初始种群P\
            Seed_ini = []
            Sample_predict = []
            choose = [0, 0, 0, 0, 0, 0]
            choose_result = []
            for j in range(3 * self.pop_size):
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Sampling # ", j)
                # 前25个 前12：0_j+1_4  后12：0_j+1_5  0_25_6
                if int(j / 12) + 4 <= 6:
                    num = int(j / 12) + 4
                    seed = "seed_0_" + str(j + 1) + "_" + str(num) + ".xosc"
                # 26-72个 0_26_1 0_27_1 ... 0_49_2 0_50_2... 0_61_
                else:                                                                                               
                    num = int((j-36)/ 12 + 1)
                    seed = "seed_0_" + str(j + 1) + "_" + str(num) + ".xosc"
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Generate seed_name: ", seed)
                #seed = "seed_0_" + str(j + 1) + "_" + str(int(j / 12) + 1) + ".xosc"
                Seed_ini.append(seed)
                cal_number = sample_simulate(P_dna[j, 0], P_dna[j, 1], P_dna[j, 2], P_dna[j, 3], P_dna[j, 4],
                                             P_dna[j, 5], P_dna[j, 6], P_dna[j, 7], seed, self.domain_flags)
                Sample_predict.append(cal_number)
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "*******************")
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "VGG error num:"+str(cal_number))
                with open('r_list.csv', 'a+', encoding='utf-8') as l:  # save entropy
                    cw = csv.writer(l)
                    cw.writerow([seed, P_dna[j], cal_number])
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", Sample_predict)

            Sample_predict = np.array(Sample_predict)
            # 加负号后按从大到小排序
            result = np.argsort(-Sample_predict)
            # result为错误数排序后的下标索引数组
            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Sample_predict result:", result)

            for m in range(3 * self.pop_size):  # select top 4 seed of every scene( 4 * 6 ) from 72 seeds
                # 12应该为pop_size * 3 / seed_num
                loc = int(result[m] / 12)
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "loc:", loc)
                # 4应该为pop_size / seed_num
                if (choose[loc] < 4):
                    choose[loc] = choose[loc] + 1
                    choose_result.append(result[m])

            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Sample choose_result:", choose_result)

            for m in range(self.pop_size):
                # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", P_dna[m])
                # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", Seed_ini[m])
                P.append(Individual(P_dna[choose_result[m]], Seed_ini[choose_result[m]]))

        return P

    # def calculate_pop_fitness(self, pop, total):
    #     # 计算Pop适应度，存储到individual中
    #     normalization = []
    #     #for p in pop:
    #     #    if p.f == None:
    #     #        p.f, p.error_number, p.list_er = p.get_fitness(self.f_funcs, total)
    #     for p in pop:
    #         p.f, p.error_number, p.list_er = p.get_fitness(self.f_funcs, total)
    #         print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", p.f)
    #     return pop

    def run_sim(self, R,
                data_collection_para):  # data_collection_para : [is_new_seed(for entropy), is_err_collection(collect err or not), err_type(collect normal(1)/sampling(2)/random data(3))]
        for p in R:
            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Run sim index: # ", R.index(p))
            p.f, p.error_number, p.list_er = p.get_fitness(self.f_funcs, self.domain_flags, data_collection_para)
            print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", p.f, '\n')

    def select(self, R):
        R.sort()
        choose_number = [0, 0]
        # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", len(R))
        pop = []
        for i in range(2):
            randomnumber1 = random.randint(0, self.pop_size - 1)
            randomnumber2 = random.randint(0, self.pop_size - 1)
            randomnumber3 = random.randint(0, self.pop_size - 1)
            randomnumber4 = random.randint(0, self.pop_size - 1)
            if R[randomnumber1].f > R[randomnumber2].f:
                pop.append(R[randomnumber1])
                choose_number[0] = randomnumber1
            else:
                pop.append(R[randomnumber2])
                choose_number[0] = randomnumber2
            if R[randomnumber3].f > R[randomnumber4].f:
                pop.append(R[randomnumber3])
                choose_number[1] = randomnumber3
            else:
                pop.append(R[randomnumber4])
                choose_number[1] = randomnumber4

            R[choose_number[0]].choose_time = R[choose_number[0]].choose_time + 1
            R[choose_number[1]].choose_time = R[choose_number[1]].choose_time + 1
        return pop, R

    def pop_cross(self, P, iterate_time):
        new_Q = []
        i = 0
        P_len = len(P)
        while i < P_len:
            # q1, q2 = P[i].cross_newObj(P[i], P[i + 1],str(iterate_time))
            q1, q2 = P[i].cross_newObj(self.domain, P[i], P[i + 1], str(iterate_time), (i + 1), (i + 2))
            new_Q.append(q1)
            new_Q.append(q2)
            i += 2
        return new_Q

    def pop_mutation(self, Q):
        for q in Q:
            q.mutation(self.domain, self.pm)
        return Q

    def calulate_pop(self, R, pri_queue_size):
        R.sort()
        return R[:pri_queue_size]

    def calculate_pop_fitness(self, R):
        fit_funcs.cal_fitness(R)
        normalization = []
        for r in R:
            normalization.append(r.f[2])
        max_val = max(normalization)
        min_val = min(normalization)
        minus = max_val - min_val if max_val - min_val != 0 else 1
        for r in R:
            r.f[0] += r.f[1] + (r.f[2] - min_val) / minus
