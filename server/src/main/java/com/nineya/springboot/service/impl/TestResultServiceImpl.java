package com.nineya.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.nineya.springboot.common.R;
import com.nineya.springboot.common.SSH.SSHUtil;
import com.nineya.springboot.common.SSH.SimulationService;
import com.nineya.springboot.constant.Constants;

import com.nineya.springboot.entity.*;
import com.nineya.springboot.mapper.*;
import com.nineya.springboot.service.TestResultService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ylq
 * @since 2023-05-10
 */
@Service
public class TestResultServiceImpl extends ServiceImpl<TestResultMapper, TestResult> implements TestResultService {
    @Autowired
    private TestResultMapper testResultMapper;
    @Autowired
    private TestTaskMapper testTaskMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MseMapper mseMapper;
    @Autowired
    private SimulationService simulationService;

    @Override
    public R getResult(Integer taskId) {
        List<TestResultVO> testResultVOList = new ArrayList<>();
        // 根据taskId查表得到对应的测试任务结果列表
        List<TestResult> testResultList = testResultMapper.selectList(new QueryWrapper<TestResult>().eq("task_id", taskId));
        for (TestResult testResult : testResultList) {
            // 根据testResult中的mseId查表得到对应的mse
            Mse mse = mseMapper.selectById(testResult.getMseId());
            // 将testResult和mse封装到TestResultVO中
            TestResultVO testResultVO = new TestResultVO();
            testResultVO.setTestResult(testResult);
            testResultVO.setMse(mse);
            testResultVOList.add(testResultVO);
        }
//        System.out.println(testResultList);
        return R.success("success", testResultVOList);
    }

    @Override
    public ResponseEntity<Resource> getVisualizeResult(Integer taskId, String modelName, String gifType) throws IOException {
        TestTask testTask = testTaskMapper.selectById(taskId);
        String gifName = gifType + ".gif";
        File imageFile = new File(Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/" + modelName + "/" + gifName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_GIF);
        Resource resource = new InputStreamResource(new FileInputStream(imageFile));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @Override
    // 返回错误数和错误种类最多的变异场景文件名
    public String processErrorCountCSV(TestTask task, Model model, String csvPath) throws IOException, CsvException
    {
        CSVReader reader = new CSVReaderBuilder(new FileReader(csvPath)).build();
        List<Integer> errorCountList = new ArrayList<>();
        // 分采样轮和每轮循环记录错误数，方便前端作图
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
            errorCountList.add(curErrorCount);
            errorCount += curErrorCount;
            int loopIdx = Integer.parseInt(row[0].split("_")[1]);
            if (loopIdx != lastLoopIdx) {
                errorCountListByLoop.add(errorCount);
                lastLoopIdx = loopIdx;
            }
            divCount += Integer.parseInt(row[2]);
            // 维持最大错误数和错误种类的序号列表
            if (curErrorCount > maxErrorCount) {
                maxErrorCount = curErrorCount;
                mostErrorCntAndDivIdxList.clear();
                mostErrorCntAndDivIdxList.add(i);
            } else if (curErrorCount == maxErrorCount) {
                mostErrorCntAndDivIdxList.add(i);
            }
        }
        errorCountListByLoop.add(errorCount);
//        System.out.println("errorCountList: " + errorCountList);
//        System.out.println("errorCountListByLoop: Size: " + errorCountListByLoop.size() + " " + errorCountListByLoop);
//        System.out.println("divCount: " + divCount);

        // 继续分mostErrorCntAndDivIdxList是否唯一选择最终的mostErrorCntAndDivIdx
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
//        System.out.println("Selected seed: " + mostErrorCntAndDivIdx + " " + rows.get(mostErrorCntAndDivIdx)[0] +
//                " errorCount: " + rows.get(mostErrorCntAndDivIdx)[1] + " divCount: " + rows.get(mostErrorCntAndDivIdx)[2]);
        // 生成测试结果记录入库
        TestResult testResult = new TestResult();
        testResult.setTaskId(task.getId());
        testResult.setModelId(model.getId());
        testResult.setModelName(model.getName());
        testResult.setDivCount(divCount);
        testResult.setErrorCount(errorCountList.toString().replaceAll(" ", ""));
        testResult.setErrorCountPerLoop(errorCountListByLoop.toString().replaceAll(" ", ""));
        // 判断TestResult表中是否存在task_id和model_id均相同的记录，若存在则取出进行更新，否则插入
        TestResult testResultInDB = testResultMapper.selectOne(new QueryWrapper<TestResult>().eq("task_id", task.getId()).eq("model_id", model.getId()));
        if (testResultInDB != null) {
            testResult.setId(testResultInDB.getId());
            testResult.setMseId(testResultInDB.getMseId());
            testResultMapper.updateById(testResult);
            System.out.println("Update test result: " + testResult);
        } else {
            testResultMapper.insert(testResult);
            System.out.println("Insert test result: " + testResult);
        }
        // 返回错误数最多的场景，进行后续处理
        return mostErrorCntAndDivIdx == 0 ? rows.get(mostErrorCntAndDivIdx + 1)[0] : rows.get(mostErrorCntAndDivIdx)[0];
    }

    @Override
    public void processOperatorsTestCSV(TestTask task, Configuration configuration, String csvPath) throws IOException, CsvException
    {
        CSVReader reader = new CSVReaderBuilder(new FileReader(csvPath)).build();
        List<String[]> rows = reader.readAll();
        List<Model> modelList = modelMapper.selectList(new QueryWrapper<Model>().inSql("id", "select model_id from config2model where config_id = " + configuration.getId()));
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            double ori_mse = Double.parseDouble(row[1]);
            double vc_mse = Double.parseDouble(row[2]);
            double tm_mse = Double.parseDouble(row[3]);
            double si_mse = Double.parseDouble(row[4]);
            double fg_mse = Double.parseDouble(row[5]);
            double rn_mse = Double.parseDouble(row[6]);
            Mse mse = new Mse(ori_mse, vc_mse, tm_mse, si_mse, fg_mse, rn_mse);
            mseMapper.insert(mse);
            System.out.println("Insert mse: " + mse);
            Model model = modelList.get(i - 1);
            // 判断TestResult表中是否存在task_id和model_id均相同的记录，若存在则取出进行更新，否则插入
            TestResult testResultInDB = testResultMapper.selectOne(new QueryWrapper<TestResult>().eq("task_id", task.getId()).eq("model_id", model.getId()));
            if (testResultInDB != null) {
                testResultInDB.setMseId(mse.getId());
                testResultMapper.updateById(testResultInDB);
                System.out.println("Update test result: " + testResultInDB);
            } else {
                TestResult testResult = new TestResult();
                testResult.setTaskId(task.getId());
                testResult.setModelId(model.getId());
                testResult.setModelName(model.getName());
                testResult.setMseId(mse.getId());
                testResultMapper.insert(testResult);
                System.out.println("Insert test result: " + testResult);
            }
        }
    }

