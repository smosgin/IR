package retrieve;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Comparator;

/**
 * This class utilizes the HashMap class in java.util to create an index from a set of
 * terms and their weights, and then use this index to handle queries of words that are 
 * passed in as command-line arguments 
 * 
 * @author Seth
 * 
 */
public class Retrieve {
	
	public static Map<String, Map<String, Float>> terms = new HashMap<String, Map<String, Float>>();
	
	public static void iterateDirectory(String dirPath) {
		 
        File dir = new File(dirPath);
        File[] directoryListing = dir.listFiles();
 
        //For each input file, determine the file's name, and use that file's
        //listing of terms to weights to populate a hash of terms to weights
        if (directoryListing != null) {
            for (File child : directoryListing) {
            	if(child.isHidden()){
            		continue;
            	}
                try {
                    String line;
 
                    InputStream fis = new FileInputStream(child.getAbsolutePath());
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader input = new BufferedReader(isr);
 
                    while ((line = input.readLine()) != null) {
                    	//Ignore the header information from previous phase
                    	if(line.contains("Token: Weight")){
                    		continue;
                    	}
                    	if(line.contains("-------")){
                    		continue;
                    	}
                    	line = line.replaceAll(": ", ":");
                    	String[] temp = line.split(":");
                    	Float weight = Float.parseFloat(temp[1]);
                        if(terms.containsKey(temp[0])){
                        	terms.get(temp[0]).put(child.getName(), weight);
                        }
                        else{
                        	terms.put(temp[0], new HashMap<String, Float>());
                        	terms.get(temp[0]).put(child.getName(), weight);
                        }
                    }
                    input.close();
                } 
                catch (FileNotFoundException e) {e.printStackTrace();} 
                catch (IOException e) {e.printStackTrace();}
            }
             
        } else {
            System.out.println("ERROR: DIRECTORY LISTING IS NULL");
        }
    }
	
	//Helper method to process the query and output the top 10 (if there are 
	//that many) documents by document weight to the user
	private static void processQuery(String[] args){
		//"Preprocess" the query
		//Discard all instances of HTML tags and punctuation, and lower case the line
		Map<String, Float> docWeights = new HashMap<String, Float>();
    	for(int i = 0; i < args.length; i++){
    		args[i] = args[i].replaceAll("\\<.*?>", "");
    		args[i] = args[i].replaceAll("[\\p{P}]", "");
    		args[i] = args[i].toLowerCase();
    		
    		//If the query term is in the dictionary, let's get the list of docs it appears in and
    		//the corresponding weights, and modify our docWeights HashMap as needed
    		if(terms.containsKey(args[i])){
    			for(Entry<String, Float> e : terms.get(args[i]).entrySet()){

    				//determine if the document already exists
    				//if it does, add the term's weight to it, or else "create" a new doc
    				if(docWeights.containsKey(e.getKey())){
    					float curWeight = docWeights.get(e.getKey());
    					docWeights.put(e.getKey(), curWeight + e.getValue());
    				}
    				else{
    					docWeights.put(e.getKey(), e.getValue());
    				}
    			}
    		}
    	}
	
		//Can use a TreeMap to "sort" the HashMap according to its Float values,
        //but we need to pass in a custom comparator in order to tell the TreeMap how to sort
        //the String keys, which in this case is by comparing the Float values
        ValueComparator valueComp =  new ValueComparator(docWeights);
        TreeMap<String, Float> byValue = new TreeMap<String, Float>(valueComp);
        byValue.putAll(docWeights);
        
        //For each doc in the docWeights hashmap, print out the doc and its doc weight
        //(up to 10 docs)
        int i = 0;
        for(String doc : byValue.keySet()){
        	Float f = docWeights.get(doc);
        	//print out the document name, and the weight
        	System.out.print(doc + ", " + String.format("%.10f", f) + "\n");
        	if(++i > 9){
        		break;
        	}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		iterateDirectory(args[0]);
		processQuery(args);
	}

}

//Comparator class used to sort the HashMap in descending order according to
//the values (weights)
class ValueComparator implements Comparator<String> {

	Map<String, Float> mapping;


	public ValueComparator(Map<String, Float> termToWeight) {
		this.mapping = termToWeight;
	}

	public int compare(String a, String b) {
		if (mapping.get(a) > mapping.get(b)) {
			return -1;
		} else if (mapping.get(a) == mapping.get(b)){
			return 0;
		} 
		else{
			return 1;
		}
	}
}
