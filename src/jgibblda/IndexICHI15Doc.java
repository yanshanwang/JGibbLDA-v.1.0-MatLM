package jgibblda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.Doc;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class IndexICHI15Doc {
	
	public static Client client;
	public static IndexResponse response;
	
	/**
	 * Read original file to a HashMap for indexing. Key stores document ID, Value stores docTitle and docBody.
	 * @param annFileName
	 * @throws IOException 
	 */
	
	public HashMap<String, List<String>> readFile(String fileName){
		HashMap<String, List<String>> orgDoc = new HashMap<String, List<String>>();
		try{
			BufferedReader bfDocList = new BufferedReader(new FileReader(fileName));
			String line = "";
			try {
				while((line=bfDocList.readLine())!=null){
					List<String> docBody = new ArrayList<String>();
					String[] doc = line.split("\\s+|\\p{Punct}|�");
					if(doc.length!=1){
						String docID =  doc[1];
						String docTitle = "";
						String docCont = "";
						for(int i=2;i<doc.length;i++){
							if(!doc[i].isEmpty()&&!doc[i].equals(" ")&&doc[i].length()!=1)
								docTitle=docTitle+" "+doc[i];
						}
						docBody.add(docTitle);
						if((line=bfDocList.readLine())!=null){
							String[] docCont_temp = line.split("\\s+|\\p{Punct}|�");						
							for(int i=0;i<docCont_temp.length;i++){
								if(!docCont_temp[i].isEmpty()&&!docCont_temp[i].equals(" ")&&docCont_temp[i].length()!=1)
									docCont=docCont+" "+docCont_temp[i];
							}
							docBody.add(docCont);
						}
						orgDoc.put(docID, docBody);
					}
				}
				}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
		return orgDoc;
	}
	
	/**
	 * Read annotated file to a HashMap for indexing. Key stores document ID, Value stores contents (synonyms and concepts).
	 * @param annFileName
	 * @throws IOException 
	 */
	
	public HashMap<String, String> readAnnFile(String annFileName) throws IOException{
		HashMap<String, String> annDoc = new LinkedHashMap<String, String>();
		try{
			BufferedReader bfDoc = new BufferedReader(new FileReader(annFileName));
			String line = "";
			try{
				while((line=bfDoc.readLine())!=null){
					String syn = "";
					String[] doc = line.split("\\s+|\"");
					doc = removeEmpty(doc);
					if(annDoc.containsKey(doc[0])){
						syn = annDoc.get(doc[0]);
						for (int i=2;i<5;i++){
							syn=syn+" "+doc[i];
						}
					}else{
						for (int i=2;i<5;i++){
							syn=syn+" "+doc[i];
						}
					}
					annDoc.put(doc[0], syn);
			}
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}catch(FileNotFoundException e1){
			e1.printStackTrace();
		}
		
		return annDoc;
	}
	
	/**
	 * Remove empty elements in a String vector.
	 * @param str[]
	 */
	public String[] removeEmpty(String[] str){
		str = Arrays.stream(str)
                .filter(s-> (s != null && s.length() > 0))
                .toArray(String[]::new);
		return str;
	}
	
	/**
	 * Open index client.
	 */
	public static void openClient(){
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "elasticsearch_ysw").build();
		client = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));
	}
	
	/**
	 * Create index.
	 * @param indexName, typeName, document
	 */
	public static void createIndex(String indexName, String typeName, String id, String field, String value){
		response = client.prepareIndex(indexName, typeName, id)
				.setSource(field, value)
		        .execute()
		        .actionGet();
	}
	
	public static void main(String[] args) throws IOException {
		//Index Corpus and annotated Corpus (including synonyms and concepts).
		args = new String[2];
		args[0] = "src/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI-Challenge-Sample-Question-Corpus.txt"; //original file
		args[1] = "src/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI-Challenge-Sample-Question-Corpus.txt.ann_new"; //annotated file 
		
		IndexICHI15Doc index = new IndexICHI15Doc();
		HashMap<String, List<String>> orgDoc = index.readFile(args[0]);
		HashMap<String, String> annDoc = index.readAnnFile(args[1]);
		
		IndexICHI15Doc.openClient();
		for(Map.Entry<String, List<String>> entry:orgDoc.entrySet()){
			String docID = entry.getKey();
			String syn = "";
			List<String> temp = entry.getValue();
			if(annDoc.containsKey(docID)){
				syn = annDoc.get(docID);
			}
			response = client.prepareIndex("ichi2015", "ques", docID)
					.setSource(jsonBuilder()
		                    .startObject()
	                        .field("docID", docID)
	                        .field("docTitle", temp.get(0))
	                        .field("docContent", temp.get(1))
	                        .field("docSyn", syn)
	                    .endObject()
	                    )
			        .execute()
			        .actionGet();
			}
			
			
		}
		//IndexICHI15Doc.createIndex("ichi2015", "ques", new HashMap<String, Object>(orgDoc));
				
	
		
}


