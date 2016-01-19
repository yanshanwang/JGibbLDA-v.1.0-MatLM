package jgibblda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PostLDAProcessor {
	
	private static double beta = 0.01;

	public static void calTopicProb(String betaFile, String outputDir) throws IOException{
		PrintWriter pwTopicProb = new PrintWriter(new FileWriter(new File(outputDir)));
		BufferedReader bfBeta = new BufferedReader(new FileReader(new File(betaFile)));
		String line = "";
		HashMap<Integer,Double> topicProbSumHm = new HashMap<Integer,Double>();
		int wordNum = 0, topicNum = 1;
		String [] lineArr=null;
		while((line=bfBeta.readLine())!=null){
			lineArr = line.split("\\s+");
			double topicProb = 0;
			for(int i=0;i<lineArr.length;i++){
				double ithProb = Double.valueOf(lineArr[i]);
				topicProb+=ithProb+beta;
			}
			topicProbSumHm.put(topicNum,topicProb);
			topicNum++;
		}
		if(lineArr!=null){
			wordNum = lineArr.length;
		}else{
			System.out.println("something is wrong with the file reading.");
		}
		
		double sumAll = 0;
		for(int i=1;i<topicNum;i++){
			System.out.println("topicNum: "+i);
			System.out.println("topicProbSumHm: "+topicProbSumHm.get(i));
			double topicProbSum = topicProbSumHm.get(i);
			sumAll += topicProbSum+beta;
		}
		pwTopicProb.println("TOPIC,RATIO");
		for(int i=1;i<topicNum;i++){
			
			double topicProb = topicProbSumHm.get(i)/sumAll; 
			pwTopicProb.println(topicNum+","+topicProb);
		}
		pwTopicProb.flush();
		pwTopicProb.close();
	}
	
	public static void calWordProb(String betaFile) throws IOException{
		BufferedReader bfBeta = new BufferedReader(new FileReader(new File(betaFile)));
		String line = "";
		HashMap<Integer,Double> wordProbSumHm = new HashMap<Integer,Double>();
		int wordNum = 0, topicNum = 0;
		String [] lineArr=null;
		while((line=bfBeta.readLine())!=null){
			lineArr = line.split("\\s+");
			
			for(int i=0;i<lineArr.length;i++){
				double ithProb = Double.valueOf(lineArr[i]);
				if(wordProbSumHm.containsKey(i)){
					double sumProb = wordProbSumHm.get(i)+ithProb;
					wordProbSumHm.put(i, sumProb);
				}else{
					wordProbSumHm.put(i, ithProb);
				}
			}
			topicNum++;
		}
		if(lineArr!=null){
			wordNum = lineArr.length;
		}else{
			System.out.println("something is wrong with the file reading.");
		}
		
		double sumAll = 0;
		for(int i=0;i<wordNum;i++){
			double wordProbSum = wordProbSumHm.get(i);
			sumAll += wordProbSum+beta;
		}
		
		for(int i=0;i<wordNum;i++){
			double wordProbSum = wordProbSumHm.get(i);
			
		}
		
		for(Map.Entry<Integer, Double> wordProbSumEntry : wordProbSumHm.entrySet()){
			int ithWord = wordProbSumEntry.getKey();
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		calTopicProb(args[0],args[1]);
	}
}
