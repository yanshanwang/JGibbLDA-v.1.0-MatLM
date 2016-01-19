/*
 * Copyright (C) 2007 by
 * 
 * 	Xuan-Hieu Phan
 *	hieuxuan@ecei.tohoku.ac.jp or pxhieu@gmail.com
 * 	Graduate School of Information Sciences
 * 	Tohoku University
 * 
 *  Cam-Tu Nguyen
 *  ncamtu@gmail.com
 *  College of Technology
 *  Vietnam National University, Hanoi
 *
 * JGibbsLDA is a free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * JGibbsLDA is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGibbsLDA; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package jgibblda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

public class Model {	
	
	//---------------------------------------------------------------
	//	Class Variables
	//---------------------------------------------------------------
	
	public static String tassignSuffix;	//suffix for topic assignment file
	public static String thetaSuffix;		//suffix for theta (topic - document distribution) file
	public static String phiSuffix;		//suffix for phi file (topic - word distribution) file
	public static String topicSuffix; //suffix for topic proportion
	public static String othersSuffix; 	//suffix for containing other parameters
	public static String twordsSuffix;		//suffix for file containing words-per-topics
	public String source;
	public String inputdir;
	public String inputFormat;
	//---------------------------------------------------------------
	//	Model Parameters and Variables
	//---------------------------------------------------------------
	
	public String wordMapFile; 		//file that contain word to id map
	public String trainlogFile; 	//training log file	
	
	public String dfile;
	public String modelName;
	public String modelDir;
	public int modelStatus; 		//see Constants class for status of model
	public LDADataset data;			// link to a dataset
	
	public int M; //dataset size (i.e., number of docs)
	public int V; //vocabulary size
	public int K; //number of topics
	public double alpha, beta; //LDA  hyperparameters
	public int niters; //number of Gibbs sampling iteration
	public int liter; //the iteration at which the model was saved	
	public int savestep; //saving period
	public int twords; //print out top words per each topic
	public int withrawdata;
	
	// Estimated/Inferenced parameters
	public double [][] theta; //theta: document - topic distributions, size M x K
	public double [][] phi; // phi: topic-word distributions, size K x V
	
	// Temp variables while sampling
	public Vector<Integer> [] z; //topic assignments for words, size M x doc.size()
	protected int [][] nw; //nw[i][j]: number of instances of word/term i assigned to topic j, size V x K
	protected int [][] nd; //nd[i][j]: number of words in document i assigned to topic j, size M x K
	protected int [] nwsum; //nwsum[j]: total number of words assigned to topic j, size K
	protected int [] ndsum; //ndsum[i]: total number of words in document i, size M
	
	// temp variables for sampling
	protected double [] p; 
	SortedMap<Integer,SortedMap<Integer,Double>> sortedPhiHm;
	List<String> fielNameList;
	
	//---------------------------------------------------------------
	//	Constructors
	//---------------------------------------------------------------	

	public Model(){
		setDefaultValues();	
		sortedPhiHm = new TreeMap<Integer,SortedMap<Integer,Double>>();
		fielNameList = new ArrayList<String>();
	}
	
	/**
	 * Set default values for variables
	 */
	public void setDefaultValues(){
		wordMapFile = "wordmap.txt";
		trainlogFile = "trainlog.txt";
		//this file is to store topic assignments to each word. Namely, the file is a matrix where row is document
		//column is the topic. The value is word/topic pair, which word is assigned which topic 
		tassignSuffix = ".tassign";
		//this file is a matrix where row is the document and column is the topic. The value is the topic distribution for a document. 
		//Namely, the theta vector of each document. It keeps updating.
		thetaSuffix = ".theta"; 
		inputdir = "./";
		//this file is a matrix where row is the topic and column is the word. The value is the topic distribution for each word.  
		phiSuffix = ".phi";
		topicSuffix = ".tratio";
		othersSuffix = ".others"; //basic information for hyperparameter, document number, vacabulary topic number and so on.
		twordsSuffix = ".twords"; //top words for each topic
		source = "txt"; //the default
		inputdir = "./";
		dfile = "trndocs.dat";
		modelName = "model-final";
		modelDir = "models";
		inputFormat = "";
		modelStatus = Constants.MODEL_STATUS_UNKNOWN;		
		
		M = 0;
		V = 0;
		K = 100;
		alpha = 50.0 / K;
		beta = 0.1;
		niters = 2000;
		liter = 0;
		
		z = null;
		nw = null;
		nd = null;
		nwsum = null;
		ndsum = null;
		theta = null;
		phi = null;
	}
	
	//---------------------------------------------------------------
	//	I/O Methods
	//---------------------------------------------------------------
	/**
	 * read other file to get parameters
	 */
	protected boolean readOthersFile(String otherFile){
		//open file <model>.others to read:
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(otherFile));
			String line;
			while((line = reader.readLine()) != null){
				StringTokenizer tknr = new StringTokenizer(line,"= \t\r\n");
				
				int count = tknr.countTokens();
				if (count != 2)
					continue;
				
				String optstr = tknr.nextToken();
				String optval = tknr.nextToken();
				
				if (optstr.equalsIgnoreCase("alpha")){
					alpha = Double.parseDouble(optval);					
				}
				else if (optstr.equalsIgnoreCase("beta")){
					beta = Double.parseDouble(optval);
				}
				else if (optstr.equalsIgnoreCase("ntopics")){
					K = Integer.parseInt(optval);
				}
				else if (optstr.equalsIgnoreCase("liter")){
					liter = Integer.parseInt(optval);
				}
				else if (optstr.equalsIgnoreCase("nwords")){
					V = Integer.parseInt(optval);
				}
				else if (optstr.equalsIgnoreCase("ndocs")){
					M = Integer.parseInt(optval);
				}
				else {
					// any more?
				}
			}
			
			reader.close();
		}
		catch (Exception e){
			System.out.println("Error while reading other file:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected boolean readTAssignFile(String tassignFile){
		try {
			int i,j;
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(tassignFile), "UTF-8"));
			
			String line;
			z = new Vector[M];			
			data = new LDADataset(M);
			data.V = V;			
			for (i = 0; i < M; i++){
				line = reader.readLine();
				StringTokenizer tknr = new StringTokenizer(line, " \t\r\n");
				
				int length = tknr.countTokens();
				
				Vector<Integer> words = new Vector<Integer>();
				Vector<Integer> topics = new Vector<Integer>();
				
				for (j = 0; j < length; j++){
					String token = tknr.nextToken();
					
					StringTokenizer tknr2 = new StringTokenizer(token, ":");
					if (tknr2.countTokens() != 2){
						System.out.println("Invalid word-topic assignment line\n");
						return false;
					}
					
					words.add(Integer.parseInt(tknr2.nextToken()));
					topics.add(Integer.parseInt(tknr2.nextToken()));
				}//end for each topic assignment
				
				//allocate and add new document to the corpus
				Document doc = new Document(words);
				data.setDoc(doc, i);
				
				//assign values for z
				z[i] = new Vector<Integer>();
				for (j = 0; j < topics.size(); j++){
					z[i].add(topics.get(j));
				}
				
			}//end for each doc
			
			reader.close();
		}
		catch (Exception e){
			System.out.println("Error while loading model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * load saved model
	 */
	public boolean loadModel(){
		String loadDirStr = this.modelDir+File.separator+modelName+"_alpha="+alpha+"_beta="+beta+"_k="+K;
		if (!readOthersFile(loadDirStr+othersSuffix))
			return false;
		
		if (!readTAssignFile(loadDirStr+tassignSuffix))
			return false;
		
		// read dictionary
		Dictionary dict = new Dictionary();
		if (!dict.readWordMap(modelDir + File.separator + wordMapFile))
			return false;
			
		data.localDict = dict;
		
		return true;
	}
	
	/**
	 * Save word-topic assignments for this model
	 */
	public boolean saveModelTAssign(String filename,LDACmdOption option){
		int i, j;
		
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			//write docs with topic assignments for words
			LinkedHashMap<Integer,Integer> globalTAssignMap = new LinkedHashMap<Integer,Integer>();
			for (i = 0; i < data.M; i++){
				for (j = 0; j < data.docs[i].length; ++j){
					if(option.inf){
						String word = this.data.localDict.id2word.get(data.docs[i].words[j]);
						int globalId = this.data.lid2gid.get(data.docs[i].words[j]);
						globalTAssignMap.put(globalId, z[i].get(j));
					}else if(option.est || option.estc){
						writer.write(data.docs[i].words[j] + ":" + z[i].get(j) + " ");
					}					
				}
				if(option.est || option.estc){
					writer.write("\n");
				}
				
				if(option.inf){
					for(Map.Entry<Integer, Integer> entry : globalTAssignMap.entrySet()){
						int wordInd = entry.getKey();
						int topic = entry.getValue();
						writer.write(wordInd+":"+topic+" ");
					}
					writer.write("\n");
				}
				
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving model tassign: " + e.getMessage());
			e.printStackTrace();
			return false;
		}	
		return true;
	}
	
	
	/**
	 * 
	 * @param inputFileStr
	 * @param labelMapFileStr
	 * @param dict
	 * @throws IOException 
	 */
	private LDADataset readJsonTrainingData(String inputFileStr) throws IOException{
		String[] sourceArr = source.split(":");
		String sourceType = sourceArr[0] ;
		//in order to save time, I would read higher category files from mesh
		//and map them to the text with their pmids.
		String[] args = new String[3];
		args[0] = inputFileStr;
		args[1] = sourceType;
		args[2] = inputFormat;
		//the following method is to extract the higher level label on the fly. 
		//it is too slow for big data. I decide to do it on the fly.
//		String[] args = new String[6];
//		args[0] = inputFileStr;
//		args[1] = sourceType;
//		args[2] = inputFormat;
//		//in bioask, only two files are involved
//		String[] mapFileNames = this.labelMapFiles.split(":");
//		args[3] = inputdir+File.separator+mapFileNames[0];
//		args[4] = inputdir+File.separator+mapFileNames[1];
//		args[5] = level;
		LDADataset data = LDADataset.readJsonDataSet(args);
		//this.readJsonData(inputFileStr, labelMapFileStr);
		return data;
	}
	
	
	/**
	 * 
	 * @param inputFileStr
	 * @param labelMapFileStr
	 * @param dict
	 * @throws IOException 
	 */
	private LDADataset readJsonTestingData(String inputFileStr,Dictionary dict) throws IOException{
		String[] sourceArr = source.split(":");
		String sourceType = sourceArr[0];
		//in order to save time, I would read higher category files from mesh
		//and map them to the text with their pmids.
		String[] args = new String[3];
		args[0] = inputFileStr;
		args[1] = sourceType;
		args[2] = inputFormat;
		//args[3] = labelMapFileStr;
		LDADataset data = LDADataset.readJsonDataSet(args, dict);
		//this.readJsonData(inputFileStr, "");
		return data;
	}
	
	/**
	 * Save theta (topic distribution) for this model
	 */
	public boolean saveModelTheta(String filename){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < M; i++){
				if((source.contains("i2b2") && source.contains("json"))){
					writer.write(data.headerInforList[i]+" "+data.docLabels[i]+" ");
					//writer.write(data.docLabels[i].trim()+"\t");
				}else if(source.contains("headerIncluded")){
					writer.write(data.headerInforList[i]+" ");
				}
				for (int j = 0; j < K; j++){
					writer.write(theta[i][j] + "\t");
				}
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving topic distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean saveModelTopicProb(String filename){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for (int j = 0; j < K; j++){
				writer.write("T"+j+","+this.p[j] + "\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving topic distribution file for this model: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Save word-topic distribution
	 */
	
	public boolean saveModelPhi(String filename,LDACmdOption option){
		try {//V=29364
			//System.out.println("global dict size: "+this.data.globalDict.id2word.size()+" local dict size: "+data.localDict.id2word.size());
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < K; i++){
				SortedMap<Integer,Double> globalPhiMap = new TreeMap<Integer,Double>();
				for (int j = 0; j < V; j++){
					if(option.inf){
						int globalId = this.data.lid2gid.get(j);
						globalPhiMap.put(globalId, phi[i][j]);
						String word = this.data.globalDict.id2word.get(globalId);
//						writer.write(word+":"+phi[i][j] + " ");
						writer.write(phi[i][j] + " ");
					}else if(option.est || option.estc){
						String word = this.data.localDict.id2word.get(j);
//						writer.write(word+":"+phi[i][j] + " ");
						writer.write(phi[i][j] + " ");
					}
				}
				sortedPhiHm.put(i, globalPhiMap);
				writer.write("\n");
			}
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving word-topic distribution:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		if(option.inf){
			try {
				BufferedWriter globalWriter = new BufferedWriter(new FileWriter(filename+".global"));
				int globalV = this.data.globalDict.id2word.size();
				for(int i=0;i<K;i++){
					SortedMap<Integer,Double> globalPhiMap = sortedPhiHm.get(i);
					for(int j=0;j<globalV;j++){
						if(globalPhiMap.containsKey(j)){
							globalWriter.write(globalPhiMap.get(j)+" ");
						}else{
							globalWriter.write(0+" ");
						}
					}
					globalWriter.newLine();
				}
				globalWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

		}
		return true;
	}
	
	/**
	 * Save other information of this model
	 */
	public boolean saveModelOthers(String filename){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			writer.write("alpha=" + alpha + "\n");
			writer.write("beta=" + beta + "\n");
			writer.write("ntopics=" + K + "\n");
			writer.write("ndocs=" + M + "\n");
			writer.write("nwords=" + V + "\n");
			writer.write("liters=" + liter + "\n");
			
			writer.close();
		}
		catch(Exception e){
			System.out.println("Error while saving model others:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Save model the most likely words for each topic
	 */
	public boolean saveModelTwords(String filename){
		try{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			
			if (twords > V){
				twords = V;
			}
			
			for (int k = 0; k < K; k++){
				List<Pair<Integer,Double>> wordsProbsList = new ArrayList<Pair<Integer,Double>>(); 
				for (int w = 0; w < V; w++){
					//Pair<Integer,Double> p = new Pair(w, phi[k][w], false);
					Pair<Integer,Double> p = new Pair<Integer,Double>(w, phi[k][w]);
					wordsProbsList.add(p);
				}//end foreach word
				
				//print topic				
				writer.write("Topic " + k + "th:\n");
				Collections.sort(wordsProbsList);
				
				for (int i = 0; i < twords; i++){
					if (data.localDict.contains((Integer)wordsProbsList.get(i).first)){
						String word = data.localDict.getWord((Integer)wordsProbsList.get(i).first);
						
						writer.write("\t" + word + " " + wordsProbsList.get(i).second + "\n");
					}
				}
			} //end foreach topic			
						
			writer.close();
		}
		catch(Exception e){
			System.out.println("Error while saving model twords: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Save model
	 */
	public boolean saveModel(String modelName,LDACmdOption option){
		System.out.println(this.modelDir+" "+modelName);
		File modelDirFile = new File(this.modelDir);
		if(!modelDirFile.exists()){
			modelDirFile.mkdir();
		}
		String saveStr = this.modelDir+File.separator+modelName+"_alpha="+option.alpha+"_beta="+option.beta+"_k="+K;
		if (!saveModelTAssign(saveStr+tassignSuffix,option)){
			return false;
		}
		
		if (!saveModelOthers(saveStr+othersSuffix)){			
			return false;
		}
		
		if (!saveModelTheta(saveStr+thetaSuffix)){
			return false;
		}
		
		if (!saveModelPhi(saveStr+phiSuffix,option)){
			return false;
		}
		
		if(!this.saveModelTopicProb(saveStr+topicSuffix)){
			return false;
		}
		
		if (twords > 0){
			if (!saveModelTwords(saveStr+twordsSuffix))
				return false;
		}
		return true;
	}
	
	//---------------------------------------------------------------
	//	Init Methods
	//---------------------------------------------------------------
	/**
	 * initialize the model
	 * @throws IOException 
	 */
	protected boolean init(LDACmdOption option) throws IOException{		
		if (option == null)
			return false;
		
		inputdir = option.inputdir;
		if (inputdir.endsWith(File.separator))
			inputdir = inputdir.substring(0, inputdir.length() - 1);
		
		modelName = option.modelName;
		K = option.K;
		
		alpha = option.alpha;
		if (alpha < 0.0)
			alpha = 50.0 / K;
		
		if (option.beta >= 0)
			beta = option.beta;
		
		niters = option.niters;
		inputFormat = option.inputFormat;
		inputdir = option.inputdir;
		source = option.source;
		if (inputdir.endsWith(File.separator))
			inputdir = inputdir.substring(0, inputdir.length() - 1);
		
		this.modelDir = option.modelDir;
		this.modelName = option.modelName;
		dfile = option.dfile;
		twords = option.twords;
		wordMapFile = option.wordMapFileName;
		//data = new LDADataset();
		return true;
	}
	
	/**
	 * Init parameters for estimation
	 * @throws IOException 
	 */
	public boolean initNewModel(LDACmdOption option) throws IOException{
		if (!init(option))
			return false;

		String inputFileStr = inputdir+File.separator+dfile;
		if(!source.contains("txt")){
			//Dictionary dict = new Dictionary();
			if(source.contains("json")){
				data = this.readJsonTrainingData(inputFileStr);
			}
			
		}else{
			//the first refers to we are reading documents from a directory under which there are many files. 
			if(source.contains("directory")){
				//read a directory and extract all texts.
				//then print them out into inputFileStr 
				//and then read them back with the format needed by LDA.
				data = new LDADataset();
				String[] sourceArr = source.split(":");
				String inputDirectory=sourceArr[1];
				String suffix = sourceArr[2];
				HashMap<String,List<String>> fileTokenListHm=FileProcessor.readFiles2StrList(inputDirectory, suffix, data.stopList);
				PrintWriter pwInputFile = new PrintWriter(new FileWriter(new File(inputFileStr)));
				//the size of the hash is the size of documents in I2B2 txt directory
				pwInputFile.println(fileTokenListHm.size());
				for(Map.Entry<String, List<String>> fileEntry : fileTokenListHm.entrySet()){
					String fileName = fileEntry.getKey();
					this.fielNameList.add(fileName);
					List<String> fileList = fileEntry.getValue();
					for(int i=0;i<fileList.size();i++){
						pwInputFile.print(fileList.get(i)+" ");
					}
					pwInputFile.println();
				}
				pwInputFile.flush();
				pwInputFile.close();
				//for now, we suppose the headIncluded is false for source includes directory
				data = LDADataset.readDataSet(inputFileStr, false);
			}
			//suppose that only one file in which each single file is written on one line with the first is the file name, which
			//is read as headerInfor.
			else if(source.contains("headerIncluded")){
				data = LDADataset.readDataSet(inputFileStr,true);
			}
			
		}
		
		int m, n, w, k;		
		p = new double[K];		
		
		if (data == null){
			System.out.println("Fail to read training data!\n");
			return false;
		}
		
		//+ allocate memory and assign values for variables		
		M = data.M;
		V = data.V;
		inputdir = option.inputdir;
		savestep = option.savestep;
		
		// K: from command line or default value
	    // alpha, beta: from command line or default values
	    // niters, savestep: from command line or default values

		nw = new int[V][K];
		for (w = 0; w < V; w++){
			for (k = 0; k < K; k++){
				nw[w][k] = 0;
			}
		}
		
		nd = new int[M][K];
		for (m = 0; m < M; m++){
			for (k = 0; k < K; k++){
				nd[m][k] = 0;
			}
		}
		
		nwsum = new int[K];
		for (k = 0; k < K; k++){
			nwsum[k] = 0;
		}
		
		ndsum = new int[M];
		for (m = 0; m < M; m++){
			ndsum[m] = 0;
		}
		
		z = new Vector[M];
		for (m = 0; m < data.M; m++){
			int N = data.docs[m].length;
			z[m] = new Vector<Integer>();
			
			//initilize for z
			for (n = 0; n < N; n++){
				int topic = (int)Math.floor(Math.random() * K);
				z[m].add(topic);
				
				// number of instances of word assigned to topic j
				nw[data.docs[m].words[n]][topic] += 1;
				// number of words in document i assigned to topic j
				nd[m][topic] += 1;
				// total number of words assigned to topic j
				nwsum[topic] += 1;
			}
			// total number of words in document i
			ndsum[m] = N;
		}
		
		theta = new double[M][K];		
		phi = new double[K][V];
		
		return true;
	}
	
	/**
	 * Init parameters for inference
	 * @param newData DataSet for which we do inference
	 * @throws IOException 
	 */
	public boolean initNewModel(LDACmdOption option, LDADataset newData, Model trnModel) throws IOException{
		if (!init(option))
			return false;
		
		int m, n, w, k;
		
		K = trnModel.K;
		alpha = trnModel.alpha;
		beta = trnModel.beta;		
		
		p = new double[K];
		System.out.println("K:" + K);
		
		data = newData;
		
		//+ allocate memory and assign values for variables		
		M = data.M;
		V = data.V;
		inputdir = option.inputdir;
		savestep = option.savestep;
		System.out.println("M:" + M);
		System.out.println("V:" + V);
		
		// K: from command line or default value
	    // alpha, beta: from command line or default values
	    // niters, savestep: from command line or default values

		nw = new int[V][K];
		for (w = 0; w < V; w++){
			for (k = 0; k < K; k++){
				nw[w][k] = 0;
			}
		}
		
		nd = new int[M][K];
		for (m = 0; m < M; m++){
			for (k = 0; k < K; k++){
				nd[m][k] = 0;
			}
		}
		
		nwsum = new int[K];
		for (k = 0; k < K; k++){
			nwsum[k] = 0;
		}
		
		ndsum = new int[M];
		for (m = 0; m < M; m++){
			ndsum[m] = 0;
		}
		
		z = new Vector[M];
		for (m = 0; m < data.M; m++){
			int N = data.docs[m].length;
			z[m] = new Vector<Integer>();
			
			//initilize for z
			for (n = 0; n < N; n++){
				int topic = (int)Math.floor(Math.random() * K);
				z[m].add(topic);
				
				// number of instances of word assigned to topic j
				nw[data.docs[m].words[n]][topic] += 1;
				// number of words in document i assigned to topic j
				nd[m][topic] += 1;
				// total number of words assigned to topic j
				nwsum[topic] += 1;
			}
			// total number of words in document i
			ndsum[m] = N;
		}
		
		theta = new double[M][K];		
		phi = new double[K][V];
		
		return true;
	}
	
	/**
	 * Init parameters for inference
	 * reading new dataset from file
	 * @throws IOException 
	 */
	public boolean initNewModel(LDACmdOption option, Model trnModel) throws IOException{
		if (!init(option))
			return false;
		
		String inputFileStr = inputdir+File.separator+dfile;
		LDADataset dataset = null;
		if(!source.contains("txt")){
			//Dictionary dict = new Dictionary();
			if(source.contains("json")){
				//dataset = this.readJsonTrainingData(inputFileStr);
				dataset = this.readJsonTestingData(inputFileStr,trnModel.data.localDict);
			}
			
		}else{
			if(source.contains("directory")){
				//read a directory and extract all texts.
				//then print them out into inputFileStr 
				//and then read them back with the format needed by LDA.
				//data = new LDADataset();
				dataset = new LDADataset();
				String[] sourceArr = source.split(":");
				String inputDirectory=sourceArr[1];
				String suffix = sourceArr[2];
				HashMap<String,List<String>> fileTokenListHm=FileProcessor.readFiles2StrList(inputDirectory, suffix, dataset.stopList);
				PrintWriter pwInputFile = new PrintWriter(new FileWriter(new File(inputFileStr)));
				//the size of the hash is the size of documents in I2B2 txt directory
				pwInputFile.println(fileTokenListHm.size());
				for(Map.Entry<String, List<String>> fileEntry : fileTokenListHm.entrySet()){
					String fileName = fileEntry.getKey();
					this.fielNameList.add(fileName);
					List<String> fileList = fileEntry.getValue();
					for(int i=0;i<fileList.size();i++){
						pwInputFile.print(fileList.get(i)+" ");
					}
					pwInputFile.println();
				}
				pwInputFile.flush();
				pwInputFile.close();
				dataset = LDADataset.readDataSet(inputFileStr,trnModel.data.localDict,false);
			}else if(source.contains("headerIncluded")){
				dataset = LDADataset.readDataSet(inputFileStr,trnModel.data.localDict,true);
			}
		}
	
		if (dataset == null){
			System.out.println("Fail to read dataset!\n");
			return false;
		}
		
		return initNewModel(option, dataset , trnModel);
	}
	
	/**
	 * init parameter for continue estimating or for later inference
	 * @throws IOException 
	 */
	public boolean initEstimatedModel(LDACmdOption option) throws IOException{
		if (!init(option))
			return false;
		
		int m, n, w, k;
		
		p = new double[K];
		
		// load model, i.e., read z and trndata
		if (!loadModel()){
			System.out.println("Fail to load word-topic assignment file of the model!\n");
			return false;
		}
		
		System.out.println("Model loaded:");
		System.out.println("\talpha:" + alpha);
		System.out.println("\tbeta:" + beta);
		System.out.println("\tM:" + M);
		System.out.println("\tV:" + V);		
		
		nw = new int[V][K];
		for (w = 0; w < V; w++){
			for (k = 0; k < K; k++){
				nw[w][k] = 0;
			}
		}
		
		nd = new int[M][K];
		for (m = 0; m < M; m++){
			for (k = 0; k < K; k++){
				nd[m][k] = 0;
			}
		}
		
		nwsum = new int[K];
	    for (k = 0; k < K; k++) {
		nwsum[k] = 0;
	    }
	    
	    ndsum = new int[M];
	    for (m = 0; m < M; m++) {
		ndsum[m] = 0;
	    }
	    
	    for (m = 0; m < data.M; m++){
	    	int N = data.docs[m].length;
	    	
	    	// assign values for nw, nd, nwsum, and ndsum
	    	for (n = 0; n < N; n++){
	    		w = data.docs[m].words[n];
	    		int topic = (Integer)z[m].get(n);
	    		
	    		// number of instances of word i assigned to topic j, a sparse matrix
	    		nw[w][topic] += 1;
	    		// number of words in document i assigned to topic j
	    		nd[m][topic] += 1;
	    		// total number of words assigned to topic j
	    		nwsum[topic] += 1;	    		
	    	}
	    	// total number of words in document i
	    	ndsum[m] = N;
	    }
	    
	    theta = new double[M][K];
	    phi = new double[K][V];
	    inputdir = option.inputdir;
		savestep = option.savestep;
	    
		return true;
	}
	
}
