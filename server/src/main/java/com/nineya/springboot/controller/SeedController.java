package com.nineya.springboot.controller;


import com.alibaba.fastjson.JSON;
import com.jcraft.jsch.JSchException;
import com.nineya.springboot.common.R;
import com.nineya.springboot.entity.Seed;
import com.nineya.springboot.service.SeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("/seed")
public class SeedController {
    @Autowired
    private SeedService seedService;

    @PostMapping("/upload")
    public R uploadSeed(@RequestParam("file") MultipartFile[] fileList, @RequestParam("seedInfo") String seedInfo) throws Exception {
        Seed seed = JSON.parseObject(seedInfo, Seed.class);
        for (MultipartFile file : fileList) {
            seedService.uploadSeedFile(file, seed);
        }
        return null;
    }
    /*
    前端GET请求第二个参数为params，{ params: { name: filename }}，后端使用@RequestParam接收
     */
    @GetMapping("/getAll")
    public R getAllSeeds(@RequestParam("name") String filterName) {
        return seedService.getAllSeeds(filterName);
    }

    @GetMapping("/preview/{id}")
    public R getFileContent(@PathVariable("id") String id) throws IOException {
//        System.out.println("getFileContent PARAM " + id);
        return seedService.getFileContent(Integer.parseInt(id));
    }
    /*
    前端POST请求会的第二个参数为data，{key:value}，后端@RequestBody可以用String接收整个json串，也可以用对象接收
     */
    @PostMapping("/modify")
    public R modifySeed(@RequestBody Seed seed) {
        System.out.println("modifySeed" + seed);
//        Seed seed = JSON.parseObject(seedInfo, Seed.class);
        return seedService.modifySeed(seed);
    }

    /*
    axios.post('http://localhost:8080/seed/delete/' + row.id) POST/GET直接拼接url，后端使用@PathVariable接收
     */
    @PostMapping("/delete/{id}")
    public R deleteSeed(@PathVariable("id") String id) {
        return seedService.deleteSeed(Integer.parseInt(id));
    }
    @PostMapping("/simulate/{id}")
    public R simulate(@PathVariable("id") String id) throws JSchException, IOException, InterruptedException
    {
        return seedService.simulate(Integer.parseInt(id));
    }
}
