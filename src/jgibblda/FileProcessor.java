package jgibblda;

/**
 * @author lidi00000
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import opennlp.tools.tokenize.SimpleTokenizer;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import edu.stanford.nlp.ling.HasWord;
//import edu.stanford.nlp.ling.Sentence;
//import edu.stanford.nlp.ling.TaggedWord;
//import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class FileProcessor {
	

	/**
	 * 
	 * @param inputDirStr
	 * @param resourceMap
	 * @throws IOException
	 */
	public static HashMap<String, String> readFileLines(String inputDirStr, String delimiter)
					throws IOException {
		HashMap<String, String> resourceMap = new HashMap<String, String> ();
		File inputFile = new File(inputDirStr);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String inputLine = "";
		while ((inputLine = br.readLine()) != null) {
			if(inputLine.split(delimiter).length==1){
				continue;
			}
			String[] inputLineArr = inputLine.split(delimiter);
			String first = inputLineArr[0].trim();
			String second = inputLineArr[1].trim();
			resourceMap.put(first, second);
		}
		System.out.println("size of resourceMap in readFileLines of FileProcessor: "+resourceMap.size());
		return resourceMap;
	}
	
	/**
	 * 
	 * @param inputDirStr
	 * @param stopHm
	 * @throws IOException
	 */
	public static HashMap<String,Integer> readSpecialStopFile(String inputDirStr) throws IOException{
		HashMap<String,Integer> stopHm = new HashMap<String,Integer> ();
		File inputFile = new File(inputDirStr);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String inputLine = "";
		//String pattern = "(\\w)(\\s+)([\\.,])";
		String pattern = "(<[^>]*>)";
		
		while ((inputLine = br.readLine()) != null) {
			inputLine = inputLine.replaceAll(pattern, "");
			if(!inputLine.matches("(\\w+)")){
				continue;
			}else{
				stopHm.put(inputLine, 1);
			}
		}
		return stopHm;
	}
	
	/**
	 * 
	 * @param phiFileDir
	 * @return
	 * @throws IOException
	 */
