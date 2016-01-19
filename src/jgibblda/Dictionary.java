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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class Dictionary {
	public SortedMap<String,Integer> word2id;
	public SortedMap<String,Integer> word4count;//word frequencies
	public SortedMap<Integer, String> id2word;
		
	//--------------------------------------------------
	// constructors
	//--------------------------------------------------
	
	public Dictionary(){
		word2id = new TreeMap<String, Integer>();
		word4count = new TreeMap<String, Integer>();
		id2word = new TreeMap<Integer, String>();
	}
	
	//---------------------------------------------------
	// get/set methods
	//---------------------------------------------------
	
	public String getWord(int id){
		return id2word.get(id);
	}
	
	public Integer getID (String word){
		return word2id.get(word);
	}
	
	public Integer getCount (String word){
		return word4count.get(word);
	}
	
	//----------------------------------------------------
	// checking methods
	//----------------------------------------------------
	/**
	 * check if this dictionary contains a specified word
	 */
	public boolean contains(String word){
		return word2id.containsKey(word);
	}
	
	public boolean contains(int id){
		return id2word.containsKey(id);
	}
	//---------------------------------------------------
	// manupulating methods
	//---------------------------------------------------
	/**
	 * add a word into this dictionary
	 * return the corresponding id
	 */
	public int addWord(String word){
		if(word.length()==0){
			return -1;
		}
		if (!contains(word)){
			int id = word2id.size();
			
			word2id.put(word, id);
			id2word.put(id,word);
			word4count.put(word, 1);
			return id;
		}
		else {
			int count = word4count.get(word)+1;
			word4count.put(word, count);
			return getID(word);		
		}
	}
	
	//---------------------------------------------------
	// I/O methods
	//---------------------------------------------------
	/**
	 * read dictionary from file
	 */
	public boolean readWordMap(String wordMapFile){		
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(wordMapFile), "UTF-8"));
			String line;
			
			//read the number of words
			line = reader.readLine();			
			int nwords = Integer.parseInt(line);
			
			//read map
			for (int i = 0; i < nwords; ++i){
				line = reader.readLine();
				StringTokenizer tknr = new StringTokenizer(line, " \t\n\r");
				//I see, since I am put word count as one field. It turned out that there are three columns now. 
				//no wonder testing would not work any more.
//				if (tknr.countTokens() != 2) continue;
//				
//				String word = tknr.nextToken();
//				String id = tknr.nextToken();
//				int intID = Integer.parseInt(id);
//				
//				id2word.put(intID, word);
//				word2id.put(word, intID);
				
				if (tknr.countTokens() != 3) continue;
				
				String word = tknr.nextToken();
				String id = tknr.nextToken();
				int intID = Integer.parseInt(id);
				
				String count = tknr.nextToken();
				int intCount = Integer.parseInt(count);
				
				id2word.put(intID, word);
				word2id.put(word, intID);
				word4count.put(word, intCount);
			}
			
			reader.close();
			return true;
		}
		catch (Exception e){
			System.out.println("Error while reading dictionary:" + e.getMessage());
			e.printStackTrace();
			return false;
		}		
	}
	
	public boolean writeWordMap(String wordMapFile){
		try{
			BufferedWriter word2idwriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(wordMapFile), "UTF-8"));
			wordMapFile = wordMapFile.replace("word2id", "id2word");
			
			BufferedWriter id2wordwriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(wordMapFile), "UTF-8"));
			
			//write number of words
			word2idwriter.write(word2id.size() + "\n");
			id2wordwriter.write(id2word.size() + "\n");
			
			//write word to id
			Iterator<String> itWord2Id = word2id.keySet().iterator();
			while (itWord2Id.hasNext()){
				String key = itWord2Id.next();
				Integer value = word2id.get(key);
				Integer count = word4count.get(key);
				word2idwriter.write(key + " " + value +" "+count+"\n");
			}
			
			word2idwriter.close();
			
			//write id to word 
			Iterator<Integer> itId2Word = id2word.keySet().iterator();
			while (itId2Word.hasNext()){
				int key = itId2Word.next();
				String value = id2word.get(key);
				id2wordwriter.write(key + " " + value + "\n");
			}
			
			id2wordwriter.close();
			return true;
		}
		catch (Exception e){
			System.out.println("Error while writing word map " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
}
