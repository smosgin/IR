package calcwts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class TokenToWts {
	
	private final String STOPWORDS_FILENAME = "/calcwts/stoplist.txt";
	private HashSet<String> stopwords = new HashSet<String>();
	public static Map<String, Integer> tokenToFreq;
	public static Map<String, Integer> tokenToDocFreq;
	public static Map<String, Map<String, Integer>> documents;
	
	public TokenToWts(Map<String, Integer> map, Map<String, Map<String, Integer>> individuals){
		tokenToFreq = map;
		documents = individuals;
		generateStopHash();
		filter();
	}
	
	//generate a HashSet from the stopwords text file
	private void generateStopHash(){
	    try {
	    	String line;

	    	InputStream ris = TokenToWts.class.getResourceAsStream(STOPWORDS_FILENAME);
	    	InputStreamReader isr = new InputStreamReader(ris);
	    	BufferedReader input = new BufferedReader(isr);

	    	while ((line = input.readLine()) != null) {
	    		//Add stopword to hashset
	    		stopwords.add(line);
	    	}
	    	input.close();
	    } 
	    catch (FileNotFoundException e) {e.printStackTrace();} 
	    catch (IOException e) {e.printStackTrace();}
	}
	
	//Filter out tokens that are in the stop list, have a length of one
	//or only appear once in the entire corpus
	private void filter(){
		for(String doc : documents.keySet()){
			Map<String, Integer> temp = documents.get(doc);
			Iterator i = temp.entrySet().iterator();
			while(i.hasNext()){
				Entry<String, Integer> e = (Entry<String, Integer>) i.next();
				String token = e.getKey();
				if(stopwords.contains(token)){
					//temp.remove(token);
					i.remove();
				}
				else if(token.length() == 1){
					//temp.remove(token);
					i.remove();
				}
				else if(tokenToFreq.get(token) == 1){
					//temp.remove(token);
					i.remove();
				}
			}
		}
	}
	
	public static void iterateDirectory(String dirPath, String outputDir) {
		 
        File dir = new File(dirPath);
        File[] directoryListing = dir.listFiles();
        tokenToDocFreq = new HashMap<String, Integer>();
        
        //for each token in the corpus, determine how many documents that token 
        //appears in
        for(String token : tokenToFreq.keySet()){
        	int freq = 0;
        	for(String document : documents.keySet()){
        		if(documents.get(document).containsKey(token)){
        			freq++;
        		}
        	}
        	tokenToDocFreq.put(token, freq);
        }
 
        //For each input file, determine the file's name, and use that file's
        //token to freqency hash to populate a hash of tokens to weights
        if (directoryListing != null) {
            for (File child : directoryListing) {
            	
            	if(child.isHidden()){
            		continue;
            	}
 
                Map<String, Float> tokenToWt = new HashMap<String, Float>();
                StringBuilder outputFilePath = new StringBuilder();
                outputFilePath.append(outputDir).append("/")
                        .append(child.getName());
 
                Map<String, Integer> temp = documents.get(child.getName());
                int corpusSize = documents.size();
                int docTermFreq = 0;
                for(Integer i : temp.values()){
                	docTermFreq += i;
                }
                for(String token : temp.keySet()){
                	int tokenAppearsInXDocs = tokenToDocFreq.get(token);
                	float tf = (float)temp.get(token) / docTermFreq;
                	float idf = (float)Math.log10(corpusSize / tokenAppearsInXDocs);
                	float weight = tf * idf;
                	tokenToWt.put(token, weight);
                }
                writeToFile(tokenToWt, outputFilePath.toString());
            }
             
        } else {
            System.out.println("ERROR: DIRECTORY LISTING IS NULL");
        }
 
    }
	
	//Helper method to write output to a file
    public static void writeToFile(Map<String, Float> mapping, String fileLocation) {
 
        File outputFile = new File(fileLocation);
         
        try {
            if (!outputFile.exists())
                outputFile.createNewFile();
            FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Token: Weight\n");
            bw.write("----------------\n");
            for (Map.Entry<String, Float> entry : mapping.entrySet()) {
                bw.write(entry.getKey() + ": " + String.format("%.10f", entry.getValue()) + "\n");
            }
 
            bw.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
    }

}
