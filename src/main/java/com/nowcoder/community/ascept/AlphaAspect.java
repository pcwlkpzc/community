package com.nowcoder.community.ascept;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    /**
     * 在切入点之气执行
     */
    @Before("pointcut()")
    public void before(){
        System.out.println("before......");
    }

    /**
     * 在切入点之后执行
     */
    @After("pointcut()")
    public void after(){
        System.out.println("after......");
    }

    /**
     * 在返回结果前执行
     */
    @AfterReturning("pointcut()")
    public void afterReturn(){
        System.out.println("afterReturn......");
    }

    /**
     * 在抛出异常前执行
     */
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing......");
    }

    /**
     * 在切入点前后都执行
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("around before......");
        Object obj = joinPoint.proceed();
        System.out.println("around after......");
        return obj;
    }

}
