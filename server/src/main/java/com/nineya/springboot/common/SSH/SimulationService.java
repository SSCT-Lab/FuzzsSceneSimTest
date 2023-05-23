package com.nineya.springboot.common.SSH;

import com.jcraft.jsch.*;
import com.nineya.springboot.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;

// 仿真服务，用于执行场景仿真，既可以用shell方式执行指令，也可以调用FileUtil通过exec方式执行指令
@Service
public class SimulationService
{
    MultiOutputStream multi = null;
    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> future;

    public int simulate(String fileName) throws IOException, JSchException, InterruptedException
    {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        Session session = sshUtil.getSession();
        Channel channel = session.openChannel("shell");
        ((ChannelShell) channel).setPty(true);
        channel.setXForwarding(true);

        OutputStream outstream = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        /* SIMULATION BEGIN */
        String[] commands = {"pkill -9 Carla && pkill -9 python", "cd /home/vangogh/software/FuzzScene/code/GA", "conda activate carla", "python simulation_carla.py " + fileName};
        byte[] tmp = new byte[1024];
        // 执行之前先清进程
        outstream.write((commands[0] + "\n").getBytes());
        outstream.flush();
        Thread.sleep(1000);
        // 执行命令列表中的命令
        for (int i = 0; i < commands.length; i++)
        {
            String command = commands[i];
            System.out.println("Executing command " + command);
            outstream.write((command + "\n").getBytes());
            outstream.flush();
            while (true)
            {
                // 输出流中有数据就按块取出
                while (in.available() > 0)
                {
                    int j = in.read(tmp, 0, 1024);
                    if (j <= 0) break;
                    String output = new String(tmp, 0, j);
                    System.out.println(output);
                    if (output.contains("simulation_carla SUCCESS finished!") || output.contains("### 125_images"))
                    {
                        System.out.println("\n--------Simulation finished! Checked simulation_carla SUCCESS---------\n");
//                        sshUtil.connect();
                        String lsCommand = "cd " + Constants.CARLA_SIMULATION_RESULT_PATH + " && ls";
                        String[] files = sshUtil.executeCommandFullOutputStream(lsCommand).split("\n");
                        postProcess(channel, session, sshUtil);
                        return files.length;
                    } else if (output.contains("simulation_carla FAILED 5 times.") || output.contains("EXIT WITH -1"))
                    {
                        System.out.println("\n--------Simulation failed! Checked simulation_carla Failed 3 times.---------\n");
                        postProcess(channel, session, sshUtil);
                        return 0;
                    }
                }
                if (channel.isClosed())
                {
                    if (in.available() > 0) continue;
                    System.out.println("shell channel exit-status: " + channel.getExitStatus());
                    break;
                } else
                {
                    if (i == commands.length - 1)
                    {
                        Thread.sleep(10000);
//                        if (in.available() <= 0)
//                        {
//                            outstream.write(("exit\n").getBytes());
//                            outstream.flush();
//                            break;
//                        }
                    } else
                    {
                        Thread.sleep(2000);
                        break;
                    }
                }
            }
        }
        /* SIMULATION END */
        postProcess(channel, session, sshUtil);
        return 0;
    }

