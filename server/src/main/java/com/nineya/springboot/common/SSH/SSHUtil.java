package com.nineya.springboot.common.SSH;

import com.jcraft.jsch.*;

import java.io.*;

// SSH通用工具类定义，负责SSH连接的建立和关闭、命令的执行和结果的返回、文件的上传和下载、文件的删除等
public class SSHUtil
{
    // SSH连接对象
    private Session session;
    // SSH连接用户名
    private String username;
    // SSH连接密码
    private String password;
    // SSH连接主机
    private String host;
    // SSH连接端口
    private int port;
    // SSH连接超时时间
    private int timeout;

    /**
     * SSH连接构造函数
     * @param username SSH连接用户名
     * @param password SSH连接密码
     * @param host SSH连接主机
     * @param port SSH连接端口
     */
    public SSHUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 创建session会话并连接
     */
    public void connect() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
//            session.setTimeout(timeout);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    /**
     * SSH关闭连接
     */
    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }

    /*
    * 获取SSH session
     */
    public Session getSession() {
        return session;
    }

    public String executeCommandFullOutputStream(String command) {
        System.out.println("SSH ExecuteCommand: " + command);
        String result = null;
        try {
            if (session == null || !session.isConnected()) {
                System.out.println("--------Session is null or not connected---------");
                connect();
            }
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            channelExec.setOutputStream(outputStream);
            channelExec.setErrStream(errorStream);
            channelExec.setCommand(command);
            channelExec.connect();
            while (!channelExec.isClosed()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int exitStatus = channelExec.getExitStatus();
            result = outputStream.toString() + errorStream.toString();
            channelExec.disconnect();
            if (exitStatus != 0) {
                System.err.println("Command failed with exit status " + exitStatus);
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * SSH执行命令
     * @param command 需执行的命令
     * @return SSH连接命令执行结果
     */
    public String executeCommand(String command) {
        System.out.println("SSH ExecuteCommand: " + command);
        // 设置SSH连接命令执行结果
        String result = null;
        try {
            if (session == null || !session.isConnected()) {
                System.out.println("--------Session is null or not connected---------");
                connect();
            }
            // 执行指令通道
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            // 设置执行指令通道的输入流
            InputStream in = channelExec.getInputStream();
            // 设置执行指令通道的命令
            channelExec.setCommand(command);
            // 执行SSH连接命令
            channelExec.connect();
            // TODO: 获取输出方式1
            // 读取输出流的内容
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }
                result = sb.toString();
            }
            // TODO: 获取输出方式2
            // 获取SSH连接命令执行结果
//            result = processStdout(channelExec, in);
            // 关闭通道的输入流
            in.close();
            // 关闭SSH连接通道
            channelExec.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 处理输入流数据
     * @param in 输入流
     * @param channelExec 通道
     * @return  输入流数据的字符串
     */
    private String processStdout(Channel channelExec, InputStream in) throws IOException {
        StringBuilder result = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                result.append(new String(tmp, 0, i));
            }
            if (channelExec.isClosed()) {
                if (in.available() > 0) continue;
//                System.out.println("processStdout exit-status: " + channelExec.getExitStatus());
                break;
            }
        }
//        if (result.length() < 0)
//            result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    /**
     * SSH上传文件
     * @param filePath SSH本地待上传文件路径
     * @param toFilePath SSH对端接收文件路径
     * @return
     */
    public void uploadFile(String filePath, String toFilePath) {
        try
        {
            if (session == null || !session.isConnected())
            {
                System.out.println("--------Session is null or not connected---------");
                connect();
            }
            // 创建SSH文件传输通道
            //        System.out.println(session.equals(null));
            Channel channel = session.openChannel("sftp");
            //        System.out.println(channel.equals(null));
            // 打开SSH文件传输通道
            channel.connect();
            // 设置SSH文件传输通道
            ChannelSftp channelSftp = (ChannelSftp) channel;
            //        System.out.println(channelSftp.equals(null));
            try
            {
                // 执行SSH连接文件上传
                channelSftp.put(filePath, toFilePath);
                System.out.println("Upload Success  " + toFilePath);
                // 关闭SSH连接通道
                channel.disconnect();
            } catch (SftpException e)
            {
                e.printStackTrace();
            }
        } catch (JSchException e) {
            e.printStackTrace();
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error cause: " + e.getCause());
        }
    }

    /**
    * SSH下载文件
    * @param filePath SSH对端待下载文件路径
    * @param toFilePath SSH本地存放文件路径
    */
    public boolean downloadFile(String filePath, String toFilePath){
        boolean success = false;
        System.out.println("--------Src FilePath: " + filePath + "---------");
        System.out.println("--------Dest FilePath: " + toFilePath + "---------");
        System.out.println("--------Start Downloading---------");
        try
        {
            if (session == null || !session.isConnected())
            {
                System.out.println("--------Session is null or not connected---------");
                connect();
            }
            // 创建SSH文件传输通道
            Channel channel = session.openChannel("sftp");
            // 打开SSH文件传输通道
            channel.connect();
            // 设置SSH文件传输通道
            ChannelSftp channelSftp = (ChannelSftp) channel;
            try
            {
                // 执行SSH连接文件下载
                channelSftp.get(filePath, toFilePath);
                // 关闭SSH连接通道
                channel.disconnect();
                System.out.println("--------Download Over---------\n");
                success = true;
            } catch (SftpException e)
            {
                e.printStackTrace();
            }
        }  catch (JSchException e) {
            e.printStackTrace();
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error cause: " + e.getCause());
        }
        return success;
    }
}
