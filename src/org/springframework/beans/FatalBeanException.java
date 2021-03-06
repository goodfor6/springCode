package org.springframework.beans;

@SuppressWarnings("serial")
public class FatalBeanException extends BeansException {

	public FatalBeanException(String msg) {
		super(msg);
	}
	
	public FatalBeanException(String msg,Throwable cause){
		super(msg,cause);
	}

}
