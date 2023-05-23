package com.nineya.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.SimResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ylq
 * @since 2023-04-06
 */
public interface SimResultService extends IService<SimResult> {

    R getAllResults(String filterName);
    R addResult(SimResult simResult);
    R modifyTask(SimResult simResult);
    ResponseEntity<Resource> downloadFile(String simResultId) throws IOException;
    R deleteTask(Integer id);
    ResponseEntity<Resource> getVisualizeResult(Integer id) throws IOException;
}
