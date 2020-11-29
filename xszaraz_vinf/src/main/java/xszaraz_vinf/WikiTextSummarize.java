package xszaraz_vinf;

import java.io.Serializable;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class WikiTextSummarize {
	
	public WikiTextSummarize() {}
	
	//spravim si histogram vyskytu slov
	private Map<String, Integer> getWordCounts(String text)
	{
		Map<String,Integer> allWords = new HashMap<String, Integer>();
		
		text.trim();
		//zbavim sa vsetkych znakov, aby som nemal napriklad Ahoj a Ahoj. ako dve rozlisne slova ale ako jedno slovo
		text = text.replace(".", "").replace(",", "").replace(":", "").replace("?", "")
				.replace("!", " ").replace("'", " ").replace(";", "").replace(")", "").replace("(", "");
		String[] words = text.split("\\s+");
		String[] modifyWord = null;
		
		for(int i = 0; i < words.length; i++)
		{
			if (words[i].contains("'s")){
				modifyWord = words[i].split("'");
				words[i] = modifyWord[0];
			}

			words[i] = words[i].toLowerCase();
			
			if (!words[i].contains("{{") && !words[i].contains("}}") 
					&& !words[i].contains("|") && !words[i].contains("&lt") 
					&& !words[i].contains("&gt") && !words[i].contains("=")) {
				if(allWords.containsKey(words[i]))
				{
					int oldv = allWords.get(words[i]);
					allWords.put(words[i], oldv += 1);
				}
				else
				{
					allWords.put(words[i], 1);
				}
			}
		}
		
		return allWords;
	}
	
	private Map<String,Integer> filterStopWords(Map<String, Integer> toBeFiltered)
	{
		//zoznam stopslov
        String[] stopWords = {         		       	        
        		"i","i'm", "me", "my", "myself", "we", "our", "ours", "ourselves", 
        		"you", "your", "yours", "your's", "yourself", "yourselves", "he", "him", 
        		"his", "himself", "she", "her", "her's", "hers", "herself", "it", "its", 
        		"itself", "they", "them", "their", "theirs", "themselves", "what", 
        		"which", "who", "whom", "this", "that", "these", "those", "am", "is", "isn't",
        		"are", "was", "were", "we're", "be", "been", "being", "have", "has", "had", "hadn't", "having", 
        		"do", "does", "did", "didn't", "doing", "a", "an", "the", "and", "but", "if", "or", "because", 
        		"as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", 
        		"into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", 
        		"in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", 
        		"when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", 
        		"such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "willn't", 
        		"just", "don", "should", "now", "able", "also", "cannot", "can't", "could", "couldn't","either", "else",
        		"ever","every", "get","got", "let", "like","likely","may", "might","must", "neither",
        		"often","said","say","says","they're", "that's", "us", "would", "wouldn't","yet", "you're", "-", "vs", "eg", "etc"
        };
        
        //zbavim sa stopslov
        for(int i= 0; i < stopWords.length; i++)
        {
        	if(toBeFiltered.containsKey(stopWords[i]))
        	{
        		toBeFiltered.remove(stopWords[i]);
        	}
        }
        
        return toBeFiltered;
	}

    @SuppressWarnings("hiding")
    //sort poctu vyskytovanych slov
	public static <String, Integer extends Comparable<? super Integer>> Map<String, Integer> sortByValue(Map<String, Integer> wordMap) {
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(wordMap.entrySet());
        list.sort(Entry.comparingByValue());

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    //rozdelim tu text na vety
    private String[] getSentences(BreakIterator bi, String source) {
    	List<String> temp = new ArrayList<String>();
        bi.setText(source);

        int lastIndex = bi.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = bi.next();

            if (lastIndex != BreakIterator.DONE) {
                String sentence = source.substring(firstIndex, lastIndex);
                //vety co obsahuju *p tu nemaju co robit lebo su to iba referencie
                if (!sentence.contains("*p")) {
	                temp.add(sentence);
                }
            }
        }
        
        String[] sentences = new String[temp.size()];
        sentences = temp.toArray(sentences);
        
        return sentences;
    }
	
	private List<String> search(String[] sentences, String word)
	{
		List<String> matchedSentences = new ArrayList<String>();
		for(String sentence: sentences)
		{
			//skontrolujem ci dana veta obsahuje hladane slovo, ak ano tak ho pridam do zoznamu najdenych viet
			if(sentence.toLowerCase().contains(word.toLowerCase()))
			{	
				matchedSentences.add(sentence);
			}
		}
		
		return matchedSentences;
	}
	
	public String Summarize(String text, int maxSummarySize)
	{

		//regexy na zbavenie sa veci, ktore ma nezaujimaju
		text = text.replaceAll("==.*==", "").replaceAll("\\&lt\\;\\!\\-\\-[[[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\(*\\)*\\~*\\]\\[*]*\\|*]*\\-\\-\\&gt\\;","").replace("[...] ", "").replace("&quot", "\"")
				.replaceAll("\\{\\{[[[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\(*\\)*\\~*\\]\\[*]*\\|*]*\\}\\}", "")
				.replaceAll("\\([[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*]*\\)\\|[[a-z]*[A-Z]*[0-9]*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*]*\\]\\]",  "")					 
				.replaceAll("\\|[[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*é*í*¾*š*è**ž*ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*]*\\]\\]", "]]")
				.replace("[[","").replace("]]","")
				.replaceAll("File\\:[[a-z]*[A-Z]*[0-9]*\\*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\(*\\)*\\|*]*\\\r", "")
				.replaceAll("http:\\/\\/[[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\^*\\**\\*\\~*\\[*\\]*\\(*\\)*\\|*]*\\&lt\\;", "&lt;")
				.replaceAll("\\&lt\\;\\/*ref[[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\^*\\**\\*\\~*\\[*\\]*\\(*\\)*\\|*]*\\&gt\\;", "")
				.replaceAll("\\&lt\\;\\/*math[[a-z]*[A-Z]*[0-9]*\\_*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È***ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\^*\\**\\*\\~*\\[*\\]*\\(*\\)*\\|*]*\\&gt\\;", "")
				.replaceAll("\\{\\{sfn[[a-z]*[A-Z*][0-9]*\\|*Ã*©*=*\"* *\\¡*â*]*\\}\\}","")
				.replaceAll("--[[a-z]*[A-Z]*[0-9]*\\*\\/*\\ *\\'*\\\"*\\=*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\(*\\)*\\|*]*-->", " ")
				.replaceAll("<ref[[a-z]*[A-Z]*[0-9]*\\*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\{*\\}*\\(*\\)*\\|*]*><\\/ref>", "")
				.replaceAll("<ref[[a-z]*[A-Z]*[0-9]*\\*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\{*\\}*\\(*\\)*\\|*]*\\/>", "")
				.replaceAll("<ref[[a-z]*[A-Z]*[0-9]*\\*\\/*\\ *\\'*\\\"*\\=*\\-*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\.*\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\{*\\}*\\(*\\)*\\|*]*>", "")
				.replaceAll("<gallery[ *[a-z]*[A-Z]*[0-9]*\\\"*\\.*\\=*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\{*\\}*\\(*\\)*\\|*]*]*><\\/gallery>", "")
				.replaceAll("<center[ *[a-z]*[A-Z]*[0-9]*\\\"*\\.*\\=*\\–*á*Á*é*É*í*Í*¾*¼*š*Š*è*È*ž*Ž*ý*Ý*\\;*\\:*\\\\,*\\!*\\?*\\@*\\#*\\$*\\%*\\&*\\^*\\**\\*\\~*\\{*\\}*\\(*\\)*\\|*]*]*><\\/center>", "")
				.replace("() ", "").replace("[", "").replace("]","").replaceAll("p\\.\\ [0-9]*\\.", "p*.").replace("i.e.", "i*e*")/**/;

		String[] textSentences = text.split("\n");
		String cleanedText = "";
				
		//zbavim sa infoboxu
		for (String sentence : textSentences) {
			sentence = sentence.replaceAll("^!", "|");
			if (!sentence.contains("|") & !sentence.contains("Infobox") && !sentence.contains("http") && !sentence.contains("{{")) 
			{
				cleanedText = cleanedText + "\n"+ sentence;
			}
		}
		
		//zbavim sa nasledovnych znakov, ktore hovoria o fomatovani textu alebo tieto znaky iba upravim na nieco ine 
		cleanedText = cleanedText.replace(".", ". ").replace("*", "").replace("'''","")
				.replace("''","").replace("--&gt", "").replace(";","").replace("&lt!--", "").replace("&lt;/ref", "").
				replaceAll("&gt;", "").replace("&lt/ref&gt","").replace("&ltref&gt", "").replace("{{cite journal", "").
				replace("\n","").replace("\r", "").replace("&ltblockquote&gt","").replace("</ref>", "")
				.replace("<small>", "").replace("</small>","").replaceAll("--+", "").replace("<ref>", "").replaceAll("<br *\\/>", "").replace("<br>", "")
				.replaceAll("__+TOC__+", "").replace("__+NOTOC__+", "")/**/;
	
		//zbavim sa oznaceni skratiek, robim to kvoli tomu, aby som mohol rozdelit text na vety podla .
		cleanedText = cleanedText.replace("Mr.", "Mr").replace("Ms.", "Ms").replace("Dr.", "Dr").replace("Jan.", "Jan").replace("Feb.", "Feb")
				.replace("Mar.", "Mar").replace("Apr.", "Apr").replace("Jun.", "Jun").replace("Jul.", "Jul").replace("Aug.", "Aug")
				.replace("Sep.","Sep").replace("Spet.", "Sept").replace("Oct.", "Oct").replace("Nov.", "Nov").replace("Dec.", "Dec")
				.replace("St.", "St").replace("Prof.", "Prof").replace("Mrs.", "Mrs").replace("Gen.", "Gen")
                .replace("Corp.", "Corp").replace("Mrs.", "Mrs").replace("Sr.","Sr").replace("Jr.", "Jr").replace("cm.", "cm")
                .replace("Ltd.", "Ltd").replace("Col.", "Col").replace("vs.", "vs").replace("Capt.", "Capt")
                .replace("Univ.", "University").replace("Sgt.", "Sgt").replace("ft.","ft").replace("in.","in")
                .replace("Ave.", "Ave").replace("Univ.", "University").replace("Lt.", "Lt").replace("etc.", "etc").replace("mm.", "mm")
                .replace("\n\n", "").replace("Important!", ".Important*").replace("}}","").replace("#", "").replace("\n", "").replace("\r", "")/**/;
		cleanedText = cleanedText.replaceAll("([A-Z])\\.", "$1");		
		
		Map<String, Integer> wordFrequencies = getWordCounts(cleanedText);		
		Map<String, Integer> filtered = filterStopWords(wordFrequencies);		
		List<String> sortedMap = new ArrayList<String>(sortByValue(filtered).keySet());
			
		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
		String[] sentences = getSentences(iterator, cleanedText);
		
		//najdem si vety, ktore obsahuju slovo, ktore sa v texte vyskytuje najviac
		List<String> matchedSentence = new ArrayList<String>();
		if (sortedMap.size() > 0) {
			matchedSentence = search(sentences, sortedMap.get(sortedMap.size()-1));
		}
		
		String summary = "";
		
		//vety, ktore obsahovali dane slovo vratim a kontrolujem ci sa mi este mestia do vyhradenej velkosti alebo nie 
		for(String sentence : matchedSentence)
		{
			if(summary.length() + sentence.length() <= maxSummarySize)
			{
				summary = summary + sentence;
			}		
		}									
	
		//znaky ktore som povodne menil aby som mohol rozdelit vetu bodka ., vratim na povodne
		summary = summary.replace("i*e*", "i.e.").replace(". ", ".").replace(".  ", ". ").replaceAll("\\ +", " ");
	
		return summary;	
	}
}