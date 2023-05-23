from __future__ import print_function

import cv2
import glob
import os
import sys
import math
import random
import csv
import shutil
import imageio
import time
import tensorflow as tf
import numpy as np
import pandas as pd
import argparse

from data_utils import *
from tensorflow.keras.applications.imagenet_utils import preprocess_input
from keras_preprocessing import image
from tensorflow.keras.layers import Convolution2D, Input, Dense, Flatten, Lambda, MaxPooling2D, Dropout, Activation, \
    SpatialDropout2D
    # remove merge
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
from tensorflow.keras import models, optimizers, backend
from PIL import Image

##########################################移植模型代码用于生成预测值并得到转角曲线###############################################


def Dave_orig(input_tensor=None, load_weights=False):  # original dave
    if input_tensor is None:
        input_tensor = Input(shape=(100, 100, 3))
    x = Convolution2D(24, (5, 5), padding='valid', activation='relu', strides=(2, 2), name='block1_conv1')(input_tensor)
    x = Convolution2D(36, (5, 5), padding='valid', activation='relu', strides=(2, 2), name='block1_conv2')(x)
    x = Convolution2D(48, (5, 5), padding='valid', activation='relu', strides=(2, 2), name='block1_conv3')(x)
    x = Convolution2D(64, (3, 3), padding='valid', activation='relu', strides=(1, 1), name='block1_conv4')(x)
    x = Convolution2D(64, (3, 3), padding='valid', activation='relu', strides=(1, 1), name='block1_conv5')(x)
    x = Flatten(name='flatten')(x)
    x = Dense(1164, activation='relu', name='fc1')(x)
    x = Dense(100, activation='relu', name='fc2')(x)
    x = Dense(50, activation='relu', name='fc3')(x)
    x = Dense(10, activation='relu', name='fc4')(x)
    x = Dense(1, name='before_prediction')(x)
    x = Lambda(atan_layer, output_shape=atan_layer_shape, name='prediction')(x)

    m = Model(input_tensor, x)
    if load_weights:
        m.load_weights('./trained_models/Model1.h5')

    # compiling
    m.compile(loss='mse', optimizer='Adam')
    # m.compile(loss=[rmse], optimizer='adadelta')

    return m


def Dave_norminit(input_tensor=None, load_weights=False):  # original dave with normal initialization
    if input_tensor is None:
        input_tensor = Input(shape=(100, 100, 3))
    x = Convolution2D(24, (5, 5), padding='valid', activation='relu', strides=(2, 2),
                      name='block1_conv1')(input_tensor)
    x = Convolution2D(36, (5, 5), padding='valid', activation='relu', strides=(2, 2),
                      name='block1_conv2')(x)
    x = Convolution2D(48, (5, 5), padding='valid', activation='relu', strides=(2, 2),
                      name='block1_conv3')(x)
    x = Convolution2D(64, (3, 3), padding='valid', activation='relu', strides=(1, 1),
                      name='block1_conv4')(x)
    x = Convolution2D(64, (3, 3), padding='valid', activation='relu', strides=(1, 1),
                      name='block1_conv5')(x)
    x = Flatten(name='flatten')(x)
    x = Dense(1164, kernel_initializer=normal_init, activation='relu', name='fc1')(x)
    x = Dense(100, kernel_initializer=normal_init, activation='relu', name='fc2')(x)
    x = Dense(50, kernel_initializer=normal_init, activation='relu', name='fc3')(x)
    x = Dense(10, kernel_initializer=normal_init, activation='relu', name='fc4')(x)
    x = Dense(1, name='before_prediction')(x)
    x = Lambda(atan_layer, output_shape=atan_layer_shape, name='prediction')(x)

    m = Model(input_tensor, x)
    if load_weights:
        m.load_weights('./trained_models/Model2.h5')

    # compiling
    m.compile(loss='mse', optimizer='Adam')
    # m.compile(loss=[rmse], optimizer='adadelta')
    return m


