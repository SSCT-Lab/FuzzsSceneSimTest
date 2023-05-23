package com.nineya.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author ylq
 * @since 2023-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("test_task")
public class TestTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("config_id")
    private Integer configId;

    @TableField("name")
    private String name;

    @TableField("model_cnt")
    private Integer modelCnt;

    @TableField("seed_cnt")
    private Integer seedCnt;

    @TableField("operator_cnt")
    private Integer operatorCnt;

    @TableField("creator")
    private String creator;

    @TableField("create_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createDate;

    @TableField("task_status")
    private Integer taskStatus;

    @TableField("task_progress")
    private Double taskProgress;

    @TableField("`desc`")
    private String desc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getModelCnt() {
        return modelCnt;
    }

    public void setModelCnt(Integer modelCnt) {
        this.modelCnt = modelCnt;
    }

    public Integer getSeedCnt() {
        return seedCnt;
    }

    public void setSeedCnt(Integer seedCnt) {
        this.seedCnt = seedCnt;
    }

    public Integer getOperatorCnt() {
        return operatorCnt;
    }

    public void setOperatorCnt(Integer operatorCnt) {
        this.operatorCnt = operatorCnt;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Double getTaskProgress()
    {
        return taskProgress;
    }

    public void setTaskProgress(Double taskProgress)
    {
        this.taskProgress = taskProgress;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "TestTask{" +
                "id=" + id +
                ", configId=" + configId +
                ", name='" + name + '\'' +
                ", modelCnt=" + modelCnt +
                ", seedCnt=" + seedCnt +
                ", operatorCnt=" + operatorCnt +
                ", creator='" + creator + '\'' +
                ", createDate=" + createDate +
                ", taskStatus=" + taskStatus +
                ", taskProgress=" + taskProgress +
                ", desc='" + desc + '\'' +
                '}';
    }
}
