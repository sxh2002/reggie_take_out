package com.wwh.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wwh.reggie.dto.SetmealDto;
import com.wwh.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存对应菜品关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐和对应菜品关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
