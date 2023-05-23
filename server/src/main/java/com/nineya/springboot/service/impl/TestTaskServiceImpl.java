package com.nineya.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.common.SSH.SSHUtil;
import com.nineya.springboot.common.SSH.SimulationService;
import com.nineya.springboot.constant.Constants;
import com.nineya.springboot.entity.*;
import com.nineya.springboot.mapper.*;
import com.nineya.springboot.service.TestResultService;
import com.nineya.springboot.service.TestTaskService;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ylq
 * @since 2023-04-02
 */
@Service
public class TestTaskServiceImpl extends ServiceImpl<TestTaskMapper, TestTask> implements TestTaskService {
    // 后续重构，Service之间调用，不要A Service调用B DAO
    @Autowired
    private ConfigurationMapper configurationMapper;
    @Autowired
    private TestTaskMapper testTaskMapper;
    @Autowired
    private TestResultMapper testResultMapper;
    @Autowired
    private MseMapper mseMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private SeedMapper seedMapper;
    @Autowired
    private OperatorMapper operatorMapper;
    @Autowired
    private Config2modelMapper config2modelMapper;
    @Autowired
    private Config2seedMapper config2seedMapper;
    @Autowired
    private Config2operatorMapper config2operatorMapper;
    @Autowired
    private SimulationService simulationService;
    @Autowired
    private TestResultService testResultService;
    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> future;

    private double lastModelProgress = 40;
    private int expectedResultLines = 0;
    private int progressPerModel = 60;

    public R getAllInfo() {
        List<Model> modelList = modelMapper.selectList(null);
        List<Seed> seedList = seedMapper.selectList(null);
        List<Operator> operatorList = operatorMapper.selectList(null);
        List<List> infoList = new ArrayList<>();
        infoList.add(modelList);
        infoList.add(seedList);
        infoList.add(operatorList);
        return R.success("success", infoList);
    }

    public R createConfig(Configuration configuration) {
//        System.out.println(configuration);
        configurationMapper.insert(configuration);
//        System.out.println("after createConfig: " + configuration);
        return R.success("success", configuration);
    }

    public R createTask(Configuration configuration, String taskName, String selectedModels, String selectedSeeds, String selectedMutSeeds, String desc) {
        createConfig(configuration);
        Integer configId = configuration.getId();
//        System.out.println("createTask after createConfig:" + configuration);
        TestTask testTask = new TestTask();
        testTask.setConfigId(configId);
        testTask.setName(taskName);
        String[] models = selectedModels.split(",");
        String[] seeds = selectedSeeds.split(",");
        String[] operators = selectedMutSeeds.split(",");
        testTask.setModelCnt(models.length);
        testTask.setSeedCnt(seeds.length);
        testTask.setOperatorCnt(operators.length);
        testTask.setCreator("ylq");
        testTask.setCreateDate(new Date());
        testTask.setTaskStatus(0);
        testTask.setTaskProgress(0.0);
        testTask.setDesc(desc);
//        System.out.println("createTask insert:" + " " + testTask);
        testTaskMapper.insert(testTask);
        for (int i = 0; i < models.length; i++) {
            Config2model config2model = new Config2model();
            config2model.setConfigId(configId);
            config2model.setModelId(Integer.parseInt(models[i]));
            config2modelMapper.insert(config2model);
        }
//        System.out.println(config2modelMapper.selectList(null));
        for (int i = 0; i < seeds.length; i++) {
            Config2seed config2seed = new Config2seed();
            config2seed.setConfigId(configId);
            config2seed.setSeedId(Integer.parseInt(seeds[i]));
            config2seedMapper.insert(config2seed);
        }
//        System.out.println(config2seedMapper.selectList(null));
        for (int i = 0; i < operators.length; i++) {
            Config2operator config2operator = new Config2operator();
            config2operator.setConfigId(configId);
            config2operator.setOperatorId(Integer.parseInt(operators[i]));
            config2operatorMapper.insert(config2operator);
        }
//        System.out.println(config2operatorMapper.selectList(null));
        System.out.println("Insert testTask success: " + testTask);
        return R.success("success", testTask);
    }

