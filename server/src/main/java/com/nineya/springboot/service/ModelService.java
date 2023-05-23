package com.nineya.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.Model;
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
public interface ModelService extends IService<Model> {
    R getAllModels(String filterName);
    R insertModel(Model model);
    R uploadModel(MultipartFile modelFile, String modelName, String fileName, String modelDesc) throws IOException;
    R deleteModel(Integer id);
    R modifyModel(Integer id, String name, String desc);
}