    public int operatorsMSETest() throws IOException, JSchException, InterruptedException {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        Session session = sshUtil.getSession();
        // Shell可交互通道
        Channel channel = session.openChannel("shell");
        ((ChannelShell) channel).setPty(true);
        channel.setXForwarding(true);
        // 命令输入流
        OutputStream outstream = channel.getOutputStream();
        // 命令执行结果输出流
        InputStream in = channel.getInputStream();
        channel.connect();
        // 开启心跳任务，每隔5分钟发送一个回车字符，保持Shell连接
        startHeartBeat(outstream);
        // 测试开始
        String[] fuzzCommands = {"pkill -9 Carla && pkill -9 python", "cd " + Constants.CARLA_RADAR_PATH, "conda activate dave", "python begin_radar_test.py "};
        byte[] tmp = new byte[1024];
        for (int i = 0; i < fuzzCommands.length; i++) {
            String command = fuzzCommands[i];
            outstream.write((command + "\n").getBytes());
            outstream.flush();
            while (true)
            {
                // 输出流中有数据就按块取出
                while (in.available() > 0)
                {
                    int j = in.read(tmp, 0, 1024);
                    if (j <= 0) break;
                    String output = new String(tmp, 0, j);
                    System.out.println(output);
                    if (output.contains("# Operators Test Over"))
                    {
                        System.out.println("\n-------- OperatorsMSETest finished! Checked Operators Test Over---------\n");
                        closeHeartBeat();
                        postProcess(channel, session, sshUtil);
                        return 2;
                    }
                }
                if (channel.isClosed())
                {
                    if (in.available() > 0) continue;
                    System.out.println("shell channel exit-status: " + channel.getExitStatus());
                    break;
                } else
                {
                    if (i == fuzzCommands.length - 1)
                    {
                        Thread.sleep(10000);
                    } else
                    {
                        Thread.sleep(1000);
                        break;
                    }
                }
            }
        }
        closeHeartBeat();
        postProcess(channel, session, sshUtil);
        return 0;
    }

    public int modelsDefectTest() throws IOException, JSchException, InterruptedException
    {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        Session session = sshUtil.getSession();
        // Shell可交互通道
        Channel channel = session.openChannel("shell");
        ((ChannelShell) channel).setPty(true);
        channel.setXForwarding(true);
        // 命令输入流
        OutputStream outstream = channel.getOutputStream();
        // 命令执行结果输出流
        InputStream in = channel.getInputStream();
        channel.connect();
        startHeartBeat(outstream);
        /* FUZZTEST BEGIN */
        String[] fuzzCommands = {"pkill -9 Carla && pkill -9 python", "rm -rf " + Constants.CARLA_SIMULATION_RESULT_PATH + "*", "cd " + Constants.CARLA_GA_PATH, "conda activate dave", "python fuzz_ga.py "};
        byte[] tmp = new byte[1024];
        for (int i = 0; i < fuzzCommands.length; i++) {
            String command = fuzzCommands[i];
            outstream.write((command + "\n").getBytes());
            outstream.flush();
            while (true)
            {
                // 输出流中有数据就按块取出
                while (in.available() > 0)
                {
                    int j = in.read(tmp, 0, 1024);
                    if (j <= 0) break;
                    String output = new String(tmp, 0, j);
                    System.out.println(output);
                    if (output.contains("# GA Loop Test Over"))
                    {
                        System.out.println("\n--------Test Task finished! Checked GA Loop Test Over---------\n");
                        closeHeartBeat();
                        postProcess(channel, session, sshUtil);
                        return 2;
                    }
                }
                if (channel.isClosed())
                {
                    if (in.available() > 0) continue;
                    System.out.println("shell channel exit-status: " + channel.getExitStatus());
                    break;
                } else
                {
                    if (i == fuzzCommands.length - 1)
                    {
                        Thread.sleep(10000);
                    } else
                    {
                        Thread.sleep(2000);
                        break;
                    }
                }
            }
        }
        closeHeartBeat();
        postProcess(channel, session, sshUtil);
        return 0;
    }

