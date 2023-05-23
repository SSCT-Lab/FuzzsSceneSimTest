import os, sys
import shutil

model_name="1"
if_sampling="True"
fitness_func="all"

error_count_file_name = "error_count_"+model_name+"_"+if_sampling+"_"+fitness_func+".csv"
src = "./" + error_count_file_name
dest = "./ga_output/" + error_count_file_name
print("************************************************************************************")
print(src)
shutil.copy("./error_count.csv", src)
if os.path.exists(dest):
    os.remove(dest)
shutil.move(src, dest)

entropy_file_name = "entropy_"+model_name+"_"+if_sampling+"_"+fitness_func+".csv"
src = "./" + entropy_file_name
dest = "./ga_output/entropy/" + entropy_file_name
shutil.copy("./entropy.txt", src)
if os.path.exists(dest):
    os.remove(dest)
shutil.move(src, dest)

r_list_file_name = "r_list_"+model_name+"_"+if_sampling+"_"+fitness_func+".csv"
src = "./" + r_list_file_name
dest = "./ga_output/r_list" + r_list_file_name
shutil.copy("./r_list.csv",src)
if os.path.exists(dest):
    os.remove(dest)
shutil.move(src, dest)
