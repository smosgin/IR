package agglomerativeClustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Cluster {
	
	//Simple class to keep track of the i,j combinations
	private static class Point{
		private int i;
		private int j;

		private Point(int i, int j) {
			this.i = i;
			this.j = j;
		}
	}

	//private static final String DICFN = "dictionary.txt";
	//private static final String POSTFN = "postings.txt";

	public static Map<String, Map<String, Float>> documents = new LinkedHashMap<String, Map<String, Float>>();
	public static ArrayList<Centroid> centroids = new ArrayList<Centroid>();
	//public static Map<String, List<Float>> documents = new HashMap<String, List<Float>>();
	public static int numDocs = 0;
	public static float max = 0;
	public static Point maxIJ = new Point(0,0);
	public static int clusteringCount = 0;

	public static void iterateDirectory(String dirPath) {

		File dir = new File(dirPath);
		File[] directoryListing = dir.listFiles();

		//For each input file, determine the file's name, and use that file's
		//listing of terms to weights to populate a hash of documents to terms/weights
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
					
					HashMap<String, Float> termToWeight = new HashMap<String, Float>();
					//List<Float> tfidfWeights = new ArrayList<Float>();
					documents.put(child.getName(), termToWeight);
					numDocs++;

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
						
						//tfidfWeights.add(weight);
						
						termToWeight.put(temp[0], weight);
						
//						if(terms.containsKey(temp[0])){
//							terms.get(temp[0]).put(child.getName(), weight);
//						}
//						else{
//							terms.put(temp[0], new HashMap<String, Float>());
//							terms.get(temp[0]).put(child.getName(), weight);
//						}
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
	
	private float dotProduct(Map<String, Float> d1, Map<String, Float> d2){
		float dp = 0;
		if(d1.size() > d2.size()){
			for(String outerTerm : d1.keySet()){
				//do shit
				if(d2.containsKey(outerTerm)){
					
				}
			}
		}
		return dp;
	}
	
	private static float[][] cosineSimilarity(){
		int i = 0;
		int j = 0;
		float[][] similarityMatrix = new float[numDocs][numDocs];
		for(String outerDoc : documents.keySet()){
			j = 0;
			Map<String, Float> outerTerms = documents.get(outerDoc);
			//int outerTermsSize = outerTerms.size();
			for(String innerDoc : documents.keySet()){
				float dp = 0;
				float distance1 = 0;
				float distance2 = 0;
				Map<String, Float> innerTerms = new HashMap<String, Float>(documents.get(innerDoc));
				//if(outerTermsSize > innerTerms.size()){
				for(String outerTerm : outerTerms.keySet()){
					//do shit
					distance1 += outerTerms.get(outerTerm) * outerTerms.get(outerTerm);
					if(innerTerms.containsKey(outerTerm)){
						dp += outerTerms.get(outerTerm) * innerTerms.get(outerTerm);
						distance2 += innerTerms.get(outerTerm) * innerTerms.get(outerTerm);
						innerTerms.remove(outerTerm);
					}
				}
				for(String innerTerm : innerTerms.keySet()){
					distance2 += innerTerms.get(innerTerm) * innerTerms.get(innerTerm);
 				}
				//}
				similarityMatrix[i][j] = (float) (dp / Math.sqrt(distance1 * distance2));
				if(similarityMatrix[i][j] < 1.0 && similarityMatrix[i][j] > max){
					max = similarityMatrix[i][j];
					//create a class to maintain the i,j combination of the max value
					maxIJ.i = i;
					maxIJ.j = j;
				}
				j++;
			}
			i++;
		}
		return similarityMatrix;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		iterateDirectory(args[0]);
		float[][] similarityMatrix = cosineSimilarity();
		String[] docArray = (String[]) documents.keySet().toArray(new String[documents.size()]);
		Centroid c = new Centroid(maxIJ.i, maxIJ.j, documents.get(docArray[maxIJ.i]), documents.get(docArray[maxIJ.j]));
		centroids.add(c);
		clusteringCount++;
		//"zero" out the rows/columns that correspond to the two objects merged
		for(int i = 0; i < similarityMatrix.length; i++){
			similarityMatrix[maxIJ.i][i] = 2;
			similarityMatrix[maxIJ.j][i] = 2;
			similarityMatrix[i][maxIJ.i] = 2;
			similarityMatrix[i][maxIJ.j] = 2;
		}
		//allocate a new array
		float[][] m = new float[numDocs + clusteringCount][numDocs + clusteringCount];
		//copy over the old array into the new one
		for(int i = 0; i < similarityMatrix.length; i++){
			for(int j = 0; j < similarityMatrix[i].length; j++){
				m[i][j] = similarityMatrix[i][j];
			}
		}
		//add the new scores for the new row/column
		
		
		while(max > 4){
			
		}
        
        try {
        	PrintWriter pw = new PrintWriter("newSimilarityMatrix.txt");
        	
    		for(int i = 0; i < similarityMatrix.length; i++){
    			for(int j = 0; j < similarityMatrix.length; j++){
    				//System.out.print(similarityMatrix[i][j] + "\t");
    				pw.write(similarityMatrix[i][j] + "\t");
    			}
    			pw.write("\n");
    		}
            pw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		//System.out.print(Arrays.deepToString(similarityMatrix));
	}

}
