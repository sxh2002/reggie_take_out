package com.wwh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwh.reggie.common.CustomException;
import com.wwh.reggie.entity.Category;
import com.wwh.reggie.entity.Dish;
import com.wwh.reggie.entity.Setmeal;
import com.wwh.reggie.mapper.CategoryMapper;
import com.wwh.reggie.service.CategoryService;
import com.wwh.reggie.service.DishService;
import com.wwh.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 根据id删除分类，删除前要判断分类是否关联了菜品
     * @param id
     */
    @Override
    public void remove(Long id) {

        //查询是否关联菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        if (count1>0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }


        //查询是否关联套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        if(count2>0){
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }


        //正常删除
        removeById(id);
    }
}
