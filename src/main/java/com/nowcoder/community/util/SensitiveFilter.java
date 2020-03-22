package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //对敏感字符的替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode =  new TrieNode();

    /**
     * 在ioc容器启动的时候，
     * 调用这个初始化方法，初始化整个前缀树
     */
    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");//读取敏感字符文件的字节流
                BufferedReader reader =new BufferedReader(new InputStreamReader(is));//使用缓冲流来处理
        ){
            String keyWord;
            while ((keyWord = reader.readLine()) != null){
                //添加到前缀树中
                this.addKeyWord(keyWord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //将一个敏感词添加到前缀数中
    private void addKeyWord(String keyWord) {
        TrieNode temp = rootNode;
        for (int i = 0 ; i < keyWord.length() ; i++){
            char key = keyWord.charAt(i);
            TrieNode subNode = temp.getSubNode(key);
            if (subNode == null){
                subNode = new TrieNode();
                temp.addSubNode(key,subNode);
            }
            //指向下一个子节点
            temp = subNode;

            //作为结束标识符
            if (i == keyWord.length()-1){
                temp.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词汇
     * @param text
     * @return
     */
    public String filter (String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < text.length() ; i++){
            char c = text.charAt(position);
            //当前字符是特殊符号
            if (isSymbol(c)){
                if (tempNode == rootNode){//指针1为头结点
                    sb.append(c);
                    //将指针2向后移1位
                    begin++;
                }
                //将指针3向后移1位
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);

            if (tempNode == null){
                //表示当前begin到position之间的字符串不是敏感词
                sb.append(text.charAt(begin));
                //将begin和position进行归位
                position = ++begin;
                //将指针1也归位
                tempNode = rootNode;
            }else if (tempNode.isKeyWordEnd()){
                //表示当前begin到position为敏感词汇，则进行和谐处理
                sb.append(REPLACEMENT);
                //对begin和position进行重新归位
                begin = ++position;
                tempNode = rootNode;
            }else {
                //当前并不是敏感词，并且还没有到前缀树的末端，检查下一个字符
                position++;
            }
        }
        //处理最后端还没有处理字符串
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断是否为特殊符号
     * 如果是特殊符号，则返回true
     * 如果不是，则返回false
     * @param c
     * @return
     */
    private boolean isSymbol(Character c){
        /*
        isAsciiAlphanumeric表示当前字符是否是合法字符，如果合法，则为true
        0x2E80~0x9FFF是东亚字符
         */
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 前缀树的节点结构
     */
    private class TrieNode{

        //关键词结束标识
        private boolean isKeyWordEnd = false;

        /**
         * 子节点
         * key为子节点的字符
         * value为子节点
         */
        Map<Character,TrieNode> subNode = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        /**
         * 添加子节点
         * @param key
         * @param value
         */
        public void addSubNode(Character key,TrieNode value){
            subNode.put(key,value);
        }

        /**
         * 获取子节点
         * @param key
         * @return
         */
        public TrieNode getSubNode(Character key){
            return subNode.get(key);
        }
    }
}
