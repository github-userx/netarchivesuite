package dk.netarkivet.common.utils.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.netarkivet.common.distribute.hadoop.HadoopBatchStatus;
import dk.netarkivet.common.distribute.hadoop.HadoopArcRepositoryClient;
import dk.netarkivet.common.distribute.hadoop.WholeFileInputFormat;
import dk.netarkivet.common.exceptions.IOFailure;

public class FileChecksumJob extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new FileChecksumJob(), args));
    }

    @Override public int run(String[] args) throws Exception {

        HadoopArcRepositoryClient client = new HadoopArcRepositoryClient();

        Job job = Job.getInstance();
        Configuration conf = job.getConfiguration();

        job.setJobName(FileChecksumJob.class.getName());
        job.setJarByClass(FileChecksumJob.class);

        //Set up input format
        Path inputDir = new Path(args[0]);
        setupWholeFileInput(job, conf, inputDir);

        job.setMapperClass(ChecksumMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(DuplicateReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        HadoopBatchStatus result = client.hadoopBatch(job, null);
        System.out.println(result);

        try (InputStream resultFile = result.getResultFile().getInputStream()) {
            IOUtils.copy(resultFile, System.out);
        } finally {
            result.getResultFile().cleanup();
        }
        System.out.println(result);

        return result.isSuccess() ? 0 : 1;

    }

    private void setupWholeFileInput(Job job, Configuration conf, Path inputDir) throws IOException {
        System.out.println("Inputdir=" + inputDir.makeQualified(inputDir.getFileSystem(conf)));
        job.setInputFormatClass(WholeFileInputFormat.class);
        WholeFileInputFormat.addInputPath(job, inputDir);
        WholeFileInputFormat.setInputPathFilter(job, MyPathFilter.class);
        WholeFileInputFormat.setInputDirRecursive(job, true);
    }

    public static class MyPathFilter extends Configured implements PathFilter {

        @Override public boolean accept(Path path) {
            String name = path.getName();

            try {
                FileSystem fileSystem = FileSystem.get(getConf());

                if (fileSystem.isDirectory(path)) {
                    return true;
                }
                return name.endsWith(".arc");
                //                return name.endsWith(".warc") || name.endsWith(".warc.gz") || name.endsWith(".arc") || name.endsWith(".arc.gz");
            } catch (IOException e) {
                throw new IOFailure("message", e);
            }
        }
    }

    public static class ChecksumMapper extends Mapper<Text, BytesWritable, Text, Text> {

        private static final Logger log = LoggerFactory.getLogger(ChecksumMapper.class);

        @Override protected void map(Text key, BytesWritable value, Context context)
                throws IOException, InterruptedException {
            log.info("Working on file {}", key.toString());
            Text out_key = new Text(DigestUtils.md5Hex(value.getBytes()));
            Text out_value = new Text(key.toString());
            context.write(out_key, out_value);
        }
    }

    public static class DuplicateReducer extends Reducer<Text, Text, Text, Text> {
        private static final Logger log = LoggerFactory.getLogger(FileChecksumJob.DuplicateReducer.class);

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            //DANGER WILL ROBINSON, THE ITERABLE IS AN ITERATOR AND CAN ONLY BE READ ONCE
            List<Text> list = new ArrayList<>();
            Iterator<Text> iterator = values.iterator();
            while (iterator.hasNext()) {
                Text next = iterator.next();
                list.add(new Text(next.toString()));
            }

            log.info("Found {} files {} with checksum {}", list.size(), list, key.toString());
            if (list.size() > 1) {
                String paths = String.join(
                        File.pathSeparator,
                        list.stream().map(Text::toString).collect(Collectors.toList()));
                context.write(key, new Text(paths));
            }

        }
    }

}