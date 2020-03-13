package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主要的实现的功能是检查登录状态！
 *
 * 此注解作用于方法上，
 * 用来使得被标记的方法在调用时，
 * 需要用户登录才能调用。
 */
@Target(ElementType.METHOD)//表示此注解只能标记在方法上
@Retention(RetentionPolicy.RUNTIME)//表示运行时有效
public @interface LoginRequired {
}
