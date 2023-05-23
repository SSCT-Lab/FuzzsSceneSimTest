import csv
import os
import shutil


def get_ori_img(ori_file_path):
    ori_img_list = []
    with open(ori_file_path, 'r') as f:
        f.seek(0)
        for i, line in enumerate(f):
            if i == 0:
                continue
            data = line.split(',')
            ori_img_id = data[0]
            # ori_carla_angle = float(data[3])
            # ori_img_predict_angle = float(data[1])
            # ori_img_data = [ori_img_id, ori_img_predict_angle]
            ori_img_list.append(ori_img_id)
    f.close()
    return ori_img_list


def get_err_img(mut_csv_path, ori_csv_path):
    img_list = [[], [], [], [], [], []]
    temp_str = ''
    with open(mut_csv_path, 'r') as f:     # get error csv 
        f.seek(0)
        for i, line in enumerate(f):
            if i == 0:
                continue
            data = line.split(',')
            if len(data) != 8 and temp_str == '':
                temp_str = line
                continue
            if temp_str != '':
                line = temp_str + line
                temp_str = ''
            data = line.split(',')
            img_id = data[0]
            predict_angle = float(data[1])
            mut_angle = float(data[2])
            if data[3][0] == '"':
                data[3] = data[3].split('"')[1]
            ori_angle = float(data[3])
            model_name = data[4]
            scene_name = int(data[5])
            frame_id = int(data[6])
            data = [img_id, predict_angle, mut_angle, ori_angle, model_name, scene_name, frame_id]
            img_list[scene_name - 1].append([data, abs(mut_angle - ori_angle)])

    list_a = []     # get top 20
    for l in img_list:
        l.sort(key=lambda x: x[1], reverse=True)
        if len(l) > 20:
            l = l[:20]
        list_a.append(l)
    img_list = list_a

    ori_img_list = get_ori_img(ori_csv_path)
    ret_img_list = [[], [], [], [], [], []]
    for l in img_list:
        for img in l:
            data = img[0]
            img_id = data[0]
            predict_angle = float(data[1])
            mut_angle = float(data[2])
            ori_angle = float(data[3])
            model_name = data[4]
            scene_name = int(data[5])
            frame_id = int(data[6])
            true_frame_id = frame_id + (scene_name - 1) * 125  # get ori img id
            ori_img_id = ori_img_list[true_frame_id]
            ret_img_list[scene_name - 1].append(
                [img_id, ori_img_id, ori_angle, predict_angle, mut_angle, scene_name, model_name, frame_id])
    return ret_img_list


def save_csv(img_list, save_path):
    with open(save_path, 'a+', encoding='utf-8') as f:
        csv_writer = csv.writer(f)
        for line in range(len(img_list)):
            csv_writer.writerow(img_list[line])


def save_img(srcfile, save_path):
    if not os.path.isfile(srcfile):
        print("%s not exist!" % srcfile)
    else:
        if not os.path.exists(save_path):
            os.makedirs(save_path)  # 创建路径
        shutil.copy(srcfile, save_path)  # 复制文件
        print("copy %s -> %s" % (srcfile, save_path))


def save_err_img(img_list_by_model, mut_file_path, save_path):
    for img in img_list_by_model:
        #ori_img_path = ori_file_path + img_by_scene[1]
        #save_img(ori_img_path, ori_save_path)
        mut_img_path = mut_file_path + img[0]
        save_img(mut_img_path, save_path)
        
        


# [dave v1, v2, v3, epoch] origin data
ori_file_path = '/home/jlutripper/Document/seed_data_train/'
mut_file_path = '/home/software/FuzzScene/code/Violated images/erimages/model_'
save_path = 'err_img/'
for i in range (1,5):
    mut_csv_path = mut_file_path + str(i) + '/error.csv'
    ori_img_path = ori_file_path + 'seed' + str(i) +'/'
    err_img_list = get_err_img(mut_csv_path = mut_csv_path, ori_csv_path=ori_file_path+'model1_oriMSE.csv')
    for j in range (1,7):
        save_csv(err_img_list[j-1],save_path = save_path + str(i) + '/' + str(j) +'/list.csv')
        save_err_img(err_img_list[j-1], mut_file_path + str(i) + '/', save_path + str(i) + '/' + str(j) + '/')

# 输入： 6*125*4 4个模型 对于6个初始场景的初始预测值
#       4个模型 100个变异种子*125张图中 发现的错误图像
# 输出： 转向角误差从大到小（绝对值）排列的 每个模型 每个场景
