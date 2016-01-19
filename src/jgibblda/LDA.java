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

import org.kohsuke.args4j.*;

public class LDA {
	//-est -alpha 2.5 -beta 0.01 -ntopics 20 -niters 1000 -savestep 100 -twords 20 -dfile all_xml_revised_i2b2Toldainput.txt -inputdir ../i2b2/i2b2Challenge2012/Data/TrainingData -modelDir ../i2b2/i2b2Challenge2012/Data/TrainingData/ldaTrainingModels -model i2b22012 -source headerIncluded:topicinput.txt -wordmap topicinput.word2idmap
	//-inf -ntopics 20 -alpha 2.5 -beta 0.01 -modelDir ../i2b2/i2b2Challenge2012/Data/TrainingData/ldaTrainingModels -model i2b22012-final -niters 1000 -twords 100 -inputdir ../i2b2/i2b2Challenge2012/Data/TestingData/ground_truth -dfile merged_xml_revised_i2b2Toldainput.txt -source i2b2_directory:../i2b2/i2b2Challenge2012/Data/TestingData/ground_truth:txt  -wordmap topicinput.word2idmap
	//-est -alpha 0.5 -beta 0.02 -ntopics 100 -niters 1000 -savestep 100 -twords 20 -dfile topicinput.txt  -inputdir /home/dingcheng/ir/depfraglm-textspace-trecpittnlp -modelDir /home/dingcheng/ir/depfraglm-textspace-trecpittnlp/ldaModels -model trecpittnlp -source headerIncluded:topicinput.txt -wordmap topicinput.word2idmap
//-est -alpha 0.25 -beta 0.1 -ntopics 50 -niters 1000 -savestep 100 -twords 20 -dfile concepts.json -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/json -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_txt.uniNorm_eventStr -source i2b2_conceptsjson -wordmap uniNorm_eventStr.word2idmap	
//	-est -alpha 0.25 -beta 0.1 -ntopics 100 -niters 1000 -savestep 100 -twords 20 -dfile concepts.json -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/json/ -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_concepts_nopunc.uniNorm_eventStr -source i2b2_concepts_json -wordmap uniNorm_eventStr.concepts.nopunc.word2idmap
	//-est -alpha 0.25 -beta 0.1 -ntopics 100 -niters 1000 -savestep 100 -twords 20 -dfile generifs_basic.json -inputdir /Users/m048100/Documents/corpora/BioCreative -modelDir /Users/m048100/Documents/corpora/BioCreative/ldaModels -model generifs_basic -source generifs_basic_json -wordmap generifs_basic.word2idmap
	//-est -alpha 0.25 -beta 0.1 -ntopics 100 -niters 1000 -savestep 100 -twords 20 -dfile aspects.json -inputdir //Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/json -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_txt.uniNorm_eventStr -source i2b2json -wordmap uniNorm_eventStr.wordmap
	//-est -alpha 0.5 -beta 0.1 -ntopics 6 -niters 100 -savestep 10 -twords 20 -dfile onefile.txt -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/fullDocLdaModels -model i2b22010_txt.uniNorm -source i2b2_directory:/Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/txt:txt -wordmap uniNorm.wordmap
	//for windows:
	//-est -alpha 0.5 -beta 0.1 -ntopics 6 -niters 100 -savestep 10 -twords 20 -dfile onefile.txt -inputdir C:\Users\m048100\Documents\i2b2\i2b2Challenge2010\Data\release3_merged -modelDir C:\Users\m048100\Documents\i2b2\i2b2Challenge2010\Data\release3_merged\fullDocLdaModels -model i2b22010_txt.uniNorm -source i2b2_directory:C:\Users\m048100\Documents\i2b2\i2b2Challenge2010\Data\release3_merged\txt:txt -wordmap uniNorm.wordmap
	
	//-est -alpha 0.5 -beta 0.1 -ntopics 6 -niters 1000 -savestep 10 -twords 20 -dfile aspects.json -inputdir C:\Users\m048100\Documents\i2b2\i2b2Challenge2010\Data\release3_merged\json -modelDir C:\Users\m048100\Documents\i2b2\i2b2Challenge2010\Data\release3_merged\ldaModels -model i2b22010_txt.uniNorm-eventStr -source i2b2json -wordmap uniNorm.wordmap
	
