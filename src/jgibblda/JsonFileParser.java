package jgibblda;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import utils.Pair;

/**
 * JsonFileParser.java is called by LDADataset.java
 * @author m048100
 *
 */
public class JsonFileParser {

	List<String> labelset;
	List<String> corpus;
	//corpusIndHm is only working for I2B2 sentence corpus where we use sentence as one text. Thus, we need to keep 
	//the text file number and sentence number too kpee track of them.
	HashMap<Pair<String,Integer>,String> corpusIndHm;
	List<String> titles;
	//more than one labels would be used for Mesh prediction. labels are the original mesh assignments
	//usedLabels are those derived from labels we would really use for training and testing. 
	List<String> labels;
	List<String> usedLabels;
	//the following would capture all remaining labels. In FileExpander of BioASQ, for example, we may add more labels.
	List<List<String>> remainLabels;
	List<String> headerInforList;  
	String featType = "BOF"; //another choice is BOF, I would add the parameter to argument
	//in data like i2b2 where each sentence is a training instance. If we do aspect prediction, we need to find 
	//which phrase some aspect is connected to. In this case, we need to remember the phrase location besides 
	//the phrase itself. Therefore, we need sidInfoList to keep track of them.
	//List<String> sidInfoList; 


	public List<String> getLset(){
		return this.labelset;
	}

	public List<String> getCorpus(){
		return this.corpus;
	}

	public List<String> getTitles(){
		return this.titles;
	}

	public List<String> getLabels(){
		return this.labels;
	}

	public List<String> getUsedLabels(){
		System.out.println("the size of usedLabels in JsonFileParser: "+usedLabels.size());
		return this.usedLabels;
	}

	//	public List<String> getSidInfoList(){
	//		return this.sidInfoList;
	//	}

	public List<List<String>> getRemainLabels(){
		return this.remainLabels;
	}

	public List<String> getPmids(){
		return this.headerInforList;
	}

	public HashMap<Pair<String,Integer>,String> getCorpusIndHm(){
		return this.corpusIndHm;
	}

	public JsonFileParser(){
		labelset = new ArrayList<String>();
		headerInforList = new ArrayList<String>();
		labels = new ArrayList<String>();
		corpus = new ArrayList<String>();
		titles = new ArrayList<String>();
		usedLabels = new ArrayList<String>();
		remainLabels = new ArrayList<List<String>>();
		corpusIndHm = new HashMap<Pair<String,Integer>,String>();
		//sidInfoList = new ArrayList<String>();
	}


	public String processRegEx(String text,String specialToken){
		Pattern pattern = Pattern.compile(specialToken);
		Matcher matcher = pattern.matcher(text);
		boolean matchFound = matcher.find();
		while(matchFound){
			//System.out.println(matcher.group());
			text = text.replace(matcher.group(), "");
			matchFound = matcher.find();
		}
		return text;
	}

