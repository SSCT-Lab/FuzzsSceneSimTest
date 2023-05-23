package com.nineya.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nineya.springboot.common.R;
import com.nineya.springboot.constant.Constants;
import com.nineya.springboot.entity.Model;
import com.nineya.springboot.mapper.ModelMapper;
import com.nineya.springboot.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements ModelService {
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public R insertModel(Model model) {
        try {
            if (modelMapper.insert(model) > 0) {
                return R.success("插入成功");
            } else {
                return R.error("插入失败");
            }
        } catch (DuplicateKeyException e) {
            return R.fatal(e.getMessage());
        }
    }

    @Override
    public R uploadModel(MultipartFile modelFile, String modelName, String fileName, String modelDesc) throws IOException {
        System.out.println(modelFile);
        String filename = modelFile.getOriginalFilename();
//            System.out.println(filename);
        String upload = Constants.UPLOAD_PATH + "models/";
//            System.out.println(upload);

        File fileDir = new File(upload);
//            System.out.println(fileDir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File[] files = fileDir.listFiles();
//            System.out.println(files.length);
//            System.out.println(files);
        String path = upload + filename;
        File filePath = new File(path);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        outputStream.write(modelFile.getBytes());
        outputStream.flush();
        outputStream.close();

        Model model = new Model();
        model.setName(modelName);
        model.setFileName(fileName);
        model.setSize(Double.valueOf(modelFile.getSize() / 1024));
        model.setCreateDate(new Date());
        model.setType(modelFile.getOriginalFilename().substring(modelFile.getOriginalFilename().lastIndexOf(".")));
        model.setDesc(modelDesc);
        insertModel(model);
//        System.out.println(model.getId());

        return R.success("insert success", null);
    }

    @Override
    public R getAllModels(String filterName) {
        QueryWrapper<Model> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", filterName);
        List<Model> modelList = modelMapper.selectList(queryWrapper);
        return R.success(null, modelList);
    }
    @Override
    public R deleteModel(Integer id) {
        File deleteFile = new File(Constants.UPLOAD_PATH + "models/" + modelMapper.selectById(id).getFileName());
        if (deleteFile.exists()) {
            deleteFile.delete();
        }
        modelMapper.deleteById(id);
        return getAllModels("");
    }

    @Override
    public R modifyModel(Integer id, String name, String desc) {
        Model model = modelMapper.selectById(id);
        model.setName(name);
        model.setDesc(desc);
        modelMapper.updateById(model);
//        System.out.println(modelMapper.selectById(id));
        return R.success(null, model);
    }
}
