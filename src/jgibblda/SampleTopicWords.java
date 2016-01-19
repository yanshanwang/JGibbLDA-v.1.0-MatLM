package jgibblda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class SampleTopicWords {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		HashMap<String,HashMap<String,Double>> topicMap = new HashMap<String,HashMap<String,Double>>();
		BufferedReader bfTopic0 = new BufferedReader(new FileReader(new File(args[0])));
		String line = "";
		HashMap<String,Double> topicPortionHm = new HashMap<String,Double>();
		String preTopicNum = bfTopic0.readLine();
		double total = 0.0;
		while((line=bfTopic0.readLine())!=null){
			if(line.startsWith("Topic")){
				String topicNum = line;
				topicPortionHm.put(preTopicNum, total);
				preTopicNum = topicNum;
				total = 0;
			}else{
				if(line.trim()==null||line.contains("unitok")){
					continue;
				}
				String[] tokenProb = line.trim().split(" ");
				total+= Double.parseDouble(tokenProb[1]);
			}
		}
		topicPortionHm.put(preTopicNum, total);
		
		BufferedReader bfTopic = new BufferedReader(new FileReader(new File(args[0])));
		line = "";
		HashMap<String,Double> tokenValueHm = new HashMap<String,Double>();
		preTopicNum = bfTopic.readLine();
		int topicCount = 0;
		while((line=bfTopic.readLine())!=null){
			if(line.startsWith("Topic")){
				String topicNum = line;
				topicMap.put(preTopicNum, tokenValueHm);
				preTopicNum = topicNum;
				tokenValueHm = new HashMap<String,Double>();
				topicCount++;
			}else{
				if(line.trim()==null||line.contains("unitok")){
					continue;
				}
				String[] tokenProb = line.trim().split(" ");
				double normalizedPortion = Double.parseDouble(tokenProb[1])/topicPortionHm.get(preTopicNum);
				//System.out.println("normalizedPortion: "+normalizedPortion+" preTopicNum: "+preTopicNum);
				tokenValueHm.put(tokenProb[0], normalizedPortion);
			}
		}
		topicMap.put(preTopicNum, tokenValueHm);
		PrintWriter pwSampleDoc = null;
		File testFile = new File("test");
		for(Map.Entry<String, HashMap<String,Double>> entry : topicMap.entrySet()){
			String topicNum = entry.getKey();
			HashMap<String,Double> topicPortHm = entry.getValue();
			System.out.println(topicNum);
			topicNum = topicNum.split(" ")[1];
			topicNum = topicNum.substring(0,topicNum.indexOf("th"));
			if(topicNum.contains("Topic 10th:")){
				System.out.println("start to debug:");
			}
			pwSampleDoc = new PrintWriter(new FileWriter(new File(testFile,topicNum+".txt")));
			for(Map.Entry<String, Double> topicPortEntry : topicPortHm.entrySet()){
				String word = topicPortEntry.getKey();
				double portion = topicPortEntry.getValue();
				for(int i=0;i<portion*1000;i++){
					pwSampleDoc.print(word+" ");
				}
			}
			pwSampleDoc.println();
			pwSampleDoc.flush();
			pwSampleDoc.close();
		}

	}
}
