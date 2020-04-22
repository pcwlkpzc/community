package com.nowcoder.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 此配置类主要用于在项目启动之前，生成WK的目录
 * 由于配置类是在整个项目启动前首先开始加载的，所以我们为了使用wk工具，
 * 应该首先确保存放长图的图片路径文件夹是存在的
 */
@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String WkImageStore;

    @PostConstruct
    public void init(){
        //创建Wk图片目录
        File file = new File(WkImageStore);
        if (!file.exists()){
            file.mkdir();
            logger.info("创建WK图片目录：" + WkImageStore);
        }
    }
}
