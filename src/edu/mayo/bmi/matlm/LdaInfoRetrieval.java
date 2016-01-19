package edu.mayo.bmi.matlm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import edu.mayo.bmi.helper.MapUtil;
import Jama.Matrix;
import Jama.util.Maths;

/**
 * This script utilizes the outputs of jgibblda and employs the IR models (Xing W's paper, Yanshan W's paper).
 * @author: m140972
 */

public class LdaInfoRetrieval {
	
	public static int numOfTerm;
	public static int numOfDoc;
	public static int numOfTopic;
	public static int numOfQuery;
	public static List<String> docID = new ArrayList<String>();
	public static List<String> queryID = new ArrayList<String>();
	public static HashMap<String,String> doc2vistHm = new HashMap<String, String>();
	
	/**
	 * Determine numOfTopic, numOfTerm, numOfDoc from phi and theta file.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 */
	public void preReadFile(String phiFileName, String thetaFileName) throws IOException{
		BufferedReader bfDocList = new BufferedReader(new FileReader(phiFileName));
		String line = "";
		int k = 0; //number of topics
		while((line=bfDocList.readLine())!=null){
			k += 1;
			if(k == 1){
				String[] tempStr = this.splitString(line,"\\s+");
				numOfTerm = tempStr.length;
			}
		}
		numOfTopic = k;
		
		bfDocList = new BufferedReader(new FileReader(thetaFileName));
		line = "";
		int m = 0; //number of docs
		while((line=bfDocList.readLine())!=null){
			m += 1;
		}
		numOfDoc = m;
	}
	/**
	 * Determine numOfTopic, numOfTerm, numOfDoc from .other file.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 */
	public void readOtherFile(String otherFileName) throws IOException{
		BufferedReader bfDocList = new BufferedReader(new FileReader(otherFileName));
		String line = "";
		while((line=bfDocList.readLine())!=null){
			String[] tempStr = this.splitString(line,"=");
			if(tempStr[0].equals("ntopics"))
				numOfTopic = Integer.valueOf(tempStr[1]);
			else if(tempStr[0].equals("ndocs"))
				numOfDoc = Integer.valueOf(tempStr[1]);
			else if(tempStr[0].equals("nwords"))
				numOfTerm = Integer.valueOf(tempStr[1]);
		}
		bfDocList.close();
	}
	/**
	 * Read phi file to a K x N matrix.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public Matrix readPhi(String phiFileName) throws NumberFormatException, IOException{
		double[][] arrayPhi = new double[numOfTopic][numOfTerm];
		BufferedReader bfDocList = new BufferedReader(new FileReader(phiFileName));
		String line = "";
		int k = 0; //number of topics
		while((line=bfDocList.readLine())!=null){
			String[] tempStr = this.splitString(line,"\\s+");
			for(int j=0;j<tempStr.length;j++){
				arrayPhi[k][j] = Double.valueOf(tempStr[j]);
			}
			k += 1;
		}
		bfDocList.close();
		Matrix matrixPhi = new Matrix (arrayPhi);
		return matrixPhi;
	}
	
	/**
	 * Read theta file to a M x K matrix.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public Matrix readTheta(String thetaFileName) throws NumberFormatException, IOException{
		double[][] arrayTheta = new double[numOfDoc][numOfTopic];
		BufferedReader bfDocList = new BufferedReader(new FileReader(thetaFileName));
		String line = "";
		int m = 0; //number of docs
		while ((line = bfDocList.readLine()) != null) {
			String[] tempStr = this.splitString(line, "\\s+");
			// check if the .theta file contains ID at each line
			if (tempStr[0].contains("0.")) {
				for (int j = 0; j < tempStr.length; j++) {
					arrayTheta[m][j] = Double.valueOf(tempStr[j]);
				}
			} else {
				for (int j = 1; j < tempStr.length; j++) {
					arrayTheta[m][j-1] = Double.valueOf(tempStr[j]);
				}
			}

			m += 1;
		}
		bfDocList.close();
		Matrix matrixTheta = new Matrix (arrayTheta);
		return matrixTheta;
	}
	/**
	 * Read TREC theta file, which has docID at the begining of each line.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public Matrix readTrecTheta(String thetaFileName) throws NumberFormatException, IOException{
		double[][] arrayTheta = new double[numOfDoc][numOfTopic];
		BufferedReader bfDocList = new BufferedReader(new FileReader(thetaFileName));
		String line = "";
		int m = 0; //number of docs
		while((line=bfDocList.readLine())!=null){
			String[] tempStr = this.splitString(line,"\\s+");;
			for(int j=1;j<tempStr.length;j++){
				arrayTheta[m][j-1] = Double.valueOf(tempStr[j]);
			}
			m += 1;
		}
		bfDocList.close();
		Matrix matrixTheta = new Matrix (arrayTheta);
		return matrixTheta;
	}
	/**
	 * Initialize a M x N matrix with zeros.
	 * Requires .
	 * @param 
	 */
	public static double[][] initZeros(int m, int n){
		double[][] mat = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				mat[i][j] = 0;
			}
		}
		return mat;
	}
	
	/**
	 * Initialize a M x N matrix with ones.
	 * Requires .
	 * @param 
	 */
	public static double[][] initOnes(int m, int n){
		double[][] mat = new double[m][n];
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++){
				mat[i][j] = 1;
			}
		}
		return mat;
	}
	/**
	 * Split a string and return nonempty string array.
	 * Requires .
	 * @param 
	 */
	public static String[] splitString(String line, String del){
		List<String> strList = new ArrayList<String>();
		String[] temp = line.split(del);
		for(String i:temp){
			if(!i.equals("")&&i!=null)
				strList.add(i);
		}
		String[] str = strList.toArray(new String[strList.size()]);
		return str;
	}
	/**
	 * Build a M x N term-document matrix where M is the number of document and N is the number of term.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 */
	public Matrix buildQueryTermDocMatrix (String queryFileName, String vocFileName) throws IOException{
		double[][] termDoc = this.initZeros(numOfQuery, numOfTerm);
		HashMap<String, Integer> vocList = this.buildVocList(vocFileName);
		BufferedReader bfDocList = new BufferedReader(new FileReader(queryFileName));
		String line = "";
		int m = 0; //number of docs
		while((line=bfDocList.readLine())!=null){
			String[] str = this.splitString(line,"\\s+");
			queryID.add(str[0]);  //first is queryID in the format.
			for(int i=1;i<str.length;i++){
				if(vocList.containsKey(str[i].toLowerCase())){  //find in vocabulary
					termDoc[m][Integer.valueOf(vocList.get(str[i].toLowerCase()))] += 1;
				}  
			}
			m += 1;
		}
		Matrix termDocMatrix = new Matrix(termDoc);
		bfDocList.close();
		return termDocMatrix;
	}
	
	/**
	 * Build a M x N term-document matrix where M is the number of document and N is the number of term.
	 * Requires .
	 * @param 
	 * @throws IOException 
	 */
	public Matrix buildTermDocMatrix (String docFileName, HashMap<String, Integer> vocList) throws IOException{
		double[][] termDoc = this.initZeros(numOfDoc, numOfTerm);
		BufferedReader bfDocList = new BufferedReader(new FileReader(docFileName));
		String line = "";
		int m = 0; //number of docs
		while((line=bfDocList.readLine())!=null){
			String[] str = this.splitString(line,"\\s+");
			docID.add(str[0]);  //first is docID in the format.
			for(int i=1;i<str.length;i++){
				if(vocList.containsKey(str[i].toLowerCase())){  //find in vocabulary
					termDoc[m][Integer.valueOf(vocList.get(str[i].toLowerCase()))] += 1;
				}  
			}
			m += 1;
		}
		Matrix termDocMatrix = new Matrix(termDoc);
		bfDocList.close();
		return termDocMatrix;
	}
	
	/**
	 * Build a M x 1 vector, meaning number of words in each document. 
	 * Requires .
	 * @param 
	 */
	public Matrix buildTermFreqVec(Matrix termDocMatrix){
		double[][] tempTermFreq = this.initZeros(termDocMatrix.getRowDimension(), 1);
		for(int i=0;i<termDocMatrix.getRowDimension();i++){
			for(int j=0;j<termDocMatrix.getColumnDimension();j++){
				tempTermFreq[i][0] += termDocMatrix.get(i, j);
			}
		}
		Matrix termFreqMatrix = new Matrix(tempTermFreq);
		return termFreqMatrix;
	}
	/**
	 * Build a 1 x N vector, meaning frequency of each word in collection.
	 * Requires .
	 * @param 
	 */
	public Matrix buildTermFreqCol(Matrix termDocMatrix){
		double[][] tempTermFreq = LdaInfoRetrieval.initZeros(1, termDocMatrix.getColumnDimension());
		for(int j=0;j<termDocMatrix.getColumnDimension();j++){
			for(int i=0;i<termDocMatrix.getRowDimension();i++){
				tempTermFreq[0][j] += termDocMatrix.get(i, j);
			}
		}
		Matrix termFreqMatrix = new Matrix(tempTermFreq);
		return termFreqMatrix;
	}
	/**
	 * Sum of a M x 1 vector.
	 * Requires .
	 * @param 
	 */
	public static double sumVec(Matrix vec){
		double val = 0;
		for(int i=0;i<vec.getRowDimension();i++){
			val+=vec.get(i, 0);
		}
		return val;
	}
	/**
	 * Multiplication of 2d double array.
	 * Requires .
	 * @param 
	 */
	public static double[][] mul2dArray(double[][] firstarray, double[][] secondarray){
		double[][] result = new double[firstarray.length][secondarray[0].length];
		for (int i = 0; i < firstarray.length; i++) { 
		    for (int j = 0; j < secondarray[0].length; j++) { 
		        for (int k = 0; k < firstarray[0].length; k++) { 
		            result[i][j] += firstarray[i][k] * secondarray[k][j];
		        }
		    }
		}
		return result;
	}
	/**
	 * Sum of 2d double array.
	 * Requires .
	 * @param 
	 */
	public static double[][] sum2dArray(double[][] firstarray, double[][] secondarray){
		double[][] result = new double[firstarray.length][secondarray[0].length];
		for (int i = 0; i < firstarray.length; i++) { 
		    for (int j = 0; j < secondarray[0].length; j++) { 
		    	result[i][j] = firstarray[i][j] + secondarray[i][j];
		    }
		}
		return result;
	}
	/**
	 * Build a M x M diagonal matrix using M x 1 vector.
	 * Requires .
	 * @param 
	 */
	public Matrix vec2Diagonal(Matrix vec){
		double[][] diag = new double[vec.getRowDimension()][vec.getRowDimension()];
		for(int i=0;i<vec.getRowDimension();i++){
			for(int j=0;j<vec.getRowDimension();j++){
				if(i == j)
					diag[i][j] = vec.get(i, 0);
				else
					diag[i][j] = 0;
			}
		}
		Matrix diagMatrix = new Matrix(diag);
		return diagMatrix;
	}
	/**
	 * Return an inverse for a M x M diagonal matrix. In other words, the elements at (i,i) position change to 1/ele.
	 * Requires .
	 * @param 
	 */
	public Matrix diagonalInverse(Matrix vec){
		double[][] diag = new double[vec.getRowDimension()][vec.getColumnDimension()];
		for(int i=0;i<vec.getRowDimension();i++){
			for(int j=0;j<vec.getColumnDimension();j++){
				if(i == j)
					diag[i][j] = 1/vec.get(i, j);
				else
					diag[i][j] = 0;
			}
		}
		Matrix diagMatrix = new Matrix(diag);
		return diagMatrix;
	}
	/**
	 * Build a M x N term-document probability matrix using Query Likelihood Model.
	 * Requires .
	 * @param 
	 */
	public Matrix buildQLMTermDocProbMatrix(Matrix termDocMatrix, double mu){
		Matrix Nd = this.buildTermFreqVec(termDocMatrix);
		Matrix Mu = new Matrix(this.initOnes(termDocMatrix.getRowDimension(), 1));
		Matrix diagTemp = this.diagonalInverse(this.vec2Diagonal(Nd.plus(Mu)));
		double Nc1 = 1 / this.sumVec(Nd);
		Matrix Id = new Matrix(this.initOnes(termDocMatrix.getRowDimension(), 1));
		Matrix C = this.buildTermFreqCol(termDocMatrix);
		Matrix diagTemp2 = this.vec2Diagonal(diagTemp.times(Mu));
		if(termDocMatrix.getRowDimension()<5000){
			Matrix termDocProbMatrix = diagTemp.times(termDocMatrix).plus(diagTemp2.times(Nc1).times(Id).times(C));
			return termDocProbMatrix;
		}
		else {
			System.out.println("compute termDoc array");
			double[][] termDocTemp = this.mul2dArray(diagTemp.getArray(), termDocMatrix.getArray());
			System.out.println("compute termDocProb array");
			Matrix second = diagTemp2.times(Nc1).times(Id).times(C);
			double[][] termDocProbTemp = this.sum2dArray(termDocTemp, second.getArray());
			Matrix termDocProbMatrix = new Matrix(termDocProbTemp);
			return termDocProbMatrix;
		}
	}
	
	/**
	 * Build a vocabulary list.
	 * Requires id2wordmap from JGibblda.
	 * @param 
	 */
	public HashMap<String, Integer> buildVocList(String vocFileName) throws IOException{
		HashMap<String, Integer> vocList = new HashMap<String, Integer>();
		BufferedReader bfDocList = new BufferedReader(new FileReader(vocFileName));
		String line = "";
		//number of words in first line
		numOfTerm = Integer.valueOf(bfDocList.readLine());
		while((line=bfDocList.readLine())!=null){
			String[] str = this.splitString(line,"\\s+");;
			vocList.put(str[1], Integer.valueOf(str[0]));
		}
		bfDocList.close();
		return vocList;
	}
	/**
	 * Calculate query-doc score (QueryLikelihoodModel).
	 * Requires .
	 * @param 
	 */
	public Matrix calQueryDocScore(Matrix termDocProbMatrix, Matrix queryTermMatrix){
		Matrix queryDocScore = termDocProbMatrix.times(queryTermMatrix.transpose()).transpose();
		return queryDocScore;
	}
	/**
	 * Calculate query-doc score (LDA-based Document Model (LBDM) by Wei&Croft).
	 * Requires .
	 * @param 
	 */
	public Matrix calLBDMQueryDocScore(Matrix termDocProbMatrix, Matrix phiMatrix, Matrix thetaMatrix, Matrix queryTermMatrix, double lambda){
		Matrix queryDocScore = termDocProbMatrix.times(lambda).plus(thetaMatrix.times(phiMatrix).times(1-lambda)).times(queryTermMatrix.transpose()).transpose();
		return queryDocScore;
	}
	/**
	 * Calculate query-doc score (Indexing by LDA (LDI) by Wang et al).
	 * Requires .
	 * @param 
	 */
	public Matrix calLDIQueryDocScore(Matrix termDocMatrix, Matrix phiMatrix, Matrix queryTermMatrix){
		Matrix d = this.buildTermFreqVec(termDocMatrix);
		Matrix termDocMatrixTran = termDocMatrix.transpose();
		Matrix D = this.topicSpace(phiMatrix);
		//p(z|d)=D.f'.diag(d)
		Matrix topicDocProb = D.times(termDocMatrixTran).times(this.vec2Diagonal(d));
		//p(z|q)=D.q'
		Matrix topicQueryProb = D.times(queryTermMatrix.transpose());		
		Matrix termDocProbMatrix = this.calMatCosSim(topicDocProb, topicQueryProb);	
		return termDocProbMatrix.transpose();
	}
	/**
	 * A is a K x L matrix, B is a K X M matrix. Return L X M matrix.
	 * Requires .
	 * @param 
	 */
	public Matrix calMatCosSim(Matrix A, Matrix B){
		int K = A.getRowDimension();
		int L = A.getColumnDimension();
		int M = B.getColumnDimension();
		double[][] cosSimArray = new double[L][M];
		for(int i=0;i<L;i++){
			int[] colA = {i};
			Matrix vec1 = A.getMatrix(0, K-1, colA);
			for(int j=0;j<M;j++){
				int[] colB = {j};				
				Matrix vec2 = B.getMatrix(0, K-1, colB);
				cosSimArray[i][j] = cosSimilarity(vec1,vec2);;
			}
		}
		Matrix cosSimMatrix = new Matrix(cosSimArray);
		return cosSimMatrix;
	}
	/**
	 * Return cosine similarity of two vectors.
	 * Requires .
	 * @param 
	 */
	public double cosSimilarity(Matrix A, Matrix B){
		double dotProduct = A.arrayTimes(B).norm1();
	    double eucledianDist = A.normF() * B.normF();
	    return dotProduct / eucledianDist;
	}
	/**
	 * Calculate p(z|w) from p(w|z)(phi).
	 * Requires .
	 * @param 
	 */
	public Matrix topicSpace(Matrix phiMatrix){
		int m = phiMatrix.getRowDimension();
		int n = phiMatrix.getColumnDimension();
		double[][] temp = new double[m][n];
		for(int j=0;j<n;j++){
			int[] col = {j};
			Matrix vec = phiMatrix.getMatrix(0, m-1, col);
			double sum = this.sumVec(vec);
			for(int i=0;i<m;i++){
				temp[i][j] = phiMatrix.get(i, j)/sum;
			}
		}
		Matrix termProbInTopic = new Matrix(temp);
		return termProbInTopic;
	}
	/**
	 * Save queryDocScore to TREC format.
	 * Requires number of document to retrieve.
	 * @param 
	 * @throws IOException 
	 */
	public void save2TrecForm(int retNum, Matrix queryDocScore, String outFileName) throws IOException{
		int l = queryDocScore.getRowDimension();
		int m = queryDocScore.getColumnDimension();
		if(retNum>queryDocScore.getColumnDimension())
			retNum = queryDocScore.getColumnDimension();
		PrintWriter pw = new PrintWriter(new FileWriter(outFileName));
		for(int i=0;i<l;i++){
			String qID = queryID.get(i);
			Map<String, Double> tempDocProb = new HashMap<String, Double>();
			for(int j=0;j<m;j++){
				String dID = docID.get(j);
				tempDocProb.put(dID, queryDocScore.get(i, j));
			}
			//sort tempDocProb
			Map<String,Double> sortedDocProb = MapUtil.sortByDescendingValue(tempDocProb);
			int k = 1;
			for(Map.Entry<String, Double> entry1 : sortedDocProb.entrySet()){
				if(k>retNum)
					break;
				String visitId = entry1.getKey();
				double score = entry1.getValue();
				pw.println(qID+"\tQ0\t"+"\t"+visitId+"\t"+k+"\t"+score+"\t"+"docContent");
				k++;
			}
		}
		pw.flush();
		pw.close();
	}
	/**
	 * Save queryDocScore to TREC Visit format.
	 * Requires number of document to retrieve.
	 * @param 
	 * @throws IOException 
	 */
	public void save2TrecVisitForm(int retNum, Matrix queryDocScore, String outFileName) throws IOException{
		int l = (int) queryDocScore.getRowDimension();
		int m = (int) queryDocScore.getColumnDimension();
		if(retNum>queryDocScore.getColumnDimension())
			retNum = (int) queryDocScore.getColumnDimension();
		PrintWriter pw = new PrintWriter(new FileWriter(outFileName));
		for(int i=0;i<l;i++){
			String qID = queryID.get(i);
			Map<String, Double> tempDocProb = new HashMap<String, Double>();
			for(int j=0;j<m;j++){
				String dID = docID.get(j);
				double score = queryDocScore.get(i, j);
				String visitID = this.doc2vistHm.get(dID);
				if(tempDocProb.containsKey(visitID)){
					double maxScore = tempDocProb.get(visitID);
					if(maxScore < score)
						tempDocProb.put(visitID, score);
				}
				else{
					tempDocProb.put(visitID, score);
				}
			}
			//sort tempDocProb
			Map<String,Double> sortedDocProb = MapUtil.sortByDescendingValue(tempDocProb);
			int k = 1;
			for(Map.Entry<String, Double> entry1 : sortedDocProb.entrySet()){
				if(k>retNum)
					break;
				String visitId = entry1.getKey();
				double score = entry1.getValue();
				pw.println(qID+"\tQ0\t"+"\t"+visitId+"\t"+k+"\t"+score+"\t"+"docContent");
				k++;
			}
		}
		pw.flush();
		pw.close();
	}
	/**
	 * convert doc id to visit id.
	 * Requires .
	 * @param 
	 */
	public static void convertDid2VisitId(String mapDir) {
		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(new File(mapDir)));
			String line = "";
			while((line=bf.readLine())!=null){
				String[] lineArr = line.split("\\s+");
				String docId = "/data5/bsi/nlp/s110067.sharp/ir/depfraglm-textspace-trecpittnlp/trecpittnlp/" + lineArr[lineArr.length-1] + ".xml.text.txt"; //for TREC result
				String visitId = lineArr[lineArr.length-2];
				doc2vistHm.put(docId, visitId);
			}
			bf.close();
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	public static void main(String[] args) throws IOException {
		args = new String[9];
		args[0] = "resources/ICHI2015-DA-Challenge-Sample-Data-Sets/topicinput.id2wordmap";
		args[1] = "resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_for_GibbLDA";
		args[3] = "resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_query_for_GibbLDA";
		args[4] = "resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_model-final_alpha=2.5_beta=0.01_k=300.phi";
		args[5] = "resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_model-final_alpha=2.5_beta=0.01_k=300.theta";
		args[6] = "1000";
		args[7] = "0.7";
		args[8] = "resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_model-final_alpha=2.5_beta=0.01_k=300.others";
		LdaInfoRetrieval ldaIr = new LdaInfoRetrieval();
		ldaIr.numOfQuery=10;
		//initialize num of word, topic, doc..
		
		ldaIr.readOtherFile(args[8]);
		Matrix phiMatrix = ldaIr.readPhi(args[4]);
		Matrix thetaMatrix = ldaIr.readTheta(args[5]);
		HashMap<String, Integer> vocList = ldaIr.buildVocList(args[0]);
		Matrix termDocMatrix = ldaIr.buildTermDocMatrix(args[1], vocList);
		double mu = Double.valueOf(args[6]);
		Matrix termDocProbMatrix = ldaIr.buildQLMTermDocProbMatrix(termDocMatrix, mu);
		Matrix queryTermMatrix = ldaIr.buildQueryTermDocMatrix(args[3], args[0]);
		Matrix queryDocScore = ldaIr.calQueryDocScore(termDocProbMatrix, queryTermMatrix);
		
		//Query likelihood Document Model////////////////////////////////////////////////////////////////////
		int retNum = 100;
		ldaIr.save2TrecForm(retNum, queryDocScore, "eval/ICHI2015_qldm_result.txt");
		System.out.println("QLDM result saved.");

		//LBDM, Wei&Croft///////////////////////////////////////////////////////////////////////////////////
		double lambda = Double.valueOf(args[7]);
		Matrix queryDocScoreLBDM = ldaIr.calLBDMQueryDocScore(termDocProbMatrix, phiMatrix, thetaMatrix, queryTermMatrix, lambda);
		//save output
		PrintWriter pw = new PrintWriter(new FileWriter("resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_queryDocScore_k=300.txt"));
		queryDocScoreLBDM.print(pw, 3, 6);
		pw.flush();
		pw.close();		
		//save to TREC format
		ldaIr.save2TrecForm(retNum, queryDocScoreLBDM, "eval/ICHI2015_lbdm_result_k=300.txt");
		System.out.println("LBDM result saved.");

		//LDI//////////////////////////////////////////////////////////////////////////////////////////////
		Matrix queryDocScoreLDI = ldaIr.calLDIQueryDocScore(termDocMatrix, phiMatrix, queryTermMatrix);	
		//save output
		pw  = new PrintWriter(new FileWriter("resources/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_queryDocScore_LDI_k=300.txt"));
		queryDocScoreLDI.print(pw, 3, 6);
		pw.flush();
		pw.close();		
		//save to TREC format
		ldaIr.save2TrecForm(retNum, queryDocScoreLDI, "eval/ICHI2015_ldi_result_k=300.txt");
		System.out.println("LDI result saved.");

	}

}
