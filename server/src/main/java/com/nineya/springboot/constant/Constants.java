package com.nineya.springboot.constant;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final boolean DEBUG = false;
    /**********************  SSH配置信息  **************************/
    public static final String USER = "vangogh";
    public static final String PASSWORD = "980326";
    public static final String HOST = "192.168.1.4";
    public static final int PORT = 22;

    /**********************  后端路径常量信息  **************************/
    // 场景仿真日志文件存放路径
    public static String SIMULATION_LOG_PATH = "src/main/resources/simulationLog/";
    // 存放前端上传的文件，以及用于向SSH对端上传
    public static String UPLOAD_PATH = "src/main/resources/uploadDir/";
    // 存放从SSH对端下载的文件，包括仿真结果和测试结果
    public static String DOWNLOAD_PATH = "src/main/resources/downloadDir/";
    // 存放从SSH对端下载的仿真结果
    public static String SIMULATION_RESULT_DOWNLOAD_PATH = DOWNLOAD_PATH + "simulationResult/";
    // 存放从SSH对端下载的测试结果
    public static String TEST_RESULT_DOWNLOAD_PATH = DOWNLOAD_PATH + "testResult/";

    /**********************  Carla端路径常量信息  **************************/
    // Carla端代码根路径
    public static String CARLA_ROOT_PATH = "/home/vangogh/software/FuzzScene/code/";
    // Carla端GA算法代码路径
    public static String CARLA_GA_PATH = CARLA_ROOT_PATH + "GA/";
    // Carla端算子测试代码路径
    public static String CARLA_RADAR_PATH = CARLA_GA_PATH + "ChartUtils/";
    // Carla端算子测试结果
    public static String CARLA_RADAR_RESULT_PATH = CARLA_RADAR_PATH + "radar_data/";
    // Carla端用于存放后端需要上传的模型文件
    public static String CARLA_MODEL_UPLOAD_PATH = CARLA_ROOT_PATH + "trained_models/";
    // Carla端用于存放后端需要上传的种子文件
    public static String CARLA_SEED_UPLOAD_PATH = CARLA_ROOT_PATH + "seed_pool/";
    // Carla端用于存放仿真完成后的结果数据
    public static String CARLA_SIMULATION_RESULT_PATH = CARLA_ROOT_PATH + "scenario_runner-0.9.13/_out/center/";
    // Carla端用于存放测试完成后的结果数据
    public static String CARLA_TEST_RESULT_PATH = CARLA_GA_PATH + "ga_output/";
    // Carla端用于存放生成的最多错误场景动图
    public static String CARLA_ERROR_GIF_PATH = CARLA_ROOT_PATH + "scenario_runner-0.9.13/_out/arrowed_out/result.gif";

    /**********************  语义算子有效性测试任务常量  **************************/
    //
    public static int RADAR_RAND_TIME = 5;


    /**********************  映射表常量  **************************/
    // 适应度函数映射表
    public static Map<String, Integer> FITNESS_MAP = new HashMap<String, Integer>(){{
        put("Error+Diversity", 1);
        put("Error", 2);
        put("Diversity", 3);
    }};
    // 模型名和序号映射表
    public static Map<String, Integer> MODEL_MAP = new HashMap<String, Integer>(){{
        put("Dave_v1", 1);
        put("Dave_v2", 2);
        put("Dave_v3", 3);
        put("Epoch", 4);
    }};
}
