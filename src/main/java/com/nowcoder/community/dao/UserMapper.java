package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * mapper等价于持久层中的dao，只不过在mybatis中，将dao换了一个称呼，叫做mapper
 * 在spring框架中，对持久层的注解为@Repository
 * @Repository 注解等价于mybatis中的@Mapper
 * 在此处也可以使用org.springframework.stereotype.Repository来替换org.apache.ibatis.annotations.Mapper
 */

@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id ,int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);
}
