package com.nineya.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.Configuration;
import com.nineya.springboot.entity.TestTask;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ylq
 * @since 2023-04-02
 */
public interface TestTaskService extends IService<TestTask> {
    R getAllInfo();
    R createConfig(Configuration configuration);
    R createTask(Configuration configuration, String taskName, String selectedModels, String selectedSeeds, String selectedMutSeeds, String desc);
    R getAllTasks(String filterName);
    R getModels(Integer taskId);
    R deleteTask(Integer id);
    R executeTask(Integer id) throws JSchException, IOException, InterruptedException, CsvException;
    R terminateTask(Integer taskId) throws IOException;
}
