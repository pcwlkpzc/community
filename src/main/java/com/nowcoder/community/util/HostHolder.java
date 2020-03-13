package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于替代session对象
 */
@Component
public class HostHolder {

    /**
     * ThreadLocal相当于一个map容器，
     * 存储的key值是当前线程，value是我们需要存储的值，
     * 所以ThreadLocal可以隔离线程，线程之间互不影响。
     */
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
