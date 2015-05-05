package agglomerativeClustering;

import java.util.Map;

public class Document {
	private String myName;
	private Map<String, Float> termsToWeights;
	
	public Document(String name, Map<String, Float> tfidf){
		myName = name;
		termsToWeights = tfidf;
	}
	
}
