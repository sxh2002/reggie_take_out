package com.wwh.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.reggie.common.BaseContext;
import com.wwh.reggie.common.R;
import com.wwh.reggie.entity.ShoppingCart;
import com.wwh.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        Long userId = BaseContext.getCurrentId();

        shoppingCart.setUserId(userId);

        //查询当前菜品或套餐是否在购物车中，在的话只要加一
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        if(dishId != null){
            //添加的时菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);

        if(one != null){
            Integer number = one.getNumber();
            one.setNumber(number+1);
            one.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(one);
            return R.success(one);
        }else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            return R.success(shoppingCart);
        }
    }

    /**
     * 减少购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        Long userId = BaseContext.getCurrentId();

        shoppingCart.setUserId(userId);

        //查询当前菜品或套餐是否在购物车中，在的话只要加一
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId,userId);

        if(dishId != null){
            //添加的时菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //添加的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart one = shoppingCartService.getOne(queryWrapper);

        if(one != null){
            Integer number = one.getNumber();
            one.setNumber(number-1);
            if(number-1 != 0){
                one.setNumber(number-1);
                one.setCreateTime(LocalDateTime.now());
                shoppingCartService.updateById(one);
                return R.success(one);
            }else {
                one.setCreateTime(LocalDateTime.now());
                shoppingCartService.removeById(one.getId());
                return R.success(one);
            }

        }else {
            return R.error("未知错误");
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}