	//for full text
	//-inf -ntopics 6 -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/fullDocLdaModels -model i2b22010_txt.uniNorm-final -niters 30 -twords 100 -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/Test/reports -dfile onefile.txt -source i2b2_directory:/Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/Test/reports/txt:txt  -wordmap uniNorm.wordmap
	//-est -alpha 0.5 -beta 0.1 -ntopics 6 -niters 100 -savestep 10 -twords 20 -dfile onefile.txt -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/fullDocLdaModels -model i2b22010_txt.uniNorm -source i2b2_directory:/Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/txt:txt -wordmap uniNorm.wordmap
	
	//-inf -ntopics 6 -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_txt.bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures-final -niters 30 -twords 100 -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/Test/reports/json -dfile aspects.json -source i2b2json  -wordmap bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix.wordmap
	//-est -alpha 0.5 -beta 0.1 -ntopics 6 -niters 100 -savestep 10 -twords 20 -dfile aspects.json -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/json -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_txt.uniNorm -source i2b2json -wordmap uniNorm.wordmap
	//-est -alpha 0.5 -beta 0.1 -ntopics 6 -niters 100 -savestep 10 -twords 20 -dfile aspects.json -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/json -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_txt.bigram.uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures -source i2b2json -wordmap bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix.wordmap
	//-inf -ntopics 6 -modelDir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/release3_merged/ldaModels -model i2b22010_txt.bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures-final -niters 30 -twords 100 -inputdir /Users/m048100/Documents/i2b2/i2b2Challenge2010/Data/Test/reports/json -dfile aspects.json -source i2b2json  -wordmap bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix.wordmap
	//-est -alpha 0.5 -beta 0.1 -ntopics 5 -niters 100 -savestep 10 -twords 20 -dir /Users/m048100/Documents/workspace-sts-2.9.2.RELEASE/BioASQ/data/Task1bPhaseB/ -model 51659356298dcd4e5100005a -dfile 51659356298dcd4e5100005a_snippets.txt -modelDir /Users/m048100/Documents/workspace-sts-2.9.2.RELEASE/BioASQ/data/Task1bPhaseBModels/
	
	
	
//	/Users/ningxia/Works/workspace/Tools/i2b2Challenge2010/Data/release3_merged/ldaModels/i2b22010_txt.bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures-final_6.others
//	/Users/ningxia/Works/workspace/Tools/i2b2Challenge2010/Data/release3_merged/ldaModels/i2b22010_txt.bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures-final_6.others
//	/Users/ningxia/Works/workspace/Tools/i2b2Challenge2010/Data/release3_merged/ldaModels/i2b22010_txt.bigram.uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures-final_6.others
//
//	bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures.wordmap
//
//	/Users/ningxia/Works/workspace/Tools/i2b2Challenge2010/Data/release3_merged/ldaModels/i2b22010_txt.bigram.uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures-final_6.others
//
//	/Users/ningxia/Works/workspace/Tools/i2b2Challenge2010/Data/release3_merged/ldaModels/
//
//	bigram.uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures.wordmap
//	bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix.wordmap
//
//	/Users/ningxia/Works/workspace/Tools/i2b2Challenge2010/Data/release3_merged/ldaModels/
//
//	bigram.uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures.wordmap
//	bigram_uniNorm_eventStr_context_document_postTagging_orthography_affix_lda_docfeatures.wordmap
	
	
	public static void main(String args[]){
		LDACmdOption option = new LDACmdOption();
		CmdLineParser parser = new CmdLineParser(option);
		
		try {
			if (args.length == 0){
				showHelp(parser);
				return;
			}
			
			parser.parseArgument(args);
			
			if (option.est || option.estc){
				Estimator estimator = new Estimator();
				estimator.init(option);
				estimator.estimate();
			}
			else if (option.inf){
				Inferencer inferencer = new Inferencer();
				inferencer.init(option);
				
				Model newModel = inferencer.inference();
			
				for (int i = 0; i < newModel.phi.length; ++i){
					//phi: K * V
					System.out.println("-----------------------\ntopic" + i  + " : ");
					for (int j = 0; j < 10; ++j){
						System.out.println(newModel.phi[i][j]);
						//System.out.println(inferencer.globalDict.id2word.get(j) + "\t" + newModel.phi[i][j]);
					}
				}
			}
		}
		catch (CmdLineException cle){
			System.out.println("Command line error: " + cle.getMessage());
			showHelp(parser);
			return;
		}
		catch (Exception e){
			System.out.println("Error in main: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public static void showHelp(CmdLineParser parser){
		System.out.println("LDA [options ...] [arguments...]");
		parser.printUsage(System.out);
	}
	
}
