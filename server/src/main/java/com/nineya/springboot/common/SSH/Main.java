package com.nineya.springboot.common.SSH;

import com.jcraft.jsch.JSchException;
import com.nineya.springboot.constant.Constants;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main
{
    private static final String USER = "vangogh";
    private static final String PASSWORD = "980326";
    private static final String HOST = "192.168.1.2";
    private static final int PORT = 22;

    public static void main(String[] args) throws InterruptedException, JSchException, IOException
    {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();

        System.out.println("Uploading error_count");
        String path="D:\\FuzzScene without data\\code\\GA\\ga_output\\";
        File resultFile = new File(path);
        List<String> uploadFileNameList = new ArrayList<>();
        if (resultFile.exists()) {
            File[] fileList = resultFile.listFiles();
            for (File file : fileList){
                if (file.getName().contains("csv")){
                    uploadFileNameList.add(file.getName());
                }
            }
        }
        System.out.println("uploadFileNameList: " + uploadFileNameList);
        for (String fileName : uploadFileNameList)
        {
            System.out.println("upload error_count file: " + fileName);
            sshUtil.uploadFile(path + fileName, Constants.CARLA_TEST_RESULT_PATH + fileName);
        }



//        String gifCommand = "bash -ic 'conda activate dave && python /home/vangogh/software/FuzzScene/code/steering-curve.py --type ori_gif'";
//        System.out.println(sshUtil.executeCommand(gifCommand));
//        System.out.println("Generating scene gif...");
//        Thread.sleep(15000);
//        String lscommand = "cd " + Constants.CARLA_SIMULATION_RESULT_PATH + " && ls";
//        System.out.println(sshUtil.executeCommand(lscommand));

//        sshUtil.downloadFile(Constants.CARLA_SIMULATION_RESULT_PATH + "result.gif", Constants.SIMULATION_RESULT_DOWNLOAD_PATH +  "seed_0_0_1" + ".gif");
//        setLogOutputStream();

        SimulationService simulationService = new SimulationService();
//        System.out.println(sshFileUtil.validateGeneratePath("/home/vangogh/code/scenario_runner-0.9.13/_out/center"));
//        Random random = new Random();
//          System.out.println(pickRandomFile("/home/vangogh/code/seed_pool"));
//        System.out.println(("seed_0_0_1.xosc").split("\\.")[0]);
//        int[][] img_cnt = new int[6][10];
//        for (int loops = 1; loops <= 10; loops++)
//        {
//            for (int fileId = 1; fileId <= 6; fileId++)
//            {
//                int fail_cnt = 0;
//                while (true)
//                {
//                    int generate_img_cnt = simulationService.simulate("seed_0_0_" + fileId + ".xosc");
//                    if (generate_img_cnt < 100)
//                    {
//                        fail_cnt += 1;
//                        if (fail_cnt >= 3) {
//                            System.out.println("# Seed seed_0_0_" + fileId + " @" + loops + "retried 3 Times. Simulation failed. ");
//                            break;
//                        }
//                        System.out.println("# Seed seed_0_0_" + fileId + " generated " + generate_img_cnt + " images, Retrying " + fail_cnt + " times");
//                    } else {
//                        img_cnt[fileId - 1][loops - 1] = generate_img_cnt;
//                        System.out.println("# Seed seed_0_0_" + fileId + " @" + loops + " Main Caller Return: True" + " Generated Imgs: " + generate_img_cnt);
//                        break;
//                    }
//                }
//            }
//        }
//
//        for (int fileId = 1; fileId <= 6; fileId++)
//        {
//            int cnt = 0;
//            for (int loops = 1; loops <= 10; loops++)
//            {
//                if (img_cnt[fileId - 1][loops - 1] > 0)
//                    cnt++;
//            }
//            System.out.println("# Seed " + fileId + " Success Rate: " + cnt + "/10" + ", Img cnt list: " + Arrays.toString(img_cnt[fileId - 1]));
//        }
//        sshCommandExecutor.clearProgress();
    }

    public static void setLogOutputStream() throws IOException
    {
        PrintStream outFile = null;
        MultiOutputStream multi = null;
        try
        {
            String curTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
            outFile = new PrintStream(new FileOutputStream(Constants.SIMULATION_LOG_PATH + curTime + ".log", true));
            //设置同时输出到控制台和日志文件
            multi = new MultiOutputStream(outFile, System.out);
            System.setOut(new PrintStream(multi));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
