package com.nineya.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author ylq
 * @since 2023-05-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("test_result")
public class TestResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("task_id")
    private Integer taskId;

    @TableField("model_id")
    private Integer modelId;

    @TableField("model_name")
    private String modelName;

    @TableField("mse_id")
    private Integer mseId;

    @TableField("div_count")
    private Integer divCount;

    @TableField("error_count")
    private String errorCount;

    @TableField("error_count_per_loop")
    private String errorCountPerLoop;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Integer taskId)
    {
        this.taskId = taskId;
    }

    public Integer getModelId()
    {
        return modelId;
    }

    public void setModelId(Integer modelId)
    {
        this.modelId = modelId;
    }

    public Integer getMseId()
    {
        return mseId;
    }

    public void setMseId(Integer mseId)
    {
        this.mseId = mseId;
    }

    public Integer getDivCount()
    {
        return divCount;
    }

    public void setDivCount(Integer divCount)
    {
        this.divCount = divCount;
    }

    public String getErrorCount()
    {
        return errorCount;
    }

    public void setErrorCount(String errorCount)
    {
        this.errorCount = errorCount;
    }

    public String getErrorCountPerLoop()
    {
        return errorCountPerLoop;
    }

    public void setErrorCountPerLoop(String errorCountPerLoop)
    {
        this.errorCountPerLoop = errorCountPerLoop;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    @Override
    public String toString()
    {
        return "TestResult{" + "id=" + id + ", taskId=" + taskId + ", modelId=" + modelId + ", modelName='" + modelName + '\'' + ", mseId=" + mseId + ", divCount=" + divCount + ", errorCount='" + errorCount + '\'' + ", errorCountPerLoop='" + errorCountPerLoop + '\'' + '}';
    }
}
