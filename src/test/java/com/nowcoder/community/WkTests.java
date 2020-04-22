package com.nowcoder.community;

import java.io.IOException;

public class WkTests {
    public static void main(String[] args) {
        String cmd = "d:/software/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/software/Idea2019/IdeaProjects/nowcoder/data/wk-images/4.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
