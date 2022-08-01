package com.wwh.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wwh.reggie.common.R;
import com.wwh.reggie.dto.DishDto;
import com.wwh.reggie.entity.Category;
import com.wwh.reggie.entity.Dish;
import com.wwh.reggie.entity.DishFlavor;
import com.wwh.reggie.service.CategoryService;
import com.wwh.reggie.service.DishFlavorService;
import com.wwh.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("菜品：{}",dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dishDtoList = records.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dishDtoList);

        return R.success(dtoPage);

    }


    /**
     * 根据id查询菜品和对应口味
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("菜品：{}",dishDto);
        dishService.updateWithFlavor(dishDto);

        String key = "dish_" + dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList = null;

        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();

        //先从redis获取
        dishDtoList = (List<DishDto>)redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        //redis中不存在，查mysql

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = dishList.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

}
