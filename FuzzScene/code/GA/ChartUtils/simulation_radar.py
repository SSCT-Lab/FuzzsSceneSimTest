import sys
import os
from multiprocessing import Process
import time
import signal
import subprocess
import psutil
from datetime import datetime
import shutil
import csv

sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants

mutation_name = sys.argv[1]

def timetask(times):
    time.sleep(times)
    print(time.localtime())

def works():
    command = ['/home/vangogh/software/CARLA_0.9.13/CarlaUE4.sh', '-carla-streaming-port=0', '-carla-rpc-port=3000']
    sim_subprocessA = subprocess.Popen(command)
    print("*********************************")
    print("Carla Server Started")
    # proc_record.append(p1)
    time.sleep(10)
    commandB = ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/scenario_runner.py', 
               '--output', '--openscenario', Constants.RADAR_SEED_POOL + mutation_name, '--sync', '--reloadWorld']
    sim_subprocessB = subprocess.Popen(commandB)
    # p2.start()
    print("*********************************")
    print("OpenScenario Simutation Started")
    time.sleep(10)
    # proc_record.append(p2)
    commandC = ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/manual_control.py', '-a', '--name', mutation_name, '--type', 'radar']
    sim_subprocessC = subprocess.Popen(commandC)
    print("*********************************")
    print("Collecting Data")

    timeout = 85
    start_time = time.time()
    print("TIMEOUT EVENT BEGIN AT:" + str(datetime.now()))
    while True:
        returncode = sim_subprocessC.poll()
        if returncode is None:
            elapsed_time = time.time() - start_time
            if (elapsed_time > timeout):
                print("!!! scenario runner TIMEOUT!!!")
                break
            time.sleep(5)
        else:
            print("!!! scenario runner FINISHED!!!")
            break;
    
    # try:
    #     # 从collecting Data到140帧时间为75秒左右，放宽限制至100秒的超时
    #     print("TIMEOUT EVENT BEGIN AT:" + str(datetime.now()))
    #     sim_subprocessB.wait(timeout=85)
    # except subprocess.TimeoutExpired:
    #     print("!!! scenario runner TIMEOUT!!!" + str(datetime.now()))

def clear_processes():
    # try:
    #     output = subprocess.check_output(['pgrep', '-f', 'CarlaUE4-Linux-Shipping'])
    #     if output:
    #         carla_id = int(output.strip())
    #     if carla_id:
    #         os.kill(carla_id, signal.SIGTERM)
    #         print("*******************")
    #         print("KILL CarlaUE4-Linux-Shipping")
    # except subprocess.CalledProcessError:
    #     pass

    # try:
    #     output = subprocess.check_output(['pgrep', '-f', 'CarlaUE4.sh'])
    #     if output:
    #         carla_id = int(output.strip())
    #     if carla_id:
    #         os.kill(carla_id, signal.SIGTERM)
    #         print("*******************")
    #         print("KILL CarlaUE4.sh")
    # except subprocess.CalledProcessError:
    #     pass

    os.system("pkill -9 Carla")

    for proc in psutil.process_iter(['pid', 'cmdline']):
        if proc.info['cmdline'] == ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/scenario_runner.py', 
               '--output', '--openscenario', Constants.RADAR_SEED_POOL + mutation_name, '--sync', '--reloadWorld']:
            proc.kill()
            print("*******************")
            print("KILL scenario_runner")

        if proc.info['cmdline'] == ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/manual_control.py', '-a', '--name', mutation_name, '--type', 'radar']:
            proc.kill()
            print("*******************")
            print("KILL manual_control")
    print("*******************")
    print("CLEAR output")
    
def clear_output():
    path = Constants.CARLA_RADAR_PNG_OUTPUT_PATH
    shutil.rmtree(path)  # 清空out
    os.mkdir(path)

    seed_pool_path = Constants.RADAR_SEED_POOL
    if os.path.exists(seed_pool_path):
        shutil.rmtree(seed_pool_path)  # 清空out
    shutil.copytree(Constants.RADAR_SEED_POOL_BAK, seed_pool_path)

    label_path = Constants.CARLA_RADAR_LABEL_OUTPUT_PATH
    if os.path.exists(label_path + 'label_test.csv'):  # 清空label_test_null.csv
        os.remove(label_path + 'label_test.csv')
    shutil.copy(label_path + 'label_test_null.csv', label_path + 'label_test.csv')

def clear_add_output(n):
    rows = []
    with open(Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + 'label_test.csv', 'r') as csvfile:
        reader = csv.reader(csvfile)
        for row in reader:
            rows.append(row)
    last_n_rows = rows[n:]
    second_column = [row[1] for row in last_n_rows]
    for value in second_column:
        os.remove(Constants.CARLA_RADAR_PNG_OUTPUT_PATH + value)
    # 删除最后n行
    rows = rows[0 : n]
    # 将修改后的数据写回CSV文件
    with open(Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + 'label_test.csv', 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerows(rows)

# tensorflow 2.5 keras 2.4.3
if __name__ == '__main__':
    # clear_output()

    begin_row = 1
    with open(Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + 'label_test.csv', 'r') as f:
        begin_row = len(f.readlines())
    
    for i in range(5):
        works()
        clear_processes()
        with open(Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + 'label_test.csv', 'r') as f:
            rows = len(f.readlines())
            add_row = rows - begin_row
            print("\nsimulation_carla generated ### " + str(add_row) + "_images")
            if (add_row >= 120):
                print("\nsimulation_carla SUCCESS finished!\n")
                break
            else:
                clear_add_output(begin_row)
                if i == 4:
                    print("\nsimulation_carla FAILED 5 times. EXIT WITH -1\n")
                else:
                    print("\nsimulation_carla FAILED No." + str(i + 1) + " times, retrying...\n")
