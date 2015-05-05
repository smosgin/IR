package calcwts;

import java.util.Map;

import tokenizer.*;

public class Calcwts {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Call phase 1
		Stopwatch sw = new Stopwatch();
		Tokenizer t = new Tokenizer();
		Map<String, Integer> m = t.iterateDirectory(args[0], args[1]);
		Map<String, Map<String, Integer>> d = Tokenizer.individualFileHashes;
		
		//Call phase 2
		Stopwatch p2 = new Stopwatch();
		TokenToWts ttw = new TokenToWts(m, d);
		ttw.iterateDirectory(args[1], args[1]);
		System.out.println("Phase 2 elapsed time: " + p2.elapsedTime() + " seconds");
	}

}
