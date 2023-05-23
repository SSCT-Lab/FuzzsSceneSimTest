# fuzz_ga.py
import numpy as np
from ga import *
import matplotlib.pyplot as plt
from fit_funcs import *

domain_flags = [True,True,True,True,True]
f_funcs, domain = get_zdt(domain_flags)
pri_queue_size = 50
pop_size = 24
dna_size = len(domain)
print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", dna_size)
pm = 0.05

if_sampling = True
is_err_collection = 1

# 下面的文件中6为seed_num
a = np.random.random_integers(0, 0, size=(6, 125))

np.savetxt("diversity.txt", a)

b = np.random.random_integers(1, 1, size=(6, 125))

np.savetxt("diversity_count.txt", b)

c = np.random.random_integers(1, 1, size=(6, 125))

np.savetxt("entropy.txt", c)

ga = ga(pri_queue_size=pri_queue_size, pop_size=pop_size, dna_size=dna_size, pc=1, pm=0.05, f_funcs=f_funcs, domain=domain, domain_flags=domain_flags)

P = ga.ini_pop(if_sampling)
print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", len(P))

ga.run_sim(P, [False, is_err_collection, 2])     # data_collection_para : [is_new_seed(for entropy), is_err_collection(collect err or not), err_type(collect normal(1)/sampling(2)/random data(3))]
ga.calculate_pop_fitness(P)

R = P

N = 20
for i in range(N):
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", '# GA Loop: ', i)
    # 选择
    P, R = ga.select(R)  # P:selected_seed, R:all_seed
    # 交叉
    Q = ga.pop_cross(P, i + 1)
    # 变异
    Q = ga.pop_mutation(Q)
    # 模拟
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'All Seeds: ', R)
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'New Seeds: ', Q)
    ga.run_sim(R, [False, 0, 0])
    ga.run_sim(Q, [True, is_err_collection, 1])
    # 合并  
    R = R + Q
    # 计算适应度
    ga.calculate_pop_fitness(R)
    ga.calulate_pop(R, pri_queue_size)

print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# GA Loop Test Over", '\n')
