package xszaraz;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DbpediaParser {

	//na vytvorenie child elementov
    private static Node getProperty(Document doc, String id, String title, String text) {
        Element property = doc.createElement("Property");
        property.setAttribute("id", id);
        property.appendChild(getPropertyElements(doc, property, "Title", title));
        property.appendChild(getPropertyElements(doc, property, "Text", text));
        return property;
    }
    
    //na vytvorenie konkretneho child elementu
    private static Node getPropertyElements(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
	
	public static void main(String[] args){
		DbpediaTextSummarize summarizer = new DbpediaTextSummarize();

		String[] text = new String[3];
		
		text[0] = "<http://dbpedia.org/resource/%21%21%21> <http://www.w3.org/2000/01/rdf-schema#comment> \"!!! is a dance-punk band that formed in Sacramento, California in 1996. Members of !!! came from other local bands such as The Yah Mos, Black Liquorice and Popesmashers. The band, who are commonly associated with the dance-punk movement, are currently based in New York City, Sacramento, California, and Portland, Oregon.\"@en .\r\n";
		text[1] = "<http://dbpedia.org/resource/%21%21%21Fuck_You%21%21%21> <http://www.w3.org/2000/01/rdf-schema#comment> \"!!!Fuck You!!! is an EP released by thrash metal band Overkill in 1987. Consisting of a cover version of the song \\\"Fuck You\\\" (originally by The Subhumans), the EP also featured 5 live tracks recorded at The Phantasy Theatre in Cleveland, Ohio. Long out of print, the EP is now extremely rare, but was re-released in 1997 with extra tracks as !!!Fuck You!!! and Then Some. The 1990 CD re-issue was sold with a reversible cover art booklet.\"@en .\r\n";
		text[2] = "<http://dbpedia.org/resource/%21%21%21Fuck_You%21%21%21_and_Then_Some> <http://www.w3.org/2000/01/rdf-schema#comment> \"!!!Fuck You!!! and Then Some is a 1996 reissue of the Overkill EPs Overkill and !!!Fuck You!!!, combined with bonus live tracks, including a cover of Black Sabbath's \\\"Hole in the Sky.\\\"\"@en .\r\n";
		
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); 
        DocumentBuilder dBuilder;
		try {
	        dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.newDocument();
	        //vytvorim si XML mainRootElement
	        Element mainRootElement = doc.createElement("Abstracts");
            doc.appendChild(mainRootElement);
            
            for (int i = 0; i < text.length; i++) {
            
        		String[] texts = text[i].split("> ");	
        		//texts[0] = titul
        		String title = summarizer.SummarizeTitle(texts[0]);	
        		//texts[2] = abstrakt
        		String abstrakt = summarizer.SummarizeAbstract(texts[2]);
	            	        			
				//vytvorim si XML elementy
				mainRootElement.appendChild(getProperty(doc, Integer.toString(i), title, abstrakt));
            }
			
            // vytvorim si XML a zapisem do neho pozadovany vystup
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("D:\\dbpediaabstracts.xml"));
            transformer.transform(source, result);
 
            System.out.println("XML Created Successfully..");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}