package org.springframework.core.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class Log4jConfigurer {
    public static final String CLASSPATH_URL_RREFIX = "classpath";
    public static final String XML_FILE_EXTENSION = ".xml";

    public static void initLogging(String location)throws FileNotFoundException {

        String resolverLocation = SystemPropertyUtils.resolvePlaceholders(location);
        URL url = ResourceUtils.getURL(resolverLocation);
        if("file".equals(url.getProtocol()) && !ResourceUtils.getFile(url).exists()){
            throw new FileNotFoundException("Log4j config file ["+resolverLocation+"] not found");
        }
        else{
           if (resolverLocation.toLowerCase().endsWith(".xml")){
               DOMConfigurator.configure(url);
           }
           else{
               PropertyConfigurator.configure(url);
           }
        }

    }

    public static void initLogging(String location,long refreshInterval)throws FileNotFoundException{
        String resolvedLocation = SystemPropertyUtils.resolvePlaceholders(location);
        File file = ResourceUtils.getFile(resolvedLocation);
        if(!file.exists()){
            throw new FileNotFoundException("Log4j config file ["+resolvedLocation+"] not found");
        }
        else{
            if(resolvedLocation.toLowerCase().endsWith(".xml")){
                DOMConfigurator.configureAndWatch(file.getAbsolutePath(),refreshInterval);
            }
            else{
                PropertyConfigurator.configureAndWatch(file.getAbsolutePath(),refreshInterval);
            }
        }
    }

    public static void shutdownLoggings(){LogManager.shutdown();}

    public static void setWorkingDirSystemProperty(String key){
        System.setProperty(key,(new File("")).getAbsolutePath());
    }
}
