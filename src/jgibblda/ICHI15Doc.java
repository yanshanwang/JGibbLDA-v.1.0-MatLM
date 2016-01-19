package jgibblda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICHI15Doc {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		args = new String[2];
		args[0] = "src/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI-Challenge-Sample-Test-Questions.txt";
		args[1] = "src/ICHI2015-DA-Challenge-Sample-Data-Sets/ICHI2015_query_for_GibbLDA";
		PrintWriter doc2gibblda = null;
		String dataFile = args[0];
		String writerN = args[1];
		class MyFilter implements FileFilter {
		    @Override
		    public boolean accept(File file) {
		      return !file.isHidden() && !file.getName().equalsIgnoreCase(".DS_Store");
		    }
		  }
		try {
			doc2gibblda = new PrintWriter(new FileWriter(new File(writerN)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try{
		BufferedReader bfDocList = new BufferedReader(new FileReader(dataFile));
		String line = "";
			try {
				while((line=bfDocList.readLine())!=null){					
					String[] DocCont = line.split("\\s+|\\p{Punct}|�");
					String str = "";
					for(int i=0;i<DocCont.length;i++){
						if(!DocCont[i].isEmpty()&&!DocCont[i].equals(" ")&&DocCont[i].length()!=1)
							str=str+" "+DocCont[i];
					}
					if(!str.isEmpty())
						doc2gibblda.println(str);
				}
					
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
}catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
		doc2gibblda.close();
	}

}
