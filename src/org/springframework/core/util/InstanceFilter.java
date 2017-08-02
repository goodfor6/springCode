package org.springframework.core.util;

import java.util.Collection;
import java.util.Collections;

public class InstanceFilter<T> {
	
	private final Collection<? extends T> inclueds;
	
	private final Collection<? extends T> excludes;

	private final boolean matchIfEmpty;
	
	public InstanceFilter(Collection<? extends T>includes,Collection<? extends T> excludes,boolean matchIfEmpty){
		this.inclueds = includes != null? includes : Collections.<T>emptyList();
		this.excludes = excludes != null? excludes : Collections.<T>emptyList();
		this.matchIfEmpty = matchIfEmpty;
	}
	
	public boolean match(T instance){
		Assert.notNull(instance,"The instance to match is mandatory");
		
		boolean includesSet = !this.inclueds.isEmpty();
		boolean excludesSet = !this.excludes.isEmpty();
		if(!includesSet && !excludesSet){
			return this.matchIfEmpty;
		}
		
		boolean matchIncludes = match(instance,this.inclueds);
		boolean matchExcludes = match(instance,this.excludes);
		
		if(!includesSet){
			return !matchExcludes;
		}
		if(!excludesSet){
			return matchIncludes;
		}
		return matchIncludes && !matchExcludes;
	}
	
	protected boolean match(T instance,T candidate){
		return instance.equals(candidate);
	}
	
	protected boolean match(T instance,Collection<? extends T>candidates){
		for(T candidate : candidates){
			if(match(instance,candidate)){
				return true;
			}
		}
	    return false;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(": includes=").append(this.inclueds);
		sb.append(",excludes=").append(this.excludes);
		sb.append(",matchIfEmpty=").append(this.matchIfEmpty);
		return sb.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
