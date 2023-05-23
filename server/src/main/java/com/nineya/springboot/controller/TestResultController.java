package com.nineya.springboot.controller;


import com.nineya.springboot.common.R;
import com.nineya.springboot.service.TestResultService;
import com.nineya.springboot.service.TestTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ylq
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/testResult")
public class TestResultController {
    @Autowired
    TestResultService testResultService;

    @GetMapping("/getResult/{id}")
    public R getResult(@PathVariable("id") String taskId) throws IOException
    {
        return testResultService.getResult(Integer.parseInt(taskId));
    }

    @GetMapping("/getVisualizeResult/{taskId}")
    public ResponseEntity<Resource> getVisualizeResult(@PathVariable("taskId") String taskId, @RequestParam("modelName") String modelName, @RequestParam("type") String gifType) throws IOException {
        return testResultService.getVisualizeResult(Integer.parseInt(taskId), modelName, gifType);
    }

}
