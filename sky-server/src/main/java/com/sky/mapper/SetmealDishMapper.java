package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {

    /**
     * 根据菜品id查询套餐id
     * @param dishId
     * @return
     */
    List<Long> getByDishId(Long dishId);
}
