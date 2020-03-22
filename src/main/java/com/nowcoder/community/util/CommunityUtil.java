package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 自定义一个工具类
 */
public class CommunityUtil {

    /**
     * 生成随机字符串
     * @return
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * 使用md5进行加密
     * @param key
     * @return
     */
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 将服务器端的数据以json数据的格式返回给客户端
     * @param code
     * @param msg
     * @param map
     * @return
     */
    public static String getJSONString (int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null){
            for (String key : map.keySet()) {
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }
    public static String getJSONString (int code, String msg){
        return getJSONString(code, msg,null);
    }
    public static String getJSONString (int code){
        return getJSONString(code, null,null);
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",24);
        System.out.println(getJSONString(200));
    }


}
