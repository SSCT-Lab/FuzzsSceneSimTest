package com.nineya.springboot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.SSH.SSHUtil;
import com.nineya.springboot.common.SSH.SimulationService;
import com.nineya.springboot.constant.Constants;
import com.nineya.springboot.entity.Configuration;
import com.nineya.springboot.entity.TestResult;
import com.nineya.springboot.entity.TestTask;
import com.nineya.springboot.mapper.ConfigurationMapper;
import com.nineya.springboot.mapper.ModelMapper;
import com.nineya.springboot.mapper.TestResultMapper;
import com.nineya.springboot.mapper.TestTaskMapper;
import com.nineya.springboot.service.TestResultService;
import com.nineya.springboot.service.TestTaskService;
import com.nineya.springboot.service.impl.TestTaskServiceImpl;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.socket.TextMessage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@SpringBootTest
@EnableScheduling
class SpringbootApplicationTests {
    @Autowired
    private TestResultMapper testResultMapper;
    @Autowired
    private TestTaskMapper testTaskMapper;
    @Autowired
    private ConfigurationMapper configurationMapper;
    @Autowired
    private TestResultService testResultService;
    @Autowired
    private TestTaskServiceImpl testTaskService;
    @Autowired
    private SimulationService simulationService;

    @Test
    public void processErrorCountCSV() throws IOException, CsvException
    {
//        String[] models = new String[]{"Dave_v1", "Dave_v2", "Dave_v3", "Epoch"};
        String[] models = new String[]{"Dave_v2"};
        int id = 41;
        for (String model : models) {
            String csvPath = "D:\\IDEA_workspace\\FuzzSceneSimTest\\server\\src\\main\\resources\\downloadDir\\testResult\\Test\\" + model + "\\error_count_2_True_all.csv";
            CSVReader reader = new CSVReaderBuilder(new FileReader(csvPath)).build();
            List<Integer> errorCountList = new ArrayList<>();
            List<Integer> errorCountListByLoop = new ArrayList<>();
            int errorCount = 0;
            List<Integer> divCountList = new ArrayList<>();
            int divCount = 0;
            int lastLoopIdx = 0;
            List<String[]> rows = reader.readAll();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                errorCountList.add(Integer.parseInt(row[1]));
                errorCount += Integer.parseInt(row[1]);
                int loopIdx = Integer.parseInt(row[0].split("_")[1]);
                if (loopIdx != lastLoopIdx) {
                    errorCountListByLoop.add(errorCount);
                    lastLoopIdx = loopIdx;
                }
                divCount += Integer.parseInt(row[2]);
            }
            errorCountListByLoop.add(errorCount);
            System.out.println("errorCountList: " + errorCountList);
            System.out.println("errorCountListByLoop: Size: " + errorCountListByLoop.size() + " " + errorCountListByLoop);
            System.out.println("divCount: " + divCount);
            TestResult testResult = new TestResult();
            testResult.setTaskId(25);
            testResult.setModelId(id);
            id++;
            testResult.setModelName(model);
            testResult.setDivCount(divCount);
            testResult.setErrorCount(errorCountList.toString().replaceAll(" ", ""));
            testResult.setErrorCountPerLoop(errorCountListByLoop.toString().replaceAll(" ", ""));
            testResultMapper.insert(testResult);
        }

    }

    @Test
    public void getResultByTaskId() {
        List<TestResult> testResultList = testResultMapper.selectList(new QueryWrapper<TestResult>().eq("task_id", 25));
        System.out.println(testResultList);
    }

    @Test
    public void selectMostErrorSeed() throws IOException, CsvException
    {
        String csvPath = "D:\\IDEA_workspace\\FuzzSceneSimTest\\server\\src\\main\\resources\\downloadDir\\testResult\\Test\\Dave_v2\\error_count_2_True_all.csv";
        CSVReader reader = new CSVReaderBuilder(new FileReader(csvPath)).build();
        List<Integer> errorCountList = new ArrayList<>();
        List<Integer> errorCountListByLoop = new ArrayList<>();
        // 用于存储读取过程中错误计数最多的变异场景文件名，若有多个场景文件错误计数相同，则再按照错误种类排序，最终挑选出最多错误数和错误种类的场景
        List<Integer> mostErrorCntAndDivIdxList = new ArrayList<>();
        int maxErrorCount = 0;
        int errorCount = 0;
        int divCount = 0;
        int lastLoopIdx = 0;
        List<String[]> rows = reader.readAll();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int curErrorCount = Integer.parseInt(row[1]);
            // 维持最大错误数和错误种类的序号列表
            if (curErrorCount > maxErrorCount) {
                maxErrorCount = curErrorCount;
                mostErrorCntAndDivIdxList.clear();
                mostErrorCntAndDivIdxList.add(i);
            } else if (curErrorCount == maxErrorCount) {
                mostErrorCntAndDivIdxList.add(i);
            }
            errorCountList.add(curErrorCount);
            errorCount += curErrorCount;
            int loopIdx = Integer.parseInt(row[0].split("_")[1]);
            if (loopIdx != lastLoopIdx) {
                errorCountListByLoop.add(errorCount);
                lastLoopIdx = loopIdx;
            }
            divCount += Integer.parseInt(row[2]);
        }
        errorCountListByLoop.add(errorCount);
        // 继续分mostErrorCntAndDivIdxList是否唯一选择最终的mostErrorCntAndDivIdx
        System.out.println("mostErrorCntAndDivIdxList: " + mostErrorCntAndDivIdxList);
        int maxDivCount = 0;
        int mostErrorCntAndDivIdx = 0;
        if (mostErrorCntAndDivIdxList.size() > 1) { // 不唯一，挑选错误种类最多的场景
            for (int i = 0; i < mostErrorCntAndDivIdxList.size(); i++) {
                int idx = mostErrorCntAndDivIdxList.get(i);
                String[] row = rows.get(idx);
                int curDivCount = Integer.parseInt(row[2]);
                if (curDivCount > maxDivCount) {
                    maxDivCount = curDivCount;
                    mostErrorCntAndDivIdx = idx;
                }
            }
            mostErrorCntAndDivIdxList.clear();
            mostErrorCntAndDivIdxList.add(mostErrorCntAndDivIdx);
        } else { // 唯一，直接选择错误数最多的场景
            mostErrorCntAndDivIdx = mostErrorCntAndDivIdxList.get(0);
        }
        System.out.println("Selected seed: " + mostErrorCntAndDivIdx + " " + rows.get(mostErrorCntAndDivIdx)[0] +
                " errorCount: " + rows.get(mostErrorCntAndDivIdx)[1] + " divCount: " + rows.get(mostErrorCntAndDivIdx)[2]);
    }

    @Test
    public void seedSimulationAndPredict() throws JSchException, IOException, InterruptedException
    {
        testResultService.processMostErrorSeed(null, null, "seed_1_1_4.xosc");
    }

    @Test
    public void downloadGif() throws JSchException
    {
        // 准备SSHUtil服务
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        String localGifName = "ori.gif";
        if(!sshUtil.downloadFile(Constants.CARLA_ERROR_GIF_PATH, Constants.TEST_RESULT_DOWNLOAD_PATH + "Test/" + localGifName)) {
            System.out.println("# Task Test" + " result gif " + localGifName + "download failed.");
            return;
        }
    }

    private ScheduledFuture<?> future;
    @Autowired
    private TaskScheduler taskScheduler;
    @Test
    public void startSchedule() throws InterruptedException
    {
        int count = 0;
        future = taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                // 这里编写任务代码
                System.out.println("Hello World!");
            }
        }, new CronTrigger("0/2 * * * * ?")); // 每分钟执行一次

        while(true) {
            Thread.sleep(2000);
            count++;
            if (count == 10) {
                System.out.println("stop schedule");
                if (future != null) {
                    future.cancel(true);
                }
            }
        }
    }
    @Test
    public void stopSchedule() {
        if (future != null) {
            future.cancel(true);
        }
    }
    @Test
    public void getMSEResult() throws JSchException, IOException, InterruptedException, CsvException
    {
        TestTask testTask = testTaskMapper.selectById(22);
        Configuration configuration = configurationMapper.selectById(testTask.getConfigId());
        testTaskService.processOperatorsMSETestResult(testTask, configuration);
    }

    @Test
    public void queryMSELinesTest() {
        simulationService.queryMSETestTaskProgress();
    }

    @Test
    public void downloadMseTest() {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        System.out.println("\n# Operators Test Download Result File");
        if(!sshUtil.downloadFile(Constants.CARLA_RADAR_RESULT_PATH + "mse.csv", Constants.TEST_RESULT_DOWNLOAD_PATH  + "FullTest2/mse.csv")) {
            System.out.println("# Task FullTest2 operators test result csv download failed.");
        }
    }

    @Test
    public void deleteTask() {
        String testResultPath = Constants.TEST_RESULT_DOWNLOAD_PATH + "Test";
        File taskDir = new File(testResultPath);
        if (taskDir.exists()) {
            System.out.println("delete task dir: " + taskDir.getAbsolutePath());
            deleteDirectory(taskDir);
        }
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

    @Test
    public void modelsDefectTest() throws JSchException, IOException, InterruptedException
    {
        simulationService.modelsDefectTest();
    }

    @Test
    public void testOutputStream() throws IOException
    {
        simulationService.setLogOutputStream();
        System.out.println("test multi output stream");
        simulationService.resetOutputStream();
        System.out.println("test multi output stream end");
    }
}