def Dave_dropout(input_tensor=None, load_weights=False):  # simplified dave
    if input_tensor is None:
        input_tensor = Input(shape=(100, 100, 3))
    x = Convolution2D(16, (3, 3), padding='valid', activation='relu', name='block1_conv1')(input_tensor)
    x = MaxPooling2D(pool_size=(2, 2), name='block1_pool1')(x)
    x = Convolution2D(32, (3, 3), padding='valid', activation='relu', name='block1_conv2')(x)
    x = MaxPooling2D(pool_size=(2, 2), name='block1_pool2')(x)
    x = Convolution2D(64, (3, 3), padding='valid', activation='relu', name='block1_conv3')(x)
    x = MaxPooling2D(pool_size=(2, 2), name='block1_pool3')(x)
    x = Flatten(name='flatten')(x)
    x = Dense(500, activation='relu', name='fc1')(x)
    x = Dropout(.5)(x)
    x = Dense(100, activation='relu', name='fc2')(x)
    x = Dropout(.25)(x)
    x = Dense(20, activation='relu', name='fc3')(x)
    x = Dense(1, name='before_prediction')(x)
    x = Lambda(atan_layer, output_shape=atan_layer_shape, name="prediction")(x)

    m = Model(input_tensor, x)
    if load_weights:
        m.load_weights('./trained_models/Model3.h5')

    # compiling
    m.compile(loss='mse', optimizer=optimizers.Adam(lr=1e-04))
    # m.compile(loss=[rmse], optimizer='adadelta')
    return m


def Epoch_model(input_tensor=None, load_weights=False):
    if input_tensor is None:
        input_tensor = Input(shape=(128, 128, 3))

    x = Convolution2D(32, (3, 3), activation='relu', padding='same')(input_tensor)
    x = MaxPooling2D((2, 2), strides=(2, 2))(x)
    x = Dropout(0.25)(x)

    x = Convolution2D(64, (3, 3), activation='relu', padding='same')(x)
    x = MaxPooling2D((2, 2), strides=(2, 2))(x)
    x = Dropout(0.25)(x)

    x = Convolution2D(128, (3, 3), activation='relu', padding='same')(x)
    x = MaxPooling2D((2, 2), strides=(2, 2))(x)
    x = Dropout(0.5)(x)

    y = Flatten()(x)
    y = Dense(1024, activation='relu')(y)
    y = Dropout(.5)(y)
    y = Dense(1)(y)

    m = Model(input_tensor, y)
    if load_weights:
        m.load_weights('./trained_models/Model4.h5')

    # compliling
    m.compile(loss='mse', optimizer=optimizers.Adam(lr=1e-04))
    # m.compile(loss=[rmse], optimizer='adadelta')

    return m

def calc_mse(yhat, label):  # used for loss cal, output float
    mse = 0.
    count = 0
    if len(yhat) != len(label):
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "yhat and label have different lengths")
        return -1
    for i in range(len(yhat)):
        count += 1
        predicted_steering = yhat[i]
        steering = label[i]
        # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", predicted_steering)
        # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", steering)
        mse += (float(steering) - float(predicted_steering)) ** 2.
    return (mse / count)


