package jgibblda;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

public class SimpleTools {
	public static void updateMap(HashMap<String, List<JSONObject>> firstLevelNode2pmidHm,  String aKey, JSONObject item){
		if(firstLevelNode2pmidHm.containsKey(aKey)){
			List<JSONObject> aList = firstLevelNode2pmidHm.get(aKey);
			aList.add(item);
			firstLevelNode2pmidHm.put(aKey, aList);
		}else{
			List<JSONObject> aList = new ArrayList<JSONObject>();
			aList.add(item);
			firstLevelNode2pmidHm.put(aKey, aList);
		}
	}

	
	public static <T> void updateMap(HashMap<T, List<T>> firstLevelNode2pmidHm,  T aKey, T item){
		if(firstLevelNode2pmidHm.containsKey(aKey)){
			List<T> aList = firstLevelNode2pmidHm.get(aKey);
			aList.add(item);
			firstLevelNode2pmidHm.put(aKey, aList);
		}else{
			List<T> aList = new ArrayList<T>();
			aList.add(item);
			firstLevelNode2pmidHm.put(aKey, aList);
		}
	}
	
	public static <T> void updateMap(SortedMap<T,List<T>> aMap, T aKey, T item){
		if(aMap.containsKey(aKey)){
			List<T> aList = aMap.get(aKey);
			aList.add(item);
			aMap.put(aKey, aList);
		}else{
			List<T> aList = new ArrayList<T>();
			aList.add(item);
			aMap.put(aKey, aList);
		}
	}
	
	public static <T> void updateMap(HashMap<T,Integer> aMap, T aKey){
		if(!aMap.containsKey(aKey)){
			aMap.put(aKey, aMap.size());
		}
	}
	
	public static <T> void updateMap(SortedMap<T,Integer> aMap, T aKey){
		if(!aMap.containsKey(aKey)){
			aMap.put(aKey, aMap.size());
		}
	}
	
	public static <T> void updateMap(SortedMap<T,Integer> aMap, SortedMap<Integer,T> contraMap,  T aKey){
		if(!aMap.containsKey(aKey)){
			aMap.put(aKey, aMap.size());
			contraMap.put(aMap.size(),aKey);
		}
	}

	
	public static int countLines(String filename) throws IOException {
		long time = System.currentTimeMillis();
	    LineNumberReader reader  = new LineNumberReader(new FileReader(filename));
	    int cnt = 0;
	    String lineRead = "";
	    while ((lineRead = reader.readLine()) != null) {}
	    cnt = reader.getLineNumber(); 
	    reader.close();
		System.out.println("time taken in ms in SimpleTools.java countLines = "
				+ (System.currentTimeMillis() - time));
	    return cnt;
	}
	
	final static Pattern NUMBER_PATTERN = Pattern.compile("[+-]?\\d*\\.?\\d+");

	static boolean isNumber(String input) {
	    Matcher m = NUMBER_PATTERN.matcher(input);
	    return m.matches();
	}
}
