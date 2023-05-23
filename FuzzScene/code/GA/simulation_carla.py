import sys
import os
from multiprocessing import Process
import time
import signal
import subprocess
import psutil
from datetime import datetime
import shutil

sys.path.append("/home/vangogh/software/FuzzScene/code/")
import Constants

mutation_name=sys.argv[1]
print(mutation_name)
# -RenderOffScreen  ./CarlaUE4.sh -quality-level=low -fps=15 -windowed -Resx=600 -Resy=480
def work1():
    os.system("timeout 150 ~/software/CARLA_0.9.13/CarlaUE4.sh -carla-streaming-port=0 -carla-rpc-port=3000")

def work2():
    os.system("timeout 100 python3 ~/software/FuzzScene/code/scenario_runner-0.9.13/scenario_runner.py --output --openscenario ~/software/FuzzScene/code/seed_pool/"+mutation_name+" --sync --reloadWorld")

def work3():
    os.system("timeout 100 python3 ~/software/FuzzScene/code/scenario_runner-0.9.13/manual_control.py -a")

def work4():
    os.system("timeout 85 pkill -9 python && pkill -9 Carla")

def work5():
    os.system("timeout 50 python3 ~/software/FuzzScene/code/scenario_runner-0.9.13/steering-curve.py --type ori_gif --name" + mutation_name)


def timetask(times):
  time.sleep(times)
  print (time.localtime())

# '-RenderOffScreen',
def works():
    command = ['/home/vangogh/software/CARLA_0.9.13/CarlaUE4.sh', '-carla-streaming-port=0', '-carla-rpc-port=3000']
    sim_subprocessA = subprocess.Popen(command)
    print("*********************************")
    print("Carla Server Started")
    # proc_record.append(p1)
    time.sleep(10)
    commandB = ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/scenario_runner.py', 
               '--output', '--openscenario', '/home/vangogh/software/FuzzScene/code/seed_pool/'+ mutation_name, '--sync', '--reloadWorld']
    sim_subprocessB = subprocess.Popen(commandB)
    # p2.start()
    print("*********************************")
    print("OpenScenario Simutation Started")
    time.sleep(10)
    # proc_record.append(p2)
    commandC = ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/manual_control.py', '-a', '--name', '', '--type', 'normal']
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
            break

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
        # pass

    os.system("pkill -9 Carla")
    print("*******************")
    print("KILL Carla")

    for proc in psutil.process_iter(['pid', 'cmdline']):
        if proc.info['cmdline'] == ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/scenario_runner.py', 
               '--output', '--openscenario', '/home/vangogh/software/FuzzScene/code/seed_pool/'+ mutation_name, '--sync', '--reloadWorld']:
            proc.kill()
            print("*******************")
            print("KILL scenario_runner")

        if proc.info['cmdline'] == ['python3', '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/manual_control.py', '-a', '--name', '', '--type', 'normal']:
            proc.kill()
            print("*******************")
            print("KILL manual_control")
    print("*******************")
    print("CLEAR output in simulation_carla")
    
def clear_output():
    path = '/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/_out/'
    shutil.rmtree(path + 'center')  # 清空out
    os.mkdir(path + 'center')
    if os.path.exists(path + 'label_test.csv'):  # 清空label_test_null.csv
        os.remove(path + 'label_test.csv')
    shutil.copy(path + 'label_test_null.csv', path + 'label_test.csv')

# tensorflow 2.5 keras 2.4.3
if __name__ == '__main__':
    for i in range(5):
        clear_output()
        works()
        clear_processes()
        with open('/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/_out/label_test.csv', 'r') as f:
            rows = len(f.readlines()) - 1
            print("\nsimulation_carla generated ### " + str(rows) + "_images")
            if (rows >= 100):
                print("\nsimulation_carla SUCCESS finished!\n")
                break
            else:
                if i == 4:
                    print("\nsimulation_carla FAILED 5 times. EXIT WITH -1\n")
                else:
                    print("\nsimulation_carla FAILED No." + str(i + 1) + " times, retrying...\n")
