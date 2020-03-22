package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 用户操作的服务层
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;//用户登录与退出的数据表

    @Value("${community.path.domain}")
    private String domain;//项目的域名

    @Value("${server.servlet.context-path}")
    private String contextPath;//项目根路径名

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 注册账户,并发送激活邮件
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        //对空值处理
        if(user == null){
            throw new IllegalArgumentException("参数不为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if ( u != null){
            map.put("usernameMsg","该账户已存在！");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));//对user设置salt随机值
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));//对密码加一个salt，然后进行加密
        user.setType(0);//设置用户属性为普通账户
        user.setStatus(0);//状态为0，表示未激活，然后使用激活码进行激活
        user.setActivationCode(CommunityUtil.generateUUID());//生成激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));//此处随机生成头像，地址中的%d就是一个占位符
        user.setCreateTime(new Date());
        userMapper.insertUser(user);//存储用户

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());

        //设置激活的网址 http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);

        //发送邮件
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    /**
     * 点击激活邮件，对激活链接进行判断，选择是否激活
     * @param userId
     * @param code
     * @return
     */
    public int activation (@PathVariable int userId,@PathVariable String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVE_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVE_SUCCESS;
        }else {
            return ACTIVE_FAILURE;
        }
    }

    /**
     * 实现登录操作
     * 登录的时候，对login_ticket表进行改变
     * 当ticket的status为0时，表示已经生效，1表示无效
     * @param username
     * @param password  用户输入的明文密码
     * @param expiredSeconds    多少秒之后，此ticket过期
     * @return
     */
    public Map<String,Object> login(String username, String password, int expiredSeconds){
        Map<String,Object> map = new HashMap<>();

        //对空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账户的合法性
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }

        //验证账号的状态，是否激活
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活！");
            return map;
        }

        /*
         验证密码:
         用户输入的为明文密码，数据库中存储的为加密密码，
         所以需要对输入的密码进行加密之后，再与数据库中的密码进行比对
         */
        password = CommunityUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确，请重新输入");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);           //设置ticket的status为0，表示有效
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds * 1000));//单位毫秒
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 退出登录，将ticket的status修改为1，表示ticket无效
     * @param ticket
     */
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    /**
     * 根据ticket凭证，查询该凭证的全部信息
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    /**
     * 更新用户的头像
     * @param userId
     * @param headUrl
     * @return
     */
    public int updateHeader(int userId,String headUrl){
        int i = userMapper.updateHeader(userId, headUrl);
        return i;
    }

    /**
     * 更新密码，在更新密码的情况下，用户已经登录，所以不需要检验用户的合法性
     * 在修改页面
     * @param user
     * @param newPassword
     * @param oldPassword
     * @return
     */
    public Map<String,Object> updatePassword(User user,String oldPassword, String newPassword, String confirmPassword){
        Map<String,Object> map = new HashMap<>();

        //验证密码长度
        if (newPassword.length()<3){
            map.put("newPasswordMsg","密码长度不能小于3位!");
            return map;
        }

        //验证两次输入密码是否相同
        if (!newPassword.equals(confirmPassword)){
            map.put("confirmPasswordMsg","两次输入的密码不一致，请输入相同的密码");
            return map;
        }

        //验证原密码是否正确
        oldPassword = CommunityUtil.md5( oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)){
            map.put("oldPasswordMsg","请输入正确的原始密码");
            return map;
        }

        //对新密码进行加密并进行修改
        newPassword = CommunityUtil.md5( newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(),newPassword);

        return map;
    }
}
