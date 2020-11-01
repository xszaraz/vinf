package xszaraz_vinf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosineSimiliarity {

    private static class Values {

        private int valueDbPedia;
        private int valueWiki;

        private Values(int v1, int v2) {
            this.valueDbPedia = v1;
            this.valueWiki = v2;
        }

        public void updateValues(int v1, int v2) {
            this.valueDbPedia = v1;
            this.valueWiki = v2;
        }
    }
    
    public double score(String abstractDbPedia, String abstractWiki) {
        //Zistime si odlisne slova medzi abstraktmi
        String[] abstractDbPediaWords = abstractDbPedia.split(" ");
        String[] abstractWikiWords = abstractWiki.split(" ");
        Map<String, Values> wordFreqVector = new HashMap<String, Values>();
        List<String> distinctWords = new ArrayList<String>();

        //zistime si frekvenciu slov v abstracte dbpedie
        for (String text : abstractDbPediaWords) {
            String word = text.trim();
            if (wordFreqVector.containsKey(word)) {
                Values values = wordFreqVector.get(word);
                values.updateValues(values.valueDbPedia + 1, values.valueWiki);
                wordFreqVector.put(word, values);
            } else {
                wordFreqVector.put(word, new Values(1, 0));
                distinctWords.add(word);
            }
        }

        //zistime si frekvenciu slov v abstracte wiki
        for (String text : abstractWikiWords) {
            String word = text.trim();
            if (wordFreqVector.containsKey(word)) {
            	Values values = wordFreqVector.get(word);
                values.updateValues(values.valueDbPedia, values.valueWiki + 1);
                wordFreqVector.put(word, values);
            } else {
                wordFreqVector.put(word, new Values(0, 1));
                distinctWords.add(word);
            } 
        }

        //vypocitame cosine similarity score
        double vectAB = 0.0000000, vectA = 0.0000000, vectB = 0.0000000;
        for (int i = 0; i < distinctWords.size(); i++) {
            Values values = wordFreqVector.get(distinctWords.get(i));
            double freqDbPedia = values.valueDbPedia;
            double freqWiki = values.valueWiki;
            vectAB += freqDbPedia * freqWiki;
            vectA += freqDbPedia * freqDbPedia;
            vectB += freqWiki * freqWiki;
        }

        return ((vectAB) / (Math.sqrt(vectA) * Math.sqrt(vectB)));
    }

    public static void main(String[] args) throws IOException {
    	CosineSimiliarity cs = new CosineSimiliarity();

    	String text1 = "";
    	String text2 = "";
    	
        double score = cs.score(text1, text2);
        System.out.println("Cosine similarity score = " + score);
    }
	
}