    @Override
    public int processMostErrorSeed(TestTask testTask, Model model, String mostErrorCntAndDivSeedName) throws JSchException, IOException, InterruptedException
    {
        String oriSeedName = "seed_0_0_" + mostErrorCntAndDivSeedName.split("_")[3];
        System.out.println("oriSeedName: " + oriSeedName);
        String[] seeds = new String[2];
        seeds[0] = oriSeedName;
        seeds[1] = mostErrorCntAndDivSeedName;
        for (int i = 0; i < 2; i++)
        {
            String seedName = seeds[i];
            // Carla端循环尝试仿真，最多失败3次返回错误
            int generate_img_cnt = simulationService.simulate(seedName);
            System.out.println("Get Simulation Return Files Count: " + generate_img_cnt);
            if (generate_img_cnt < 100)
            {
                System.out.println("# Simulation Failed!");
                return 0;
            } else {
                // 调用模型得到生成图像的转角预测值、画转角曲线、生成GIF
                System.out.println("# Simulation Succeed! Begin predict and generate GIF...");
                String taskName = testTask.getName();
                String modelIdx = String.valueOf(Constants.MODEL_MAP.get(model.getName()));
                simulationService.predictAndGenerateGif(modelIdx, i);
                // 准备SSHUtil服务
                SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
                sshUtil.connect();
                // 下载生成的GIF到本地对应测试结果文件夹中
                String localGifName = i == 0 ? "ori.gif" : "trans.gif";
                if(!sshUtil.downloadFile(Constants.CARLA_ERROR_GIF_PATH, Constants.TEST_RESULT_DOWNLOAD_PATH + testTask.getName() + "/" + model.getName() + "/" + localGifName)) {
                    System.out.println("# Task " + testTask.getName() + " result gif " + localGifName + " download failed.");
                    sshUtil.disconnect();
                    return 0;
                }
                sshUtil.disconnect();
            }
        }
        return 2;
    }
}