def model_predict(model_name):
    # Train variables define & Input variables parse
    # TODO
    if not isinstance(model_name, str):
        model_name = str(model_name)
    batch_size = 64
    nb_epoch = 30
    image_shape = (100, 100)
    # model_name = sys.argv[1]

    test_dataset_path = './scenario_runner-0.9.13/_out/'
    with open(test_dataset_path + 'label_test.csv', 'r') as f:
        rows = len(f.readlines()) - 1
        if rows == 0:
            return 0

    # --------------------------------------Build Model---------------------------------------- #
    # Dave_v1
    if model_name == '1':
        model = Dave_orig(None, True)
        save_model_name = './trained_models/Model1.h5'

    # Dave_v2
    elif model_name == '2':
        # K.set_learning_phase(1)
        model = Dave_norminit(None, True)
        save_model_name = './trained_models/Model2.h5'
        # batch_size = 64 # 1 2 3 4 5 6x
        nb_epoch = 30

    # Dave_v3
    elif model_name == '3':
        # K.set_learning_phase(1)
        model = Dave_dropout(None, True)
        save_model_name = './trained_models/Model3.h5'
        # nb_epoch = 30

    # Udacity Epoch Model
    elif model_name == '4':
        model = Epoch_model(None, True)
        save_model_name = './trained_models/Model4.h5'
        image_shape = (128, 128)
        nb_epoch = 30
        batch_size = 32
    else:
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", bcolors.FAIL + 'invalid model name, must in [1, 2, 3, 4]' + bcolors.ENDC)

    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", bcolors.OKGREEN + 'model %s built' % model_name + bcolors.ENDC)

    # --------------------------------------Evaluation---------------------------------------- #
    # Different evaluation methods for different model
    png_name_list = []
    if model_name != '4':
        K.set_learning_phase(0)
        test_generator, samples_per_epoch = load_carla_test_data(path=test_dataset_path, batch_size=batch_size,
                                                                 shape=image_shape)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'test samples: ', samples_per_epoch)
        loss = model.evaluate(test_generator, steps=math.ceil(samples_per_epoch * 1. / batch_size), verbose=1)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "model %s evaluate_generator loss: %.8f" % (model_name, loss))
        # --------------------------------------Predict Dave---------------------------------------- #
        true_angle_list = []
        filelist = []
        with open(test_dataset_path + 'label_test.csv', 'r') as f:
            rows = len(f.readlines()) - 1
            f.seek(0)
            for i, line in enumerate(f):
                if i == 0:
                    continue
                file_name = line.split(',')[0]
                # TODO BEGIN
                # if i > int(rows * 0.75):
                filelist.append(test_dataset_path + 'center/' + file_name)
                png_name_list.append(file_name)
                true_angle_list.append(float(line.split(',')[2]))
                # TODO END

        # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'filelist length: ', len(filelist))
        # print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'true_angle_list', true_angle_list)

        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "--------IMG READ-------")
        predict_angle_list = []
        imgs = []
        raw_imgs = []
        count = 0
        ori_image_size = (720, 1280)
        for f in filelist:
            count += 1
            if (count % 100 == 0):
                print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", str(count) + ' images read')
            orig_name = f
            gen_img = preprocess_image(orig_name, image_shape)
            raw_img = preprocess_image(orig_name, ori_image_size)
            imgs.append(gen_img)
            raw_imgs.append(raw_img)
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "--------IMG READ COMPLETE-------")

        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "--------DAVE PREDICT-------")
        count = 0
        imgs = np.array(imgs)
        for i in range(len(imgs)):
            predict_angle_list.append(model.predict(imgs[i])[0])
            # TODO: Add arrows to raw images and save
            # gen_img_deprocessed = draw_arrow3(deprocess_image(raw_imgs[i], (720, 1280, 3)), -true_angle_list[i], -predict_angle_list[-1])
            # imsave('./test_output_carla/' + str(i) + 'th_img.png', gen_img_deprocessed)
            # count += 1
            # if (count % 20 == 0):
            #     print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", str(count) + ' images saved')
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "--------DAVE PREDICT COMPLETE-------")
        yhat = np.array([x.flatten()[0] for x in predict_angle_list])
        test_y = true_angle_list


    else:
        test_steering_log = path.join(test_dataset_path, 'label_test.csv')
        test_data = carla_load_steering_data(test_steering_log)
        png_name_list = get_png_name_list(test_steering_log)
        # dataset divide
        time_list_test = carla_load_frame_id(test_data)

        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'time_list_test: ', len(time_list_test), time_list_test)

        test_generator = carla_data_generator(frame_id=time_list_test,
                                              steering_log=test_steering_log,
                                              image_folder=test_dataset_path,
                                              unique_list=time_list_test,
                                              gen_type='test',
                                              batch_size=len(time_list_test),
                                              image_size=image_shape,
                                              shuffle=False,
                                              preprocess_input=normalize_input,
                                              preprocess_output=exact_output)

        # --------------------------------------Predict Epoch---------------------------------------- #
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "--------EPOCH PREDICT-------")
        test_x, test_y = next(test_generator)
        yhat = model.predict(test_x, verbose=1)
        yhat = [item[0] for item in yhat]
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "--------EPOCH PREDICT COMPLETE-------")

    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'png_name_list', png_name_list)
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'yhat', yhat)
    #----------------------存储预测结果用于绘制转角曲线------------------------------
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'Write predict result to csv file')
    write_rows = zip(png_name_list, yhat)
    with open(test_dataset_path + 'predict_result.csv', 'w', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['png_file', 'predict_angle'])
        for row in write_rows:
            writer.writerow(row)

def get_png_name_list(steering_log):
    png_name_list = []
    df_steer = pd.read_csv(steering_log, usecols=['frame_id', 'steering_angle_change'], index_col=False)
    for frame in df_steer['frame_id']:
        png_name_list.append(frame)
    return png_name_list

##########################################移植模型代码用于生成预测值并得到转角曲线###############################################

err_img_path = sys.path[0] + '/err_img_new/'

def draw_arrow(img, angle, thickness = 1, imgtype=0):
    pt1 = (int(img.shape[1] / 2), img.shape[0])
    pt2_angle1 = (int(img.shape[1] / 2 + img.shape[0] / 3 * math.sin(angle)),
                  int(img.shape[0] - img.shape[0] / 3 * math.cos(angle)))
    if imgtype == 1:
        img = cv2.arrowedLine(img, pt1, pt2_angle1, (255, 0, 0), thickness)
    else:
        img = cv2.arrowedLine(img, pt1, pt2_angle1, (0, 0, 255), thickness)
    return img

