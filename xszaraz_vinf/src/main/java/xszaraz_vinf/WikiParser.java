package xszaraz_vinf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.File;

public class WikiParser {
	
	public static WikiTextSummarize summarizer = new WikiTextSummarize();
	
	public static class XmlInputFormat1 extends TextInputFormat {

	    public static final String START_TAG_KEY = "xmlinput.start";
	    public static final String END_TAG_KEY = "xmlinput.end";

	    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) {
	        return new XmlRecordReader();
	    }

	    /**
	     * XMLRecordReader class to read through a given xml document to output
	     * xml blocks as records as specified by the start tag and end tag
	     *
	     */

	    public static class XmlRecordReader extends RecordReader<LongWritable, Text> {
	        private byte[] startTag;
	        private byte[] endTag;
	        private long start;
	        private long end;
	        private FSDataInputStream fsin;
	        private DataOutputBuffer buffer = new DataOutputBuffer();

	        private LongWritable key = new LongWritable();
	        private Text value = new Text();
	        
	        @Override
	        public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
	            Configuration conf = context.getConfiguration();
	            startTag = conf.get(START_TAG_KEY).getBytes("utf-8");
	            endTag = conf.get(END_TAG_KEY).getBytes("utf-8");
	            FileSplit fileSplit = (FileSplit) split;

	            // open the file and seek to the start of the split
	            start = fileSplit.getStart();
	            end = start + fileSplit.getLength();
	            Path file = fileSplit.getPath();
	            FileSystem fs = file.getFileSystem(conf);
	            fsin = fs.open(fileSplit.getPath());
	            fsin.seek(start);

	        }
	        
	        @Override
	        public boolean nextKeyValue() throws IOException, InterruptedException {
	            if (fsin.getPos() < end) {
	                if (readUntilMatch(startTag, false)) {
	                    try {
	                        buffer.write(startTag);
	                        if (readUntilMatch(endTag, true)) {
	                            key.set(fsin.getPos());
	                            value.set(buffer.getData(), 0,
	                                    buffer.getLength());
	                            return true;
	                        }
	                    } finally {
	                        buffer.reset();
	                    }
	                }
	            }
	            return false;
	        }
	        @Override
	        public LongWritable getCurrentKey() throws IOException, InterruptedException {
	            return key;
	        }

	        @Override
	        public Text getCurrentValue() throws IOException, InterruptedException {
	            return value;
	        }
	        @Override
	        public void close() throws IOException {
	            fsin.close();
	        }
	        @Override
	        public float getProgress() throws IOException {
	            return (fsin.getPos() - start) / (float) (end - start);
	        }

	        private boolean readUntilMatch(byte[] match, boolean withinBlock) throws IOException {
	            int i = 0;
	            while (true) {
	                int b = fsin.read();
	                // end of file:
	                    if (b == -1)
	                        return false;
	                // save to buffer:
	                    if (withinBlock)
	                        buffer.write(b);
	                // check if we're matching:
	                    if (b == match[i]) {
	                        i++;
	                        if (i >= match.length)
	                            return true;
	                    } else
	                        i = 0;
	                    // see if we've passed the stop point:
	                    if (!withinBlock && i == 0 && fsin.getPos() >= end)
	                        return false;
	            }
	        }
	    }
	}

	public static class Map extends Mapper<LongWritable, Text,Text, Text> {
	    @Override
	    protected void map(LongWritable key, Text value, Mapper.Context context) throws IOException, InterruptedException {
	        String document = value.toString();

	        try {
	            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(document.getBytes()));
	            String propertyName = "";
	            String propertyValue = "";
	            String currentElement = "";
	            while (reader.hasNext()) {
	                int code = reader.next();
	                switch (code) {
	                case XMLStreamConstants.START_ELEMENT: //START_ELEMENT:
	                    currentElement = reader.getLocalName();
	                    break;
	                case XMLStreamConstants.CHARACTERS: //CHARACTERS:
	                    if (currentElement.equalsIgnoreCase("title")) {
	                        propertyName += reader.getText();
	                    } else if (currentElement.equalsIgnoreCase("text")) {
	                    	propertyValue += reader.getText();
	                    }
	                    break;
	                }
	            }
	            reader.close();
	            
	            String textToBeSplitted = "";
		    	String abstrakt = "";

				if (!propertyValue.contains("#REDIRECT")) {				
	                textToBeSplitted = propertyValue.replaceAll("==\\ *See also\\ *==", "==See also==");
	                //splitnem text podla See also, pretoze pod See also su iba referencie co ma pri abstrakte nezaujima
	    			String[] texts = textToBeSplitted.split("==See also=="); 
	    			if (texts.length>0) {
		    			abstrakt = summarizer.Summarize(texts[0], 512);
		    			    			
		    			propertyName = propertyName.trim() + "\t";
		    	    	abstrakt = abstrakt.trim();
		    	    	
		    	    	if (abstrakt != "" && !abstrakt.startsWith("Redirect ") && !abstrakt.startsWith("redirect")) {
		    	    		abstrakt = abstrakt + "\t";
		    	    		System.out.println(propertyName + abstrakt);
		    	    		context.write(new Text(propertyName), new Text(abstrakt));
		    	    	}
					}            
				}
	        }
	        catch(Exception e){
	            throw new IOException(e);
	        }
	    }
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
            Configuration conf = new Configuration();
            
            conf.set("xmlinput.start", "<page>");
            conf.set("xmlinput.end", "</page>");
            Job job = new Job(conf);
            job.setJarByClass(WikiParser.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            job.setMapperClass(WikiParser.Map.class);
            job.setReducerClass(Reducer.class);

            job.setInputFormatClass(XmlInputFormat1.class);
            job.setOutputFormatClass(TextOutputFormat.class);        
            
	        String path1 = "D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\enwiki-latest-pages-articles.xml"; //wiki xml dump
	        String path2 = "D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\wikiOutput"; //output direction

            FileInputFormat.addInputPath(job, new Path(path1));
            FileOutputFormat.setOutputPath(job, new Path(path2));

            job.waitForCompletion(true);
	}
}