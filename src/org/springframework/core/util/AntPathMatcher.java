package org.springframework.core.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

public class AntPathMatcher implements PathMatcher {
	
	public static final String DEFAULT_PATH_SEPARATOR="/";
	
	private static final int CACHE_TURNOFF_THRESHOLD=65536;
	
	private static final Pattern VARIABLE_PATTERN=Pattern.compile("\\{[^/]=?\\}");
	
	
	private String pathSeparator;
	
	private PathSeparatorPatternCache pathSeparatorPatternCache;
	
	private boolean trimTokens=true;
	
	private volatile Boolean cachePatterns;
	
	private final Map<String,String[]>tokenizedPatternCache=new ConcurrentHashMap<String,String[]>(256);
	
	final Map<String,AntPathStringMatcher>stringMatcherCache=new ConcurrentHashMap<String,AntPathStringMatcher>(256);
	 

	protected AntPathStringMatcher getStringMatcher(String pattern){
		return null;
	}
	
	protected static class AntPathStringMatcher{
		private static final Pattern CLOB_PATTERN=Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}");
		private static final String DEFAULT_VARIABLE_PATTERN="(.*)";
		private final Pattern pattern;
		private final List<String>variableNames=new LinkedList<String>();
		
		public AntPathStringMatcher(String pattern){
			StringBuilder patternBuilder=new StringBuilder();
		    Matcher m=CLOB_PATTERN.matcher(pattern);
		    int end=0;
		    while(m.find()){
		    	patternBuilder.append(quote(pattern,end,m.start()));
		    	String match=m.group();
		    	if("?".equals(match)){
		    		patternBuilder.append('.');
		    	}
		    	else if("*".equals(match)){
		    		patternBuilder.append('.');
		    	}
		    	else if (match.startsWith("{")&& match.endsWith("}")){
		    		int colonIdx=match.indexOf(':');
		    		if(colonIdx==-1){
		    			patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
		    			this.variableNames.add(m.group(1));
		    		}else{
		    			String variablePattern=match.substring(colonIdx+1, match.length()-1);
		    			patternBuilder.append('(');
		    			patternBuilder.append(variablePattern);
		    			patternBuilder.append(')');
		    			String variableName=match.substring(1,colonIdx);
		    			this.variableNames.add(variableName);
		    		}
		    	}
		    	end=m.end();
		    }
		    patternBuilder.append(quote(pattern,end,pattern.length()));
		    this.pattern=Pattern.compile(patternBuilder.toString());
		}
		private String quote(String s,int start,int end){
			if(start==end){
				return "";
			}
			return Pattern.quote(s.substring(start,end));
		}
		
