package xszaraz_vinf;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.IntWritable;
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


public class DbpediaParser {

	public static DbpediaTextSummarize summarizer = new DbpediaTextSummarize();
	
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

	    public void setup(Context context) throws IOException, InterruptedException{
	        Path pt=new Path("D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\shortabstract_en.nt");
	        FileSystem fs = FileSystem.get(new Configuration());
	        BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
	        String line;
	        line=br.readLine();
    		String title = "";
    		String abstrakt = "";
	        while (line != null){
	    		String[] texts = line.split("> ");	
	    		//texts[0] = titul
	    		title = summarizer.SummarizeTitle(texts[0]);	
	    		//texts[2] = abstrakt
	    		abstrakt = summarizer.SummarizeAbstract(texts[2]);
	            System.out.println("title: " + title + ", abstrakt: " + abstrakt);
	            line=br.readLine();
	            context.write(new Text(title), new Text(abstrakt));
	        }
	    }		
	}
	
	public static class Reduce extends Reducer<Text, Text, Text, Text> {

	    @Override
	    protected void setup(Context context) throws IOException, InterruptedException {
	        context.write(new Text("<dbpedia>"), null);
	    }

	    @Override
	    protected void cleanup(Context context) throws IOException, InterruptedException {
	        context.write(new Text("</dbpedia>"), null);
	    }

	    private Text outputKey = new Text();
	    
	    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	        for (Text value : values) {
		        outputKey.set(constructPropertyXml(key, value));
		        context.write(outputKey, null);		
	        }
	    }

	    public static String constructPropertyXml(Text title, Text abstrakt) {
	        StringBuilder sb = new StringBuilder();
	        sb.append("<property><title>").append(title)
	        .append("</title><abstrakt>").append(abstrakt)
	        .append("</abstrakt></property>");
	        return sb.toString();
	    }
	}

	    public static void main(String[] args) throws Exception {
	        Configuration conf = new Configuration();
	        Job job = new Job(conf, "Read a File");

	        job.setOutputKeyClass(Text.class);
	        job.setOutputValueClass(Text.class);

	        job.setMapperClass(DbpediaParser.Map.class);
	        job.setReducerClass(DbpediaParser.Reduce.class);

	        job.setInputFormatClass(TextInputFormat.class);
	        job.setOutputFormatClass(TextOutputFormat.class);

	        FileInputFormat.addInputPath(job, new Path("D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\shortabstract_en.nt"));
	        FileOutputFormat.setOutputPath(job, new Path("D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\dbpediaoutput"));
	        
	        job.setJarByClass(DbpediaParser.class);     
	        job.waitForCompletion(true);
	    }
}