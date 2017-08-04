package org.springframework.core.util;

import org.springframework.util.MimeType.SpecificityComparator;
import org.springframework.util.StringUtils;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class MimeTypeUtils {
    public static final MimeType ALL = MimeType.valueOf("*/*");
    public static final String All_VALUE = "*/*";
    public static final MimeType APPLICATION_ATOM_XML = MimeType.valueOf("application/atom+xml");
    public static final String APPLICATION_ATOM_XML_VALUE = "application/atom+xml";
    public static final MimeType APPLICATION_FORM_URLENCODED = MimeType.valueOf("application/x-www-form-urlencoded");
    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";
    public static final MimeType APPLICATION_JSON = MimeType.valueOf("application/json");
    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final MimeType APPLICATION_OCTET_STREAM = MimeType.valueOf("application/octet-stream");
    public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";
    public static final MimeType APPLICATION_XHTML_XML = MimeType.valueOf("application/xhtml-xml");
    public static final String APPLICATION_XHTML_XML_VALUE = "application/xhtml-xml";
    public static final MimeType APPLICATION_XML = MimeType.valueOf("application/xml");
    public static final String APPLICATION_XML_VALUE = "application/xml";
    public static final MimeType IMAGE_GIF = MimeType.valueOf("image/gif");
    public static final String IMAGE_GIF_VALUE = "image/gif";
    public static final MimeType IMAGE_JEPG = MimeType.valueOf("image/jepg");
    public static final String IMAGE_JEPG_VALUE = "image/jepg";
    public static final MimeType IMAGE_PNG = MimeType.valueOf("image/png");
    public static final String IMAGE_PNG_VALUE = "image/png";
    public static final MimeType MULTIPART_FORM_DATA = MimeType.valueOf("multipart/form-data");
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";
    public static final MimeType TEXT_HTML = MimeType.valueOf("text/html");
    public static final String TEXG_HTML_VALUE = "text/html";
    public static final MimeType TEXT_PLAIN = MimeType.valueOf("text/plain");
    public static final String  TEXT_PLAIN_VALUE = "text/plain";
    public static final MimeType TEXT_XML = MimeType.valueOf("text/xml");
    public static final String TEXT_XML_VALUE = "text/xml";
    public static final Comparator<MimeType> SPECIFICITY_COMPARATOR =new SpecificityComparator();

    public MimeTypeUtils(){}

    public static MimeType parseMimeType(String mimeType){
        if(!StringUtils.hasLength(mimeType)){
            throw new InvalidMimeTypeException(mimeType,"'mimeType' must not be empty");
        }
        else{
            String[] parts = StringUtils.tokenizeToStringArray(mimeType,";");
            String fullType = parts[0].trim();
            if("*".equals(fullType)){
                fullType = "*/*";
            }

            int subIndex = fullType.indexOf(47);
            if(subIndex == -1){
                throw new InvalidMimeTypeException(mimeType,"does not contain '/'");
            }
            else if(subIndex == fullType.length()-1){
                throw new InvalidMimeTypeException(mimeType,"does not contain subtype after '/'");
            }
            else{
                String type = fullType.substring(0,subIndex);
                String subtype = fullType.substring(subIndex+1,fullType.length());
                if("*".equals(type) && !"*".equals(subtype)){
                    throw new InvalidMimeTypeException(mimeType,"wildcard type is legal only in '*/*' ( all mime types)");
                }
                else{
                    Map<String,String> parameters = null;
                    if(parts.length > 1){
                        parameters = new LinkedHashMap(parts.length-1);

                        for(int i=1;i < parts.length; ++i){
                            String parameter = parts[i];
                            int eqIndex = parameter.indexOf(61);
                            if(eqIndex != -1){
                                String attribute = parameter.substring(0,eqIndex);
                                String value = parameter.substring(eqIndex+1,parameter.length());
                                parameters.put(attribute,value);
                            }
                        }
                    }
                    try{
                        return new MimeType(type,subtype,parameters);
                    }
                    catch( UnsupportedCharsetException var12){
                        throw new InvalidMimeTypeException(mimeType,"unsupported charset '"+var12.getCharsetName()+"'");
                    }
                    catch(IllegalArgumentException var13){
                        throw new InvalidMimeTypeException(mimeType,var13.getMessage());
                    }
                }
            }
        }
    }
}

