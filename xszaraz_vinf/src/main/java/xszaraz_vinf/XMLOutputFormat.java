package xszaraz_vinf;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class XMLOutputFormat extends FileOutputFormat<Text, Text> {
	protected static class XMLRecordWriter extends RecordWriter<Text, Text> {
		private DataOutputStream out;
		
		public XMLRecordWriter(DataOutputStream out) throws IOException {
			this.out = out;
			out.writeBytes("<Output>\n");
		}
		
		private void writeStyle(String xml_tag,String tag_value) throws IOException{
			out.writeBytes("<"+xml_tag+">"+tag_value+"</"+xml_tag+">\n");
		}
		
		public synchronized void write(Text key, Text value) throws IOException{
			out.writeBytes("<property>\n");
			this.writeStyle("title", key.toString());
			this.writeStyle("abstrakt", value.toString());
			out.writeBytes("</property>\n");
		}
		
		public synchronized void close(TaskAttemptContext job) throws IOException{
			try {
				out.writeBytes("</Output>\n");
			} finally {
				out.close();
			}
		}
	}
	
	public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext job) throws IOException {
		String file_extension = ".xml";
		Path file = getDefaultWorkFile(job, file_extension);
		FileSystem fs = file.getFileSystem(job.getConfiguration());
		FSDataOutputStream fileOut = fs.create(file, false);
		return new XMLRecordWriter(fileOut);
		}
	}
