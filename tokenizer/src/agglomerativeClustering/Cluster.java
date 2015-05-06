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
	public static ComparisonPoint maxPoint;
	public static Point maxIJ = new Point(0,0);
	public static int clusteringCount = 0;
	public static ArrayList<String> lines = new ArrayList<String>();

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
					centroids.add(new Centroid(child.getName(), termToWeight));
					input.close();
				} 
				catch (FileNotFoundException e) {e.printStackTrace();} 
				catch (IOException e) {e.printStackTrace();}
			}

		} else {
			System.out.println("ERROR: DIRECTORY LISTING IS NULL");
		}
	}
	
	private static ComparisonPoint[][] cosineSimilarity(){
		int i = 0;
		int j = 0;
		ComparisonPoint[][] similarityMatrix = new ComparisonPoint[numDocs][numDocs];
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
				float similarityScore = (float) (dp / Math.sqrt(distance1 * distance2));
				boolean pointExists = false;
				if(similarityMatrix[j][i] != null){
					if(similarityMatrix[j][i].contains(outerDoc, innerDoc)){
						similarityMatrix[i][j] = similarityMatrix[j][i];
						pointExists = true;
					}
				}
				if(pointExists == false){
					ComparisonPoint p = new ComparisonPoint(outerDoc, innerDoc, similarityScore);
					similarityMatrix[i][j] = p;
				}
				if(similarityScore < 1.0 && similarityScore > max){
					max = similarityScore;
					maxPoint = similarityMatrix[i][j];
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
		ComparisonPoint[][] similarityMatrix = cosineSimilarity();
		while(max > 0.4){
		Centroid c1 = null;
		Centroid c2 = null;
		for(Centroid temp : centroids){
			if(temp.getName().equals(maxPoint.getName1())){
				c1 = temp;
			}
			else if(temp.getName().equals(maxPoint.getName2())){
				c2 = temp;
			}
		}
		//String[] docArray = (String[]) documents.keySet().toArray(new String[documents.size()]);
		lines.add("Object " + maxPoint.getName1() + " is being merged with " + maxPoint.getName2());
		Centroid c = new Centroid(maxPoint.getName1(), maxPoint.getName2(), c1.getTFIDF(), c2.getTFIDF());
		//Centroid c = new Centroid(maxPoint.getName1(), maxPoint.getName2(), documents.get(docArray[maxIJ.i]), documents.get(docArray[maxIJ.j]));
		centroids.add(c);
		clusteringCount++;
		//"zero" out the rows/columns that correspond to the two objects merged
		for(int i = 0; i < similarityMatrix.length; i++){
			similarityMatrix[maxIJ.i][i] = null;
			similarityMatrix[maxIJ.j][i] = null;
			similarityMatrix[i][maxIJ.i] = null;
			similarityMatrix[i][maxIJ.j] = null;
		}
		//allocate a new array
		ComparisonPoint[][] m = new ComparisonPoint[numDocs - clusteringCount][numDocs - clusteringCount];
		//copy over the old array into the new one
		int x = 0;
		int y = 0;
		boolean wasNull = false;
		boolean wasNull2 = false;
		boolean wasNull3 = false;
		for(int i = 0; i < similarityMatrix.length; i++){
			wasNull = false;
			wasNull2 = false;
			for(int j = 0; j < similarityMatrix[i].length; j++){
				if(similarityMatrix[i][j] == null && wasNull == true && wasNull2 == true){
					wasNull3 = true;
				}
				else if(similarityMatrix[i][j] == null && wasNull == true){
					wasNull2 = true;
				}
				else{
					wasNull = false;
				}
				if(similarityMatrix[i][j] != null){
					m[x][y] = similarityMatrix[i][j];
				}
				else{
					wasNull = true;
					y--;
				}
				y++;
			}
			if(wasNull3 == true){
				wasNull3 = false;
			}
			else{
				x++;
			}
			y = 0;
		}
		//add the new scores for the new row/column
		for(int i = 0; i < m.length; i++){
			for(Centroid temp : centroids){
				if(temp.getName().equals(m[0][i].getName1())){
					ComparisonPoint tc = new ComparisonPoint(temp.getName(), c.getName(), c.cosineSimilarity(temp));
					m[i][m.length - 1] = tc;
					m[m.length - 1][i] = tc;
				}
			}
		}
		similarityMatrix = m;
		
		//find the next max
		max = 0;
		for(int i = 0; i < similarityMatrix.length; i++){
			for(int j = 0; j < similarityMatrix[i].length; j++){
				if(similarityMatrix[i][j].getValue() < 1.0 && similarityMatrix[i][j].getValue() > max){
					max = similarityMatrix[i][j].getValue();
					maxIJ.i = i;
					maxIJ.j = j;
					maxPoint = similarityMatrix[i][j];
				}
			}
		}
	}
		
//		while(max > 0.4){
//			
//		}
		
		try {
        	PrintWriter pw = new PrintWriter("p5output.txt");
        	
    		for(String s : lines){
    			pw.write(s);
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
        
        try {
        	PrintWriter pw = new PrintWriter("newSimilarityMatrix.txt");
        	
    		for(int i = 0; i < similarityMatrix.length; i++){
    			for(int j = 0; j < similarityMatrix.length; j++){
    				//System.out.print(similarityMatrix[i][j] + "\t");
    				pw.write(similarityMatrix[i][j].getValue() + "\t");
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
