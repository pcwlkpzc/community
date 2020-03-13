package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * 用户的登录和退出，持久层管理
 */
@Mapper
public interface LoginTicketMapper {

    /**
     * 用户登录的时候，向数据库中增添loginTicket，完成登录状态
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    //设置自动生成主键，并且指定数据库中的主键对应Javabean中的id属性
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据ticket来查询LoginTicket
     * @param ticket
     * @return
     */
    @Select({
            "select id,user_id,ticket,status,expired from login_ticket ",
            "where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    /**
     * 通过更改ticket的状态，来判断当前用户是否已经退出登录
     * @param ticket
     * @param status
     * @return
     */
    @Update({
            "update login_ticket set status=#{status} ",
            "where ticket=#{ticket}"
    })
    int updateStatus(String ticket,int status);
}
