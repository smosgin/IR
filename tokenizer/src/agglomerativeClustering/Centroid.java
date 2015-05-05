package agglomerativeClustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Centroid {
	private SortedSet<Integer> docs;
	private float myScore;
	//private static float[][] similarityMatrix;
	private Map<String, Float> termsToWeights;
	
	public Centroid(int docNum1, int docNum2, Map<String, Float> tfidf1, Map<String, Float> tfidf2){
		docs = new TreeSet<Integer>();
		docs.add(docNum1);
		//myScore = score;
		//similarityMatrix = matrix;
		termsToWeights = tfidf1;
		Map<String, Float> tfidfCopy = new HashMap<String, Float>(tfidf2);
		for(String term : termsToWeights.keySet()){
			//term is already in the first tfidf map
			if(tfidf2.containsKey(term)){
				float curWeight = termsToWeights.get(term) * 1;
				float toAdd = tfidf2.get(term) * 1;
				termsToWeights.put(term, (curWeight + toAdd) / (docs.size() + 1));
				tfidfCopy.remove(term);
			}
			//term needs to be added
			else{
				termsToWeights.put(term, (tfidf1.get(term) * 1) / (docs.size() + 1));
			}
		}
		for(String term : tfidfCopy.keySet()){
			termsToWeights.put(term, (tfidf2.get(term) * 1) / (docs.size() + 1));
		}
		docs.add(docNum2);
	}
	
	public void addObject(Centroid c){
		for(String term : c.termsToWeights.keySet()){
			//term is already in the centroid
			if(termsToWeights.containsKey(term)){
				float curWeight = termsToWeights.get(term) * docs.size();
				float toAdd = c.termsToWeights.get(term) * c.docs.size();
				
				termsToWeights.put(term, (curWeight + toAdd) / (docs.size() + c.docs.size()));
			}
			//term needs to be added
			else{
				termsToWeights.put(term, (c.termsToWeights.get(term) * c.docs.size()) / (docs.size() + c.docs.size()));
			}
		}
	}
	
	//
	public float cosineSimilarity(Map<String, Float> tfidf){
		float returnValue = 0;
		float dp = 0;
		float distance1 = 0;
		float distance2 = 0;

		Map<String, Float> innerTerms = new HashMap<String, Float>(tfidf);

		for(String outerTerm : termsToWeights.keySet()){
			distance1 += termsToWeights.get(outerTerm) * termsToWeights.get(outerTerm);
			if(innerTerms.containsKey(outerTerm)){
				dp += termsToWeights.get(outerTerm) * innerTerms.get(outerTerm);
				distance2 += innerTerms.get(outerTerm) * innerTerms.get(outerTerm);
				innerTerms.remove(outerTerm);
			}
		}

		for(String innerTerm : innerTerms.keySet()){
			distance2 += innerTerms.get(innerTerm) * innerTerms.get(innerTerm);
		}
		returnValue = (float) (dp / Math.sqrt(distance1 * distance2));
		return returnValue;
	}
	
	public float cosineSimilarity(Centroid c){
		float returnValue = 0;
		float dp = 0;
		float distance1 = 0;
		float distance2 = 0;

		Map<String, Float> innerTerms = new HashMap<String, Float>(c.termsToWeights);

		for(String outerTerm : termsToWeights.keySet()){
			distance1 += termsToWeights.get(outerTerm) * termsToWeights.get(outerTerm);
			if(innerTerms.containsKey(outerTerm)){
				dp += termsToWeights.get(outerTerm) * innerTerms.get(outerTerm);
				distance2 += innerTerms.get(outerTerm) * innerTerms.get(outerTerm);
				innerTerms.remove(outerTerm);
			}
		}

		for(String innerTerm : innerTerms.keySet()){
			distance2 += innerTerms.get(innerTerm) * innerTerms.get(innerTerm);
		}
		returnValue = (float) (dp / Math.sqrt(distance1 * distance2));
		return returnValue;
	}

}
