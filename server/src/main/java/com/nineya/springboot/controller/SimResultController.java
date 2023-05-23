package com.nineya.springboot.controller;


import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.SimResult;
import com.nineya.springboot.service.SimResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ylq
 * @since 2023-04-06
 */
@RestController
@RequestMapping("/sim_result")
public class SimResultController {
    @Autowired
    SimResultService simResultService;
    @GetMapping("/getAll")
    public R getAllResults(@RequestParam("name") String filterName) {
        return simResultService.getAllResults(filterName);
    }
    @PostMapping("/modify")
//    public R modifyTask(@RequestParam("id") String id, @RequestParam("name") String name, @RequestParam("desc") String desc) {
    public R modifyTask(@RequestBody SimResult simResult) {
        System.out.println("modifyTask@" + simResult);
//        return R.success("success", null);
        return simResultService.modifyTask(simResult);
    }

    @GetMapping(value="/download", produces= MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFile(@RequestParam("id") String simResultId) throws IOException {
        return simResultService.downloadFile(simResultId);
    }

    @PostMapping("/delete/{id}")
    public R deleteTask(@PathVariable("id") String id) {
        return simResultService.deleteTask(Integer.parseInt(id));
    }

    @GetMapping("/visualize/{id}")
    public ResponseEntity<Resource> getVisualizeResult(@PathVariable("id") String id) throws IOException {
        return simResultService.getVisualizeResult(Integer.parseInt(id));
    }

}
