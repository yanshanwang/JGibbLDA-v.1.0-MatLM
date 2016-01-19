package jgibblda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author m048100
 *
 */
public class Lda2IrModel {
	// $id2wordHash, $stop, $testfilenames, $finalphi, $finaltheta, $testOut
	private HashMap<String,String> word2idHm;
	private HashMap<String,Integer> stopHm ;
	private String testfileNamesDir;
	List<HashMap<Integer,HashMap<Integer,Double>>> nestedPhiHmList;
	List<HashMap<Integer,HashMap<Integer,Double>>> nestedThetaHmList ;
	HashMap<Integer,String> docId2pmidHm;
	int topicNum = 0;
	int docNum = 0;

	PrintWriter pwOut;

	public Lda2IrModel(String id2wordDir,String stopDir, String testfileNamesDir, List<String> ldaPhiDirList,List<String> ldaThetaDirList,String output) throws IOException{
		String delimiter = "\\s+";
		word2idHm = FileProcessor.readFileLines(id2wordDir, delimiter);
		stopHm = FileProcessor.readSpecialStopFile(stopDir);
		this.testfileNamesDir = testfileNamesDir;
		docId2pmidHm = new HashMap<Integer,String>();
		nestedPhiHmList = new ArrayList<HashMap<Integer,HashMap<Integer,Double>>>();
		nestedThetaHmList = new ArrayList<HashMap<Integer,HashMap<Integer,Double>>>();
		for(int i=0;i<ldaPhiDirList.size();i++){
			String phiFileDir = ldaPhiDirList.get(i);
			HashMap<Integer,HashMap<Integer,Double>> nestedPhiHm = FileProcessor.readPhiFile(phiFileDir);
			nestedPhiHmList.add(nestedPhiHm);
			if(topicNum==0){
				topicNum = nestedPhiHm.size();
			}
		}

		for(int i=0;i<ldaThetaDirList.size();i++){
			String thetaFileDir = ldaThetaDirList.get(i);
			System.out.println("thetaFileDir in the constructor of Lda2IrModel.java: "+thetaFileDir);
			HashMap<Integer,HashMap<Integer,Double>> nestedThetaHm = FileProcessor.readThetaFile(thetaFileDir, docId2pmidHm);
			nestedThetaHmList.add(nestedThetaHm);
			if(docNum==0){
				docNum = docId2pmidHm.size();
			}
		}
		pwOut=new PrintWriter(new FileWriter(new File(output)));
	}

	/**
	 * @throws IOException
	 */
	public HashMap<String,HashMap<Integer,Double>> buildLdaRelevance() throws IOException{
		File testFileNameFile = new File(this.testfileNamesDir);
		BufferedReader bfFileNameFile = new BufferedReader(new FileReader(testFileNameFile));
		String line = bfFileNameFile.readLine();
		File testFile = new File(testFileNameFile.getParent()+"/"+line);
		BufferedReader bfTestFile = new BufferedReader(new FileReader(testFile));
		
		long time = System.currentTimeMillis();
		HashMap<String,HashMap<Integer,Double>> probVDHash = new HashMap<String,HashMap<Integer,Double>>();
		HashMap<String,Double> probWDHm = new HashMap<String,Double>();
		HashMap<String,Integer> queryHm = new LinkedHashMap<String,Integer>();
		
		while((line=bfTestFile.readLine())!=null&&bfTestFile.readLine().length()!=1){
			String[] doc = line.split("\\s+|\\p{Punct}|�");
			String docC = "";
			String docID = "";
			if(doc.length!=1){
				docID =  doc[1];
				
				for(int i=2;i<doc.length;i++){
					if(!doc[i].isEmpty()&&!doc[i].equals(" ")&&doc[i].length()!=1)
						docC=docC+" "+doc[i];
				}
				if((line=bfTestFile.readLine())!=null){
					String[] docCont_temp = line.split("\\s+|\\p{Punct}|�");						
					for(int i=0;i<docCont_temp.length;i++){
						if(!docCont_temp[i].isEmpty()&&!docCont_temp[i].equals(" ")&&docCont_temp[i].length()!=1)
							docC=docC+" "+docCont_temp[i];
					}
				}
			}
			String re = "\\w+";
			Pattern pattern = Pattern.compile(re);
			probVDHash.clear();
			probWDHm.clear();
			queryHm.clear();
			//in order to handle duplicated query words, I read file firstly.
			Matcher matcher = pattern.matcher(docC);
			String[] testLineArr = docC.split("\\s+");
			while(matcher.find()){
				String matchStr = matcher.group().toLowerCase().trim().replaceAll("\\d", "");
				
				if(!matchStr.equals("")){
				if(stopHm.containsKey(matchStr)){
					continue;
				}
				if(queryHm.containsKey(matchStr)){
					int cnt = queryHm.get(matchStr)+1;
					queryHm.put(matchStr, cnt);
				}else{
					queryHm.put(matchStr, 1);
				}
				}
			}

			outerloop:			
				for(Map.Entry<String, Integer> queryEntry : queryHm.entrySet()){
					String matchStr = queryEntry.getKey();
					HashMap<Integer,Double> docRatioHm = new HashMap<Integer,Double>();
					for(int k=0;k<this.topicNum;k++){
						for(int d=0;d<this.docNum;d++){
							for(int ii=0;ii<this.nestedPhiHmList.size();ii++){
								HashMap<Integer,HashMap<Integer,Double>> nestedPhiHm = nestedPhiHmList.get(ii);
								HashMap<Integer,HashMap<Integer,Double>> nestedThetaHm = nestedThetaHmList.get(ii);
								HashMap<Integer,Double> thetaMap = nestedThetaHm.get(d); //theta is d by k
								HashMap<Integer,Double> wordMap = nestedPhiHm.get(k);//phi is k by w

								if(!word2idHm.containsKey(matchStr)){
									System.out.println("matchStr in buildLdaRelevance: "+matchStr);									
									continue outerloop;
								}
								//the following attempts to handle fuzzy search, I will return this later.
//								for(Map.Entry<String, String> word2idEntry : word2idHm.entrySet()){
//									String word = word2idEntry.getKey();
//									if(word.contains(matchStr)){
//										
//									}
//								}
								
								int matchStrInd = Integer.parseInt(this.word2idHm.get(matchStr));
								if(wordMap.containsKey(matchStrInd)){
									double wordPhiRatio = wordMap.get(matchStrInd);
									double topicThetaRatio = thetaMap.get(k);
									double prod = wordPhiRatio*topicThetaRatio;
									if(docRatioHm.containsKey(d)){
										double docProb = docRatioHm.get(d)+prod;
										docRatioHm.put(d, docProb);
									}else{
										docRatioHm.put(d, prod);
									}
								}
							}
						}
					}
					for(int d=0;d<this.docNum;d++){
						double finalDocProb = docRatioHm.get(d)/this.nestedPhiHmList.size();
						docRatioHm.put(d, finalDocProb);
					}	
					probVDHash.put(matchStr, docRatioHm);
				}

			this.calLdaLM(probVDHash,probWDHm);
			this.printResult(docID, probWDHm);
			System.out.println("time taken in ms = "
					+ (System.currentTimeMillis() - time));
		
			
		}
		
		bfTestFile.close();		
		bfFileNameFile.close();
		pwOut.flush();
		pwOut.close();
		
		return probVDHash;
	}
	

