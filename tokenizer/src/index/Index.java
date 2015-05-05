package index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This class utilizes the HashMap class in java.util to create an index from a set of
 * terms and their weights, and then output this information into a dictionary text file
 * and a postings text file.
 * 
 * @author Seth
 * 
 */
public class Index {
	
	//Simple class to keep track of a term's number of entries and its
	//starting line in the postings file
	private static class Point{
		private int numEntries;
		private int startingLine;
		
		private Point(int numEntries, int startingLine) {
			this.numEntries = numEntries;
			this.startingLine = startingLine;
		}
	}
	
	public static Map<String, Map<String, Float>> terms = new HashMap<String, Map<String, Float>>();
	public static Map<String, Point> termsToDictionaryEntries = new HashMap<String, Point>();
	
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
 
                //Map<String, Float> tokenToWt = new HashMap<String, Float>();
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
	
	//Helper method to sort the terms HashMap by its keys
	private static void sortTerms(){
		//Can use a TreeMap to "sort" the HashMap according to its String keys
        Map<String, Map<String, Float>> byKeys = new TreeMap<String, Map<String, Float>>(terms);
        terms = byKeys;
	}
	
	//Helper method to write the dictionary text file
	private static void writeDictionary(String outputDir){
		StringBuilder outputFilePath = new StringBuilder();
        outputFilePath.append(outputDir).append("/dictionary.txt");
        File outputFile = new File(outputFilePath.toString());
        
        try {
        	if (!outputFile.getParentFile().exists())
        	    outputFile.getParentFile().mkdirs();
            if (!outputFile.exists())
                outputFile.createNewFile();
            
            FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
            for(String term : terms.keySet()){
    			Point p = termsToDictionaryEntries.get(term);
    			//print out the document name, and the weight
    			bw.write(term + "\n");
    			bw.write("Number of entries: " + p.numEntries + "\n");
    			bw.write("Starting Line: " + p.startingLine + "\n");
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
	
	//Helper method to write the postings text file
	private static void writePostings(String outputDir){
		StringBuilder outputFilePath = new StringBuilder();
        outputFilePath.append(outputDir).append("/postings.txt");
        File outputFile = new File(outputFilePath.toString());
        int startingLine = 1;
        
        try {
        	if (!outputFile.getParentFile().exists())
        	    outputFile.getParentFile().mkdirs();
            if (!outputFile.exists())
                outputFile.createNewFile();
            
            FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            
            for(String term : terms.keySet()){
            	int numEntries = 0;
    			for(Entry<String, Float> e : terms.get(term).entrySet()){
    				//print out the document name, and the weight
    				bw.write(e.getKey() + "," + String.format("%.10f", e.getValue()) + "\n");
    				numEntries++;
    			}
    			
    			//This HashMap keeps track of the number of entries and the starting line
    			//for each term.  It will be used in the writeDictionary helper method
    			Point p = new Point(numEntries, startingLine);
    			termsToDictionaryEntries.put(term, p);
    			startingLine += numEntries;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		iterateDirectory(args[0]);
		sortTerms();
		writePostings(args[1]);
		writeDictionary(args[1]);
	}

}
