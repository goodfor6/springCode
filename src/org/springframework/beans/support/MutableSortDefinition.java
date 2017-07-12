package org.springframework.beans.support;

import java.io.Serializable;

import org.springframework.util.StringUtils;

public class MutableSortDefinition implements SortDefinition,Serializable {
	
	private String property="";
	
	private boolean ignoreCase=true;
	
	private boolean ascending=true;
	
	private boolean toggleAscendingOnProperty=false;
	
	public MutableSortDefinition(){
		
	}

	public MutableSortDefinition(SortDefinition source){
		this.property=source.getProperty();
		this.ignoreCase=source.isIgnoreCase();
		this.ascending=source.isAscending();
	}
	
	public MutableSortDefinition(String property,boolean ignoreCase,boolean ascending){
		this.property=property;
		this.ignoreCase=ignoreCase;
		this.ascending=ascending;
	}
	
	public MutableSortDefinition(boolean toggleAscendingOnSameProperty){
		this.toggleAscendingOnProperty=toggleAscendingOnSameProperty;
	}
	
	public void setProperty(String property){
		if(!StringUtils.hasLength(property)){
			this.property="";
		}
		else{
			if(isToggleAscendingOnProperty()){
				this.ascending=(!property.equals(this.property)|| !this.ascending);
			}
			this.property=property;
		}
	}
	public boolean isToggleAscendingOnProperty(){
		return this.toggleAscendingOnProperty;
	}
	
	@Override
	public boolean equals(Object other){
		if(this==other){
			return true;
		}
		if(!(other instanceof SortDefinition)){
			return false;
		}
		SortDefinition otherSd=(SortDefinition)other;
		return (getProperty().equals(otherSd.getProperty())&&
				isAscending()==otherSd.isAscending()&&
				isIgnoreCase()==otherSd.isIgnoreCase());
	}
	
	@Override
	public int hashCode(){
		int hashCode=getProperty().hashCode();
		hashCode=29 * hashCode + (isIgnoreCase()?1:0);
		hashCode=29 * hashCode + (isAscending()?1:0);
		return hashCode;
	}
	
	@Override
	public String getProperty() {
		return this.property;
	}

	@Override
	public boolean isIgnoreCase() {
		return this.ignoreCase;
	}

	@Override
	public boolean isAscending() {
		return this.ascending;
	}

}
