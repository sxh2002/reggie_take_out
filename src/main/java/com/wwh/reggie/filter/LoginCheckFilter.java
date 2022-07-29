package com.wwh.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.wwh.reggie.common.BaseContext;
import com.wwh.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器
    public static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        //1.获取请求uri
        String uri = request.getRequestURI();

        log.info("拦截到请求：{}",uri);

        String[] urls = new String[]{
            "/employee/login", "/employee/logout","/backend/**","/front/**","/user/login","/user/sendMsg"
        };

        //2.判断本次请求是否要处理

        boolean check = check(urls,uri);

        //3.如果不需要处理则放行

        if (check){
            log.info("本次请求{}不需要处理",uri);
            filterChain.doFilter(request,response);
            return;
        }

        //4.判断登录状态，如果已经登录，则放行
        if (request.getSession().getAttribute("employee") != null){
            log.info("employee用户{}已登录",request.getSession().getAttribute("employee"));

            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        if (request.getSession().getAttribute("user") != null){
            log.info("user用户{}已登录",request.getSession().getAttribute("user"));

            Long userId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }


        //5.如果未登录，返回未登录结果

        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 检验本次请求是否要放行
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = pathMatcher.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
