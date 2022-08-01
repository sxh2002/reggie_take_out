package com.wwh.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wwh.reggie.common.R;
import com.wwh.reggie.entity.User;
import com.wwh.reggie.service.UserService;
import com.wwh.reggie.utils.SMS;
import com.wwh.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    private R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        //生成验证码
        if(StringUtils.isNotEmpty(phone)){
            Integer code = ValidateCodeUtils.generateValidateCode(4);

            log.info("验证码：=================="+Integer.toString(code));
            //调用阿里云短信服务
//            try {
//                SMS.sendMsg(phone,Integer.toString(code));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            //将验证码保存到session
//            session.setAttribute(phone,code);

            redisTemplate.opsForValue().set(phone,code.toString() ,5, TimeUnit.MINUTES);
            return R.success("短信发送成功");
        }

        return R.error("短信发送失败");

        //将验证码保存到session
    }


    /**
     * 移动端登录
     * @param phoneAndCode
     * @param session
     * @return
     */
    @PostMapping("/login")
    private R<User> login(@RequestBody Map<String,String> phoneAndCode, HttpSession session){
        //获取手机号
        String phone = phoneAndCode.get("phone");
        String code = phoneAndCode.get("code").toString();

        if (phone==null || code==null){
            return R.error("登录失败");
        }

        //从session中获取验证码
//        String codeInSession =  session.getAttribute(phone).toString();


        String codeInSession = redisTemplate.opsForValue().get(phone).toString();

        if(codeInSession==null){
            return R.error("未获取验证码");
        }

        if(codeInSession!=null && codeInSession.equals(code)){
            //成功登录
            //判断是否为新用户，新用户自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                //自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            session.setAttribute("user",user.getId());

            //登录成功删除redis中的验证码
            redisTemplate.delete(phone);

            return R.success(user);
        }

        return R.error("登录失败");

    }


}
