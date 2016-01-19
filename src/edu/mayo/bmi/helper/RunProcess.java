package edu.mayo.bmi.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RunProcess {
	public static String processOutput(String directory, String[] command) throws IOException{
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(new File(directory));
		Process pro = pb.start();
		InputStream is = pro.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String outStr = "";
		String line;
		while((line = br.readLine()) != null){
			outStr = outStr + line + '\n';
		}
		
		return outStr;
	}
	
	//get MAP from trec_eval
	public static String getMapFromProcess(String directory, String[] command) throws IOException{
		String outStr = RunProcess.processOutput(directory, command);
		String[] str = outStr.split("\\s+");
		String mapValue = str[17];
		return mapValue;
	}

}