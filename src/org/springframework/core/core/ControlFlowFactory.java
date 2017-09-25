package org.springframework.core.core;

import org.springframework.core.util.Assert;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Administrator on 2017/9/25 0025.
 */
public abstract class ControlFlowFactory {

    public static ControlFlow createControlFlow(){return new Jdk14ControlFlow();}

    static class Jdk14ControlFlow implements ControlFlow{
        private StackTraceElement[] stack;

        public Jdk14ControlFlow(){this.stack = new Throwable().getStackTrace();}

        public boolean under(Class<?> clazz){
            Assert.notNull(clazz,"Class muust not be null");
            String className = clazz.getName();
            for(int i = 0; i < stack.length; i++){
                if(this.stack[i].getClassName().equals(className)){
                    return true;
                }
            }
            return false;
        }

        public boolean under(Class<?> clazz, String methodName){
            Assert.notNull(clazz,"Class must not be null");
            Assert.notNull(methodName,"Method name must not be null");
            String className = clazz.getName();
            for(int i = 0; i < this.stack.length; i++){
                if(this.stack[i].getClassName().equals(className) &&
                        this.stack[i].getMethodName().equals(methodName)){
                    return true;
                }
            }
            return false;
        }

        public boolean underToken(String token){
            if(token == null){
                return false;
            }
            StringWriter sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            return stackTrace.indexOf(token)!= -1;
        }

        public String toString(){
            StringBuilder sb = new StringBuilder("Jdk14ControlFlow:");
            for(int i = 0;i < this.stack.length;i++){
                if(i > 0){
                    sb.append("\n\t@");
                }
                sb.append(this.stack[i]);
            }
            return sb.toString();
        }
    }

}
