package com.nineya.springboot.mapper;

import com.nineya.springboot.entity.Operator;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ylq
 * @since 2023-04-06
 */

/*
不加会报找不到Bean
 */
@Repository
public interface OperatorMapper extends BaseMapper<Operator> {

}
