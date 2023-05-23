package com.nineya.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.common.SSH.SSHUtil;
import com.nineya.springboot.common.SSH.SimulationService;
import com.nineya.springboot.constant.Constants;
import com.nineya.springboot.entity.Seed;
import com.nineya.springboot.entity.SimResult;
import com.nineya.springboot.mapper.SeedMapper;
import com.nineya.springboot.service.SeedService;
import com.nineya.springboot.service.SimResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ylq
 * @since 2023-04-02
 */
@Service
public class SeedServiceImpl extends ServiceImpl<SeedMapper, Seed> implements SeedService {
    @Autowired
    SeedMapper seedMapper;
    @Autowired
    SimResultService simResultService;
    @Autowired
    SimulationService simulationService;

    public R uploadSeedFile(MultipartFile seedFile, Seed seed) throws IOException {
        String filename = seedFile.getOriginalFilename();
        String upload = Constants.UPLOAD_PATH + "seeds/";
        // IO操作
        File fileDir = new File(upload);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        String path = upload + filename;
        File filePath = new File(path);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        outputStream.write(seedFile.getBytes());
        outputStream.flush();
        outputStream.close();
        // 数据库操作
        seed.setCreateDate(new Date());
        System.out.println(seed);
        seedMapper.insert(seed);
        System.out.println(seed.getId());
        return R.success("insert success", null);
    }

    @Override
    public R getAllSeeds(String filterName) {
        QueryWrapper<Seed> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", filterName);
        List<Seed> seedList = seedMapper.selectList(queryWrapper);
        return R.success(null, seedList);
    }

    @Override
    public R getFileContent(Integer id) throws IOException {
        String fileName = seedMapper.selectById(id).getName();
        fileName = Constants.UPLOAD_PATH + "seeds/" + fileName;
        // 文件操作，读取fileName路径的文件内容
        byte[] bytes = Files.readAllBytes(Paths.get(fileName));
        return R.success(null, new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public R modifySeed(Seed seed) {
        Seed Seed = seedMapper.selectById(seed.getId());
        Seed.setCarlaMap(seed.getCarlaMap());
        Seed.setType(seed.getType());
        Seed.setDesc(seed.getDesc());
        seedMapper.updateById(Seed);
        return R.success(null, Seed);
    }

    public R deleteSeed(Integer id) {
        File deleteFile = new File(Constants.UPLOAD_PATH + "seeds/" + seedMapper.selectById(id).getName());
        if (deleteFile.exists()) {
            deleteFile.delete();
        }
        seedMapper.deleteById(id);
        return getAllSeeds("");
    }

    public R simulate(Integer id) throws JSchException, IOException, InterruptedException
    {
        // 插入仿真结果记录
        String seedName = seedMapper.selectById(id).getName();
        // 如果已经存在仿真结果的SeedId与参数id相同，则不再插入
        QueryWrapper<SimResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seed_id", id);
        int record_cnt = simResultService.list(queryWrapper).size();
        if (record_cnt > 0)
        {
            return R.error("Simulation Result Already Exists");
        }
        // 创建新的场景仿真任务并开始执行
        SimResult simResult = new SimResult();
        simResult.setName(seedName + " Simulation");
        simResult.setSeedId(id);
        simResult.setAuthor("admin");
        simResult.setCreateDate(new Date());
        simResult.setTaskStatus(1);
        simResult.setTaskDesc(seedMapper.selectById(id).getDesc());
        simResultService.addResult(simResult);
        System.out.println("simResult inserted" + simResult);
        // 调用SSHUtil将seedName上传至服务器目录，并执行仿真指令
        int simulateStatus = 1;
        SSHUtil sshUtil = new SSHUtil(Constants.USER, Constants.PASSWORD, Constants.HOST, Constants.PORT);
        sshUtil.connect();
//        System.out.println("local path:" + Constants.UPLOAD_PATH + "seeds/" + seedName + "  to path:" +  Constants.CARLA_UPLOAD_PATH + seedName);
        sshUtil.uploadFile(Constants.UPLOAD_PATH + "seeds/" + seedName, Constants.CARLA_SEED_UPLOAD_PATH + seedName);
        simulationService.setLogOutputStream();
        // Carla端会循环尝试仿真，最多失败3次返回错误
        int generate_img_cnt = simulationService.simulate(seedName);
        System.out.println("Get Simulation Return Files Count: " + generate_img_cnt);
        if (generate_img_cnt < 100)
        {
            simulateStatus = 0;
        } else {
            String zipCommand = "cd " + Constants.CARLA_SIMULATION_RESULT_PATH + " && zip -r result.zip *";
            sshUtil.executeCommandFullOutputStream(zipCommand);
            Thread.sleep(2000);
            if(!sshUtil.downloadFile(Constants.CARLA_SIMULATION_RESULT_PATH + "result.zip", Constants.SIMULATION_RESULT_DOWNLOAD_PATH +  seedName.split("\\.")[0] + ".zip")) {
                System.out.println("# Seed " + seedName + " zip download failed.");
                simulateStatus = 0;
            } else {
                String gifCommand = "source /home/vangogh/anaconda3/etc/profile.d/conda.sh && conda activate dave && python /home/vangogh/software/FuzzScene/code/steering-curve.py --type ori_gif";
                sshUtil.executeCommandFullOutputStream(gifCommand);
                System.out.println("Generating scene gif...");
                Thread.sleep(13000);
                if(!sshUtil.downloadFile(Constants.CARLA_SIMULATION_RESULT_PATH + "result.gif", Constants.SIMULATION_RESULT_DOWNLOAD_PATH +  seedName.split("\\.")[0] + ".gif")) {
                    System.out.println("# Seed " + seedName + " gif download failed.");
                    simulateStatus = 0;
                } else {
                    simulateStatus = 2;
                }
            }
        }
        sshUtil.disconnect();
        simResult.setTaskStatus(simulateStatus);
        simResultService.modifyTask(simResult);
        simulationService.resetOutputStream();
        // TODO:完成消息机制，在完成仿真任务后，将gif动图和zip文件下载到本地存储，并修改前端状态结果显示已完成，否则返回未完成。以文件名为不可修改的唯一标示查找和删除仿真结果。
        return R.success("simulate complete", null);
    }
}
