package com.wwh.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwh.reggie.dto.DishDto;
import com.wwh.reggie.entity.Dish;
import org.springframework.stereotype.Service;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入口味数据
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品和对应口味
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品和对应口味数据
    public void updateWithFlavor(DishDto dishDto);
}
