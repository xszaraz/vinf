package xszaraz;

public class DbpediaTextSummarize {

	public String SummarizeTitle(String text) {		
		//kedze je title v url, tak si rozdelim url adresu podla /
		String[] splits = text.split("/");
		
		//kedze je title ako posledny v url tak to znamena ze je za poslednou /
		String title  = splits[splits.length-1];
		
		//nahradim si HTML znaky normalnymi znakmi
		title = title.replace("%21", "!").replace("%22","\"").replace("%23", "#").replace("%24", "$")
				.replace("%25", "%").replace("%26", "&").replace("%27", "'").replace("%28", "(")
				.replace("%29", ")").replace("%2A", "*").replace("%2B", "+").replace("%2C", ",")
				.replace("%2D", "-").replace("%2E", ".").replace("%2F", "/").replace("%3A", ":")
				.replace("%3B", ";").replace("%3C", "<").replace("%3D", "=").replace("%3E", ">")
				.replace("%3F", "?").replace("%40", "@").replace("%5B", "[").replace("%5C", "\\")
				.replace("%5D", "]").replace("%5E", "^").replace("%5F", "_").replace("%60", "`")
				.replace("%7B", "{").replace("%7C", "|").replace("%7D", "}").replace("%7E", "~")
				.replace("%A0", " ").replace("%A1", "¡").replace("%A2", "¢").replace("%A3", "£")
				.replace("%A4", "¤").replace("%A5", "¥").replace("%A6", "¦").replace("%A7", "§")
				.replace("%A8", "¨").replace("%A9", "©").replace("%AA", "ª").replace("%AB", "«")
				.replace("%AC", "¬").replace("%AD", "­").replace("%AE", "®").replace("%AF", "¯")
				.replace("%B0", "°").replace("%B1", "±").replace("%B2", "²").replace("%B3", "³")
				.replace("%B4", "´").replace("%B5", "µ").replace("%B6", "¶").replace("%B7", "·")
				.replace("%B8", "¸").replace("%B9", "¹").replace("%BA", "º").replace("%BB", "»")
				.replace("%BC", "¼").replace("%BD", "½").replace("%BE", "¾").replace("%BF", "¿")
				.replace("%C0", "À").replace("%C1", "Á").replace("%C2", "Â").replace("%C3", "Ã")
				.replace("%C4", "Ä").replace("%C5", "Å").replace("%C6", "Æ").replace("%C7", "Ç")
				.replace("%C8", "È").replace("%C9", "É").replace("%CA", "Ê").replace("%CB", "Ë")
				.replace("%CC", "Ì").replace("%CD", "Í").replace("%CE", "Î").replace("%CF", "Ï")
				.replace("%D0", "Ð").replace("%D1", "Ñ").replace("%D2", "Ò").replace("%D3", "Ó")
				.replace("%D4", "Ô").replace("%D5", "Õ").replace("%D6", "Ö").replace("%D7", "×")
				.replace("%D8", "Ø").replace("%D9", "Ù").replace("%DA", "Ú").replace("%DB", "Û")
				.replace("%DC", "Ü").replace("%DD", "Ý").replace("%DE", "Þ").replace("%DF", "ß")
				.replace("%E0", "à").replace("%E1", "á").replace("%E2", "â").replace("%E3", "ã")
				.replace("%E4", "ä").replace("%E5", "å").replace("%E6", "æ").replace("%E7", "ç")
				.replace("%E8", "è").replace("%E9", "é").replace("%EA", "ê").replace("%EB", "ë")
				.replace("%EC", "ì").replace("%ED", "í").replace("%EE", "î").replace("%EF", "ï")
				.replace("%F0", "ð").replace("%F1", "ñ").replace("%F2", "ò").replace("%F3", "ó")
				.replace("%F4", "ô").replace("%F5", "õ").replace("%F6", "ö").replace("%F7", "÷")
				.replace("%F8", "ø").replace("%F9", "ù").replace("%FA", "ú").replace("%FB", "û")
				.replace("%FC", "ü").replace("%FD", "ý").replace("%FE", "þ").replace("%FF", "ÿ");
		
		return title;
	}
	
	public String SummarizeAbstract(String text) {

		//upravim si abstrakt
		String abstrakt = text.replaceAll("^\"","").replace("\"@en .", "").replace("\\", "").replace("\n", "").replace("\r", "");
		
		return abstrakt;
	}
	
}
