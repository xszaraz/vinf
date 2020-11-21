package xszaraz_vinf;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class CompareTwoFiles {

	public static CosineSimiliarity cosineSimilarity = new CosineSimiliarity();
	
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] Sentences = value.toString().split("\t");
			
			String title = Sentences[0];
			String abstrakt = Sentences[Sentences.length-1];
			
			context.write(new Text(title), new Text(abstrakt));
		}
	}
		
	public static class Map2 extends Mapper<LongWritable, Text, Text, Text> {
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] Sentences = value.toString().split("\t");
			
			String title = Sentences[0];
			String abstrakt = Sentences[Sentences.length-1];
			
			context.write(new Text(title), new Text(abstrakt));
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {

		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String[] lines = new String[2];
		    int i = 0;
		    double score = 0;
		    	    
		    for (Text text : values) {
		    	if (i == 2) {
		    		break;
		    	}
		    	System.out.println(text);
		        lines[i] = text.toString();
		        i++;
		    }
		    
		    System.out.println(lines.length);
		    if (lines.length >= 2) {
			    if (lines[0] != null && lines[1] != null) {	
			    	//lines[1] abstrakt z DBpedie, lines[0] abstrakt z Wiki
			    	score = cosineSimilarity.score(lines[1], lines[0]);	    
			    	
			    	System.out.println(score*100+"% match");
	
				    context.write(new Text(key + ":\tDBpedia: " + lines[1] + "\tWikipedia: " + lines[0]), new Text(String.valueOf(score*100)+"% match"));	    
			    }
		    }
		}
	}
	

	public static void main(String[] args) throws Exception {
	
		Configuration conf = new Configuration();

		Job job = new Job(conf, "Compare Two Files and Identify the Difference");
		
		job.setJarByClass(CompareTwoFiles.class);
		
		FileOutputFormat.setOutputPath(job, new Path("D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\comparisionOutput"));
		
		job.setReducerClass(CompareTwoFiles.Reduce.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		MultipleInputs.addInputPath(job, new Path("D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\dbpediaOut"), TextInputFormat.class, CompareTwoFiles.Map.class);
		MultipleInputs.addInputPath(job, new Path("D:\\STU_FIIT\\Inzinierske_studium\\3semester\\VINF\\wikiOut"), TextInputFormat.class, CompareTwoFiles.Map2.class);
		
		job.waitForCompletion(true);
	}
	
}