    public int predictAndGenerateGif(String modelIdx, int arrow_type) throws JSchException, IOException, InterruptedException
    {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        Session session = sshUtil.getSession();
        Channel channel = session.openChannel("shell");
        ((ChannelShell) channel).setPty(true);
        channel.setXForwarding(true);

        OutputStream outstream = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();
        
        String[] commands = {"cd " + Constants.CARLA_ROOT_PATH, "conda activate dave", "python steering-curve.py --model " + modelIdx + " --type " + arrow_type};
        byte[] tmp = new byte[1024];
        boolean commandFinished = false;
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            outstream.write((command + "\n").getBytes());
            outstream.flush();
            while (true)
            {
                // 输出流中有数据就按块取出
                while (in.available() > 0)
                {
                    int j = in.read(tmp, 0, 1024);
                    if (j <= 0) break;
                    String output = new String(tmp, 0, j);
                    System.out.println(output);
                    if (output.contains("# Generate Arrowed GIF Over"))
                    {
                        System.out.println("--------PredictAndGenerateGif finished! Checked Generate Arrowed GIF Over---------");
                        commandFinished = true;
                    }
                }
                if (channel.isClosed())
                {
                    if (in.available() > 0) continue;
                    System.out.println("shell channel exit-status: " + channel.getExitStatus());
                    break;
                } else
                {
                    if (i == commands.length - 1)
                    {
                        if (commandFinished) { // 如果指令执行完成
                            Thread.sleep(1000); // 等待一段时间，确保所有输出都已经被读取
                            if (in.available() > 0) continue; // 如果还有数据可读，继续读取
                            System.out.println("\n--------All commands finished!---------\n");
                            postProcess(channel, session, sshUtil);
                            return 0;
                        } else {
                            Thread.sleep(10000);
                        }
                    } else
                    {
                        Thread.sleep(2000);
                        break;
                    }
                }
            }
        }
        postProcess(channel, session, sshUtil);
        return 0;
    }

    public int queryDefectsTestTaskProgress()
    {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        int retLines = 0;
        String command = "wc -l " + Constants.CARLA_GA_PATH + "sample_num_vgg.csv";
        String result = sshUtil.executeCommand(command);
        retLines += Integer.parseInt(result.split(" ")[0]) - 1;
        command = "wc -l " + Constants.CARLA_GA_PATH + "error_count.csv";
        result = sshUtil.executeCommand(command);
        retLines += Integer.parseInt(result.split(" ")[0]) - 1;
        sshUtil.disconnect();
        return retLines;
    }

    public int queryMSETestTaskProgress() {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        int retLines = 0;
        String command = "wc -l " + Constants.CARLA_RADAR_RESULT_PATH + "label_data/label_test.csv";
        String result = sshUtil.executeCommand(command);
        retLines += Integer.parseInt(result.split(" ")[0]) - 1;
        command = "ls " + Constants.CARLA_RADAR_RESULT_PATH;
        result = sshUtil.executeCommand(command);
        String[] files = result.split("\r\n");
        for (String file : files) {
            if (file.matches("radar\\d+\\.csv")) {
                System.out.println(file + " matched.");
                command = "wc -l " + Constants.CARLA_RADAR_RESULT_PATH + file;
                result = sshUtil.executeCommand(command);
                retLines += Integer.parseInt(result.split(" ")[0]) - 1;
                System.out.println("retLines: " + retLines);
            }
        }
        sshUtil.disconnect();
        return retLines;
    }

    public void terminateCarla() {
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
        String command = "pkill -9 python && pkill -9 Carla";
        sshUtil.executeCommand(command);
        sshUtil.disconnect();
        return;
    }

    public void postProcess(Channel channel, Session session, SSHUtil sshUtil) throws IOException
    {
        if (channel != null && channel.isConnected()) {
            channel.getOutputStream().write(("exit\n").getBytes());
            channel.getOutputStream().flush();
            if (channel.isConnected())
            {
                channel.disconnect();
            }
        }
        if (session != null && session.isConnected()) session.disconnect();
        if (sshUtil != null)    sshUtil.disconnect();
    }

    public void setLogOutputStream() throws IOException
    {
        PrintStream outFile = null;
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

    public void resetOutputStream() throws IOException
    {
        if (multi != null)
            multi.close();
        System.setOut(System.out);
    }

    public void startHeartBeat(OutputStream outstream) throws IOException
    {
        System.out.println("# Begin HeartBeat Task!");
        future = taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    outstream.write(("\n").getBytes());
                    System.out.println("\n# HeartBeat sent!\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new CronTrigger("0 0/5 * * * ?")); // 5分钟执行一次
    }

    public void closeHeartBeat() {
        if (future != null) {
            future.cancel(true);
        }
    }
}
