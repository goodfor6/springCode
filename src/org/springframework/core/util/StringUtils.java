package org.springframework.core.util;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Administrator on 2017/8/17 0017.
 */
public abstract class StringUtils {
    private static final String FOLDER_SEPARATOR = "/";
    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
    private static final String TOP_PATH = "..";
    private static final String CURRENT_PATH = ".";
    private static final char EXTENSION_SEPARATOR = '.';

    public StringUtils(){

    }

    public static boolean isEmpty(Object str){
        return str == null || "".equals(str);
    }

    public static boolean hasLength(CharSequence str){
        return str != null && str.length() > 0;
    }

    public static boolean hasLength(String str){return hasLength((CharSequence)str);}

    public static boolean hasText(CharSequence str){
        if(!hasLength(str)){
            return false;
        }
        else{
            int strLen = str.length();

            for(int i = 0; i< strLen;++i){
                if(!Character.isWhitespace(str.charAt(i))){
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean hasText(String str){
        return hasText((CharSequence)str);
    }

    public static boolean containsWhitespace(CharSequence str){
        if(!hasLength(str)){
            return false;
        }
        else{
            int strlen =str.length();

            for(int i=0 ;i < strlen; ++i){
                if(Character.isWhitespace(str.charAt(i))){
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean constainsWhitespace(String str){
        return  containsWhitespace((CharSequence)str);
    }

    public static String trimWhitespace(String str){
        if(!hasLength(str)){
            return str;
        }
        else{
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(0))){
                sb.deleteCharAt(0);
            }

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))){
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    public static String trimAllWhitespace(String str){
        if(!hasLength(str)){
            return str ;
        }
        else{
            int len = str.length();
            StringBuilder sb = new StringBuilder(str.length());

            for(int i = 0; i< len; ++i){
                char c = str.charAt(i);
                if(!Character.isWhitespace(c)){
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    public static String trimLeadingWhitespace(String str){
        if(!hasLength(str)){
            return str;
        }
        else{
            StringBuilder sb = new StringBuilder(str);
            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(0))){
                sb.deleteCharAt(0);
            }
            return sb.toString();
        }
    }

    public static String trimTrailingWhiterspace(String str){
        if(!hasLength(str)){
            return str;
        }
        else{
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1 ))){
                sb.deleteCharAt(sb.length() -1);
            }
            return sb.toString();
        }
    }

    public static String trimLeadingCharacter(String str,char leadingCharacter){
        if(!hasLength(str)){
            return str;
        }
        else{
            StringBuilder sb = new StringBuilder(str);
            while(sb.length() > 0 && sb.charAt(0) == leadingCharacter){
                sb.deleteCharAt(0);
            }
            return sb.toString();

        }
    }

    public static String trimTrailingCharacter(String str,char trailinCharacter){
        if(!hasLength(str)){
            return str;
        }
        else{
            StringBuilder sb = new StringBuilder(str);
            while(sb.length() > 0 && sb.charAt(sb.length() - 1) ==trailinCharacter){
                sb.deleteCharAt(sb.length() -1);
            }
            return sb.toString();
        }
    }

    public static boolean startWithIgnoreCase(String str,String prefix){
        if( str != null && prefix != null){
            if(str.startsWith(prefix)){
                return true;
            }
            else if(str.length() < prefix.length()){
                return false;
            }
            else {
                String lcStr = str.substring(0,prefix.length()).toLowerCase();
                String lcPrefix = prefix.toLowerCase();
                return lcStr.equals(lcPrefix);
            }
        }
        else{
             return false;
        }
    }

    public static boolean endsWithIgnoreCase(String str, String suffix){
        if(str != null && suffix != null){
            if(str.endsWith(suffix)){
                return true;
            }
            else if(str.length() < suffix.length()){
                return false;
            }
            else{
                String lcStr = str.substring(str.length()- suffix.length()).toLowerCase();
                String lcSuffix = suffix.toLowerCase();
                return lcStr.equals(lcSuffix);
            }

        }
        else{
            return false;
        }
    }

    public static boolean substringMatch(CharSequence str, int index, CharSequence subString){
        for(int j = 0;j < subString.length() ;++j){
            int i = index + j;
            if(i >= str.length() || str.charAt(i) != subString.charAt(j)){
                return false;
            }
        }
        return true;
    }

    public static int countOccurrencesOf(String str,String sub){
        if(str != null && sub != null && str.length() != 0 && sub.length() != 0){
            int count = 0;
            int idx;
            for(int pos = 0 ; (idx = str.indexOf(sub,pos))!= -1;pos = idx + sub.length()){
                ++count;
            }
            return count;
        }
        else{
            return 0;
        }
    }

    public static String replace (String inString,String oldPattern,String newPattern){
        if(hasLength(inString) && hasLength(oldPattern) && newPattern != null){
            StringBuilder sb = new StringBuilder();
            int pos = 0;
            int index = inString.indexOf(oldPattern);

            for(int patLen = oldPattern.length();index >= 0;index = inString.indexOf(oldPattern,pos)){
                sb.append(inString.substring(pos,index));
                sb.append(newPattern);
                pos = index + patLen;
            }
            sb.append(inString.substring(pos));
            return sb.toString();
        }
        else{
            return inString;
        }
    }

    public static String delete(String inString,String pattern){return replace(inString,pattern,"");}

    public static String deleteAny(String inString,String charsToDelete){
        if(hasLength(inString) && hasLength(charsToDelete)){
            StringBuilder sb = new StringBuilder();

            for(int i =0 ;i < inString.length(); ++i){
                char c = inString.charAt(i);
                if(charsToDelete.indexOf(c)==-1){
                    sb.append(c);
                }
            }
            return sb.toString();
        }
        else{
            return inString;
        }
    }

    public static String quote(String str){return str != null ? "'"+str+"'":null;}

    public static Object quoteIfString(Object obj){return obj instanceof String?quote((String)obj): obj;}

    public static String unqualify(String qualifiedName){return unqualify(qualifiedName,'.');}

    public static String unqualify(String qualifiedName,char separator){
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }

    public static String capitalize(String str){return changeFirstCharacterCase(str,true);}

    public static String uncapotalize(String str){return changeFirstCharacterCase(str,false);}

    private static String changeFirstCharacterCase(String str,boolean capitalize){
        if(str != null && str.length() !=0){
            StringBuilder sb = new StringBuilder(str.length());
            if(capitalize){
                sb.append(Character.toUpperCase(str.charAt(0)));
            }
            else{
                sb.append(Character.toLowerCase(str.charAt(0)));
            }

            sb.append(str.substring(1));
            return sb.toString();
        }
        else{
            return str;
        }
    }

    public static String getFilename(String path){
        if(path == null){
            return null;
        }
        else{
            int separatorIndex = path.lastIndexOf("/");
            return separatorIndex != -1 ?path.substring(separatorIndex +1): path;
        }
    }

    public static String getFilenameExtension(String path){
        if(path == null){
            return null;
        }
        else{
            int extIndex = path.lastIndexOf(46);
            if(extIndex == -1){
                return null;
            }
            else{
                int folderIndex = path.lastIndexOf("/");
                return folderIndex > extIndex? null:path.substring(extIndex +1);
            }
        }
    }

    public static String stripFilenameExtension(String path){
        if(path == null){
            return null;
        }
        else{
            int extIndex = path.lastIndexOf(46);
            if(extIndex == -1){
                return path;
            }
            else{
                int folderIndex = path.lastIndexOf("/");
                return folderIndex > extIndex?path:path.substring(0,extIndex);
            }
        }
    }

    public static String applyRelativePath(String path,String relativePath){
        int separatorIndex = path.lastIndexOf("/");
        if(separatorIndex != -1){
            String newPath = path.substring(0,separatorIndex);
            if(!relativePath.startsWith("/")){
                newPath = newPath+"/";
            }
            return newPath + relativePath;
        }
        else{
            return relativePath;
        }
    }

    public static String cleanPath(String path){
        if(path == null){
            return null;
        }
        else{
            String pathToUse = replace(path,"\\", "/");
            int prefixIndex = pathToUse.indexOf(":");
            String  prefix = "";
            if(prefixIndex != -1){
                prefix = pathToUse.substring(0,prefixIndex + 1);
                if(prefix.contains("/")){
                    prefix = "";
                }
                else{
                    pathToUse = pathToUse.substring(prefixIndex + 1);
                }
            }
            if(pathToUse.startsWith("/")){
                prefix = prefix + "/";
                pathToUse = pathToUse.substring(1);
            }
            String pathArray = delimiterdListToStringArray(pathToUse,"/");
            List<String> pathElements = new LinkedList();
            int tops = 0;

            int i= 0;
            for(i = pathArray.length()-1;i >= 0;--i){
                String element = pathArray[i];
                if(!".".equals(element)){
                    if("..".equals(element)){
                        ++tops;
                    }
                    else if(tops > 0){
                        --tops;
                    }
                    else {
                        pathElements.add(0,element);
                    }
                }
            }
            for(i = 0; i < tops; ++i){
                pathElements.add(0,"..");
            }
            return prefix + collectionToDelimitedString(pathElements,"/");
        }
    }

    public static boolean pathEquals(String path1,String path2){return cleanPath(path1).equals(cleanPath(path2));}

    public static Locale pareLocaleString(String localeString){
        String[] parts = takenizeToStringArray(localeString,"_",false,false);
        String language = parts.length > 0 ? parts[0]:"";
        String country = parts.length > 1 ? parts[1]:"";
        validateLocalePart(language);
        validateLocalePart(country);
        String variant = "";
        if(parts.length > 2){
            int endIndexOfCountyCode = localeString.indexOf(country,language.length())+ country.length();
            variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountyCode));
            if(variant.startsWith("_")){
                variant = trimLeadingCharacter(variant,'_');
            }
        }
        return language.length() > 0 ?new Locale(language,country,variant):null;
    }

    private static void validateLocalePart(String localePart){
        for(int i = 0; i < localePart.length(); i++){
            char ch = localePart.charAt(i);
            if(ch != 95 && ch !=32 && !Character.isLetterOrDigit(ch)){
                throw new IllegalArgumentException("Locale part \""+ localePart +"\"contains invalid characters");
            }
        }
    }

    public static String toLanguageTag(Locale locale){
        return locale.getLanguage() + (hasText(locale.getCountry())? "-" + locale.getCountry():"");
    }

    public static TimeZone parseTimeZoneString(String timeZoneString){
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
        if("GMT".equals(timeZone.getID()) && !timeZoneString.startsWith("GMT)")){
            throw new IllegalArgumentException("invalid time zone specification '"+timeZone+"'");
        }
        else{
            return timeZone;
        }
    }

    public static String[] addStringToArray(String[] array,String str){
        if(ObjectUtils.isEmpty(array)){
            return new String[]{str};
        }
        else{
            String [] newArr = new String[array.length + 1];
            System.arraycopy(array,0,newArr,0,array.length);
            newArr[array.length] = str;
            return newArr;
        }
    }

    public static String[] concatenateStringArrays(String[] array1,String[] array2){
        if(ObjectUtils.isEmpty(array1)){
            return array2;
        }
        else if(ObjectUtils.isEmpty(array2)){
            return array1;
        }
        else{
            String [] newArr = new String[array1.length + array2.length];
            System.arraycopy(array1,0,newArr,0,array1.length);
            System.arraycopy(array2,0,newArr,array1.length,array2.length);
            return newArr;
        }
    }

    public static String[] mergeStringArrays(String[] array1,String [] array2){
        if(ObjectUtils.isEmpty(array1)){
            return array2;
        }
        else if(ObjectUtils.isEmpty(array2)){
            return array1;
        }
        else{
            List<String> result = new ArrayList();
            result.addAll(Arrays.asList(array1));
            String[] var3 = array2;
            int var4 = array2.length;

            for(int var5 = 0; var5 < var4; ++var5){
                String str = var3[var5];
                if(!result.contains(str)){
                    result.add(str);
                }
            }
            return toStringArray((Collection)result);
        }
    }

    public static String [] sortStringArray(String[] array){
        if(ObjectUtils.isEmpty(array)){
            return new String[0];
        }
        else {
            Arrays.sort(array);
            return array;
        }
    }

    public static String[] toStringArray(Collection<String>collection){
        return collection = null ? null: (String[])collection.toArray(new String[collection.size()]);
    }

    public static String[] toStringArray(Enumeration<String> enumeration){
        if(enumeration == null){
            return null;
        }
        else{
            List<String> list = Collections.list(enumeration);
            return (String[]) list.toArray(new String[list.size()]);
        }
    }

    public static String[] trimArrayElements(String[] array){
        if(ObjectUtils.isEmpty(array)){
            return new String[0];
        }
        else{
            String [] result = new String[array.length];
            for(int i = 0; i < array.length; ++i){
                String element = array[i];
                result[i] = element != null ? element.trim():null;
            }
            return result;
        }
    }

    public static String[] removeDuplicateString(String[] array){
        if(ObjectUtils.isEmpty(array)){
            return array;
        }
        else{
            Set<String> set = new TreeSet();
            String [] var2 = array;
            int var3 = array.length;

            for(int var4 =0; var4 < var3; ++var4){
                String element = var2[var4];
                set.add(element);
            }
            return toStringArray((Collection)set);
        }
    }

    public static String[] split(String toSplit, String delimiter){
        if(hasLength(toSplit) && hasLength(delimiter)){
            int offset = toSplit.indexOf(delimiter);
            if(offset < 0){
                return null;
            }
            else{
                String beforeDelimiter = toSplit.substring(0,offset);
                String afterDelimiter = toSplit.substring(offset + delimiter.length());
                return new String[]{beforeDelimiter, afterDelimiter};
            }
        }
        else{
            return null;
        }
    }

    public static Properties splitArrayElementIntoProperties(String[] array,String delimiter){
        return splitArrayElementIntoProperties(array, delimiter,(String) null);
    }

    public static Properties splitArrayElementsIntoProperties(String[] array,String delimiter,String charsToDelete)
}
