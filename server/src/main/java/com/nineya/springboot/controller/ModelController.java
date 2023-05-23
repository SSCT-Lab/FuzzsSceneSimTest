package com.nineya.springboot.controller;


import com.nineya.springboot.common.R;
import com.nineya.springboot.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ylq
 * @since 2023-04-02
 */
@RestController
@RequestMapping("/model")
public class ModelController {
    @Autowired
    private ModelService modelServiceService;

    @GetMapping("/getAll")
    public R getAllModels(@RequestParam("name") String filterName) {
        return modelServiceService.getAllModels(filterName);
    }

    @PostMapping("/upload")
    public R uploadModel(@RequestParam("file") MultipartFile[] fileList, @RequestParam("name") String modelName, @RequestParam("fileName") String fileName, @RequestParam("desc") String modelDesc) throws Exception {
        System.out.println("modifyModel PARAM " + " @name = " + modelName + "@fileName = " + fileName + " @desc = " + modelDesc);
        for (MultipartFile file : fileList) {
            modelServiceService.uploadModel(file, modelName, fileName, modelDesc);
        }
        return null;
    }

    @PostMapping("/modify")
    public R modifyModel(@RequestParam("id") String id, @RequestParam("name") String name, @RequestParam("desc") String desc) {
        System.out.println("modifyModel PARAM @id = " + Integer.parseInt(id) + " @name = " + name + " @desc = " + desc);
        return modelServiceService.modifyModel(Integer.parseInt(id), name, desc);
    }

    @PostMapping("/delete/{id}")
    public R deleteModel(@PathVariable("id") String id) {
        System.out.println("deleteModel PARAM @id = " + Integer.parseInt(id));
        return modelServiceService.deleteModel(Integer.parseInt(id));
    }

}
