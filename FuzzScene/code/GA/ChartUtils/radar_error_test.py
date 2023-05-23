# usage: python driving_models.py 1 0 - train the dave-orig model
from __future__ import print_function

import csv
import shutil
import sys
import os

sys.path.append("/home/vangogh/software/FuzzScene/code/")
from data_utils import *
import pandas as pd
from tensorflow.keras.layers import Convolution2D, Input, Dense, Flatten, Lambda, MaxPooling2D, Dropout, Activation
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
from tensorflow.keras import models, optimizers, backend
import matplotlib.pyplot as plt
import tensorflow as tf
import numpy as np
import time
import Constants

dataset_path = '/home/software/FuzzScene/code/train_carla/'


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
        m.load_weights(Constants.MODEL_DIR_PATH + 'Model1.h5')

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
        m.load_weights(Constants.MODEL_DIR_PATH + 'Model2.h5')

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
        m.load_weights(Constants.MODEL_DIR_PATH + 'Model3.h5')

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
        m.load_weights(Constants.MODEL_DIR_PATH + 'Model4.h5')

    m.compile(loss='mse', optimizer=optimizers.Adam(lr=1e-04))

    return m


def rmse(y_true, y_pred):  # used for loss metric, output tensor
    '''Calculates RMSE
    '''
    return K.sqrt(K.mean(K.square(y_pred - y_true)))


def calc_rmse(yhat, label):  # used for loss cal, output float
    mse = 0.
    count = 0
    if len(yhat) != len(label):
        print("yhat and label have different lengths")
        return -1
    for i in range(len(yhat)):
        count += 1
        predicted_steering = yhat[i]
        steering = label[i]
        # print(predicted_steering)
        # print(steering)
        mse += (float(steering) - float(predicted_steering)) ** 2.
    return (mse / count) ** 0.5


def calc_mse(yhat, label):  # used for loss cal, output float
    mse = 0.
    count = 0
    if len(yhat) != len(label):
        print("yhat and label have different lengths")
        return -1
    for i in range(len(yhat)):
        count += 1
        predicted_steering = yhat[i]
        steering = label[i]
        # print(predicted_steering)
        # print(steering)
        mse += (float(steering) - float(predicted_steering)) ** 2.
    return (mse / count)


