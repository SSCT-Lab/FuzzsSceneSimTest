package com.nineya.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.Seed;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ylq
 * @since 2023-04-02
 */
public interface SeedService extends IService<Seed> {

    R uploadSeedFile(MultipartFile seedFile, Seed seed) throws IOException;
    R getAllSeeds(String filterName);
    R deleteSeed(Integer id);
    R simulate(Integer id) throws JSchException, IOException, InterruptedException;
    R modifySeed(Seed seed);
    R getFileContent(Integer id) throws IOException;

}
