package com.nineya.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author ylq
 * @since 2023-04-06
 */

@TableName("configuration")
public class Configuration implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("pri_queue_size")
    private Integer priQueueSize;

    @TableField("init_pop_size")
    private Integer initPopSize;

    @TableField("sampling")
    private Integer sampling;

    @TableField("radar_loop")
    private Integer radarLoop;

    @TableField("`loop`")
    private Integer loop;

    @TableField("fit_function")
    private String fitFunction;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPriQueueSize() {
        return priQueueSize;
    }

    public void setPriQueueSize(Integer priQueueSize) {
        this.priQueueSize = priQueueSize;
    }

    public Integer getInitPopSize() {
        return initPopSize;
    }

    public void setInitPopSize(Integer initPopSize) {
        this.initPopSize = initPopSize;
    }

    public Integer getSampling() {
        return sampling;
    }

    public void setSampling(Integer sampling) {
        this.sampling = sampling;
    }

    public Integer getRadarLoop()
    {
        return radarLoop;
    }

    public void setRadarLoop(Integer radarLoop)
    {
        this.radarLoop = radarLoop;
    }

    public String getFitFunction()
    {
        return fitFunction;
    }

    public Integer getLoop() {
        return loop;
    }

    public void setLoop(Integer loop) {
        this.loop = loop;
    }

    public void setFitFunction(String fitFunction) {
        this.fitFunction = fitFunction;
    }

    @Override
    public String toString()
    {
        return "Configuration{" + "id=" + id + ", priQueueSize=" + priQueueSize + ", initPopSize=" + initPopSize + ", sampling=" + sampling + ", radarLoop=" + radarLoop + ", loop=" + loop + ", fitFunction='" + fitFunction + '\'' + '}';
    }
}
