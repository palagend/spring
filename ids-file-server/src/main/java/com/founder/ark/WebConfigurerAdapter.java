package com.founder.ark;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

@Configuration
public class WebConfigurerAdapter extends WebMvcConfigurerAdapter {

    @Value("${server.file-mapping-path}")
    private String mappingPath;

    @Value("${server.file-save-path}")
    private String savePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        mappingPath = !mappingPath.startsWith("/") ? "/" + mappingPath : mappingPath;
        mappingPath = !mappingPath.endsWith("/") ? mappingPath + "/" : mappingPath;
        savePath = !savePath.endsWith(File.separator) ? savePath + File.separator : savePath;

        registry.addResourceHandler(mappingPath + "**").addResourceLocations("file:" + savePath);
        super.addResourceHandlers(registry);
    }

}
