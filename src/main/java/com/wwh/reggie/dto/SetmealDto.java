package com.wwh.reggie.dto;

import com.wwh.reggie.entity.Setmeal;
import com.wwh.reggie.entity.SetmealDish;
import com.wwh.reggie.entity.Setmeal;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
