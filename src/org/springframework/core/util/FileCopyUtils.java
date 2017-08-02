package org.springframework.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.springframework.util.StreamUtils;

public abstract class FileCopyUtils {
	
	public static final int BUFFER_SIZE =StreamUtils.BUFFER_SIZE;
	
	public static int copy(File in,File out)throws IOException{
		Assert.notNull(in,"NO input File specified");
		Assert.notNull(out,"No output File specified");
		return copy(new BufferedInputStream(new FileInputStream(in)),new BufferedOutputStream(new FileOutputStream(out)));
	}
	
	public static void copy(byte[] in,File out)throws IOException{
		Assert.notNull(in,"No input byte array specified");
		Assert.isNull(out,"No output File specified");
		ByteArrayInputStream inStream = new ByteArrayInputStream(in);
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(out));
		copy(inStream,outStream);
	}
	
	public static byte[] copyToByteArray(File in) throws IOException{
		Assert.notNull(in,"No input File specified");
		return copyToByteArray(new BufferedInputStream(new FileInputStream(in)));
	}
	
	public static int copy(InputStream in,OutputStream out)throws IOException{
		Assert.notNull(in,"No InputSteam specified");
		Assert.notNull(out,"No OutputSteam specified");
		try{
			return StreamUtils.copy(in,out);
		}
		finally{
			try{
				in.close();
			}
			catch(IOException ex)
			{
			}
			try{
				out.close();
			}
			catch(IOException ex)
			{
			}
			
		}
	}
	
	public static void copy(byte[] in,OutputStream out)throws IOException{
		Assert.notNull(in,"No inout byte array specified");
		Assert.notNull(out,"No outputStream specified");
		try{
			out.write(in);
		}
		finally{
			try{
				out.close();
			}
			catch(IOException ex){
				
			}
		}
	}
	
	public static byte[] copyToByteArray(InputStream in)throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		copy(in,out);
		return out.toByteArray();
	}
	
	public static int copy(Reader in,Writer out)throws IOException{
		Assert.notNull(in,"No Reader specified");
		Assert.notNull(out,"No writer specified");
		try{
			int byteCount = 0;
			char[] buffer = new char[BUFFER_SIZE];
			int bytesReader = -1;
			while ()
		}
	}
	

}
