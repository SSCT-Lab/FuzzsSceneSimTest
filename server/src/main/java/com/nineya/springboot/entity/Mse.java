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
@TableName("mse")
public class Mse implements Serializable {

    public Mse(Double oriMse, Double vcMse, Double tmMse, Double siMse, Double rnMse, Double fgMse)
    {
        this.oriMse = oriMse;
        this.vcMse = vcMse;
        this.tmMse = tmMse;
        this.siMse = siMse;
        this.rnMse = rnMse;
        this.fgMse = fgMse;
    }

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("ori_mse")
    private Double oriMse;

    @TableField("vc_mse")
    private Double vcMse;

    @TableField("tm_mse")
    private Double tmMse;

    @TableField("si_mse")
    private Double siMse;

    @TableField("rn_mse")
    private Double rnMse;

    @TableField("fg_mse")
    private Double fgMse;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Double getOriMse()
    {
        return oriMse;
    }

    public void setOriMse(Double oriMse)
    {
        this.oriMse = oriMse;
    }

    public Double getVcMse()
    {
        return vcMse;
    }

    public void setVcMse(Double vcMse)
    {
        this.vcMse = vcMse;
    }

    public Double getTmMse()
    {
        return tmMse;
    }

    public void setTmMse(Double tmMse)
    {
        this.tmMse = tmMse;
    }

    public Double getSiMse()
    {
        return siMse;
    }

    public void setSiMse(Double siMse)
    {
        this.siMse = siMse;
    }

    public Double getRnMse()
    {
        return rnMse;
    }

    public void setRnMse(Double rnMse)
    {
        this.rnMse = rnMse;
    }

    public Double getFgMse()
    {
        return fgMse;
    }

    public void setFgMse(Double fgMse)
    {
        this.fgMse = fgMse;
    }

    @Override
    public String toString()
    {
        return "Mse{" + "id=" + id + ", oriMse=" + oriMse + ", vcMse=" + vcMse + ", tmMse=" + tmMse + ", siMse=" + siMse + ", rnMse=" + rnMse + ", fgMse=" + fgMse + '}';
    }
}
