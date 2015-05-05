package tokenizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.io.*;
 
/**
 * This class uses the StringTokenizer class in java.util to tokenize and downcase
 * all words in HTML documents
 * 
 * @author Ian and Seth
 * 
 */
public class Tokenizer {
	
	public static Map<String, Map<String, Integer>> individualFileHashes;// = new HashMap<String, Map<String, Integer>>();
 
    /**
     * @param args
     */
    public Tokenizer() {
        //iterateDirectory(args[0], args[1]);
 
    }
 
    public static Map<String, Integer> iterateDirectory(String dirPath, String outputDir) {
 
    	Stopwatch globalWatch = new Stopwatch();
        File dir = new File(dirPath);
        File outputDirectory = new File(outputDir);
        outputDirectory.mkdir();
        File[] directoryListing = dir.listFiles();
        Map<String, Integer> tokenToFreq = new HashMap<String, Integer>();
        individualFileHashes = new HashMap<String, Map<String, Integer>>();
 
        //For each input file, loop through the file line by line, "tokenizing"
        //along the way using java.util's StringTokenizer class, which essentially splits
        //a string on a specified delimiter, which in the default case is a standard set of
        //delimiters: the space character, the tab character, the newline character, 
        //the carriage-return character, and the form-feed character.
        if (directoryListing != null) {
            for (File child : directoryListing) {
            	if(child.isHidden()){
            		continue;
            	}
 
            	Stopwatch localWatch = new Stopwatch();
                Map<String, Integer> currentFileHash = new HashMap<String, Integer>();
                ArrayList<String> lines = new ArrayList<String>();
                StringBuilder outputFilePath = new StringBuilder();
                outputFilePath.append(outputDir).append("/")
                        .append(child.getName().replaceAll("\\.*", ""))
                        .append(".txt");
 
                try {
                    String line;
 
                    InputStream fis = new FileInputStream(child.getAbsolutePath());
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader input = new BufferedReader(isr);
 
                    while ((line = input.readLine()) != null) {
                    	//Discard all instances of HTML tags and punctuation, and lower case the line
                    	line = line.replaceAll("\\<.*?>", "");
                    	line = line.replaceAll("[\\p{P}]", "");
                    	line = line.toLowerCase();
                        StringTokenizer st = new StringTokenizer(line);
                        lines.add(line);
                        while (st.hasMoreTokens()) {
                            String currentToken = st.nextToken();
                            if (!tokenToFreq.containsKey(currentToken))
                                tokenToFreq.put(currentToken, 1);
                            else {
                                tokenToFreq.put(currentToken,
                                        tokenToFreq.get(currentToken) + 1);
                            }
                            if (!currentFileHash.containsKey(currentToken))
                                currentFileHash.put(currentToken, 1);
                            else {
                                currentFileHash.put(currentToken, currentFileHash.get(currentToken) + 1);
                            }
                        }
                    }
                    input.close();
                } 
                catch (FileNotFoundException e) {e.printStackTrace();} 
                catch (IOException e) {e.printStackTrace();}
 
                individualFileHashes.put(child.getName().replaceAll("\\.*", "") + ".txt", currentFileHash);
                writeToFile(lines, outputFilePath.toString(), localWatch.elapsedTime());
 
            }
            
            /*Removing sortedByToken and sortedByFrequency; phase2 doesn't need them
            //Can use a TreeMap to "sort" the HashMap according to its String keys
            Map<String, Integer> byKeys = new TreeMap<String, Integer>(tokenToFreq);
            StringBuilder sortedByKey = new StringBuilder();
            sortedByKey.append(outputDir).append("/sortedByToken.txt");
            writeToFile(byKeys, sortedByKey.toString(), globalWatch.elapsedTime());
             
            //Can still use a TreeMap to "sort" the HashMap according to its Integer values,
            //but we need to pass in a custom comparator in order to tell the TreeMap how to sort
            //the String keys, which in this case is by comparing the Integer values
            ValueComparator valueComp =  new ValueComparator(tokenToFreq);
            TreeMap<String,Integer> byValue = new TreeMap<String,Integer>(valueComp);
            byValue.putAll(tokenToFreq);
            StringBuilder sortedByValue = new StringBuilder();
            sortedByValue.append(outputDir).append("/sortedByFrequency.txt");
            writeToFile(byValue, sortedByValue.toString(), globalWatch.elapsedTime());
            */
             
        } else {
            System.out.println("ERROR: DIRECTORY LISTING IS NULL");
        }
        return tokenToFreq;
    }
    
  //Helper method to write output to a file
    public static void writeToFile(ArrayList<String> lines, String fileLocation, double time) {
 
        File outputFile = new File(fileLocation);
         
        try {
            if (!outputFile.exists())
                outputFile.createNewFile();
            FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write("Approximate time to generate this file: " + time + " seconds\n");
//            bw.write("Begin token stream...\n");
//            bw.write("----------------\n");
            for (String entry : lines) {
                bw.write(entry + "\n");
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
 
    //Helper method to write output to a file
    public static void writeToFile(Map<String, Integer> mapping, String fileLocation, double time) {
 
        File outputFile = new File(fileLocation);
         
        try {
            if (!outputFile.exists())
                outputFile.createNewFile();
            FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("Approximate time to generate this file: " + time + " seconds\n");
            bw.write("Token: Frequency\n");
            bw.write("----------------\n");
            for (Map.Entry<String, Integer> entry : mapping.entrySet()) {
                bw.write(entry.getKey() + ": " + entry.getValue() + "\n");
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
 
//Comparator class used to sort the HashMap in descending order according to
//the values (frequencies)
class ValueComparator implements Comparator<String> {
 
    Map<String, Integer> mapping;
     
 
    public ValueComparator(Map<String, Integer> tokenToFreq) {
        this.mapping = tokenToFreq;
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
