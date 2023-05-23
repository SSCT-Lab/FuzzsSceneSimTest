package com.nineya.springboot.controller;


import com.alibaba.fastjson2.JSON;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.Configuration;
import com.nineya.springboot.service.TestTaskService;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ylq
 * @since 2023-04-02
 */
@RestController
@RequestMapping("/testTask")
public class TestTaskController {
    @Autowired
    TestTaskService testTaskService;

    @GetMapping("/init")
    public R getAllInfo() {
        return testTaskService.getAllInfo();
    }

    /*
    POST参数@RequestParam  对应的全是JSON串
    构造表单const param = new FormData()
    param.append("name", "testTaskName")
    const subparam = { selectedModels: [], selectedSeeds: [], selectedMutSeeds: [], priQueSize: '', initPopNum: '', sampling: false, loopNum: '', fitnessFuc: '' }
    param.append('config', JSON.stringify(subparam))
     */
    @PostMapping("/create")
    public R createTask(@RequestParam("name") String taskName, @RequestParam("selectedModels") String selectedModels,
                        @RequestParam("selectedSeeds") String selectedSeeds, @RequestParam("selectedMutSeeds") String selectedMutSeeds,
                        @RequestParam("config") String configInfo, @RequestParam("desc") String desc) {
        Configuration configuration = JSON.parseObject(configInfo, Configuration.class);
        System.out.println("createTask PARAM " + " @name = " + taskName + " @selectedModels = " + selectedModels + " @selectedSeeds = " + selectedSeeds + " @selectedMutSeeds = " + selectedMutSeeds + " @config = " + configInfo + " @desc = " + desc);
        return testTaskService.createTask(configuration, taskName, selectedModels, selectedSeeds, selectedMutSeeds, desc);
    }

    @GetMapping("/getAll")
    public R getAllTasks(@RequestParam("name") String filterName) {
        return testTaskService.getAllTasks(filterName);
    }

    @GetMapping("/getModels/{id}")
    public R getModels(@PathVariable("id") String taskId) {
        return testTaskService.getModels(Integer.parseInt(taskId));
    }

    @PostMapping("/delete/{id}")
    public R deleteTask(@PathVariable("id") String id) {
        return testTaskService.deleteTask(Integer.parseInt(id));
    }

    @PostMapping("/execute/{id}")
    public R executeTask(@PathVariable("id") String id) throws JSchException, IOException, InterruptedException, CsvException
    {
        return testTaskService.executeTask(Integer.parseInt(id));
    }

    @PostMapping("/terminate/{id}")
    public R terminateTask(@PathVariable("id") String id) throws IOException
    {
        return testTaskService.terminateTask(Integer.parseInt(id));
    }
}
