package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增菜品和对应的口味数据
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);

        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size()>0){
            flavors.forEach(d -> d.setDishId(dishId));
            //向口味表中插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    public void deleteByIds(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            //菜品是否在起售中
            if(dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            //菜品是否被套餐关联了
            List<Long> setmealDish = setmealDishMapper.getByDishId(id);
            if (setmealDish != null && setmealDish.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
            //删除符合条件菜品和对应的口味
            /*dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);*/
        }
        //删除符合条件菜品和对应的口味
        dishMapper.deleteBatchIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //获取菜品表信息
        Dish dish = dishMapper.selectById(id);

        //获取菜品对应的口味表信息
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 修改菜品和对应的口味数据
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());

        //修改菜品基本信息
        UpdateWrapper wrapper = new UpdateWrapper();
        wrapper.eq("id", dish.getId());
        dishMapper.update(dish, wrapper);

        //修改口味信息
        dishFlavorMapper.deleteByDishId(dish.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(d -> d.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> getByCategoryId(Long categoryId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("category_id", categoryId);
        wrapper.eq("status", StatusConstant.ENABLE);

        return dishMapper.selectList(wrapper);
    }

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        // 起售菜品时，如果菜品对应的分类被禁用，那么不能起售
        if (status == StatusConstant.ENABLE) {
            Category category = categoryMapper.selectById(dishMapper.selectById(id).getCategoryId());
            if (category.getStatus() == StatusConstant.DISABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ENABLE_FAILED);
            }
        }
        Dish dish = Dish.builder().id(id).status(status).updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId()).build();
        dishMapper.updateById(dish);

        if(status == StatusConstant.DISABLE) {
            // 如果是停售操作，那么包含当前菜品的套餐也要停售
            List<Long> setmealIds = setmealDishMapper.getByDishId(id);
            if (setmealIds != null && setmealIds.size()>0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder().id(setmealId).status(status).updateTime(LocalDateTime.now())
                            .updateUser(BaseContext.getCurrentId()).build();
                    setmealMapper.updateById(setmeal);

                }
            }
        }
    }
}
