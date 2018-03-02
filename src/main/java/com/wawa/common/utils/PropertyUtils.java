package com.wawa.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

/**
 *
 * Created by Administrator on 2018/3/2.
 */
public class PropertyUtils {
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    public static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        String filePath = System.getProperty("user.dir") + "/" + fileName;
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                boolean suc = file.createNewFile();
                if (!suc) {
                    logger.error("failed to create file:" + file.getAbsolutePath());
                }
                return props;
            }
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
            return props;
        } catch (Exception e) {
            logger.error("" + e);
        }
        return props;
    }

    public static boolean writeProperties(String fileName, Properties properties) {
        String filePath = System.getProperty("user.dir") + "/" + fileName;
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                boolean suc = file.createNewFile();
                if (!suc) {
                    logger.error("failed to create file:" + file.getAbsolutePath());
                    return false;
                }
            }
            Writer writer = new FileWriter(file);
            properties.store(writer, "init success");
            return true;
        } catch (Exception e) {
            logger.error("" + e);
        }
        return false;
    }

}