		public boolean matchStrings(String str,Map<String,String>uriTemplateVariables){
			Matcher matcher =this.pattern.matcher(str);
			if(matcher.matches()){
			if(uriTemplateVariables!=null){
				Assert.isTrue(this.variableNames.size()==matcher.groupCount(),
				" The number of capturing grouping in the patern segment "+ this.pattern+
				" does not match the mumber of URI template variables  it defines , which can occur if "+
				" capturing groups are used in a URI template regex. Use non-capuring groups instead. ");
				for(int i=1;i<matcher.groupCount();i++){
					String name=this.variableNames.get(i-1);
					String value=matcher.group(i);
					uriTemplateVariables.put(name,value);
				}
			}
			return true;
		}
		else{
			return false;
		}
	  }
	}
	
	private static class PathSeparatorPatternCache{
		
		private final String endsOnWildCard;
		
		private final String endsOnDoubleWildCard;
		
		public PathSeparatorPatternCache(String pathSeparator){
			this.endsOnWildCard=pathSeparator+"*";
			this.endsOnDoubleWildCard=pathSeparator+"*";
		}
		public String getEndsOnWildCard(){
			return this.endsOnWildCard;
		}
		public String getEndsOnDoubleWildCard(){
			return this.endsOnDoubleWildCard;
		}
	}

	@Override
	public boolean isPattern(String path) {
		return (path.indexOf('*')!=-1||path.indexOf('?')!=-1);
	}

	@Override
	public boolean match(String pattern, String path) {
		return doMatch(pattern,path,true,null);
	}

	@Override
	public boolean matchStart(String pattern, String path) {
		return doMatch(pattern,path,false,null);
	}

	@Override
	public String extractPathWithinPattern(String pattern, String path) {
		String [] patternParts=StringUtils.tokenizeToStringArray(pattern, this.pathSeparator,this.trimTokens,true);
		String [] pathParts = StringUtils.tokenizeToStringArray(path, this.pathSeparator,this.trimTokens,true);
		StringBuilder builder = new StringBuilder();
		boolean pathStarted = false;
		
		for(int segment=0;segment<patternParts.length;segment++){
			String patternPart = patternParts[segment];
			if(patternPart.indexOf('*')>-1 || patternPart.indexOf('?')>-1){
				for(;segment<pathParts.length;segment++){
					if(pathStarted || (segment==0&&!pattern.startsWith(this.pathSeparator))){
						builder.append(this.pathSeparator);
					}
					builder.append(pathParts[segment]);
					pathStarted = true;
				}
			}
		}
		return builder.toString();
	}

	@Override
	public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		Map<String,String>variables = new LinkedHashMap<String,String>();
		boolean result = doMatch(pattern,path, true, variables);
		Assert.state(result,"Pattern \""+pattern+"\" is not a match for \""+path+"\"");
		return variables;
	}

	@Override
	public Comparator<String> getPatternComparator(String path) {
		return new AntPatternComparator(path);
	}
	
	protected static class AntPatternComparator implements Comparator<String> {

		private final String path;

		public AntPatternComparator(String path) {
			this.path = path;
		}

	public int compare(String pattern1, String pattern2) {
		PatternInfo info1 = new PatternInfo(pattern1);
		PatternInfo info2 = new PatternInfo(pattern2);

		if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
			return 0;
		}
		else if (info1.isLeastSpecific()) {
			return 1;
		}
		else if (info2.isLeastSpecific()) {
			return -1;
		}

		boolean pattern1EqualsPath = pattern1.equals(path);
		boolean pattern2EqualsPath = pattern2.equals(path);
		if (pattern1EqualsPath && pattern2EqualsPath) {
			return 0;
		}
		else if (pattern1EqualsPath) {
			return -1;
		}
		else if (pattern2EqualsPath) {
			return 1;
		}

		if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
			return 1;
		}
		else if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
			return -1;
		}

		if (info1.getTotalCount() != info2.getTotalCount()) {
			return info1.getTotalCount() - info2.getTotalCount();
		}

		if (info1.getLength() != info2.getLength()) {
			return info2.getLength() - info1.getLength();
		}

		if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
			return -1;
		}
		else if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
			return 1;
		}

		if (info1.getUriVars() < info2.getUriVars()) {
			return -1;
		}
		else if (info2.getUriVars() < info1.getUriVars()) {
			return 1;
		}

		return 0;
	}


	/**
	 * Value class that holds information about the pattern, e.g. number of
	 * occurrences of "*", "**", and "{" pattern elements.
	 */
	private static class PatternInfo {

		private final String pattern;

		private int uriVars;

		private int singleWildcards;

		private int doubleWildcards;

		private boolean catchAllPattern;

		private boolean prefixPattern;

		private Integer length;

		public PatternInfo(String pattern) {
			this.pattern = pattern;
			if (this.pattern != null) {
				initCounters();
				this.catchAllPattern = this.pattern.equals("/**");
				this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
			}
			if (this.uriVars == 0) {
				this.length = (this.pattern != null ? this.pattern.length() : 0);
			}
		}

		protected void initCounters() {
			int pos = 0;
			while (pos < this.pattern.length()) {
				if (this.pattern.charAt(pos) == '{') {
					this.uriVars++;
					pos++;
				}
				else if (this.pattern.charAt(pos) == '*') {
					if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == '*') {
						this.doubleWildcards++;
						pos += 2;
					}
					else if (!this.pattern.substring(pos - 1).equals(".*")) {
						this.singleWildcards++;
						pos++;
					}
					else {
						pos++;
					}
				}
				else {
					pos++;
				}
			}
		}

		public int getUriVars() {
			return this.uriVars;
		}

		public int getSingleWildcards() {
			return this.singleWildcards;
		}

		public int getDoubleWildcards() {
			return this.doubleWildcards;
		}

		public boolean isLeastSpecific() {
			return (this.pattern == null || this.catchAllPattern);
		}

		public boolean isPrefixPattern() {
			return this.prefixPattern;
		}

		public int getTotalCount() {
			return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
		}

		/**
		 * Returns the length of the given pattern, where template variables are considered to be 1 long.
		 */
		public int getLength() {
			if (this.length == null) {
				this.length = VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length();
			}
			return this.length;
		}
	 }
   }
	
	@Override
	public String combine(String pattern1, String pattern2) {
		if (!StringUtils.hasText(pattern1) && !StringUtils.hasText(pattern2)) {
			return "";
		}
		if (!StringUtils.hasText(pattern1)) {
			return pattern2;
		}
		if (!StringUtils.hasText(pattern2)) {
			return pattern1;
		}

		boolean pattern1ContainsUriVar = pattern1.indexOf('{') != -1;
		if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
			return pattern2;
		}

		if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnWildCard())) {
			return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
		}

		if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnDoubleWildCard())) {
			return concat(pattern1, pattern2);
		}

		int starDotPos1 = pattern1.indexOf("*.");
		if (pattern1ContainsUriVar || starDotPos1 == -1 || this.pathSeparator.equals(".")) {
			// simply concatenate the two patterns
			return concat(pattern1, pattern2);
		}
		String extension1 = pattern1.substring(starDotPos1 + 1);
		int dotPos2 = pattern2.indexOf('.');
		String fileName2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
		String extension2 = (dotPos2 == -1 ? "" : pattern2.substring(dotPos2));
		String extension = extension1.startsWith("*") ? extension2 : extension1;
		return fileName2 + extension;
	}
	
	private String concat(String path1, String path2) {
		if (path1.endsWith(this.pathSeparator) || path2.startsWith(this.pathSeparator)) {
			return path1 + path2;
		}
		return path1 + this.pathSeparator + path2;
	}
	
	protected boolean doMatch(String pattern,String path,boolean fullMatch,Map<String,String>uriTemplateVariables){
		if(path.startsWith(this.pathSeparator)!=pattern.startsWith(this.pathSeparator)){
			return false;
		}
		
		String [] pattDirs=tokenizePattern(pattern);
		String [] pathDirs=tokenizePath(path);
		
		int pattIdxStart=0;
		int pattIdxEnd=pattDirs.length-1;
		int pathIdxStart=0;
		int pathIdxEnd=pathDirs.length-1;
		
		while(pattIdxStart<=pattIdxEnd && pathIdxStart<=pathIdxEnd){
			String pattDir=pattDirs[pattIdxStart];
			if("*".equals(pattDir)){break;}
		if(!matchStrings(pattDir,pathDirs[pathIdxStart],uriTemplateVariables)){
			return false;
		}
		pattIdxStart++;
		pathIdxStart++;
	  }
		if(pathIdxStart>pathIdxEnd){
			if(pathIdxStart>pattIdxEnd){
				return(pattern.endsWith(this.pathSeparator)?path.endsWith(this.pathSeparator):
					!path.endsWith(this.pathSeparator));
			}
			if(!fullMatch){
				return true;
			}
			if(pattIdxStart==pattIdxEnd&&pattDirs[pattIdxStart].equals("*")&&path.endsWith(this.pathSeparator)){
				return true;
			}
			for(int i=pattIdxStart;i<=pattIdxEnd;i++){
				if(!pattDirs[i].equals("**")){
					return false;
				}
			}
			return true;
		}
		else if(pattIdxStart>pattIdxEnd){
			return false;
		}
		else if(!fullMatch&&"**".equals(pattDirs[pattIdxStart])){
			return true;
		}
		
		while (pattIdxStart <=pattIdxEnd && pattIdxStart<=pathIdxEnd){
			String pattDir=pattDirs[pattIdxEnd];
			if(pattDir.equals("**")){
				break;
			}
			pattIdxEnd--;
			pathIdxEnd--;
		}
		if(pathIdxStart>pathIdxEnd){
			for(int i=pattIdxStart;i<=pattIdxEnd;i++){
				if(!pattDirs[i].equals("**")){
					return false;
				}
			}
			return true;
		}
		while(pattIdxStart!=pattIdxEnd && pathIdxStart<=pathIdxEnd){
			int patIdxTem=-1;
			for(int i=pattIdxStart+1;i<pattIdxEnd;i++){
				if(pattDirs[i].equals("**")){
					patIdxTem=i;
					break;
				}
			}
			if(patIdxTem==pattIdxStart+1){
				pattIdxStart++;
				continue;
			}
			int patLength = (patIdxTem-pattIdxStart-1);
			int strLength = (pathIdxEnd-pathIdxStart+1);
			int foundIdx=-1;
			
			strLoop:
				for(int i=0;i<strLength-patLength;i++){
					for(int j=0;j<patLength;j++){
						String subPat=pattDirs[pattIdxStart+j+1];
						String subStr=pathDirs[pathIdxStart+i+1];
						if(!matchStrings(subPat,subStr,uriTemplateVariables)){
							continue strLoop;
						}
					}
					foundIdx=pathIdxStart+i;
					break;
				}
			if(foundIdx==-1){
				return false;
			}
			pattIdxStart=patIdxTem;
			pathIdxStart=foundIdx+patLength;
		 }
		for(int i=pattIdxStart;i<=pattIdxEnd;i++){
			if(!pattDirs[i].equals("**")){
				return false;
			}
		}
		return true;
	}
	
	
	protected String[] tokenizePattern(String pattern){
		String [] tokenized=null;
		Boolean cacehPatterns=this.cachePatterns;
		if(cachePatterns==null || cachePatterns.booleanValue()){
			tokenized=this.tokenizedPatternCache.get(pattern);
		}
		if(tokenized ==null){
			tokenized=tokenizePath(pattern);
			if(cachePatterns==null&&this.tokenizedPatternCache.size()>=CACHE_TURNOFF_THRESHOLD){
				deactivatePatternCache();
				return tokenized;
			}
			if(cachePatterns==null|| cachePatterns.booleanValue()){
				this.tokenizedPatternCache.put(pattern, tokenized);
			}
		}
		return tokenized;
	}
	
	protected String[] tokenizePath(String path){
		return StringUtils.tokenizeToStringArray(path, this.pathSeparator,this.trimTokens,true);
	}
	private void deactivatePatternCache(){
		this.cachePatterns=false;
		this.tokenizedPatternCache.clear();
		this.stringMatcherCache.clear();
	}
	private boolean matchStrings(String pattern,String str,Map<String,String>uriTemplateVariables){
		return getStringMatcher(pattern).matchStrings(str, uriTemplateVariables);
	}
}