    public R getAllTasks(String filterName) {
        QueryWrapper<TestTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", filterName);
        List<TestTask> testTaskList = testTaskMapper.selectList(queryWrapper);
        return R.success("success", testTaskList);
    }

    @Override
    public R getModels(Integer taskId)
    {
        TestTask testTask = testTaskMapper.selectById(taskId);
        Configuration configuration = configurationMapper.selectById(testTask.getConfigId());
        List<Model> modelList = modelMapper.selectList(new QueryWrapper<Model>().inSql("id", "select model_id from config2model where config_id = " + configuration.getId()));
        return R.success("success", modelList);
    }

    public R deleteTask(Integer id) {
        TestTask testTask = testTaskMapper.selectById(id);
        Integer configId = testTask.getConfigId();
        // 删除配置关联记录和配置记录
        config2modelMapper.delete(new QueryWrapper<Config2model>().eq("config_id", configId));
        config2seedMapper.delete(new QueryWrapper<Config2seed>().eq("config_id", configId));
        config2operatorMapper.delete(new QueryWrapper<Config2operator>().eq("config_id", configId));
        configurationMapper.deleteById(configId);
        // 删除测试任务记录
        testTaskMapper.deleteById(id);
        // 删除mse记录
        List<TestResult> testResultList = testResultMapper.selectList(new QueryWrapper<TestResult>().eq("task_id", id));
        for (TestResult testResult : testResultList) {
            mseMapper.deleteById(testResult.getMseId());
        }
        // 删除测试结果记录
        testResultMapper.delete(new QueryWrapper<TestResult>().eq("task_id", id));
        // 删除测试结果文件夹
        String testResultPath = Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName();
        File taskDir = new File(testResultPath);
        if (taskDir.exists()) {
            deleteDirectory(taskDir);
        }
        return R.success("success", null);
    }