//	public static double[][] readPhiFileTo2DArr(String phiFileDir) throws IOException{
//		int fileLineNum = countLines(phiFileDir);
//		double[][] phi2DArr = new double[fileLineNum][];
//		File inputFile = new File(phiFileDir);
//		BufferedReader br = new BufferedReader(new FileReader(inputFile));
//		String inputLine = "";
//		int topicId = 0;
//		while ((inputLine = br.readLine()) != null) {
//			String[] lineArr = inputLine.split("\\s+");
//			//System.out.println("line length in phi file"+lineArr.length);
//			int wordId = 0;
//			double[] onelineArr = new double[lineArr.length]; 
//			for(String item : lineArr){
//				onelineArr[wordId] = Double.valueOf(lineArr[wordId]);
//				wordId++;
//			}
//			phi2DArr[topicId]=onelineArr;
//			topicId++;
//		}
//		return phi2DArr;
//	}
	
	/**
	 * 
	 * @param phiFileDir
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer,HashMap<Integer,Double>> readPhiFile(String phiFileDir) throws IOException{
		HashMap<Integer,HashMap<Integer,Double>> nestedPhiHm = new HashMap<Integer,HashMap<Integer,Double>>();
		File inputFile = new File(phiFileDir);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String inputLine = "";
		int topicId = 0;
		while ((inputLine = br.readLine()) != null) {
			String[] lineArr = inputLine.split("\\s+");
			//System.out.println("line length in phi file"+lineArr.length);
			int wordId = 0;
			HashMap<Integer,Double> word2ratioHm = new HashMap<Integer,Double>();
			for(String item : lineArr){
				word2ratioHm.put(wordId, Double.valueOf(item));
				wordId++;
			}
			nestedPhiHm.put(topicId, word2ratioHm);
			topicId++;
		}
		System.out.println("size of nestedPhiHm: "+nestedPhiHm.size());
		return nestedPhiHm;
	}
	
	/**
	 * 
	 * @param thetaFileDir
	 * @param docId2pmidHm
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer,HashMap<Integer,Double>> readThetaFile(String thetaFileDir, HashMap<Integer,String> docId2pmidHm) throws IOException{
		HashMap<Integer,HashMap<Integer,Double>> nestedThetaHm = new HashMap<Integer,HashMap<Integer,Double>>();
		File inputFile = new File(thetaFileDir);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String inputLine = "";
		int docId = 0;
		while ((inputLine = br.readLine()) != null) {
			String[] lineArr = inputLine.split("\\s+");
			int counter = 0;
			int topicId = 0;
			HashMap<Integer,Double> topic2ratioHm = new HashMap<Integer,Double>();
			//System.out.println("line length in theta file: "+lineArr.length);
			for(String item : lineArr){
				if(!item.equals("")){
					
					if(counter==0){
						if(!docId2pmidHm.containsKey(docId)){
							docId2pmidHm.put(docId, item);
						}
						counter++;
						continue;
					}	
					topic2ratioHm.put(topicId, Double.valueOf(item));
					counter++;
					topicId++;
				}
			}
			nestedThetaHm.put(docId, topic2ratioHm);
			docId++;
		}
		System.out.println("size of nestedThetaHm: "+nestedThetaHm.size());
		return nestedThetaHm;
	}

	/**
	 * 
	 * @param textBuf
	 * @param phrase
	 * @param index
	 * @return
	 */
	public static int[] findByteSpan(StringBuffer textBuf, String phrase,
			int index) {
		int[] bytespan = new int[2];
		int start = 0, end = 0;
		// System.out.println("phrase in findByteSpan: "+phrase);
		String[] phraseArray = phrase.split(" ");
		if (phrase.isEmpty()) {
			bytespan[0] = start;
			bytespan[1] = end;
			return bytespan;
		} else {
			if (index == 0) {
				System.out.println(phraseArray[0]);
				start = textBuf.indexOf(phraseArray[0]);
				// end = textBuf.indexOf(phraseArray[phraseArray.length-1]);
				end = start + phrase.length() - 1;
				// index=end;
			} else {
				
				start = textBuf.indexOf(phraseArray[0], index+1);
				// end = textBuf.indexOf(phraseArray[phraseArray.length-1]);
				end = start + phrase.length() - 1;
				// index=end;
				// System.out.println("from index in findBytesSpan: "+ index);
				System.out.println(phraseArray[0]+" start "+start+" end: "+end+" index: "+index);
			}
		}
		// System.out.println("start and end: "+start+" "+end);

		bytespan[0] = start;
		bytespan[1] = end;
		return bytespan;
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc(String docName) {
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			try {
				document = builder.parse(inputFile);
				Node child = document.getFirstChild();
				sbAce.append(child.getTextContent());
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbAce;
	}
	
	/**
	 * 
	 * @param dir
	 * @param suffix
	 * @param stopList
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,List<String>> readFiles2StrList(String dir,String suffix,List<String> stopList) throws IOException{
		File dirFile = new File(dir);
		HashMap<String,List<String>> fileTokenListHm = new HashMap<String,List<String>>();
		if(dirFile.isDirectory()){
			File[] dirFileArr = dirFile.listFiles();
			for(int i=0;i<dirFileArr.length;i++){
				File singFile = dirFileArr[i];
				List<String> singFileTokenList = new ArrayList<String>();
				if(singFile.getName().endsWith(suffix)){
					BufferedReader brFile = new BufferedReader(new FileReader(singFile));
					String line = "";
					while((line=brFile.readLine())!=null){
						String[] lineArr = line.split(" ");
						for(int j=0;j<lineArr.length;j++){
							String token = lineArr[j];
							if(stopList.contains(token)){
								continue;
							}
							singFileTokenList.add(token);
						}
					}
					fileTokenListHm.put(singFile.getName(), singFileTokenList);
				}
			}
		}
		return fileTokenListHm;
	}

	/**
	 * reads the file and returns a string
	 * 
	 * @param docName
	 * @return
	 * @throws IOException
	 */
	public static StringBuffer readDoc2(String docName) {
		File inputFile = new File(docName);
		StringBuffer sbAce = new StringBuffer();
		org.w3c.dom.Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// File inputFile = new File(inputString);
		DocumentBuilder builder;
		try {
			BufferedReader brXml;
			try {
				try {
					brXml = new BufferedReader(new FileReader(inputFile));
					StringBuffer sbXml = new StringBuffer();
					String line = "";
					while ((line = brXml.readLine()) != null) {
						if (line.startsWith("time")) {
							continue;
						} else {
							sbXml.append(line);
						}
					}
					builder = factory.newDocumentBuilder();
					document = builder.parse(new InputSource(new StringReader(
							sbXml.toString())));
					Node child = document.getFirstChild();
					sbAce.append(child.getTextContent());
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sbAce;
	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static String[] readFile2(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList<String> list = new ArrayList<String>();
		String line;
		int i =0;
		while ((line = in.readLine()) != null){
			//System.out.println(line);
			list.add(line);

		}
		return (String[]) list.toArray(new String[0]);
	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static List<String> readFile(String dir) throws IOException {
		File file = new File(dir);
		BufferedReader in = new BufferedReader(new FileReader(file));
		ArrayList<String> list = new ArrayList<String>();
		String line;
		int i =0;
		while ((line = in.readLine()) != null){
			//System.out.println(line);
			list.add(line);

		}
		return list;
	}
	
	
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static String[] readFile3(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList<String> list = new ArrayList<String>();
		String line="";
		String tempLine="";
		int i =0;
		while ((line = in.readLine()) != null){
			//System.out.println(line);
			//the following line aims at joining sentences which belong to the same document.
			//But it seems that we don't need it at all since we can join them when we process the documents rather than in read
			//files processed. For example, taggingCtrfData() in CtrfDataMaker.java has done that in that function. 
			//but I still keep it her in case it may be used in future.
			int dotInd = line.indexOf(".");
			if(line.contains(".") && line.charAt(dotInd-1)==' '){
				tempLine = line;
			}else{
				if(tempLine.isEmpty()){
					list.add(line);
				}else{
					//this case happens when the line ends with ".". stanford tagger will regard it
					//as independent sentenes. 
					line=tempLine+line;
					list.add(line);
					tempLine="";
				}
				
			}
		
		}
		return (String[]) list.toArray(new String[0]);
	}

	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static String[] readEdtFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList list = new ArrayList();
		String line;
		while ((line = in.readLine()) != null){
			if(line.isEmpty()){
				continue;
			}else{
				list.add(line);
			}
		}
		return (String[]) list.toArray(new String[0]);
	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static StringBuffer readPlainFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		StringBuffer strbuffer = new StringBuffer();
		String line;
		while ((line = in.readLine()) != null){
			strbuffer.append(line+" ");
		}
		return strbuffer;
	}	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static StringBuffer readEdtFile2Strb(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		StringBuffer edtStrbuffer = new StringBuffer();
		String line;
		while ((line = in.readLine()) != null){
			//System.out.println(line+" |||||||");
			if(line.isEmpty()){
				continue;
			}else if(!line.endsWith(".")){
				//System.out.println(line);
				//edtStrbuffer.append(line+". ");
				//temporarily remove section part
				continue;
			}else{
				edtStrbuffer.append(line+" ");
			}
		}
		return edtStrbuffer;
	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static String[] readFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList list = new ArrayList();
		String line;
		
		while ((line = in.readLine()) != null){
			
			String[] lineArray = line.split(" ");
			int firstInd = line.indexOf(lineArray[1]);
			line = line.substring(firstInd);
			list.add(line);
		}
		return (String[]) list.toArray(new String[0]);
	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static HashMap<Integer,String> readFile(File f, String delimiter) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		HashMap<Integer,String> num2idHm = new HashMap<Integer,String>();
		String line;
		
		while ((line = in.readLine()) != null){
			String[] lineArray = line.split(delimiter);
			int index = Integer.valueOf(lineArray[0]);
			String id = lineArray[1];
			num2idHm.put(index, id);
		}
		return num2idHm;
	}
	
	/**
	 * Reads every line from a given text file. Specifically, from I2B2 format which is annotated as 
	 *one sentence one line. so quite simple. Probably, I will enrich this method in future to handle 
	 *different formats
	 * 
	 * @param f
	 *            Input file.
	 * @return ArrayList<String> Array containing each line in <code>f</code>.
	 */
	public static ArrayList<String> readFile2List(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList<String> list = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null){
			list.add(line);
		}
		in.close();
		return list;
	}

	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing the labels.
	 */
	public static String[] exLabelFromFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		ArrayList list = new ArrayList();
		String line;
		while ((line = in.readLine()) != null){
			String[] lineArray = line.split(" ");
			list.add(lineArray[0]);
		}
		return (String[]) list.toArray(new String[0]);
	}
	
//	/**
//	 * Reads every line from a given text file and tokenize each line into array of words.
//	 * and store them as an array of array
//	 * @param f
//	 *            Input file.
//	 * @return String[][] Array containing each line in <code>f</code>.
//	 */
//	public static String[][] readFile2DArray(File f) throws IOException {
//		BufferedReader in = new BufferedReader(new FileReader(f));
//		String[][] twoDArrayOfWords = null;
//		String line;
//		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
//		while ((line = in.readLine()) != null){
//			String[] tokens = tokenizer.tokenize(line);
//			String[] lineArray = line
//		}
//		return (String[]) list.toArray(new String[0]);
//	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return HashMap<String,Intger> containing words and their frequencies <code>f</code>.
	 */
	public static HashMap<String,Integer> calWofdFreq(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		HashMap<String,Integer> hmWordFreq = new HashMap<String,Integer>();
		String line;
		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		while ((line = in.readLine()) != null){
			String[] tokens = tokenizer.tokenize(line);
			for(int i=0;i<tokens.length;i++){
				String token = tokens[i];
				if(hmWordFreq.containsKey(token)){
					int count = hmWordFreq.get(token)+1;
					hmWordFreq.put(token, count);
				}else{
					hmWordFreq.put(token, 1);
				}
			}
		}
		return hmWordFreq;
	}
	

	/**
	 * 
	 * this function returns HashMap<String,Integer> where String is the word and Integer is 
	 * the count. The difference between calFreqWofD only lies in the file here is a directory.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Integer> getVacabulary(File file) throws IOException{
		HashMap<String,Integer> vacabulary = new HashMap<String,Integer>();
		int freqWofD = 0;
		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		if(file.isDirectory()){
			File[] listOfFiles = file.listFiles();
			for(int i=0;i<listOfFiles.length;i++){
				File ithFile = listOfFiles[i];
				BufferedReader in = new BufferedReader(new FileReader(ithFile));
				String line;
				while ((line = in.readLine()) != null){
					String[] tokens = tokenizer.tokenize(line);
					for(int j=0;j<tokens.length;j++){
						String token = tokens[j];
						if(vacabulary.containsKey(token)){
							int count = vacabulary.get(token)+1;
							vacabulary.put(token,count);
						}else{
							vacabulary.put(token,1);
						}
					}
				}
			}
		}else{
			//if file is a single file
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null){
				String[] tokens = tokenizer.tokenize(line);
				for(int j=0;j<tokens.length;j++){
					String token = tokens[j];
					if(vacabulary.containsKey(token)){
						int count = vacabulary.get(token)+1;
						vacabulary.put(token,count);
					}else{
						vacabulary.put(token,1);
					}
				}
			}
		}
		return vacabulary;
	}
	
	
	/**
	 * 
	 * this function returns TreeMap<String,Integer> where String is the word and Integer is 
	 * the count. The difference between calFreqWofD only lies in the file here is a directory.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static SortedMap<String,Integer> getSortedVacabulary(File file,String fileSuffix) throws IOException{
		SortedMap<String,Integer> vacabulary = new TreeMap<String,Integer>();
		int freqWofD = 0;
		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		if(file.isDirectory()){
			File[] listOfFiles = file.listFiles();
			for(int i=0;i<listOfFiles.length;i++){
				File ithFile = listOfFiles[i];
				if(!ithFile.getName().endsWith(fileSuffix)){
					continue;
				}
				BufferedReader in = new BufferedReader(new FileReader(ithFile));
				String line;
				while ((line = in.readLine()) != null){
					String[] tokens = tokenizer.tokenize(line);
					for(int j=0;j<tokens.length;j++){
						String token = tokens[j];
						if(vacabulary.containsKey(token)){
							int count = vacabulary.get(token)+1;
							vacabulary.put(token,count);
						}else{
							vacabulary.put(token,1);
						}
					}
				}
			}
		}else{
			//if file is a single file
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null){
				String[] tokens = tokenizer.tokenize(line);
				for(int j=0;j<tokens.length;j++){
					String token = tokens[j];
					if(vacabulary.containsKey(token)){
						int count = vacabulary.get(token)+1;
						vacabulary.put(token,count);
					}else{
						vacabulary.put(token,1);
					}
				}
			}
		}
		return vacabulary;
	}
	

	/**
	 * 	this function returns HashMap<String,Integer> where String is the word and Integer is 
	 * the count. The difference between calFreqWofD only lies in the file here is a directory.
	 * @param listOfStr
	 * @return
	 * @throws IOException
	 */
	public static List<String> getOrderedVac(String[] listOfStr,List<String> responses) throws IOException{
		List<String> vacabulary =new ArrayList<String>();
		//opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		for(int i=0;i<listOfStr.length;i++){
			//String[] tokens = tokenizer.tokenize(listOfStr[i]);
			String[] tokens = listOfStr[i].split(" ");
			//labeled means that the first token of the input is the label of the whole line
			//usually there is a colon between the label and its contents
			if(!responses.contains(tokens[0])){
				responses.add(tokens[0]);
			}
			for(int j=2;j<tokens.length;j++){
				String token = tokens[j];
				if(!vacabulary.contains(token)){
					vacabulary.add(token);
				}
			}			
		}
		Collections.sort(vacabulary);
		Collections.sort(responses);
		return vacabulary;
	}
	
	/**
	 * 	this function returns HashMap<String,Integer> where String is the word and Integer is 
	 * the count. The difference between calFreqWofD only lies in the file here is a directory.
	 * @param listOfStr
	 * @return
	 * @throws IOException
	 */
	public static List<String> getOrderedVac(List<String> listOfStr) throws IOException{
		List<String> vacabulary =new ArrayList<String>();
		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		for(int i=0;i<listOfStr.size();i++){
			//String[] tokens = tokenizer.tokenize(listOfStr[i]);
			String[] tokens = listOfStr.get(i).split(" ");
			for(int j=2;j<tokens.length;j++){
				String token = tokens[j];
				if(!vacabulary.contains(token)){
					vacabulary.add(token);
				}
			}			
		}
		Collections.sort(vacabulary);
		return vacabulary;
	}
	
//	/**
//	 * 
//	 * @param listOfStr
//	 * @param labelDocList
//	 * @return List<Pair> here, pair is the the pair first of which is the number of numbers
//	 * the second of which is the number of sentences in a document
//	 * @throws IOException
//	 */
//	public static List<Pair> getTermSentList(String[] listOfStr,List<String> labelDocList,List<List<Pair>> listWordSent) throws IOException {
//		List<Pair> listTermSent = new ArrayList<Pair>();
//		//opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
//		for (int i = 0; i < listOfStr.length; i++) {
//			String ithDoc = listOfStr[i].substring(listOfStr[i].indexOf(":")+2);
//			if(ithDoc.contains(".")){
//				int indexOfDot = ithDoc.indexOf(".");
//				if(ithDoc.charAt(indexOfDot-1)!=' ' && ithDoc.charAt(indexOfDot+1)==' '){
//						ithDoc=ithDoc.replace('.', 'd');
//				}
//			}
//			List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(ithDoc));
//			List<Pair> listWords2Sent = new ArrayList<Pair>();
//			int index = 0;
//		    for (ArrayList<? extends HasWord> sentence : sentences) {
//		    	//System.out.println(sentence);
//		    	Pair wordSentPair = new Pair(sentence.size(),index);
//		    	listWords2Sent.add(wordSentPair);
//		    	index++;
//		    }
//		    listWordSent.add(listWords2Sent);
//			// String[] tokens = tokenizer.tokenize(listOfStr[i]);
//			String[] tokens = listOfStr[i].split(" ");
//			// labeled means that the first token of the input is the label of
//			// the whole line
//			// usually there is a colon between the label and its contents
//			labelDocList.add(tokens[0]);
//			Pair termSentPair = new Pair(tokens.length - 2, sentences.size());
//			listTermSent.add(termSentPair);
//		}
//		return listTermSent;
//	}
	
	/** 
	 * @param listOfStr
	 * @param labelDocList
	 * @return List<Pair> here, pair is the the pair first of which is the number of numbers
	 * the second of which is the number of sentences in a document
	 * The difference between this function and getTermSentList lies in that the latter would tokenize the text
	 * while the following function would not since the text has been tokenized 
	 * @throws IOException
	 */
	public static List<Pair> getTermSentList2(String[] listOfStr,List<String> labelDocList,List<List<Pair>> listWordSent) throws IOException {
		List<Pair> listTermSent = new ArrayList<Pair>();
		//opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		for (int i = 0; i < listOfStr.length; i++) {
			String ithDoc = listOfStr[i].substring(listOfStr[i].indexOf(":")+2);
			String label = listOfStr[i].substring(0,listOfStr[i].indexOf(":")-1);
			labelDocList.add(label);
			if(ithDoc.contains(".")){
				char[] ithCharArray = ithDoc.toCharArray();
				for(int j=1;j<ithCharArray.length-1;j++){
					if(ithCharArray[j]=='.'){
						if(!Character.isSpaceChar(ithCharArray[j-1])){// && Character.isSpaceChar(ithCharArray[j+1])){
							//probably, the better way to concatenate strings should use StringBuffer or StringBuilder
							ithDoc=ithDoc.substring(0,j)+"d"+ithDoc.substring(j+1);
						}
					}
				}
			}
			System.out.println(label+" "+i+"th doc: "+ithDoc);
			String[] tokens = ithDoc.split(" ");
			ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();
//			if(ithDoc.contains(".")){
				ArrayList<String> sentList = new ArrayList<String>();
				for(int j=0;j<tokens.length;j++){
					if(tokens[j].equals(".")){
						sentList.add(tokens[j]);
						sentences.add(sentList);
						sentList=new ArrayList<String>();
					}else{
						sentList.add(tokens[j]);
						if(j==(tokens.length-1)){
							sentences.add(sentList);
							break;
						}
					}
				}
//			}
			
			List<Pair> listWords2Sent = new ArrayList<Pair>();
			int index = 0;
		    for (ArrayList<String> sentence : sentences) {
		    	//System.out.println(sentence);
		    	//only a little bit difference between getTermSentList2 and getTermSentList. 
		    	//we still need MaxentTagger.tokenizeText since I want the minidoc involves more than 1 sentence which 
		    	//is delimited by periods. But we don't want to tokenized the texts since the input text has been 
		    	//tokenzed. So, I put tokens.length here since tokens is obtained only by split blank space. 
		    	//Pair wordSentPair = new Pair(tokens.length - 2,index);
		    	Pair wordSentPair = new Pair(sentence.size(),index);
		    	listWords2Sent.add(wordSentPair);
		    	index++;
		    }
		    listWordSent.add(listWords2Sent);
			// String[] tokens = tokenizer.tokenize(listOfStr[i]);
			
			// labeled means that the first token of the input is the label of
			// the whole line
			// usually there is a colon between the label and its contents
			
			Pair termSentPair = new Pair(tokens.length, sentences.size());
			listTermSent.add(termSentPair);
		}
		return listTermSent;
	}
	
	
	/** 
	 * @param listOfStr
	 * @param labelDocList
	 * @return List<Pair> here, pair is the the pair first of which is the number of numbers
	 * the second of which is the number of sentences in a document
	 * The difference between this function and getTermSentList lies in that the latter would tokenize the text
	 * while the following function would not since the text has been tokenized 
	 * @throws IOException
	 */
	public static List<Pair> getTermSentList(List<String> listOfStr,List<List<Pair>> listWordSent) throws IOException {
		List<Pair> listTermSent = new ArrayList<Pair>();
		//opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		for (int i = 0; i < listOfStr.size(); i++) {
			String ithDoc = listOfStr.get(i);
			if(ithDoc.contains(".")){
				char[] ithCharArray = ithDoc.toCharArray();
				for(int j=1;j<ithCharArray.length-1;j++){
					if(ithCharArray[j]=='.'){
						if(!Character.isSpaceChar(ithCharArray[j-1])){// && Character.isSpaceChar(ithCharArray[j+1])){
							//probably, the better way to concatenate strings should use StringBuffer or StringBuilder
							ithDoc=ithDoc.substring(0,j)+"d"+ithDoc.substring(j+1);
						}
					}
				}
			}
			System.out.println(i+"th doc: "+ithDoc);
			String[] tokens = ithDoc.split(" ");
			ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();
//			if(ithDoc.contains(".")){
				ArrayList<String> sentList = new ArrayList<String>();
				for(int j=0;j<tokens.length;j++){
					if(tokens[j].equals(".")){
						sentList.add(tokens[j]);
						sentences.add(sentList);
						sentList=new ArrayList<String>();
					}else{
						sentList.add(tokens[j]);
						if(j==(tokens.length-1)){
							sentences.add(sentList);
							break;
						}
					}
				}
//			}
			
			List<Pair> listWords2Sent = new ArrayList<Pair>();
			int index = 0;
		    for (ArrayList<String> sentence : sentences) {
		    	//System.out.println(sentence);
		    	//only a little bit difference between getTermSentList2 and getTermSentList. 
		    	//we still need MaxentTagger.tokenizeText since I want the minidoc involves more than 1 sentence which 
		    	//is delimited by periods. But we don't want to tokenized the texts since the input text has been 
		    	//tokenzed. So, I put tokens.length here since tokens is obtained only by split blank space. 
		    	//Pair wordSentPair = new Pair(tokens.length - 2,index);
		    	Pair wordSentPair = new Pair(sentence.size(),index);
		    	listWords2Sent.add(wordSentPair);
		    	index++;
		    }
		    listWordSent.add(listWords2Sent);
			// String[] tokens = tokenizer.tokenize(listOfStr[i]);
			
			// labeled means that the first token of the input is the label of
			// the whole line
			// usually there is a colon between the label and its contents
			
			Pair termSentPair = new Pair(tokens.length, sentences.size());
			listTermSent.add(termSentPair);
		}
		return listTermSent;
	}
	
	
//	/**
//	 * 
//	 * @param listOfStr
//	 * @param labelDocList
//	 * @return List<Pair> here, pair is the the pair first of which is the number of numbers
//	 * the second of which is the number of sentences in a document
//	 * The difference between this function and getTermSentList lies in that the latter would tokenize the text
//	 * while the following function would not since the text has been tokenized 
//	 * @throws IOException
//	 */
//	public static List<Pair> getTermSentList3(String[] listOfStr,List<String> labelDocList,List<List<Pair>> listWordSent) throws IOException {
//		List<Pair> listTermSent = new ArrayList<Pair>();
//		//opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
//		for (int i = 0; i < listOfStr.length; i++) {
//			String ithDoc = listOfStr[i].substring(listOfStr[i].indexOf(":")+2);
//			String[] tokens = listOfStr[i].split(" ");
//			if(ithDoc.contains(".")){
				//int indexOfDot = ithDoc.indexOf(".");
				//the second condition may not be necessary. It aims at not replacing words like u.s. as udsd.
				//the following line is used to check white space. But there are some problems
				// ithDoc.charAt(indexOfDot+1)==' '
//				if(!Character.isWhitespace(ithDoc.charAt(indexOfDot-1)) && Character.isWhitespace(ithDoc.charAt(indexOfDot+1))){
//						ithDoc=ithDoc.replace('.', 'd');
//						System.out.println(i+"th doc: "+ithDoc);
//				}else 
				//it seems that isSpaceChar and isWhiteSpace are doing the same thing. If I leave the following without 
				//the second condition, larger numbers of sentences will be included. Understandable since if this way, 
				//many sentences without space after the dot will be included. 
				//the following code is a wrong way to replace the dot with d one by one.  what a fool!
//				if(!Character.isSpaceChar(ithDoc.charAt(indexOfDot-1)) && Character.isSpaceChar(ithDoc.charAt(indexOfDot+1)) ){
//					//ithDoc=ithDoc.replace('.', 'd');
//					//ithDoc=ithDoc.substring(0);
//					System.out.println("charAT(indexOfDot-1): "+ithDoc.charAt(indexOfDot-1)+" "+i+"th doc: "+ithDoc);
//				}
//				char[] ithCharArray = ithDoc.toCharArray();
//				for(int j=1;j<ithCharArray.length-1;j++){
//					if(ithCharArray[j]=='.'){
//						if(!Character.isSpaceChar(ithCharArray[j-1]) && Character.isSpaceChar(ithCharArray[j+1])){
//							//probably, the better way to concatenate strings should use StringBuffer or StringBuilder
//							ithDoc=ithDoc.substring(0,j)+"d"+ithDoc.substring(j+1);
//						}
//					}
//				}
//			}
//			System.out.println(i+"th doc: "+ithDoc);
//			
//			
//			
//			List<Sentence<? extends HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(ithDoc));
//			List<Pair> listWords2Sent = new ArrayList<Pair>();
//			int index = 0;
//		    for (ArrayList<? extends HasWord> sentence : sentences) {
//		    	//System.out.println(sentence);
//		    	//only a little bit difference between getTermSentList2 and getTermSentList. 
//		    	//we still need MaxentTagger.tokenizeText since I want the minidoc involves more than 1 sentence which 
//		    	//is delimited by periods. But we don't want to tokenized the texts since the input text has been 
//		    	//tokenzed. So, I put tokens.length here since tokens is obtained only by split blank space.
//		    	//but this is also wrong. 
//		    	Pair wordSentPair = new Pair(tokens.length - 2,index);
//		    	listWords2Sent.add(wordSentPair);
//		    	index++;
//		    }
//		    listWordSent.add(listWords2Sent);
//			// String[] tokens = tokenizer.tokenize(listOfStr[i]);
//			
//			// labeled means that the first token of the input is the label of
//			// the whole line
//			// usually there is a colon between the label and its contents
//			labelDocList.add(tokens[0]);
//			Pair termSentPair = new Pair(tokens.length - 2, sentences.size());
//			listTermSent.add(termSentPair);
//		}
//		return listTermSent;
//	}
	
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<String> getOrderedVac(File file) throws IOException{
		List<String> vacabulary =new ArrayList<String>();
		int freqWofD = 0;
		opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
		if(file.isDirectory()){
			File[] listOfFiles = file.listFiles();
			for(int i=0;i<listOfFiles.length;i++){
				File ithFile = listOfFiles[i];
				BufferedReader in = new BufferedReader(new FileReader(ithFile));
				String line;
				while ((line = in.readLine()) != null){
					String[] tokens = tokenizer.tokenize(line);
					for(int j=0;j<tokens.length;j++){
						String token = tokens[j];
						if(!vacabulary.contains(token)){
							vacabulary.add(token);
						}
					}
				}
			}
		}else{
			//if file is a single file
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null){
				String[] tokens = tokenizer.tokenize(line);
				for(int j=0;j<tokens.length;j++){
					String token = tokens[j];
					if(!vacabulary.contains(token)){
						vacabulary.add(token);
					}
				}
			}
		}
		Collections.sort(vacabulary);
		return vacabulary;
	}
	
	/**
	 * Reads every line from a given text file.
	 * 
	 * @param f
	 *            Input file.
	 * @return String[] Array containing each line in <code>f</code>.
	 */
	public static HashMap<String,Integer> calWofdFreq(String[] tokens) throws IOException {
		HashMap<String,Integer> hmWordFreq = new HashMap<String,Integer>();
		for(int i=0;i<tokens.length;i++){
			String token = tokens[i];
			if(hmWordFreq.containsKey(token)){
				int count = hmWordFreq.get(token)+1;
				hmWordFreq.put(token, count);
			}else{
				hmWordFreq.put(token, 1);
			}
		}
		return hmWordFreq;
	}
	
	
	/**
	 * we have a list pf NE pair text list (like list of documents), calculate word frequency of each NE pair text
	 * @param nePairTextList
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Integer>[] calFreqWofd(List<String> nePairTextList) throws IOException{
		HashMap<String,Integer>[] hmArrayWofd = new HashMap[nePairTextList.size()];
		for(int i=0;i<nePairTextList.size();i++){
			String nePairText = nePairTextList.get(i);
			opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
			//use opennlp tokenizer to split text words, aiming at split some abbreviated words such he's or punctuation marks
			String[] tokens = tokenizer.tokenize(nePairText);
			HashMap hmWordFreq = FileProcessor.calWofdFreq(tokens);
			hmArrayWofd[i] = hmWordFreq;
		}
		return hmArrayWofd;
	}
	
	/**
	 * we have a list pf NE pair text array (like array of documents), 
	 * calculate word frequency of each NE pair text
	 * @param nePairTextArray
	 * @return 
	 * @throws IOException
	 */
	public static HashMap<String,Integer>[] calFreqWofd(String[] nePairTextArray) throws IOException{
		HashMap<String,Integer>[] hmArrayWofd = new HashMap[nePairTextArray.length];
		for(int i=0;i<nePairTextArray.length;i++){
			String nePairText = nePairTextArray[i];
			opennlp.tools.tokenize.Tokenizer tokenizer = new SimpleTokenizer();
			//use opennlp tokenizer to split text words, aiming at split some abbreviated words such he's or punctuation marks
			String[] tokens = tokenizer.tokenize(nePairText);
			HashMap hmWordFreq = FileProcessor.calWofdFreq(tokens);
			hmArrayWofd[i] = hmWordFreq;
		}
		return hmArrayWofd;
	}
	

	/**
	 * 	given a file, let us calculate word frequency of each document
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Integer>[] calFreqWofd(File dir) throws IOException{
		HashMap<String,Integer>[] hmArrayWofd = null;
		if(dir.isDirectory()){
			File[] listOfFiles = dir.listFiles();
			for(int i=0;i<listOfFiles.length;i++){
				File ithFile = listOfFiles[i];
				if(ithFile.getName().endsWith(".txt")){
					hmArrayWofd[i] = FileProcessor.calWofdFreq(ithFile);
				}
				
			}
		}
		return hmArrayWofd;
	}
	

	/**
	 * 	given a file, let us calculate word frequency of Document
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Integer> calFreqWofD(File dir) throws IOException{
		HashMap<String,Integer> hmWofDFreq = new HashMap<String,Integer>();
		HashMap<String,Integer>[] hmArrayWofd = FileProcessor.calFreqWofd(dir);
		HashMap<String,Integer> vacabulary = FileProcessor.getVacabulary(dir);
		Iterator<String> valIter = vacabulary.keySet().iterator();
		while(valIter.hasNext()){
			String word = valIter.next();
			for(int i=0;i<hmArrayWofd.length;i++){
				HashMap<String,Integer> hmWofd = hmArrayWofd[i];
				if(hmWofd.containsKey(word)){
					if(hmWofDFreq.containsKey(word)){
						int count=hmWofDFreq.get(word)+1;
						hmWofDFreq.put(word, count);
					}else{
						hmWofDFreq.put(word, 1);
					}
				}
			}
		}
		return hmWofDFreq;
	}
	
	/**
	 * 	given a file, let us calculate word frequency of Document
	 * different from calFreqWofD, we use integer to represent each word here so that LibSVM or LDA in R
	 * can use it.
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer,Integer> calFreqWinD(File dir) throws IOException{
		HashMap<Integer,Integer> hmWofDFreq = new HashMap<Integer,Integer>();
		HashMap<String,Integer>[] hmArrayWofd = FileProcessor.calFreqWofd(dir);
		HashMap<String,Integer> vacabulary = FileProcessor.getVacabulary(dir);
		Iterator<String> valIter = vacabulary.keySet().iterator();
		while(valIter.hasNext()){
			String word = valIter.next();
			int wordInd = vacabulary.get(word);
			for(int i=0;i<hmArrayWofd.length;i++){
				HashMap<String,Integer> hmWofd = hmArrayWofd[i];
				if(hmWofd.containsKey(word)){
					if(hmWofDFreq.containsKey(word)){
						int count=hmWofDFreq.get(word)+1;
						hmWofDFreq.put(wordInd, count);
					}else{
						hmWofDFreq.put(wordInd, 1);
					}
				}
			}
		}
		return hmWofDFreq;
	}
	
	/**
	 * 	given a file, let us calculate word frequency of Document
	 * different from calFreqWofD, we use integer to represent each word here so that LibSVM or LDA in R
	 * can use it.
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static HashMap<Integer,Integer> calFreqWinD(File dir, HashMap<String,Integer> vacabulary) throws IOException{
		HashMap<Integer,Integer> hmWofDFreq = new HashMap<Integer,Integer>();
		HashMap<String,Integer> hmArrayWofd = new HashMap<String,Integer>(); 
		if(dir.getName().endsWith(".txt")){
			hmArrayWofd = FileProcessor.calWofdFreq(dir);
		}
		Iterator<String> valIter = hmArrayWofd.keySet().iterator();
		while(valIter.hasNext()){
			String word = valIter.next();
			int wordInd = vacabulary.get(word);
			int freq = hmArrayWofd.get(word);
			hmWofDFreq.put(wordInd, freq);
		}
		return hmWofDFreq;
	}
	
	
	/**
	 * 	given a file, let us calculate word frequency of Document
	 * different from calFreqWofD, we use integer to represent each word here so that LibSVM or LDA in R
	 * can use it.
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static SortedMap<Integer,Integer> calFreqWSorted(File dir, SortedMap<String,Integer> vacabulary) throws IOException{
		SortedMap<Integer,Integer> hmWofDFreq = new TreeMap<Integer,Integer>();
		HashMap<String,Integer> hmArrayWofd = new HashMap<String,Integer>(); 
		if(dir.getName().endsWith(".txt")){
			hmArrayWofd = FileProcessor.calWofdFreq(dir);
		}
		Iterator<String> valIter = hmArrayWofd.keySet().iterator();
		while(valIter.hasNext()){
			String word = valIter.next();
			int wordInd = vacabulary.get(word);
			int freq = hmArrayWofd.get(word);
			hmWofDFreq.put(wordInd, freq);
		}
		return hmWofDFreq;
	}
	
	/**
	 * by call calFreqWinD for each document, the whole corpus would be written into lda R format.
	 * namely, matrix forms with frequency count for each column
	 * @param strDir
	 * @param outDir
	 * @throws IOException
	 */
	public static void calFreqWLdaR(String strDir, String outDir, String fileSuffix) throws IOException{
		PrintWriter pwFreqFile = new PrintWriter(new FileWriter(new File(outDir)));
		File fileDir = new File(strDir);
		SortedMap<String,Integer> vacabulary = FileProcessor.getSortedVacabulary(fileDir, fileSuffix);
		
		Iterator<String> iterVoc = vacabulary.keySet().iterator();
		pwFreqFile.print("word ");
		while(iterVoc.hasNext()){
			String word = iterVoc.next();
			pwFreqFile.print(word+" ");
		}
		pwFreqFile.println();
		File[] listOfFile = fileDir.listFiles();
		for(int i=0;i<listOfFile.length;i++){
			File ithFile = listOfFile[i];
			if(!ithFile.getName().endsWith(fileSuffix)){
				continue;
			}
			SortedMap<Integer,Integer> hmWofDFreq = FileProcessor.calFreqWSorted(ithFile, vacabulary);
			pwFreqFile.print(ithFile.getName()+" ");	
			Iterator<String> iterVoc1 = vacabulary.keySet().iterator();
			pwFreqFile.print(ithFile.getName()+" ");
			while(iterVoc1.hasNext()){
				String word = iterVoc1.next();
				int wordInd = vacabulary.get(word);
				if(hmWofDFreq.containsKey(wordInd)){
					Integer freq = hmWofDFreq.get(wordInd);
					pwFreqFile.print(freq+" ");
				}else{
					pwFreqFile.print(0+" ");
				}
			}
			pwFreqFile.println();
		}
		pwFreqFile.flush();
		pwFreqFile.close();
	}
	
	/**
	 * by call calFreqWinD for each document, the whole corpus would be written into SVM format.
	 * @param strDir
	 * @param outDir
	 * @throws IOException
	 */
	public static void calFreqWinD(String strDir, String outDir, String fileSuffix) throws IOException{
		PrintWriter pwFreqFile = new PrintWriter(new FileWriter(new File(outDir)));
		File fileDir = new File(strDir);
		HashMap<String,Integer> vacabulary = FileProcessor.getVacabulary(fileDir);
		File[] listOfFile = fileDir.listFiles();
		for(int i=0;i<listOfFile.length;i++){
			File ithFile = listOfFile[i];
			if(!ithFile.getName().endsWith(fileSuffix)){
				continue;
			}
			HashMap<Integer,Integer> hmWofDFreq = FileProcessor.calFreqWinD(ithFile, vacabulary);
			pwFreqFile.print(ithFile.getName()+" ");
			for(Map.Entry<Integer, Integer> entry : hmWofDFreq.entrySet()){
				Integer tokenInd = entry.getKey();
				Integer freq = entry.getValue();
				pwFreqFile.print(tokenInd+":"+freq+" ");
			}
			pwFreqFile.println();
		}
		pwFreqFile.flush();
		pwFreqFile.close();
	}
	
	/**
	 * given vacabulary of the whole corpus and word frequency of each document, we get word frequency of Document 
	 * it looks that the following function is not correct at all since hmArrayWofd is the same as hmWofDFreq. I will 
	 * check this later.
	 * @param vacabulary
	 * @param hmArrayWofd
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Integer> calFreqWofD(HashMap<String,Integer> vacabulary,HashMap<String,Integer>[] hmArrayWofd) throws IOException{
		HashMap<String,Integer> hmWofDFreq = new HashMap<String,Integer>();
		Iterator<String> valIter = vacabulary.keySet().iterator();
		while(valIter.hasNext()){
			String word = valIter.next();
			for(int i=0;i<hmArrayWofd.length;i++){
				HashMap<String,Integer> hmWofd = hmArrayWofd[i];
				if(hmWofd.containsKey(word)){
					if(hmWofDFreq.containsKey(word)){
						int count=hmWofDFreq.get(word)+1;
						hmWofDFreq.put(word, count);
					}else{
						hmWofDFreq.put(word, 1);
					}
				}
			}
		}
		return hmWofDFreq;
	}
	
}
