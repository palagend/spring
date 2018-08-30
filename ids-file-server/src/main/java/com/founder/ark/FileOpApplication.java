package com.founder.ark;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * 启动类
 *
 * @author Jiang haicheng
 */
@SpringCloudApplication
public class FileOpApplication {
    /**
     * 启动
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(FileOpApplication.class, args);
    }
}
