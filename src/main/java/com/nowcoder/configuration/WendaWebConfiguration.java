package com.nowcoder.configuration;

import com.nowcoder.interceptor.LoginRequiredInterceptor;
import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author 胡启航
 * @date 2019/9/19 - 10:46
 */
@Component
public class WendaWebConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    // 注册拦截器(拦截器真正的加到controller上)
    public void addInterceptors(InterceptorRegistry registry) {
        // loginRequiredInterceptor在passportInterceptor后加入
        // 先使用passportInterceptor在cookie中寻找ticket
        // 再在loginRequiredInterceptor在passportInterceptor判断用户是否需要跳转到登录
        registry.addInterceptor(passportInterceptor);
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("user/*");
        super.addInterceptors(registry);
    }
}