	public void parseJsonFile2(String[] args){
		JSONParser parser = new JSONParser();
		FileReader fileR = null;
		JSONObject jsonObject = null;
		try {
			fileR = new FileReader(args[0]);
			jsonObject = (JSONObject) parser.parse(fileR);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String sourceType = args[1];
		System.out.println("sourceType: "+sourceType);
		int ignoreLines = 0;
		if(sourceType.contains("-")){
			ignoreLines = Integer.valueOf(sourceType.split("-")[1]);
			sourceType = sourceType.split("-")[0];
		}
		
		String[] tagValues = args[2].split(":");
		System.out.println("tag values printing from parseJsonFile2 of JsonFileParser: ");
		for(int i=0;i<tagValues.length;i++){
			System.out.print(tagValues[i]+" ");
		}
		System.out.println();
		JSONArray drugList = (JSONArray)jsonObject.get(tagValues[0]);
		for(int i=0;i<drugList.size();i++){
			JSONObject fileObj = (JSONObject) drugList.get(i);
			String name = (String) fileObj.get(tagValues[1]);
			String pmid = (String) fileObj.get(tagValues[2]);
			String absText = (String) fileObj.get(tagValues[3]);
			labels.add(pmid);
			this.headerInforList.add(pmid+ " "+name);
			corpus.add(absText);
		}
	}
	
	public void parseJsonFile(String[] args){
		JSONParser parser = new JSONParser();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(args[0]));
			String sourceType = args[1];
			int ignoreLines = 0;
			if(sourceType.contains("-")){
				ignoreLines = Integer.valueOf(sourceType.split("-")[1]);
				sourceType = sourceType.split("-")[0];
			}
			String[] tagValues = args[2].split(":");

			//BufferedReader bf = new BufferedReader(new FileReader("/Volumes/shannon/projects/data/bioasq/allMeSH.json"));
			String line = "";
			int countLine=0;
			while(countLine<ignoreLines){
				line = bf.readLine();
				countLine++;
			}
			while((line=bf.readLine())!=null){
				//System.out.println(line);
				if(countLine % 1000000 == 0){
					System.out.println(countLine);
				}
				if(line.endsWith(",")){
					line = line.substring(0,line.lastIndexOf(",")).trim();
				}else if(line.contains("]") && !line.contains("[")){
					line = line.substring(0,line.indexOf("]"));
				}
				//System.out.println(countLine);
				//System.out.println(line);
				Object lineObj = parser.parse(line);
				JSONObject jsonLineObject =  (JSONObject) lineObj;
				//generif_basic is used in biocreative
				if(sourceType.contains("generifs_basic")){
					//String[] tags = {"taxId","geneId","pmid","timeStamp","text"};
					//note: if biocreative, the final processing of phi or theta or tassign and so on
					//would use the following regular expressions.
					//String regex = "([\\d]+)\\s+([\\d]+)\\s+([\\d]+)\\s+([\\d]+-[\\d]+-[\\d]+\\s[\\d]+:[\\d]+)\\s+(.*)";
					String taxId = (String)jsonLineObject.get("taxId");
					String text = (String)jsonLineObject.get("text");
					String pmid = (String)jsonLineObject.get("pmid");
					String timeStamp = (String)jsonLineObject.get("timeStamp");
					String geneId = (String)jsonLineObject.get("text");
					//although we do not need labels, we still keep it since without it, there would 
					//errors. In addition, if we do need labels, we can use them conveniently.
					labels.add(geneId.trim());
					this.headerInforList.add(pmid+ " "+timeStamp+" "+taxId+" "+geneId);
					corpus.add(text);
				}
				else if(sourceType.equals("bioasq")){
					if(tagValues.length>3){ //bigger than 3 is better than define specific numbers.
						//for training, title, topMeshMajor, meshMajor,abstract and pmid 
						//meshMajor:doc:pmid:firstMeshMajor
						String title = (String) jsonLineObject.get(tagValues[0]);
						if(title!=null){
							titles.add(title.toLowerCase().trim());
						}

						JSONArray meshList = (JSONArray)jsonLineObject.get(tagValues[1]);
						String meshString = "";
						//this check is for distinguish training and testing. For testing,
						//labels would be null.
						if(meshList.size()>0){
							for(int j=0;j<meshList.size();j++){
								meshString+=String.valueOf(meshList.get(j)).trim().toLowerCase()+":";
							}
							labels.add(meshString.trim());
						}
						//System.out.println("what is tagValues[2] in JsonFileParser: "+tagValues[2]);
						String abstractText = (String) jsonLineObject.get(tagValues[2]);
						corpus.add(abstractText.toLowerCase().trim());
						String pmid = (String) jsonLineObject.get(tagValues[3]);
						headerInforList.add(pmid);
						if(tagValues.length==5){
							JSONArray usedMeshList = (JSONArray)jsonLineObject.get(tagValues[4]);
							String usedMeshString = "";
							//this check is for distinguish training and testing. For testing,
							//labels would be null. Meanwhile, in training, it is found that some mesh cannot 
							//find their higher level ones. Then, we have to remove those for consistency
							if(usedMeshList.size()>0){
								for(int j=0;j<usedMeshList.size();j++){
									usedMeshString+=String.valueOf(usedMeshList.get(j)).trim().toLowerCase()+":";
								}
								usedLabels.add(usedMeshString.trim());
							}else{
								labels.remove(meshString.trim());
								corpus.remove(abstractText.toLowerCase().trim());
								headerInforList.remove(pmid);
							}
						}else if(tagValues.length>5){
							//this means that there are more than 5 tags we need to use. This is usually used in FileExpander
							//since we want to copy all original data into a new file and meanwhile, we want to add more lower level
							//mesh terms (mesh terms are just one example, any kind of ontology fits)
							for(int i=4;i<tagValues.length;i++){
								JSONArray ithMeshList = (JSONArray)jsonLineObject.get(tagValues[i]);
								String ithMeshString = "";
								//this check is for distinguish training and testing. For testing,
								//labels would be null. Meanwhile, in training, it is found that some mesh cannot 
								//find their higher level ones. Then, we have to remove those for consistency
								List<String> ithLabels = new ArrayList<String>();
								if(ithMeshList.size()>0){
									for(int j=0;j<ithMeshList.size();j++){
										ithMeshString+=String.valueOf(ithMeshList.get(j)).trim().toLowerCase()+":";
									}
									ithLabels.add(ithMeshString.trim());
								}
								remainLabels.add(ithLabels);
							}
						}

					}else if(tagValues.length==3){
						//for testing, there is no meshHeadings
						String title = (String) jsonLineObject.get(tagValues[0]);
						String abstractText = (String) jsonLineObject.get(tagValues[1]);
						if(abstractText == null){
							abstractText = (String) jsonLineObject.get("abstract");
						}
						corpus.add(abstractText.toLowerCase().trim());
						String pmid = String.valueOf(jsonLineObject.get(tagValues[2]));
						headerInforList.add(pmid);
					}

				}else if(sourceType.equals("ade")){

					JSONObject setIdObj = (JSONObject) jsonLineObject.get(tagValues[0]);
					String[] tagValue1Arr = tagValues[1].split("=");
					JSONObject sectionTextObj = (JSONObject)jsonLineObject.get(tagValue1Arr[0]);
					String sectionText = (String) sectionTextObj.get(tagValue1Arr[2]);
					String[] tokenArr = sectionText.split("[\\s]+");
					String symbols = "[=@`\\^\\]\\[\\$&*+><~#%\\(\\)/\\-!?'\"]+";
					String specialToken = symbols+"\\w+"+symbols;
					String specialToken2 = symbols+"\\w+";
					String specialToken3 = "\\w+"+symbols;
					String specialToken4 = "("+symbols+"\\w+"+symbols+")+";
					for(int i=0;i<tokenArr.length;i++){
						String token = tokenArr[i].trim();
						if(token!=""){
							token = processRegEx(token, specialToken).toLowerCase().trim();
							token = processRegEx(token, specialToken2).toLowerCase().trim();
							token = processRegEx(token, specialToken3).toLowerCase().trim();
							token = processRegEx(token, specialToken4).toLowerCase().trim();
						}
					}
				}else if(sourceType.contains("i2b2_aspects")){
					//note:  the json files would have fewer lines since it is based on sentences where
					//a few labels may appear in the same sentence. For example, for the 477 files in the 
					//i2b22010, thera are only 11818 json objects. But it has more than 18000 assertions.
					//we need to extract sentence, aspects, filename, sentNo, offsets 
					String sentence = (String) jsonLineObject.get("sentence");
					//now, instead of using sentence, we plan to use features which represent the ideas of replacing 
					//BOW to BOF. 
					String feature = (String) jsonLineObject.get("feature");
					//System.out.println(feature);
					if(featType.equals("BOW")){
						corpus.add(sentence.toLowerCase().trim());
					}else{
						corpus.add(feature.toLowerCase().trim());
					}

					String filename = (String) jsonLineObject.get("fileName");


					JSONObject labelObj = (JSONObject)jsonLineObject.get("labels");

					String aspect = (String) labelObj.get("aspect");
					String phrase = (String) labelObj.get("phrase");
					String startOffset = (String) labelObj.get("startOffset");
					String endOffset = (String) labelObj.get("endOffset");
					//sidInfoList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
					this.headerInforList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
					usedLabels.add(aspect.trim()+" ");
					labels.add(aspect.trim()+" ");
					String beginOffset = startOffset.replaceAll("[\\(\\)]", "");
					int sentNo = Integer.valueOf(beginOffset.split(":")[0]);
					Pair<String,Integer> fileSentPair = new Pair<String,Integer>(filename,sentNo);
					//corpusIndHm.put(fileSentPair, sentence);
					corpusIndHm.put(fileSentPair, feature);
				}
				//else if(sourceType.contains("i2b2_concepts")){
//					String document = (String) jsonLineObject.get("document");
//					//now, instead of using sentence, we plan to use features which represent the ideas of replacing 
//					//BOW to BOF. 
//					String feature = (String) jsonLineObject.get("feature");
//					//System.out.println(feature);
//					if(featType.equals("BOW")){
//						corpus.add(sentence.toLowerCase().trim());
//					}else{
//						corpus.add(feature.toLowerCase().trim());
//					}
//=======
//					//JSONObject labelObj = (JSONObject) labelArr.get(ii);
//>>>>>>> .r10246
//
//<<<<<<< .mine
//					String filename = (String) jsonLineObject.get("fileName");
//=======
//					String aspect = (String) labelObj.get("aspect");
//					String phrase = (String) labelObj.get("phrase");
//					String startOffset = (String) labelObj.get("startOffset");
//					String endOffset = (String) labelObj.get("endOffset");
//					//sidInfoList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
//					this.headerInforList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
//					usedLabels.add(aspect.trim()+" ");
//					labels.add(aspect.trim()+" ");
//					String beginOffset = startOffset.replaceAll("[\\(\\)]", "");
//					int sentNo = Integer.valueOf(beginOffset.split(":")[0]);
//					Pair<String,Integer> fileSentPair = new Pair<String,Integer>(filename,sentNo);
//					//corpusIndHm.put(fileSentPair, sentence);
//					corpusIndHm.put(fileSentPair, feature);
//>>>>>>> .r10246
//
//
//					JSONObject labelObj = (JSONObject)jsonLineObject.get("labels");
//					String aspect = (String) labelObj.get("aspect");
//					String phrase = (String) labelObj.get("phrase");
//					String startOffset = (String) labelObj.get("startOffset");
//					String endOffset = (String) labelObj.get("endOffset");
//					//sidInfoList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
//					this.headerInforList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
//					usedLabels.add(aspect.trim()+" ");
//					labels.add(aspect.trim()+" ");
//					String beginOffset = startOffset.replaceAll("[\\(\\)]", "");
//					int sentNo = Integer.valueOf(beginOffset.split(":")[0]);
//					Pair<String,Integer> fileSentPair = new Pair<String,Integer>(filename,sentNo);
//					//corpusIndHm.put(fileSentPair, sentence);
//					corpusIndHm.put(fileSentPair, feature);
//				}
			else if(sourceType.contains("i2b2_concepts")){
					String filename = (String) jsonLineObject.get("fileName");
					JSONArray document = (JSONArray) jsonLineObject.get("document");
					String docText = "";
					System.out.println(filename);
					if(document==null){
						continue;
					}
					for(int i=0;i<document.size();i++){
						JSONObject sentObj = (JSONObject) document.get(i);
						String sentence = (String) sentObj.get("sentence");
						JSONObject labelObj = (JSONObject)sentObj.get("labels");
						String concept = (String) labelObj.get("concept");
						String phrase = (String) labelObj.get("phrase");
						String startOffset = (String) labelObj.get("startOffset");
						String endOffset = (String) labelObj.get("endOffset");
						//sidInfoList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
						//this.headerInforList.add(filename+ " "+phrase+" "+startOffset+" "+endOffset);
						//usedLabels.add(concept.trim()+" ");
						//labels.add(concept.trim()+" ");
						String beginOffset = startOffset.replaceAll("[\\(\\)]", "");
						int sentNo = Integer.valueOf(beginOffset.split(":")[0]);
						Pair<String,Integer> fileSentPair = new Pair<String,Integer>(filename,sentNo);
						corpusIndHm.put(fileSentPair, sentence);
						docText +=  sentence+" ";
					}
					labels.add(filename);
					headerInforList.add(filename);
					corpus.add(docText);
				}
				countLine++;
			}
			bf.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * data/sample.json data/titleMesh.txt data/abstract.txt bioasq title:meshMajor:abstractText
	 * /Users/m048100/Documents/modelblocks/mallet/sample-data/clinical/myocardial_infarction.sparql data/titleMesh.txt data/abstract.txt ade-2 setId=sectionText
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		JsonFileParser parser = new JsonFileParser();
		parser.parseJsonFile(args);
		List<String> labelset = parser.getLset();
		for(String label : labelset){
			System.out.println(label);
		}
		List<String> corpus = parser.getCorpus();
		for(String doc : corpus){
			System.out.println(doc);
		}
		List<String> labels = parser.getLabels();
		for(String label : labels){
			System.out.println(label);
		}
		List<String> headerInforList = parser.getPmids();
		for(String pmid : headerInforList){
			System.out.println(pmid);
		}
	}
}