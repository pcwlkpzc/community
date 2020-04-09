package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    //创建日志对象
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 处理正常的注册请求
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/register" , method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);

        if(map == null || map.isEmpty()){//此时表明注册成功
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 对激活邮件中的激活链接进行处理
     * @param model
     * @param userId
     * @param code
     * @return
     */
    //激活链接的模板样式： http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model,
                             @PathVariable(name = "userId") int userId,
                             @PathVariable(name = "code") String code){
        int result = userService.activation(userId,code);
        if (result == ACTIVE_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","/login");      //激活成功之后，跳转到登录页面
        }else if (result == ACTIVE_REPEAT){
            model.addAttribute("msg","无效操作，此账户已激活，请勿重复激活！");
            model.addAttribute("target","/index");      //激活成功之后，跳转到登录页面
        }else{
            model.addAttribute("msg","激活失败，您提供的激活码不正确");
            model.addAttribute("target","/index");      //激活成功之后，跳转到登录页面
        }
        return "/site/operate-result";
    }

    /**
     * 生成验证码
     * 原来使用session进行存储
     *
     * 第一次重构，使用redis进行缓存
     */
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response){
        //生成验证码字符串
        String text = kaptchaProducer.createText();
        //将生成的验证码字符串传入到kaptcha中，生成验证码图片
        BufferedImage image = kaptchaProducer.createImage(text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);//设置cookie的生效时间60秒
        cookie.setPath(contextPath);//在整个项目路径下都是生效的
        response.addCookie(cookie);//将cookie发送给客户端

        //将验证码存储到redis中
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);//设置失效时间为60秒

        //将图片存储到response中，然后携带到客户端中
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();//从response中获取输出流
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("验证码传输有误:"+e.getMessage());
        }
    }

    /**
     * 处理正常的登录请求
     *
     * 第一次重构，从redis中取出验证码
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

        String kaptcha = null;

        //从redis中取出验证码
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        //检查验证码是否正确
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码错误");
            return "/site/login";
        }

        //检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);

        if (map.containsKey("ticket")){//map中有ticket，代表着登录成功
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);//设置生效的路径
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {//登录失败
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
