package jgibblda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public class ComputeMAP {
	
	/**
	 * Read qrels file to HashMap<String, List<String>>. Key stores query ID, value stores relevant document ID.
	 * The qrels file follows TREC format, which contains three-way judgments 0:  not relevant 1:  partially relevant 2:  relevant
	 * @param qrelsFileName
	 * @throws IOException 
	 */
	public HashMap<String, List<String>> readQrels(String qrelsFileName) throws IOException{
		HashMap<String, List<String>> qrels = new HashMap<String, List<String>>();
		BufferedReader bfDocList = new BufferedReader(new FileReader(qrelsFileName));
		String line = "";
		List<String> relDocID = new ArrayList<String>();
		while((line=bfDocList.readLine())!=null){
			String[] doc = line.split("\\s+");
			String queryID = doc[0];
			//only for relevant docs
			if(doc[3].equals("2")){
				if(qrels.containsKey(queryID)){
					relDocID.add(doc[2]);
				}else{
					//reallocate memory, .clear() will vanish everything stored in HashMap
					relDocID = new ArrayList<String>();
					relDocID.add(doc[2]);					
				}
				qrels.put(queryID, relDocID);
			}
		}
		bfDocList.close();
		return qrels;
	}
	
	/**
	 * Read result file to HashMap<String, LinkedHashMap<String, Double>>. Key stores query ID, value stores relevant document ID and ranking score.
	 * The qrels file follows TREC format, which contains six columns. Assume that documents are ranked according to the scores.
	 * @param resultFileName
	 * @throws IOException 
	 */
	public HashMap<String, LinkedHashMap<String, Double>> readResults(String resultFileName) throws IOException {
		HashMap<String, LinkedHashMap<String, Double>> res = new HashMap<String, LinkedHashMap<String, Double>>();
		BufferedReader bfDocList = new BufferedReader(new FileReader(resultFileName));
		String line = "";
		LinkedHashMap<String, Double> relDoc = new LinkedHashMap<String, Double>();
		while((line=bfDocList.readLine())!=null){
			String[] doc = line.split("\\s+");
			String queryID = doc[0];
			//only for relevant docs
			if(res.containsKey(queryID)){
				relDoc.put(doc[2], Double.valueOf(doc[4]));
			}else{
				//reallocate memory, .clear() will vanish everything stored in HashMap
				relDoc = new LinkedHashMap<String, Double>();
				relDoc.put(doc[2], Double.valueOf(doc[4]));					
			}
			res.put(queryID, relDoc);
		}
		bfDocList.close();
		return res;
	}
	
	/**
	 * Compute Average Precision for each query.
	 * @param qrels, res
	 */
	public List<Double> compAveragePrecision(HashMap<String, List<String>> qrels, HashMap<String, LinkedHashMap<String, Double>> res){
		List<Double> aPrecision = new ArrayList<Double>();
		List<String> qIte = new ArrayList<String>();
		HashMap<String, Double> dIte = new HashMap<String, Double>();
		for(Map.Entry<String, List<String>> entry: qrels.entrySet()){
			double ptemp = 0;
			int cntq = 0, cntd = 0;
			String queryID = entry.getKey();
			qIte = entry.getValue();
			dIte = res.get(queryID);
			if(dIte != null && !dIte.isEmpty()){
				for(Map.Entry<String, Double> entry1:dIte.entrySet()){
				cntd++;
				if(qIte.contains(entry1.getKey())){
					cntq++;
					ptemp = ptemp + (Double.valueOf(cntq)/Double.valueOf(cntd));
					}
				}
				
				if(cntq!=0)
					ptemp /= cntq;
				else
					ptemp = 0;
				aPrecision.add(ptemp);
			}
		}
		if(aPrecision == null || aPrecision.isEmpty())
			aPrecision.add(0.0);
		return aPrecision;
	}
	
	/**
	 * Compute Mean Average Precision for a query set.
	 * @param aP
	 */
	public Double compMeanAveragePrecision(List<Double> aP){
		OptionalDouble average = aP
	            .stream()
	            .mapToDouble(a -> a)
	            .average();
		return average.isPresent() ? average.getAsDouble() : 0;
	}
	
	public static Double ComputeMAPFn(String qrelFileName, String resultFileName) throws IOException{
		ComputeMAP commap = new ComputeMAP();
		HashMap<String, List<String>> qrels = commap.readQrels(qrelFileName);
		HashMap<String, LinkedHashMap<String, Double>> res = commap.readResults(resultFileName);
		List<Double> aPrecision = commap.compAveragePrecision(qrels, res);
		Double mAP = commap.compMeanAveragePrecision(aPrecision);
		return mAP;
	}

	public static void main(String[] args) throws IOException {
		args = new String[7];
		args[0] = "eval/ichi15_qrels.txt";
		args[1] = "eval/ICHI2015_results.txt";
		ComputeMAP commap = new ComputeMAP();
		HashMap<String, List<String>> qrels = commap.readQrels(args[0]);
		HashMap<String, LinkedHashMap<String, Double>> res = commap.readResults(args[1]);
		List<Double> aPrecision = commap.compAveragePrecision(qrels, res);
		Double mAP = commap.compMeanAveragePrecision(aPrecision);
		System.out.println(mAP.doubleValue());
	}

}