def preprocess_image(img_path, target_size=(100, 100)):
    img = image.load_img(img_path, target_size=target_size)
    input_img_data = image.img_to_array(img)
    input_img_data = np.expand_dims(input_img_data, axis=0)
    input_img_data = preprocess_input(input_img_data)
    return input_img_data

def deprocess_image(x,shape=(100,100,3)):
    tmp = x.copy()
    tmp = tmp.reshape(shape)
    # Remove zero-center by mean pixel
    tmp[:, :, 0] += 103.939
    tmp[:, :, 1] += 116.779
    tmp[:, :, 2] += 123.68
    # 'BGR'->'RGB'
    tmp = tmp[:, :, ::-1]
    tmp = np.clip(tmp, 0, 255).astype('uint8')
    return tmp

def process_csv(csv_path):
    img_name_dic = {}
    with open(csv_path, 'r') as f:
        for m, line in enumerate(f):
            if m == 0:
                continue
            line_info = line.rstrip('\n').split(',')
            img_name_dic[line_info[0]] = line_info[1:]
    print('img_name_dic', img_name_dic)
    return img_name_dic

def convertToPDF(path):
    # Open the image
    # path = os.path.join(sys.path[0], 'selected_diff_imgs/12626571.png')
    image = Image.open(path)
    image = image.convert("RGB")
    # Save the image as a PDF
    path = path.replace('selected_diff_imgs', 'selected_diff_pdfs')
    image.save(path.replace('png', 'pdf'))

def generateNewCsv():
    for model_id in range(1, 5):
        for scene_id in range(1, 7):
            # 1. 复制一个名为list.csv文件到其所在目录命名为list_new.csv；
            cur_list_path = err_img_path + str(model_id) + "/" + str(scene_id) + "/"
            shutil.copy(cur_list_path + "list.csv", cur_list_path + "list_new.csv")

            # 2. 打开list_new.csv文件并对其最右边添加一列，这一列的值为单元格D1与E1的差的绝对值。将这一计算规则运用于该list_new.csv的所有行数据。
            with open(cur_list_path + "list_new.csv", "r") as f:
                reader = csv.reader(f)
                data = list(reader)

            # add the new column
            data[0].append("angle_diff")
            for i in range(1, len(data)):
                data[i].append(abs(float(data[i][3]) - float(data[i][4])))

            with open(cur_list_path + "list_new.csv", "w", newline="") as f:
                writer = csv.writer(f)
                writer.writerows(data)

def mergeFile():
    for model_id in range(1, 5):
        for scene_id in range(1, 7):
            cur_list_path = err_img_path + str(model_id) + "/" + str(scene_id) + "/"
            if model_id == 1 and scene_id == 1:
                # 读取第一个文件并创建合并后的 DataFrame
                df_merged = pd.read_csv(cur_list_path + "list_new.csv", header=0)
            else:
                df = pd.read_csv(cur_list_path + "list_new.csv", header=0)
                df_merged = pd.concat([df_merged, df])
    df_merged.to_csv(err_img_path + "merged_csv_new.csv", index=False)

def find_files(path, extension):
    file_paths = []
    for root, dirs, files in os.walk(path):
        for file in files:
            if file.endswith(extension):
                file_paths.append(os.path.join(root, file))
    return file_paths

def files_process(path, extension):
    png_files = find_files(path, extension)
    i = 1
    for png_path in png_files:
        print("--Convert Progress: " + str(i) + " / 50")
        convertToPDF(png_path)
        i += 1

