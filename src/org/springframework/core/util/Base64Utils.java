package org.springframework.core.util;

import java.nio.charset.Charset;
import java.util.Base64;
import org.springframework.util.ClassUtils;

public abstract class Base64Utils {
	
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	private static final Base64Delegate delegate;
	
	static {
		Base64Delegate delegateToUse = null;
		if(ClassUtils.isPresent("java.util.Base64",Base64Utils.class.getClassLoader())){
			delegateToUse = new JdkBase64Delegate();
		}
		else if (ClassUtils.isPresent("org.apache.commons.codec.binary.Base64", Base64Utils.class.getClassLoader())){
			delegateToUse = new CommonsCodeBase64Delegate();
		}
		delegate = delegateToUse;
	}
	
	private static void assertSupported(){
		Assert.state(delegate != null,  "Neither Java 8 nor Apache Commons Coder found - Base64 encoding not supported");
	}
	
	public static byte[] encode(byte[] src){
		assertSupported();
		return delegate.encode(src);
	}
	
	public static String encodeToString(byte[] src){
		assertSupported();
		if( src == null){
			return null;
		}
		if(src.length == 0){
			return "";
		}
		return new String(delegate.encode(src),DEFAULT_CHARSET);
	}
	
	public static byte[] decode(byte [] src){
		assertSupported();
		return delegate.decode(src);
	}
	
	public static byte[] decodeFromString(String src){
		assertSupported();
		if(src == null){
			return null;
		}
		if(src.length() == 0){
			return new byte[0];
		}
		return delegate.decode(src.getBytes(DEFAULT_CHARSET));
	}
	
	private interface Base64Delegate{
		byte [] encode(byte[]src);
		byte [] decode(byte[] src);
	}
	
	private static class JdkBase64Delegate implements Base64Delegate{

		@Override
		public byte[] encode(byte[] src) {
			if(src == null || src.length == 0){
				return src;
			}
			return Base64.getEncoder().encode(src);
		}

		@Override
		public byte[] decode(byte[] src) {
			if(src == null || src.length == 0){
				return src;
			}
			return Base64.getDecoder().decode(src);
		}
		
	}
	
	private static class CommonsCodeBase64Delegate implements Base64Delegate{
		
		private final org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
		
		public byte[] encode(byte[] src){
			return this.base64.encode(src);
		}
		
		public byte[] decode(byte[] src){
			return this.base64.decode(src);
		}
	}
}
