package com.nowcoder.controller;

import com.nowcoder.async.EventProducer;
import com.nowcoder.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author 胡启航
 * @date 2019/9/18 - 21:15
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    // 登录
    @RequestMapping(path = {"/login"}, method = {RequestMethod.POST})
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "next", required = false) String next,
                        @RequestParam(value = "remember", defaultValue = "false") boolean remember,
                        // 用于在cookie中设置t票
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(username, password);
            // 验证登录后，判断是否有t票
            // 无t票，则为未成功验证t票(验证登录成功，自动发放t票)
            if (map.containsKey("ticket")) {
                // 将t票，放入浏览器cookie
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                // 根据是否点击remember按钮，设置cookie保存时间(5天)
                if (remember) {
                    cookie.setMaxAge(3600 * 24 * 5);
                }
                response.addCookie(cookie);

                // 假设判断异常登录
                // 触发异常登录事件，异步发送站内信
//                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
//                        .setActorId((int) map.get("userId"))
//                        .setExt("username", username)
//                        .setExt("email", "qihanghu@foxmail.com"));

                // 判断是否是从其他页面，跳转到当前登录页面
                // 若是则，登录后自动跳转回原页面
                // 否则，登录后自动跳转回首页
                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            } else {
                // 无t票未成功验证t票，记录错误信息
                // 返回登录页面
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        } catch (Exception e) {
            logger.error("登录异常" + e.getMessage());
            return "login";
        }
    }

    // 登出
    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        // 登出将t票过期，返回首页
        userService.logout(ticket);
        return "redirect:/";
    }

    // 注册
    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})
    public String reg(Model model,
                      @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("next") String next,
                      @RequestParam(value = "remember", defaultValue = "false") boolean remember,
                      HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.register(username, password);
            // 注册完后，自动登录(一定有t票(和登录不同))
            // 将t票，放入浏览器cookie
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            // 根据是否点击remember按钮，设置cookie保存时间(5天)
            if (remember) {
                cookie.setMaxAge(3600 * 24 * 5);
            }
            response.addCookie(cookie);

            // 判断是否是从其他页面，跳转到当前登录页面
            // 若是则，登录后自动跳转回原页面
            // 否则，登录后自动跳转回首页
            if (StringUtils.isNotBlank(next)) {
                return "redirect:" + next;
            }
            return "redirect:/";
        } catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            model.addAttribute("msg", "服务异常");
            return "login";
        }
    }

    // 登录注册页面
    // 可能是从别的页面跳转到当前登录注册页面，网址中会有个next参数，记录是从哪个页面跳转过来的，用于登录完后自动跳转回
    // 原页面(应该判断是否为站内的网址，再进行判断)
    // 将next的值记录在model中，用于后续跳转操作
    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET})
    public String regloginPage(Model model,
                           // requestparam中要设其他参数时，要如下操作(required，变量是否必须)
                           @RequestParam(value = "next", required = false) String next) {
        // 将next字段，埋入html的form中，在提交登录时，可以将next字段传递过来
        // 在登陆成功后，判断next字段是否为空，来判断是跳转到首页，还是next
        model.addAttribute("next", next);
        return "login";
    }
}