	/**
	 * 
	 * @param probVDHash
	 * @param probWDHm
	 */
	private void calLdaLM(HashMap<String,HashMap<Integer,Double>> probVDHash,HashMap<String,Double> probWDHm ){
		for(Map.Entry<String, HashMap<Integer,Double>> entry : probVDHash.entrySet()){
			String word = entry.getKey();
			HashMap<Integer,Double> docRatioHm = entry.getValue();
			Set<Integer> docSet = docRatioHm.keySet();
			List<Integer> docList = new ArrayList(docSet);
			Collections.sort(docList);
			for(int i=0;i<docList.size();i++){
				int docInd = docList.get(i);
				String docId = this.docId2pmidHm.get(docInd);
				double docRatio = docRatioHm.get(docInd);
				if(!probWDHm.containsKey(docId)){
					probWDHm.put(docId, Math.log10(docRatio));
				}else{
					double fullRatio = probWDHm.get(docId);
					fullRatio += Math.log10(docRatio);
					//fullRatio += docRatio;
					probWDHm.put(docId,fullRatio);
				}
			}
		}
	}

	/**
	 * <queryid> <the string "Q0"> <document id> <rank within topic> <score descending order> <string tag fora this run>
	 * 105 Q0 report51362 1 -4.83934542755196 textlda 
	 * @param filename
	 * @param probWDHm
	 */
	private void printResult(String filename,HashMap<String,Double> probWDHm){
		List<Double> valueList = new ArrayList<Double>(probWDHm.values());
		Collections.sort(valueList,Collections.reverseOrder());
		SortedMap<Integer,String> probWDRankHm = new TreeMap<Integer,String>();
		for(Map.Entry<String, Double> entry : probWDHm.entrySet()){
			String docId = entry.getKey();
			//int pmid = this.docId2pmidHm.get(docId);
			double prob = entry.getValue();
			int rank = valueList.indexOf(prob)+1; //ranks start from 1 rather than 0
			//since prob can be identical, therefore, we need to give those identical probs different ranks with the following method
			while(probWDRankHm.containsKey(rank)){
				rank = rank+1;
			}
			probWDRankHm.put(rank, docId);
		}
		//this.pwOut.println("filename\tQ0\tdocIdDir\trank\tprobWD\ttextlda");
		for(Map.Entry<Integer, String> entry : probWDRankHm.entrySet()){
			int rank = entry.getKey();
			///data5/bsi/nlp/s110067.sharp/ir/depfraglm-textspace-trecpittnlp/trecpittnlp/report57213.xml.text.txt
			String docIdDir = entry.getValue();
			System.out.println(docIdDir);
			if(docIdDir.contains(".xml")){
				String docId = docIdDir.substring(docIdDir.lastIndexOf("/")+7,docIdDir.indexOf(".xml"));
				double prob = probWDHm.get(docIdDir);
				this.pwOut.println(filename+"\tQ0\t"+docId+"\t"+rank+"\t"+prob+"\ttextlda");
			}else{
				double prob = probWDHm.get(docIdDir);
				this.pwOut.println(filename+"\tQ0\t"+docIdDir+"\t"+rank+"\t"+prob+"\ttextlda");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long time = System.currentTimeMillis();
		String id2wordDir = args[0];
		String stopDir = args[1]; 
		String testfileNamesDir = args[2];
		List<String> ldaPhiDirList = Arrays.asList(args[3].split("\\s+"));
		List<String> ldaThetaDirList = Arrays.asList(args[4].split("\\s+"));
		String output=args[5];
		Lda2IrModel lda2IrModel = new Lda2IrModel(id2wordDir,stopDir,testfileNamesDir,ldaPhiDirList,ldaThetaDirList,output);
		lda2IrModel.buildLdaRelevance();
		System.out.println("time taken in ms = "
				+ (System.currentTimeMillis() - time));
	}
}
