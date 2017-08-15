package org.springframework.core.util;

import java.io.*;

/**
 * Created by Administrator on 2017/8/15 0015.
 */
public abstract class SerializationUtils {
    public SerializationUtils(){

    }

    public static byte[] serialize(Object object){
        if(object == null){
            return null;
        }
        else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

            try{
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                oos.flush();
            }
            catch(IOException var3){
                throw new IllegalArgumentException("Failed to serialize object of type:"+object.getClass(),var3);
            }
            return baos.toByteArray();
        }
    }

    public static Object descrialize(byte[] bytes){
        if(bytes == null){
            return null;
        }
        else {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return ois.readObject();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to desccialize object",e);
            }
            catch(ClassNotFoundException var3){
                throw new IllegalArgumentException("Failed  to deserialize object type ",var3);
            }
        }
    }
}
