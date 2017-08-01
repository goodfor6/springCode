package org.springframework.core.util;

import java.io.File;
import java.io.IOException;

import org.springframework.util.FileCopyUtils;

public abstract class FileSystemUtils {

	public static boolean deleteRecursively(File root){
		if(root != null && root.exists()){
			if(root.isDirectory()){
				 File [] children = root.listFiles();
				 if(children != null){
					 for(File child : children){
						 deleteRecursively(child);
					 }
				 }
			}
			return root.delete();
		}
		return false;
	}
	
	public static void copyRecursively(File src,File dest) throws IOException{
		Assert.isTrue(src != null && (src.isDirectory() || src.isFile()),"Source File must denote a directory or file");
		Assert.notNull(dest,"Destination File must not be null");
		doCopyRecursively(src,dest);
	}
	
	private static void doCopyRecursively(File src,File dest)throws IOException{
		if(src.isDirectory()){
			dest.mkdir();
			File [] entries = src.listFiles();
			if(entries == null){
				throw new IOException("Could not list file in directory:"+src);
			}
			for(File entry: entries){
				doCopyRecursively(entry,new File(dest,entry.getName()));
			}
		}
		else if(src.isFile()){
			try{
				dest.createNewFile();
			}
			catch(IOException ex){
				IOException ioex = new IOException("Failed to create file:"+ dest);
				ioex.initCause(ex);
				throw ioex;
			}
			FileCopyUtils.copy(src, dest);
		}
		else{
			
		}
	}
	
}
