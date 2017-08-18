package org.springframework.core.util;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2017/8/17 0017.
 */
public abstract class StreamUtils {
    public static final int BUFFER_SIZE = 4096;

    public StreamUtils(){}

    public static byte[] copyToByteArray(InputStream in)throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        copy((InputStream)in,out);
        return out.toByteArray();
    }

    public static String copyToString(InputStream in, Charset charset)throws IOException{
        Assert.notNull(in,"No InputStream specified");
        StringBuilder out = new  StringBuilder();
        InputStreamReader reader = new InputStreamReader(in,charset);
        char[] buffer = new char[4096];
        boolean var5 = true;

        int bytesRead;
        while((bytesRead = reader.read(buffer))!= -1){
            out.append(buffer,0,bytesRead);
        }
        return out.toString();
    }

    public static void copy(byte[] in,OutputStream out)throws IOException{
        Assert.notNull(in, "No input byte array specified");
        Assert.notNull(out,"No OutputStream specified");
        out.write(in);
    }

    public static void copy(String in,Charset charset,OutputStream out)throws IOException{
        Assert.notNull(in,"No input String specified");
        Assert.notNull(charset,"No charset specified ");
        Assert.notNull(out,"No OutputStream specified");
        Writer writer = new OutputStreamWriter(out ,charset);
        writer.write(in);
        writer.flush();
    }

    public static int copy(InputStream in , OutputStream out)throws IOException{
        Assert.notNull(in,"No InputStream specified");
        Assert.notNull(out,"No OutputStream specified");
        int byteCount = 0;
        byte[] buffer = new byte[4096];

        int bytesRead;
        for(boolean var4 = true;(bytesRead = in.read(buffer))!=-1;byteCount += bytesRead){
            out.write(buffer,0,bytesRead);
        }

        out.flush();
        return byteCount;
    }

    public static InputStream nonClosing(InputStream in){
        Assert.notNull(in,"No InputStream specified");
        return new StreamUtils.NonClosingInputStream(in);
    }

    public static OutputStream nonClosing(OutputStream out){
        Assert.notNull(out,"No OutputStream specified");
        return new StreamUtils.NonClosingOutputStream(out);
    }

    public static class NonClosingOutputStream extends FilterOutputStream{
        public NonClosingOutputStream(OutputStream out){super(out);}

        public void write(byte[] b,int off,int let)throws IOException{
            this.out.write(b,off,let);
        }
        public void close() throws IOException{}
    }

    public static class NonClosingInputStream extends FilterInputStream{
        public NonClosingInputStream(InputStream in){super(in);}

        public void close() throws IOException{}
    }


}