    public void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    public R executeTask(Integer id) throws JSchException, IOException, InterruptedException, CsvException
    {
        // 更新任务状态为进行中
        TestTask testTask = testTaskMapper.selectById(id);
        testTask.setTaskProgress(0.00);
        testTask.setTaskStatus(1);
        testTaskMapper.updateById(testTask);
        // 获取测试任务对应的模型列表、种子列表、算子列表
        // TODO: 简单SQL语句直接写在 QueryWrapper里，可优化为将SQL写在xml文件中作为新的查询方法
        Configuration configuration = configurationMapper.selectById(testTask.getConfigId());
        System.out.println("\n# TestTask " + testTask.getName() + " Configuration List: ");
        List<Model> modelList = modelMapper.selectList(new QueryWrapper<Model>().inSql("id", "select model_id from config2model where config_id = " + configuration.getId()));
        System.out.println(modelList);
        List<Seed> seedList = seedMapper.selectList(new QueryWrapper<Seed>().inSql("id", "select seed_id from config2seed where config_id = " + configuration.getId()));
        System.out.println(seedList);
        List<Operator> operatorList = operatorMapper.selectList(new QueryWrapper<Operator>().inSql("id", "select operator_id from config2operator where config_id = " + configuration.getId()));
        System.out.println(operatorList);
        simulationService.setLogOutputStream();
        int taskStatus = 1;
        /******* 上传测试所需的种子文件和模型文件 *******/
        if (!Constants.DEBUG) {
            // 准备SSHUtil服务
            SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
            sshUtil.connect();
            // 将种子文件上传至仿真服务器以供后续使用
            System.out.println("\n# TestTask Begin Upload Seeds");
            for (int i = 0; i < seedList.size(); i++) {
                Seed seed = seedList.get(i);
                String renameSeed = "seed_0_0_" + (i + 1) + ".xosc";
                System.out.println("Uploading seed: " + seed.getName() + " to " + Constants.CARLA_SEED_UPLOAD_PATH + renameSeed);
                sshUtil.uploadFile(Constants.UPLOAD_PATH + "seeds/" + seed.getName(), Constants.CARLA_SEED_UPLOAD_PATH + renameSeed);
            }
            // 将模型上传至仿真服务器以供后续使用
            System.out.println("\n# TestTask Begin Upload Models");
            for (int i = 0; i < modelList.size(); i++) {
                Model model = modelList.get(i);
                String renameModel = "Model" + Constants.MODEL_MAP.get(model.getName()) + ".h5";
                System.out.println("Uploading model: " + model.getName() + " to " + Constants.CARLA_MODEL_UPLOAD_PATH + renameModel);
                sshUtil.uploadFile(Constants.UPLOAD_PATH + "models/" + model.getFileName(), Constants.CARLA_MODEL_UPLOAD_PATH + renameModel);
            }
            sshUtil.disconnect();
        }
        // 生成语义算子是否变异的mutFlags数组，两个测试任务均需使用
        boolean[] mutFlags = new boolean[5];
        Arrays.fill(mutFlags, false);
        for (Operator operator : operatorList)
        {
            mutFlags[operator.getId() - 1] = true;
        }
        String mutFlagsStr = Arrays.toString(mutFlags).replaceAll(" ", "").replaceAll("true", "True").replaceAll("false", "False");
        // 开启语义算子有效性测试进度定时任务，每隔5分钟查询一次任务执行进度
        expectedResultLines = testTask.getOperatorCnt() * testTask.getSeedCnt() * configuration.getRadarLoop() * 125 * (testTask.getModelCnt() + 1);
        future = taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                int curLines = simulationService.queryMSETestTaskProgress();
                double curPercentage = (double) curLines / expectedResultLines * 40;
                System.out.println("curLines: " + curLines + " expectedResultLines: " + expectedResultLines +  " curPercentage: " + curPercentage);
                testTask.setTaskProgress(curPercentage);
                testTaskMapper.updateById(testTask);
                System.out.println("\n# TestTask " + testTask.getName() + " Progress: " + curPercentage + " %");
            }
        }, new CronTrigger("0 0/1 * * * ?")); // 5分钟执行一次
        /******* 执行语义算子有效性测试 *******/
        taskStatus = executeOperatorsMSETest(mutFlagsStr, testTask, configuration, modelList);
        // 关闭定时任务
        if (future != null) {
            future.cancel(true);
        }
        /******* 执行模型缺陷测试 *******/
        if (taskStatus == 2 || Constants.DEBUG) {
            // 执行模型缺陷测试
            taskStatus = executeModelsDefectTest(mutFlagsStr, testTask, configuration, modelList, seedList);
        }
        // 关闭定时任务
        if (future != null) {
            future.cancel(true);
        }
        // 关闭输出流
        simulationService.resetOutputStream();
        // 更新测试任务状态
        testTask.setTaskStatus(taskStatus);
        testTaskMapper.updateById(testTask);
        return R.success("success", null);
    }

    public int executeOperatorsMSETest(String mutFlagsStr, TestTask testTask, Configuration configuration, List<Model> modelList) throws JSchException, IOException, InterruptedException, CsvException
    {
        // 生成语义算子有效性测试任务所需参数
        int radarRandTime = configuration.getRadarLoop();
        int seedNum = testTask.getSeedCnt();
        int modelNum = testTask.getModelCnt();
        String clearOutput = "True";
        int[] modelArray = new int[modelList.size()];
        for (int i = 0; i < modelList.size(); i++) {
            modelArray[i] = Constants.MODEL_MAP.get(modelList.get(i).getName());
        }
        String modelArrayStr = Arrays.toString(modelArray).replaceAll(" ", "");
        // 准备SSHUtil服务
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        // 设置语义算子测试参数至服务器
        System.out.println("\n# Operators Test Set Parameters: ");
        System.out.println("radarRandTime: " + radarRandTime + " seedNum: " + seedNum + " modelNum: " + modelNum + " clearOutput: " + clearOutput);
        System.out.println(sshUtil.executeCommandFullOutputStream("source /home/vangogh/anaconda3/etc/profile.d/conda.sh " +
                "&& conda activate dave && " +
                "cd " + Constants.CARLA_RADAR_PATH +
                " && python set_paras.py " + mutFlagsStr + " " + radarRandTime + " " + seedNum + " " + modelNum + " " + modelArrayStr + " " + clearOutput));
        sshUtil.disconnect();
        // 调用仿真服务开始测试
        System.out.println("\n# Task " + testTask.getName() +  " Operators MSE Test Begin ");
        int taskStatus = simulationService.operatorsMSETest();
//        int taskStatus = 2;
        // 测试完成，开始处理测试执行结果
        if (taskStatus == 2) {
            taskStatus = processOperatorsMSETestResult(testTask, configuration);
            // 更新测试任务进度，完成语义算子有效性测试，任务进度为40%
            testTask.setTaskProgress(40.00);
            testTaskMapper.updateById(testTask);
        }
        return taskStatus;
    }

    public int processOperatorsMSETestResult(TestTask testTask, Configuration configuration) throws JSchException, IOException, InterruptedException, CsvException
    {
        // 准备该测试任务的结果文件夹
        String taskDirPath = Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName();
        File taskDir = new File(taskDirPath);
        if (!taskDir.exists()) {
            taskDir.mkdirs();
        }
        // 准备SSHUtil服务
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        // 从服务器下载测试结果文件
        System.out.println("\n# Operators Test Download Result File");
        if(!sshUtil.downloadFile(Constants.CARLA_RADAR_RESULT_PATH + "mse.csv", Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/mse.csv")) {
            System.out.println("# Task " + testTask.getName() + " operators test result csv download failed.");
            sshUtil.disconnect();
            return 0;
        }
        // 读取结果数据并写入数据库
        testResultService.processOperatorsTestCSV(testTask, configuration, Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/mse.csv");
        sshUtil.disconnect();
        return 2;
    }

    public int executeModelsDefectTest(String mutFlagsStr, TestTask testTask, Configuration configuration,
                                        List<Model> modelList, List<Seed> seedList) throws JSchException, IOException, InterruptedException, CsvException
    {
        // 生成缺陷测试任务所需参数
        int priQueueSize = configuration.getPriQueueSize();
        int initPopSize = configuration.getInitPopSize();
        String sampling = configuration.getSampling() == 1 ? "True" : "False";
        int loop = configuration.getLoop();
        int fitFunction = Constants.FITNESS_MAP.get(configuration.getFitFunction());
        // 分模型开始遗传算法循环测试
        System.out.println("\n# Begin Defect Test By Model");
        // 开启模型缺陷测试进度定时任务，每隔5分钟查询一次任务执行进度
        progressPerModel = 60 / modelList.size();
        expectedResultLines = (configuration.getSampling() == 1 ? configuration.getInitPopSize() * 4 :
                configuration.getInitPopSize() * 2) + configuration.getLoop() * 4;
        future = taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                int curLines = simulationService.queryDefectsTestTaskProgress();
                double curModelProgress = (double) curLines / expectedResultLines * progressPerModel;
                double curTotalProgress = lastModelProgress + curModelProgress;
                System.out.println("curLines: " + curLines + " expectedResultLines: " + expectedResultLines +  " curModelProgress: " + curModelProgress + " curTotalProgress: " + curTotalProgress);
                testTask.setTaskProgress(curTotalProgress);
                testTaskMapper.updateById(testTask);
                System.out.println("\n# TestTask " + testTask.getName() + " Progress: " + curTotalProgress + " %");
            }
        }, new CronTrigger("0 0/1 * * * ?")); // 4分钟执行一次

        int taskStatus = 1;
        for (int i = 0; i < modelList.size(); i++) {
            Model model = modelList.get(i);
            // 准备SSHUtil服务
            SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
            sshUtil.connect();
            // 打印任务参数并调用set_paras.py设置任务参数至仿真服务器端
            System.out.println("\n# Models Defect Test Set Parameters: ");
            int modelId = Constants.MODEL_MAP.get(model.getName());
            System.out.println("model: " + modelId + " mutFlags: " + mutFlagsStr + " priQueueSize: " + priQueueSize + " initPopSize: " + initPopSize + " sampling: " + sampling + " loop: " + loop + " fitFunction: " + fitFunction);
            System.out.println(sshUtil.executeCommandFullOutputStream("source /home/vangogh/anaconda3/etc/profile.d/conda.sh " +
                    "&& conda activate carla && " +
                    "cd " + Constants.CARLA_GA_PATH +
                    " && python set_paras.py " + modelId + " " + mutFlagsStr + " " + priQueueSize + " " + initPopSize + " " + sampling + " " + loop + " " + fitFunction));
            sshUtil.disconnect();
            // 调用仿真服务开始测试
            System.out.println("\n# Model " + model.getName() +  " Defect Test Begin ");
            taskStatus = simulationService.modelsDefectTest();
            if (taskStatus == 2 || Constants.DEBUG) // 测试任务执行完成GA LOOP
            {
                taskStatus = processDefectTestResult(testTask, model);
                if (taskStatus == 2) {
                    // 一个模型执行完成，更新测试任务进度
                    lastModelProgress += progressPerModel;
                    if (i == modelList.size() - 1 && lastModelProgress != 100) {
                        lastModelProgress = 100;
                    }
                    System.out.println("\n# Model " + model.getName() + " Defect Test Success. " +
                            "Update Task Progress: " + lastModelProgress);
                }
                testTask.setTaskProgress(lastModelProgress);
                testTaskMapper.updateById(testTask);
            } else if (taskStatus == 0) {  // 测试任务未成功执行完
                testTask.setTaskStatus(taskStatus);
                testTaskMapper.updateById(testTask);
                return 0;
            }
        }
        return 2;
    }

    public int processDefectTestResult(TestTask testTask, Model model) throws JSchException, IOException, InterruptedException, CsvException
    {
        // 准备SSHUtil服务
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        // 测试完成后调用rename.py处理测试结果
        System.out.println("\n# Model " + model.getName() + " Defect Test End, Processing Results... ");
        String processOutput = sshUtil.executeCommandFullOutputStream("source /home/vangogh/anaconda3/etc/profile.d/conda.sh " +
                "&& conda activate carla && " + "cd " + Constants.CARLA_GA_PATH + " && python rename.py");
        System.out.println(processOutput);
        String resultName = processOutput.split("./")[1].replaceAll("\n", "");
        System.out.println("Test Result Name:" + resultName);
        // 准备该测试任务的结果文件夹
        String taskDirPath = Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName();
        File taskDir = new File(taskDirPath);
        if (!taskDir.exists())
        {
            taskDir.mkdirs();
        }
        File modelDir = new File(taskDirPath + "/" + model.getName());
        if (!modelDir.exists())
        {
            modelDir.mkdirs();
        }
        // 下载对应的测试结果文件到本地
        if (!sshUtil.downloadFile(Constants.CARLA_TEST_RESULT_PATH + resultName, Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/" + model.getName() + "/" + resultName))
        {
            System.out.println("# Model " + model.getName() + " result csv download failed.");
            return 0;
        }
        sshUtil.disconnect();
        // 对下载的测试结果进行入库处理并得到错误最多的种子场景
        System.out.println("# Process " + model.getName() + " test result. Update database and get most errors seed. ");
        //                String mostErrorCntAndDivSeedName = testResultService.processErrorCountCSV(testTask, model,
        //                        Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/" + model.getName() + "/" + "FuzzScene.csv");
        String mostErrorCntAndDivSeedName = testResultService.processErrorCountCSV(testTask, model,
                Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/" + model.getName() + "/" + resultName);
        System.out.println("mostErrorCntAndDivSeedName: " + mostErrorCntAndDivSeedName);
        // 将错误数最多的种子场景执行仿真生成图像帧，并放入模型预测得到预测值，添加转向曲线得到GIF动图
        return testResultService.processMostErrorSeed(testTask, model, mostErrorCntAndDivSeedName);
    }

    @Override
    public R terminateTask(Integer taskId) throws IOException
    {
        TestTask testTask = testTaskMapper.selectById(taskId);
        testTask.setTaskStatus(0);
        testTask.setTaskProgress(0.00);
        testTaskMapper.updateById(testTask);
        simulationService.terminateCarla();
        simulationService.resetOutputStream();
        // 关闭定时任务
        if (future != null) {
            future.cancel(true);
        }
        return R.success("success", null);
    }
}
