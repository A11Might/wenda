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
        // 再在loginRequiredInterceptor判断用户是否需要记录当前url并跳转到登录
        // addPathPatterns()：拦截要求
        // addPathPatterns("user/*")，只有用户界面未登录时，才记录url(其实站内的url都应该记录，演示方便)
        registry.addInterceptor(passportInterceptor);
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("user/*");
        super.addInterceptors(registry);
    }
}
