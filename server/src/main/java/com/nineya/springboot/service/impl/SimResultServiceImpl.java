package com.nineya.springboot.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nineya.springboot.common.R;
import com.nineya.springboot.constant.Constants;
import com.nineya.springboot.entity.SimResult;
import com.nineya.springboot.mapper.SeedMapper;
import com.nineya.springboot.mapper.SimResultMapper;
import com.nineya.springboot.service.SimResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ylq
 * @since 2023-04-06
 */
@Service
public class SimResultServiceImpl extends ServiceImpl<SimResultMapper, SimResult> implements SimResultService {
    @Autowired
    SimResultMapper simResultMapper;
    @Autowired
    SeedMapper seedMapper;
    @Override
    public R getAllResults(String filterName) {
        QueryWrapper<SimResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", filterName);
        List<SimResult> resultList = simResultMapper.selectList(queryWrapper);
        List<String> jsonList = new ArrayList<>();
        for (SimResult simResult : resultList) {
            JSONObject json = (JSONObject) JSON.toJSON(simResult);
            json.put("seedName", seedMapper.selectById(simResult.getSeedId()).getName());
//            System.out.println(json);
            jsonList.add(JSON.toJSONString(json));
        }
        return R.success(null, JSONArray.from(jsonList));
    }
    @Override
    public R addResult(SimResult simResult) {
        simResultMapper.insert(simResult);
        return R.success("insert success", null);
    }
    @Override
    public R modifyTask(SimResult simResult) {
        SimResult target = simResultMapper.selectById(simResult.getId());
        target.setName(simResult.getName());
        target.setTaskDesc(simResult.getTaskDesc());
        simResultMapper.updateById(simResult);
//        System.out.println(modelMapper.selectById(id));
        return R.success(null, simResult);
    }
    @Override
    public R deleteTask(Integer id) {
        String seedFileName = seedMapper.selectById(simResultMapper.selectById(id).getSeedId()).getName().split("\\.")[0];
        System.out.println("deleteTask@" + seedFileName);
        File deleteZipFile = new File(Constants.SIMULATION_RESULT_DOWNLOAD_PATH + seedFileName + ".zip");
        if (deleteZipFile.exists()) {
            deleteZipFile.delete();
        }
        File deleteGifFile = new File(Constants.SIMULATION_RESULT_DOWNLOAD_PATH + seedFileName + ".gif");
        if (deleteGifFile.exists()) {
            deleteGifFile.delete();
        }
        simResultMapper.deleteById(id);
        return getAllResults("");
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String simResultId) throws IOException
    {
        int seedId = simResultMapper.selectById(simResultId).getSeedId();
        String seedName = seedMapper.selectById(seedId).getName().split("\\.")[0];
        String filename = seedName + ".zip";
        String url = Constants.SIMULATION_RESULT_DOWNLOAD_PATH + filename;
        File file = new File(url);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // 非常重要，不设置这个，前端无法获取到文件名，Fuck!~
        // 还有因为mock.js会拦截下载类型请求进行封装改变bold请求头导致前端无法解析
        List<String> accessControlExposeHeadersList = new ArrayList<>();
        accessControlExposeHeadersList.add("Content-Disposition");
        headers.setAccessControlExposeHeaders(accessControlExposeHeadersList);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getName()).build());
        System.out.println("downloadFile headers: " + headers);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> getVisualizeResult(Integer id) throws IOException {
        String seedNamePre = seedMapper.selectById(simResultMapper.selectById(id).getSeedId()).getName().split("\\.")[0];
        File imageFile = new File(Constants.SIMULATION_RESULT_DOWNLOAD_PATH + seedNamePre + ".gif");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_GIF);
        Resource resource = new InputStreamResource(new FileInputStream(imageFile));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
