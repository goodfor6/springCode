package org.springframework.core.util;

import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;

/**
 * Created by Administrator on 2017/8/15 0015.
 */
public abstract  class ResourceUtils {
    public static final String CLASSPATH_URL_PREFIX = "classpath:";
    public static final String FILE_URL_PREFIX = "file:";
    public static final String JAR_URL_PREFIX = "jar:";
    public static final String URL_PROTOCAL_FILE = "file";
    public static final String URL_PROTOCAL_JAR = "jar";
    public static final String URL_PROTOCAL_ZIP ="zip";
    public static final String URL_PROTOCAL_WSJAR = "wsjar";
    public static final String URL_PROTOCAL_VFSZIP = "vfsfile";
    public static final String URL_PROTOCAL_VFS = "vfs";
    public static final String JAR_FILE_EXTENSTION = ".jar";
    public static final String JAR_URL_SEPARATOR = "!/";

    public static boolean isUrl(String resourceLocation){
        if(resourceLocation == null){
            return false;
        }
        else if(resourceLocation.startsWith("classpath:")){
            return true;
        }
        else{
            try {
                new URL(resourceLocation);
                return true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }

        }
    }

    public static URL getURL(String resourceLocation)throws FileNotFoundException {
        Assert.notNull(resourceLocation,"Resource location must not be null");
        if(resourceLocation.startsWith("classpath")){
            String path = resourceLocation.substring("classpath:".length());
            ClassLoader c1 = ClassUtils.getDefaultClassLoader();
            URL url = c1!=null? c1.getResource(path): ClassLoader.getSystemResource(path);
            if(url == null){
                String descriptor = "class path resource ["+path+"]";
                throw new FileNotFoundException(descriptor+" cannot be resolver to URL because it does not exist");
            }
            else{
                return url;
            }
        }
        else{
            try {
                return new URL(resourceLocation);
            } catch (MalformedURLException e) {
                try {
                    return (new File(resourceLocation)).toURL();
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                    throw new FileNotFoundException("Resource location ["+resourceLocation+"] is neither a URL not a well-formed file path"  );
                }
            }
        }
    }

    public static File getFile(String resourceLocation)throws FileNotFoundException{
        Assert.notNull(resourceLocation,"Resource location must not be null");
        if(resourceLocation.startsWith("classpath:")){
            String path = resourceLocation.substring("classpath:".length());
            String desciption = "class path resource ["+path+ "]".length();
            ClassLoader c1 = ClassUtils.getDefaultClassLoader();
            URL url = c1 != null? c1.getResource(path):ClassLoader.getSystemResource(path);
            if(url == null){
                throw new FileNotFoundException(desciption +"cannot be resolver to absolute file path because it does not exist");
            }
            else{
                return getFile(url,desciption);
            }
        }
        else{
            try {
                return getFile(new URL(resourceLocation));
            } catch (MalformedURLException e) {
                return new File(resourceLocation);
            }
        }
    }

    public static File getFile(URL reourceUrl)throws FileNotFoundException{
        return getFile(reourceUrl,"URL");
    }

    public static File getFile(URL resourceUrl,String description)throws FileNotFoundException{
        Assert.notNull(resourceUrl,"Resource URL must not be null");
        if(!"file".equals(resourceUrl.getProtocol())){
            throw new FileNotFoundException(description +"cannot be resolver ot sbsolute file path "+"becaust it does not reside in file system:"+resourceUrl);
        }
        else{
            try {
                return new File(toURI(resourceUrl).getSchemeSpecificPart());
            } catch (URISyntaxException e) {
                return new File(resourceUrl.getFile());
            }
        }
    }

    public static File getFile(URI resourceUri,String description)throws FileNotFoundException{
        Assert.notNull(resourceUri,"Resource URI must not be null");
        if(!"file".equals(resourceUri.getScheme())){
            throw new FileNotFoundException(description+"cannot be resolved to sbsolute file path"+"because it does not reside in the file system:"+resourceUri);
        }
        else{
            return new File(resourceUri.getSchemeSpecificPart());
        }
    }

    public static boolean isFileURL(URL url){
        String protocol = url.getProtocol();
        return "file".equals(protocol) || "vfsfile".equals(protocol) || "vfs".equals(protocol);
    }

    public static boolean ifJarURL (URL url){
        String protocol =url.getProtocol();
        return "jar".equals(protocol) || "zip".equals(protocol) || "vfszip".equals(protocol) || "wsjar".equals(protocol);
    }

    public static boolean isJarFileURL(URL url){
        return "file".equals(url.getProtocol()) && url.getPath().toLowerCase().endsWith(".jar");
    }

    public static URL extractJarFileURL(URL jarUrl)throws MalformedURLException{
        String urlFile = jarUrl.getFile();
        int separatorIndex = urlFile.indexOf("!/");
        if(separatorIndex != -1){
            String jarFile  = urlFile.substring(0,separatorIndex);
            try{
                return new URL(jarFile);
            }
            catch(MalformedURLException var5){
                if(!jarFile.startsWith("/")){
                    jarFile = "/" + jarFile;
                }
                return new URL("file:"+jarFile);
            }
        }
        else{
                return jarUrl;
        }
    }

    public static URI toURI(URL url)throws URISyntaxException{
        return toURI(url.toString());
    }

    public static URI toURI(String location)throws URISyntaxException{
        return new URI(StringUtils.replace(location,"","%20"));
    }

    public static void useCachesIfNecessary(URLConnection con){
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

}