def error_test(seq_label_name, model_name):
    batch_size = 64
    image_shape = (100, 100)
    model_existed = 1
    test_dataset_path = Constants.CARLA_RADAR_DATA_PATH
    label_path = Constants.CARLA_RADAR_LABEL_OUTPUT_PATH + seq_label_name
    with open(label_path, 'r') as f:
        rows = len(f.readlines()) - 1
        if rows == 0:
            return 0

    # --------------------------------------Build Model---------------------------------------- #
    # Dave_v1
    if model_name == '1':
        if model_existed == '0':
            model = Dave_orig()
        else:
            model = Dave_orig(None, True)

    # Dave_v2
    elif model_name == '2':
        # K.set_learning_phase(1)
        if model_existed == '0':
            model = Dave_norminit()
        else:
            model = Dave_norminit(None, True)

    # Dave_v3
    elif model_name == '3':
        # K.set_learning_phase(1)
        if model_existed == '0':
            model = Dave_dropout()
        else:
            model = Dave_dropout(None, True)
        # nb_epoch = 30

    # Udacity Epoch Model
    elif model_name == '4':
        if model_existed == '0':
            model = Epoch_model()
        else:
            model = Epoch_model(None, True)
        image_shape = (128, 128)
        batch_size = 32
    else:
        print(bcolors.FAIL + 'invalid model name, must in [1, 2, 3, 4]' + bcolors.ENDC)

    print(bcolors.OKGREEN + 'model %s built' % model_name + bcolors.ENDC)

    # --------------------------------------Training---------------------------------------- #
    # Dave serial model
    if model_name == '4':
        test_steering_log = label_path
        test_data = carla_load_steering_data(test_steering_log)
        test_frame_id = carla_load_frame_id(test_data)
        print('testset frame_id len: ', len(test_frame_id))

        # dataset divide
        # time_list_train = []
        time_list_test = []

        # for j in range(0, len(frame_id)):
        #     time_list_train.append(frame_id[j])

        for j in range(0, len(test_frame_id)):
            time_list_test.append(test_frame_id[j])

        # print('time_list_train len: ', len(time_list_train))
        print('time_list_test len: ', len(time_list_test))

    print(bcolors.OKGREEN + 'Model %s trained' % model_name + bcolors.ENDC)

    # --------------------------------------Evaluation---------------------------------------- #
    # Different evaluation methods for different model
    if model_name != '4':
        K.set_learning_phase(0)
        test_generator, samples_per_epoch = load_carla_test_data(path=label_path, batch_size=batch_size,
                                                                 shape=image_shape)
        print('test samples: ', samples_per_epoch)
        loss = model.evaluate(test_generator, steps=math.ceil(samples_per_epoch * 1. / batch_size), verbose=1)
        print("model %s evaluate_generator loss: %.8f" % (model_name, loss))
        # --------------------------------------Predict Dave---------------------------------------- #
        filelist = []
        true_angle_list = []

        with open(label_path, 'r') as f:
            rows = len(f.readlines()) - 1
            f.seek(0)
            for i, line in enumerate(f):
                if i == 0:
                    continue
                file_name = line.split(',')[1]
                filelist.append(test_dataset_path + 'center/' + file_name)
                true_angle_list.append(float(line.split(',')[3]))

        print("--------IMG READ-------")
        predict_angle_list = []
        imgs = []
        raw_imgs = []
        count = 0
        ori_image_size = (720, 1280)
        for f in filelist:
            count += 1
            if count % 100 == 0:
                print(str(count) + ' images read')
            orig_name = f
            gen_img = preprocess_image(orig_name, image_shape)
            raw_img = preprocess_image(orig_name, ori_image_size)
            imgs.append(gen_img)
            raw_imgs.append(raw_img)
        print("--------IMG READ COMPLETE-------")

        print("--------DAVE PREDICT-------")
        count = 0
        imgs = np.array(imgs)
        for i in range(len(imgs)):
            predict_angle_list.append(model.predict(imgs[i])[0])
        print("--------DAVE PREDICT COMPLETE-------")
        yhat = predict_angle_list
        test_y = true_angle_list


    else:
        test_generator = carla_data_generator(frame_id=test_frame_id,
                                              steering_log=test_steering_log,
                                              image_folder=Constants.CARLA_RADAR_PNG_OUTPUT_PATH,
                                              unique_list=time_list_test,
                                              gen_type='test',
                                              batch_size=len(time_list_test),
                                              image_size=image_shape,
                                              shuffle=False,
                                              preprocess_input=normalize_input,
                                              preprocess_output=exact_output)

        # --------------------------------------Predict Epoch---------------------------------------- #
        print("--------EPOCH PREDICT-------")
        test_x, test_y = next(test_generator)
        yhat = model.predict(test_x, verbose=1)
        print("--------EPOCH PREDICT COMPLETE-------")
    # print(yhat)
    loss = calc_mse(yhat, test_y)
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", 'len(yhat): ', len(yhat))

    # # --------------------------------------FIND ERROR---------------------------------------- #
    # filelist_list = []
    # list_row = []
    # with open(test_dataset_path + 'label_test.csv', 'r') as f:
    #     rows = len(f.readlines()) - 1
    #     f.seek(0)
    #     for i, line in enumerate(f):
    #         if i == 0:
    #             continue
    #         file_name = line.split(',')[1]
    #         filelist_list.append(file_name)

    # lamb = 1
    # error_list = []
    # lenm = len(filelist_list)

    # with open(test_dataset_path + 'model' + model_name + '_oriMSE.csv', 'r') as f:
    #     rows = len(f.readlines()) - 1
    #     f.seek(0)
    #     m = 0
    #     for i, line in enumerate(f):    # [seed_name, img_id, ori_angle, 4*ori_angle]
    #         if i == 0:
    #             continue
    #         predict_steering_angle = line.split(',')[1]
    #         oriMSE = line.split(',')[2]
    #         true_angle_gt = line.split(',')[3]
    #         if ((float(yhat[m]) - float(predict_steering_angle)) ** 2) > (lamb * float(oriMSE)):
    #             countcc = countcc + 1
    #             list_row.append(
    #                 [filelist_list[m], predict_steering_angle, float(yhat[m]), true_angle_gt, model_name, m])
    #             print(predict_steering_angle, float(yhat[m]), oriMSE)
    #             error_list.append(m)
    #
    #         if (m + 1) < lenm:
    #             m = m + 1
    #         else:
    #             break

    ori_data_list = []
    with open(test_dataset_path + 'model' + model_name + '_oriMSE.csv', 'r') as f:
        f.seek(0)
        for i, line in enumerate(f):
            if i == 0:
                continue
            predict_steering_angle = line.split(',')[1]
            oriMSE = line.split(',')[2]
            true_angle_gt = line.split(',')[3]
            ori_data_list.append([predict_steering_angle, oriMSE, true_angle_gt])

    data_list_wb = []  # write back data # [seed_name, operator, cur_predict_angle, ori_predict_angle, is_err, ori_carla_angle, cur_carla_angle]
    with open(label_path, 'r') as f:
        f.seek(0)
        frame = 0
        last_seed_name = ''
        for i, line in enumerate(f):
            dt = line.split(',')  # line in label_test.csv
            seed_name = dt[0]  # full seed name
            if i == 0:
                continue
            frame += 1
            if seed_name != last_seed_name:
                frame = 1
                last_seed_name = seed_name
            operator = seed_name.split('_')[1]
            scene_name = int(seed_name.split('_')[3][0])
            cur_carla_angle = float(dt[3])  # 4*cur_carla_angle

            cur_predict_angle = yhat[i - 1]  # cur predict angle
            cur_frame = (scene_name - 1) * 125 + frame - 1

            ori_predict_angle = ori_data_list[cur_frame][0]  # data from oriMSE.csv
            oriMSE = ori_data_list[cur_frame][1]
            ori_carla_angle = ori_data_list[cur_frame][2]
            if ((cur_predict_angle - float(ori_predict_angle)) ** 2) > (1 * float(oriMSE)):
                is_err = 1
            else:
                is_err = 0
            data_list_wb.append(
                [seed_name, operator, cur_predict_angle[0], ori_predict_angle, is_err, float(ori_carla_angle),
                 cur_carla_angle])

    radar_path = Constants.CARLA_RADAR_DATA_PATH + "radar" + str(model_name) + ".csv"
    if not os.path.exists(radar_path):
        shutil.copy(Constants.CARLA_RADAR_DATA_PATH + "radar_null.csv", radar_path)

    with open(radar_path, 'a+', encoding='utf-8') as f:
        csv_writer = csv.writer(f)
        for line in range(len(data_list_wb)):
            csv_writer.writerow(data_list_wb[line])
    
    print("[" + os.path.basename(__file__) + ", Line " + str(sys._getframe().f_lineno) + ", " + sys._getframe().f_code.co_name + "] ", "Append sub_csv" + label_path + " to radar" + str(model_name) + ".csv done!")

if __name__ == '__main__':
    model_name = sys.argv[1]
    seq = sys.argv[2]
    path = 'label_test_' + str(seq) + '.csv'
    error_test(path, model_name)
    # error_list = error_test()
    # error_count = './error_count.csv'
    # with open(error_count, 'a+', encoding='utf-8') as f:
    #     csv_writer = csv.writer(f)
    #     csv_writer.writerow([sys.argv[3], error_list])
