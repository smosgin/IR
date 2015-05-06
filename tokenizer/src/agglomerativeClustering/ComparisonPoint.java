package agglomerativeClustering;

public class ComparisonPoint {
	private String object1;
	private String object2;
	private float comparisonValue;
	
	public ComparisonPoint(String name1, String name2, float value){
		object1 = name1;
		object2 = name2;
		comparisonValue = value;
	}
	
	public boolean contains(String name1, String name2){
		boolean returnValue = false;
		if(name1.equalsIgnoreCase(object1)){
			if(name2.equalsIgnoreCase(object2)){
				returnValue = true;
			}
		}
		else if(name1.equalsIgnoreCase(object2)){
			if(name2.equalsIgnoreCase(object1)){
				returnValue = true;
			}
		}
		return returnValue;
	}
	
	public String getName1(){
		return object1;
	}
	
	public String getName2(){
		return object2;
	}
	
	public float getValue(){
		return comparisonValue;
	}
}