def main():
    seed_town_dic = [0, 4, 4, 6, 7,  7, 7]
    model_dic = ['', 'dave_v1', 'dave_v2', 'dave_v3', 'epoch']
    print("---IMG READ---")
    raw_imgs = []
    muted_imgs = []
    img_name_dic = process_csv(os.path.join(err_img_path, 'merged_csv_new.csv'))

    filelist = list(img_name_dic.keys())

    for img_name in filelist:
        muted_img_path = err_img_path + img_name_dic[img_name][5] + '/' + img_name_dic[img_name][4] + '/' + img_name
        ori_img_path = sys.path[0] + '/seed_data_train/seed' + str(img_name_dic[img_name][4]) + '_town0' + str(seed_town_dic[int(img_name_dic[img_name][4])]) + '/' + img_name_dic[img_name][0]
        # print(muted_img_path + "  " + ori_img_path)
        muted_img = preprocess_image(muted_img_path,(720,1280))
        muted_imgs.append(muted_img)
        raw_img = preprocess_image(ori_img_path,(720,1280))
        raw_imgs.append(raw_img)
    print("---IMG READ COMPLETE---")

    print("---IMG WRITE---")
    imsave = imageio.imsave
    for i in range(len(raw_imgs)): 
        print("--Write Progress: " + str(i) + "/" + str(len(raw_imgs)))
        # print(image_idx_arr[i], img_name_dic[image_idx_arr[i]][1], img_name_dic[image_idx_arr[i]][2], img_name_dic[image_idx_arr[i]][3])
        
        ## -------------- 用于对变异后的图片划线 ----------------
        gen_img_deprocessed = draw_arrow(deprocess_image(muted_imgs[i],(720,1280,3)), float(img_name_dic[filelist[i]][3]) * math.pi / 2, 10, 1)
        imsave(os.path.join(sys.path[0], 'diff_imgs_new/seed') + str(img_name_dic[filelist[i]][4]) + '_town0' + str(seed_town_dic[int(img_name_dic[filelist[i]][4])]) + 
            '/' + model_dic[int(img_name_dic[filelist[i]][5])] + '/' + filelist[i], gen_img_deprocessed)

        ## -------------- 用于对原图片划线 ---------------------
        gen_img_deprocessed = draw_arrow(deprocess_image(raw_imgs[i],(720,1280,3)), float(img_name_dic[filelist[i]][2]) * math.pi / 2, 10, 0)
        imsave(os.path.join(sys.path[0], 'diff_imgs_new/seed') + str(img_name_dic[filelist[i]][4]) + '_town0' + str(seed_town_dic[int(img_name_dic[filelist[i]][4])]) + 
            '/' + model_dic[int(img_name_dic[filelist[i]][5])] + '/' + filelist[i][:-4] + '_' + img_name_dic[filelist[i]][0], gen_img_deprocessed)
    print("---IMG WRITE COMPLETE---")
    return

def generate_arrowed_pics(input_path, dir_name, color):
    img_path = input_path + 'center/'
    csv_path = input_path + dir_name + '.csv'
    output_path = input_path + 'arrowed_out/'
    if not isinstance(color, int):
        color = int(color)
    
    print("---IMG READ---")
    img_data = []
    img_name_dic = process_csv(csv_path)
    filelist = list(img_name_dic.keys())
    for img_name in filelist:
        raw_img = preprocess_image(img_path + img_name,(720, 1280))
        img_data.append(raw_img)
    print("---IMG READ COMPLETE---")

    print("---CLEAR OUTPUT FOLDER---")
    for filename in os.listdir(output_path):
        file_path = os.path.join(output_path, filename)
        os.remove(file_path)
    
    print("---IMG WRITE---")
    imsave = imageio.imsave
    for i in range(len(img_data)): 
        print("--Write Progress: " + str(i + 1) + "/" + str(len(img_data)))
        ## -------------- 用于对图片划线 ---------------------
        gen_img_deprocessed = draw_arrow(deprocess_image(img_data[i],(720, 1280, 3)), float(img_name_dic[filelist[i]][0]) * math.pi / 2, 10, color)
        imsave(output_path + filelist[i], gen_img_deprocessed)
    print("---IMG WRITE COMPLETE---")
    return

def convertToGif(input_path):
    images = []
    png_files = find_files(input_path, '.png')
    png_files.sort()
    i = 0
    # 按顺序打开一系列图片
    for filename in png_files:
        img = Image.open(filename)
        images.append(img)
        # i += 1
        # if i >= 115:
        #     break
    

    # 将图片转换为GIF动图并保存
    images[0].save(input_path + "result.gif", save_all=True, append_images=images[1:], duration=100, loop=0)

# CUDNN 11.2 CUDA 8.1
if __name__ == '__main__':
    description = ("Steering-curve script.")
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument('--model', type=str, default='')
    parser.add_argument('--type', type=str, default='ori_gif')
    # parser.add_argument('--name', type=str, default='')
    args = parser.parse_args()

    if args.type == 'ori_gif':
        convertToGif('/home/vangogh/software/FuzzScene/code/scenario_runner-0.9.13/_out/center/')
    else:
        model_predict(args.model)
        generate_arrowed_pics('./scenario_runner-0.9.13/_out/', 'predict_result', args.type)
        convertToGif('./scenario_runner-0.9.13/_out/arrowed_out/')
        print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "# Generate Arrowed GIF Over")

    # main()
    # convertToPDF()
    # mergeFile()

    # files_process("d:/BaiduNetdiskDownload/selected_diff_imgs", ".png")



    # generate_arrowed_pics('./scenario_runner-0.9.13/', 'predict_result', 1)

    # convertToGif(sys.path[0] + '/videos/seed1')
    # convertToGif(sys.path[0] + '/videos/seed2')

