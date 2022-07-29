package com.wwh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwh.reggie.common.CustomException;
import com.wwh.reggie.dto.SetmealDto;
import com.wwh.reggie.entity.Setmeal;
import com.wwh.reggie.entity.SetmealDish;
import com.wwh.reggie.mapper.SetmealMapper;
import com.wwh.reggie.service.SetmealDishService;
import com.wwh.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时保存对应菜品关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        //保存套餐基本信息
        this.save(setmealDto);

        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        List<Object> dishList = dishes.stream().map(dish -> {
            dish.setSetmealId(setmealDto.getId());
            return dish;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(dishes);
    }

    /**
     * 删除套餐和对应菜品关联数据
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //停售的套餐才能删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);

        if(count>0){
            throw new CustomException("有套餐正在售卖中，不能删除");
        }

        //先删除套餐，在删除对应菜品关系
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(lambdaQueryWrapper);



    }
}
