package com.nineya.springboot.service;

import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.Configuration;
import com.nineya.springboot.entity.Model;
import com.nineya.springboot.entity.TestResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nineya.springboot.entity.TestTask;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.core.Constants;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ylq
 * @since 2023-05-10
 */
public interface TestResultService extends IService<TestResult> {
    R getResult(Integer id);

    ResponseEntity<Resource> getVisualizeResult(Integer taskId, String modelName, String gifType) throws IOException;

    String processErrorCountCSV(TestTask task, Model model, String csvPath) throws IOException, CsvException;

    void processOperatorsTestCSV(TestTask task, Configuration configuration, String csvPath) throws IOException, CsvException;

    int processMostErrorSeed(TestTask testTask, Model model, String mostErrorCntAndDivSeedName) throws JSchException, IOException, InterruptedException;
}
