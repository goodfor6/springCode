package org.springframework.beans;

public interface Mergeable {
	
	boolean isMergeEnabled();
	
	Object mege(Object parent);

}
