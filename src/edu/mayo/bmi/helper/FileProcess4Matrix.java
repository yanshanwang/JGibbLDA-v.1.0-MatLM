package edu.mayo.bmi.helper;

import java.util.ArrayList;
import java.util.List;

public class FileProcess4Matrix {
	/**
	 * Split a string and return nonempty string array.
	 * Requires .
	 * @param 
	 */
	public static String[] splitString(String line, String del){
		List<String> strList = new ArrayList<String>();
		String[] temp = line.split(del);
		for(String i:temp){
			if(!i.equals("")&&i!=null)
				strList.add(i);
		}
		String[] str = strList.toArray(new String[strList.size()]);
		return str;
	}
}
