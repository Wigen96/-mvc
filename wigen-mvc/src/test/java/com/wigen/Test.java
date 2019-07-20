package com.wigen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @Author wwq
 */
public class Test {
    private Properties properties = new Properties();

    @org.junit.Test
    public void test() throws FileNotFoundException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("classpath:test.properties");
        InputStream in = classLoader.getResourceAsStream("test.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    @org.junit.Test
    public void aa() {
        String value = "com.wigen.web";
        System.out.println(value.replace(".", "/"));
    }

}
