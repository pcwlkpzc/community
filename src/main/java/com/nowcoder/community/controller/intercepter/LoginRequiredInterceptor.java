package com.nowcoder.community.controller.intercepter;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 此拦截器主要用于拦截,
 * 标注有@LoginRequired注解的方法
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    /**
     * 对所有请求进行拦截，判断请求是否添加有@LoginRequired注解的方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod){//首先判断拦截到的请求是否是方法
            HandlerMethod handlerMethod = (HandlerMethod) handler;//如果是方法的话，就对其进行转型，转为方法类型
            LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);//尝试从方法上取出LoginRequired注解
            if (loginRequired != null && hostHolder.getUser() == null){//如果方法上有LoginRequired注解,而且用户没有登录
                response.sendRedirect(request.getContextPath()+"/login");//将页面进行重定向，直接发送到登录页面
                return false;//拦截此请求
            }
        }
        return true;
    }
}
