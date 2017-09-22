package org.springframework.core.core;

/**
 * Created by Administrator on 2017/9/22 0022.
 */
public class ConstantException extends IllegalArgumentException {

    public ConstantException(String className,String field ,String message){
        super("Field'"+field+"'"+message+"in class["+className+"]");
    }

    public ConstantException(String className,String namePrefix,Object value){
        super("No'"+namePrefix+"' field with value '"+value+"'found in class["+className+"]");
    }

}
