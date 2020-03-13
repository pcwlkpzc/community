package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 此配置文件主要用于生成验证码，属于一个验证码生成的工具
 */
@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer(){

        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");//验证码图片的宽度
        properties.setProperty("kaptcha.image.height","40");//验证码图片高度
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");//验证码元素范围
        properties.setProperty("kaptcha.textproducer.char.length","4");//验证码的长度
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");//设置噪声类型

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
