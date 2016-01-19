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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LDADataset {
	//---------------------------------------------------------------
	// Instance Variables
	//---------------------------------------------------------------

	public Dictionary localDict;			// local dictionary	
	public Document [] docs; 		// a list of documents	
	public int M; 			 		// number of documents
	public int V;			 		// number of words

	// map from local coordinates (id) to global ones 
	// null if the global dictionary is not set
	public Map<Integer, Integer> lid2gid;
	private boolean useStopList = true; 

	//link to a global dictionary (optional), null for train data, not null for test data
	public Dictionary globalDict;	 		
	public String[] headerInforList; //this is for publications with pmid.
	public String[] docLabels;
	public List<String> stopList;
	private static String regex = "(.*?)\\s+";
	//--------------------------------------------------------------
	// Constructor
	//--------------------------------------------------------------
	public LDADataset() throws IOException{
		localDict = new Dictionary();
		M = 0;
		V = 0;
		docs = null;

		globalDict = null;
		lid2gid = null;
		headerInforList = null;
		docLabels = null;
		stopList = FileProcessor.readFile2List(new File("resources/stoplist.txt"));
		//headerInforList = new String[M];
		//docLabels = new String[M];
	}

	public LDADataset(int M) throws IOException{
		localDict = new Dictionary();
		this.M = M;
		this.V = 0;
		docs = new Document[M];	
		stopList = FileProcessor.readFile2List(new File("resources/stoplist.txt"));
		globalDict = null;
		lid2gid = null;
		headerInforList = new String[M];
		docLabels = new String[M];
	}

	public LDADataset(int M, Dictionary globalDict) throws IOException{
		localDict = new Dictionary();	
		stopList = FileProcessor.readFile2List(new File("resources/stoplist.txt"));
		this.M = M;
		this.V = 0;
		docs = new Document[M];	

		this.globalDict = globalDict;
		lid2gid = new HashMap<Integer, Integer>();
		headerInforList = new String[M];
		docLabels = new String[M];
	}

	//-------------------------------------------------------------
	//Public Instance Methods
	//-------------------------------------------------------------
	/**
	 * set the document at the index idx if idx is greater than 0 and less than M
	 * @param doc document to be set
	 * @param idx index in the document array
	 */	
	public void setDoc(Document doc, int idx){
		if (0 <= idx && idx < M){
			docs[idx] = doc;
		}
	}
	/**
	 * set the document at the index idx if idx is greater than 0 and less than M
	 * @param str string contains doc
	 * @param idx index in the document array
	 */
	public void setDoc(String str, int idx){
		if (0 <= idx && idx < M){
			//String [] words = str.split("[ \\t\\n]");

			Vector<Integer> ids = new Vector<Integer>();
			String TOKEN_REGEX = "\\w+";
			Pattern TOKEN_PATTERN = Pattern.compile(TOKEN_REGEX);

			Matcher tokenMatcher = TOKEN_PATTERN.matcher(str.toLowerCase());
			while(tokenMatcher.find()){
				String word = tokenMatcher.group();
				if(useStopList){
					if(stopList.contains(word)){
						continue;
					}
				}
				
				if(SimpleTools.isNumber(word)){
					continue;
				}

				int _id = localDict.word2id.size();

				if (localDict.contains(word))		
					_id = localDict.getID(word);

				if (globalDict != null){
					//get the global id					
					Integer id = globalDict.getID(word);
					//System.out.println(id);

					if (id != null){
						localDict.addWord(word);

						lid2gid.put(_id, id);
						ids.add(_id);
					}
					else { //not in global dictionary
						//do nothing currently
					}
				}
				else {
					localDict.addWord(word);
					ids.add(_id);
				}
			}

			Document doc = new Document(ids, str);
			docs[idx] = doc;
			V = localDict.word2id.size();			
		}
	}
	//---------------------------------------------------------------
	// I/O methods
	//---------------------------------------------------------------

	/**
	 *  read a dataset from a stream, create new dictionary
	 *  @return dataset if success and null otherwise
	 */
	public static LDADataset readDataSet(String filename, boolean headerIncluded){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));

			LDADataset data = readDataSet(reader,headerIncluded,filename);

			reader.close();
			return data;
		}
		catch (Exception e){
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * read a dataset from a file with a preknown vocabulary
	 * @param filename file from which we read dataset
	 * @param dict the dictionary
	 * @return dataset if success and null otherwise
	 */
	public static LDADataset readDataSet(String filename, Dictionary dict, boolean headerIncluded){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			LDADataset data = readDataSet(reader, dict,headerIncluded);

			reader.close();
			return data;
		}
		catch (Exception e){
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *  read a dataset from a stream, create new dictionary
	 *  @return dataset if success and null otherwise
	 */
	public static LDADataset readDataSet(BufferedReader reader, boolean headerIncluded,String inputFileStr){
		try {
			//read number of document
			String line;
			line = reader.readLine();
			int M = 0;
			LDADataset data = null;
			if(line.trim().matches("\\d+")){
				M = Integer.parseInt(line.trim());
				data = new LDADataset(M);
				for (int i = 0; i < M; ++i){
					line = reader.readLine();
					readLdaLine(headerIncluded,line,data,i);
				}
			}else{	
				M = SimpleTools.countLines(inputFileStr);
				data = new LDADataset(M);
				readLdaLine(headerIncluded,line,data,0);
				for (int i = 1; i < M; ++i){
					line = reader.readLine();
					readLdaLine(headerIncluded,line,data,i);
				}
			}


			return data;
		}
		catch (Exception e){
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param headerIncluded
	 * @param line
	 * @param data
	 * @param lineN
	 */
	private static void readLdaLine(boolean headerIncluded,String line,LDADataset data,int lineN){
		boolean useRegex = true;
		if(headerIncluded){
			String header = "";
			if(useRegex){
				Pattern pattern = Pattern.compile(regex);
				System.out.println(lineN);
				//System.out.println(line);
				Matcher matcher = pattern.matcher(line);

				if(matcher.find()){
					header = matcher.group(1);
				}
				line = line.substring(header.length()+1);
			}else{
				System.out.println(lineN);
				if(lineN==189){
					System.out.println("start to debug: ");
				}
				header = line.split("\t")[0];
				line = line.split("\t")[1];
			}

			data.headerInforList[lineN] = header;
		}
		data.setDoc(line, lineN);
	}

	/**
	 * read a dataset from a stream with respect to a specified dictionary
	 * @param reader stream from which we read dataset
	 * @param dict the dictionary
	 * @return dataset if success and null otherwise
	 */
	public static LDADataset readDataSet(BufferedReader reader, Dictionary dict, boolean headerIncluded){
		try {
			//read number of document
			String line;
			line = reader.readLine();
			int M = Integer.parseInt(line.trim());
			System.out.println("NewM:" + M);

			LDADataset data = new LDADataset(M, dict);
			for (int i = 0; i < M; ++i){
				line = reader.readLine();

				data.setDoc(line, i);

			}

			return data;
		}
		catch (Exception e){
			System.out.println("Read Dataset Error: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * read a dataset from a string, create new dictionary
	 * @param str String from which we get the dataset, documents are seperated by newline character 
	 * @return dataset if success and null otherwise
	 * @throws IOException 
	 */
	public static LDADataset readDataSet(String [] strs) throws IOException{
		LDADataset data = new LDADataset(strs.length);

		for (int i = 0 ; i < strs.length; ++i){
			data.setDoc(strs[i], i);
		}
		return data;
	}

	/**
	 * read a dataset from a string with respect to a specified dictionary
	 * @param str String from which we get the dataset, documents are seperated by newline character	
	 * @param dict the dictionary
	 * @return dataset if success and null otherwise
	 * @throws IOException 
	 */
	public static LDADataset readDataSet(String [] strs, Dictionary dict) throws IOException{
		//System.out.println("readDataset...");
		LDADataset data = new LDADataset(strs.length, dict);

		for (int i = 0 ; i < strs.length; ++i){
			//System.out.println("set doc " + i);
			data.setDoc(strs[i], i);
		}
		return data;
	}

	/**
	 *this is called by readJsonTrainingData or readJsonTestingData of Model.java.
	 * similar to readDataSet, but for json files
	 *  read a dataset from a json stream, create new dictionary
	 *  the following is not considering data with labels
	 *  added by DC. Be careful, it is different from the original one and has special
	 *  usage.
	 *  @return dataset if success and null otherwise
	 * @throws IOException 
	 */
	public static LDADataset readJsonDataSet(String[] inputs) throws IOException{
		String inputFileStr = inputs[0];
		int M = SimpleTools.countLines(inputFileStr);
		System.out.println("M in the original files from readJsonDataSet(inputs): "+M);
		LDADataset data = new LDADataset(M);
		data=data.callJsonReader(data, inputs, null);
		return data;
	}

	/**
	 * the function is called by Models.readJsonTestingData(String inputFileStr,Dictionary dict) 
	 * @param inputs
	 * @param globalDict
	 * @return
	 * @throws IOException
	 */
	public static LDADataset readJsonDataSet(String[] inputs,Dictionary globalDict) throws IOException{
		String inputFileStr = inputs[0];
		int M = SimpleTools.countLines(inputFileStr);
		System.out.println("M in the original files from readJsonDataSet(inputs,globalDict): "+M);
		LDADataset data = new LDADataset(M, globalDict);
		data = data.callJsonReader(data, inputs, globalDict);
		return data;
	}


	/**
	 *the function is called by readJsonDataSet
	 * @since May 15, 2013
	 * the following suppose that mesh category files are in different files. This seems not a good approach.
	 * Instead, from now on we should use another approach. We may attach those categories as another json array into
	 * original files so as to make things simpler.
	 * @param data
	 * @param inputs
	 * @throws IOException 
	 */
	public LDADataset callJsonReader(LDADataset data,String[] inputs,Dictionary globalDict) throws IOException{
		//parser 2 parse file with the first line the labels and other lines only involve 
		//pmid and texts
		JsonFileParser jsonFileParser = new JsonFileParser();

		if(inputs[0].contains("drug_repurpose")){
			jsonFileParser.parseJsonFile2(inputs);
		}else{
			jsonFileParser.parseJsonFile(inputs);
		}
		List<String> corpus = jsonFileParser.getCorpus();
		List<String> labels = jsonFileParser.getLabels();
		List<String> headerInforList = jsonFileParser.getPmids();
		//the following if is based on the aspect.json generated from i2b2Challenge 2010 where I generate json files with 
		//each line as the sentence rather than as the number of aspects. However, there may be more than one aspects in 
		//one sentence. So, if we do not change M, the final results would be less. 
		//		if(headerInforList.size()>data.M){
		//			data = new LDADataset(headerInforList.size(),globalDict);
		//		}
		List<String> usedLabels = jsonFileParser.getUsedLabels();
		//List<String> sideInfoList = jsonFileParser.getSidInfoList();

		//the following if is for training data. else is for the testing. Evidenetly, testing 
		//does not have labels.
		if(labels.size()>0){
			if(this.M > labels.size()){
				this.M=labels.size(); 
				if(globalDict!=null){
					data = new LDADataset(M, globalDict);
				}else{
					data = new LDADataset(M);
				}
			}

			List<String> labelset = jsonFileParser.getLset();
			for(String label : labelset){
				System.out.println(label);
			}
			System.out.println("M: "+M);
			for (int i = 0; i < M; ++i){
				String docLine = corpus.get(i);
				String docLabel = labels.get(i);
				//String docLabels = usedLabels.get(i);
				String pmid = headerInforList.get(i);
				//the following method is to extract the higher level label on the fly. 
				//it is too slow for big data. I decide to do it on the fly.
				//data.setDoc(docLine, docLabels, pmid, i, inputs[3],inputs[4],inputs[5]);
				data.setDoc(docLine, pmid, docLabel, i);

			}	
		}else{
			for(int i=0;i < M;++i){
				String docLine = corpus.get(i);
				String pmid = headerInforList.get(i);
				String docLabel = labels.get(i);
				//no labels for testing data
				data.setDoc(docLine, pmid, docLabel, i);
			}
		}
		return data;
	}

	/**
	 * the following is designed for data with labels and texts separated.
	 * for example, if we have json parser parsed the json file and have texts, headerInforList and labels returned
	 * this function is called by callJsonReader  
	 * @param str
	 * @param docLabels
	 * @param pmid
	 * @param idx
	 * @param mapfileNames
	 * @return
	 * @throws IOException 
	 */
	public String[] setDoc(String text,String pmid,String docLabel,int idx) throws IOException{

		//Hierarchy hierarchy = new Hierarchy(mapfileName1,mapfileName2);
		if (0 <= idx && idx < M){
			this.headerInforList[idx] = "\""+pmid+"\"";
			this.docLabels[idx] = docLabel;
			//ids is used to store words in a document from begining to end.
			Vector<Integer> ids = new Vector<Integer>();
			String TOKEN_REGEX = "\\w+";
			Pattern TOKEN_PATTERN = Pattern.compile(TOKEN_REGEX);
			Matcher tokenMatcher = TOKEN_PATTERN.matcher(text.toLowerCase());
			while(tokenMatcher.find()){
				String word = tokenMatcher.group();
				if(useStopList){
					if(stopList.contains(word)){
						continue;
					}
				}
				
				if(SimpleTools.isNumber(word)){
					continue;
				}

				int _id = localDict.word2id.size();

				if (localDict.contains(word))		
					_id = localDict.getID(word);

				if (globalDict != null){
					//get the global id					
					Integer id = globalDict.getID(word);
					//System.out.println(id);

					if (id != null){
						localDict.addWord(word);
						//_id is the word's ID in localDict.
						//lid2gid is a HashMap which stores id which the word's ID in globalDict
						lid2gid.put(_id, id);
						ids.add(_id);
					}
					else { //not in global dictionary
						//do nothing currently
					}
				}
				else {
					//for training, globalDict should be null. For the constructor of readJsonTrainingData in Model, there 
					//is no Dictionary. in testing, there should be a Dictionary.
					localDict.addWord(word);
					ids.add(_id);
				}
			}
			//Document is composed of ids (a vector storing words from begining to end) and str is the string of text
			//labelRep of the document is added as well, labelRep is a vector in which only one of them is 1 and all others 
			//are zeros.
			//Document doc = new Document(ids, str, label);

			Document doc = new Document(ids, text);
			docs[idx] = doc;
			V = localDict.word2id.size();
			//System.out.println("number of labelRep in LDADataSet : "+K);
		}
		return null;
	}
}
