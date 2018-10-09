package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.ContainerFormat;
import com.amazonaws.services.ec2.model.CreateInstanceExportTaskRequest;
import com.amazonaws.services.ec2.model.CreateInstanceExportTaskResult;
import com.amazonaws.services.ec2.model.DescribeExportTasksRequest;
import com.amazonaws.services.ec2.model.DescribeExportTasksResult;
import com.amazonaws.services.ec2.model.DiskImageFormat;
import com.amazonaws.services.ec2.model.ExportEnvironment;
import com.amazonaws.services.ec2.model.ExportTask;
import com.amazonaws.services.ec2.model.ExportToS3TaskSpecification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;

/**
 * S3 based image manager.
 *
 */
public class S3ImageManager implements IXenGuestImageManager {
	private static final Logger logger = LoggerFactory.getLogger(S3ImageManager.class);

	@Value("${virtue.aws.s3.image.bucketName}")
	private String bucketName;
	private AmazonS3 s3;
	private String filename;

	private ExecutorService executor;

	private AmazonEC2 ec2;

	public S3ImageManager(VirtueAwsEc2Provider ec2Provider) {
		this.ec2 = ec2Provider.getEc2();
		this.s3 = ec2Provider.getS3();
		this.filename = "disk.qcow2";
		ThreadFactory threadFactory = new ThreadFactory() {
			private int id = 0;

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, getName());
			}

			private synchronized String getName() {
				id++;
				return "S3-ImageUpload-" + id;
			}
		};
		this.executor = Executors.newFixedThreadPool(2, threadFactory);
	}

	@Override
	public void pushImageToStream(String path, OutputStream out) throws IOException {
		pushImageToStreamFromCustomBucket(bucketName, path, out);
	}

	private void pushImageToStreamFromCustomBucket(String bucket, String path, OutputStream out) throws IOException {
		String key = path + "/" + filename;
		// This method has some specific limitations that need to be headed! check the
		// docs
		S3ObjectInputStream stream = null;
		try {
			logger.debug("Exporting image at " + path + " started.");
			S3Object obj = s3.getObject(bucket, key);
			stream = obj.getObjectContent();
			IOUtils.copyLarge(stream, out);
			logger.debug("Exporting image at " + path + " finished.");
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					logger.warn("unable to close s3 object stream", e);
				}
			}
		}

	}

	@Override
	public void pushImageToStreamWindows(String path, OutputStream out) throws IOException {
		try {
			String bucket = "temp-win-images";
			CreateInstanceExportTaskRequest createInstanceExportTaskRequest = new CreateInstanceExportTaskRequest();
			logger.debug("attempting to export " + path + " to bucket " + bucket);
			createInstanceExportTaskRequest.withInstanceId("i-00085cc3a52b6b827");
			ExportToS3TaskSpecification exportToS3Task = new ExportToS3TaskSpecification();
			exportToS3Task.withContainerFormat(ContainerFormat.Ova);
			exportToS3Task.withDiskImageFormat(DiskImageFormat.VMDK);
			exportToS3Task.withS3Bucket(bucket);
			createInstanceExportTaskRequest.setExportToS3Task(exportToS3Task);
			createInstanceExportTaskRequest.setTargetEnvironment(ExportEnvironment.Vmware);
			CreateInstanceExportTaskResult exportResult = ec2.createInstanceExportTask(createInstanceExportTaskRequest);
			String taskId = exportResult.getExportTask().getExportTaskId();
			String state = exportResult.getExportTask().getState();
			logger.debug("image state=" + state);
			while (!"completed".equals(state)) {
				JavaUtil.sleepAndLogInterruption(1000);
				DescribeExportTasksRequest detr = new DescribeExportTasksRequest();
				detr.withExportTaskIds(taskId);
				DescribeExportTasksResult taskResult = ec2.describeExportTasks(detr);
				List<ExportTask> tasks = taskResult.getExportTasks();
				if (tasks.size() == 1) {
					ExportTask task = taskResult.getExportTasks().get(0);
					state = task.getState();
					String msg = task.getStatusMessage();
					logger.debug("image state=" + state+" msg="+msg);
				} else {
					throw new SaviorException(SaviorErrorCode.AWS_ERROR,
							"Error tracking windows export for path" + path);
				}
			}
			logger.debug("finished pushing image to s3, path=" + path);
			pushImageToStreamFromCustomBucket(bucket, path, out);
		} catch (Exception e) {
			logger.debug("Failed remove me", e);
		}
	}

	@Override
	public void storeStreamAsImage(String path, String type, InputStream uncloseableStream) throws IOException {
		storeStreamAsImageFromCustomBucket(bucketName, path, type, uncloseableStream);
	}

	private void storeStreamAsImageFromCustomBucket(String bucket, String path, String type,
			InputStream uncloseableStream) throws IOException {
		String key = path + "/disk." + type;
		File tmp = null;
		try {
			tmp = File.createTempFile("aws-img", ".dat");
			tmp.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tmp);
			IOUtils.copy(uncloseableStream, fos);
			fos.close();
			File file = tmp;
			Runnable r = () -> {
				try {
					logger.info("Starting import to S3.  Key=" + key);
					s3.putObject(bucket, key, file);
					logger.info("Finished importing to S3.  Key=" + key);
				} finally {
					if (file != null && file.exists()) {
						logger.info("deleting file at " + file.getAbsolutePath());
						file.delete();
					}
				}

			};
			runAsync(r);
		} catch (IOException e) {
			if (tmp != null && tmp.exists()) {
				tmp.delete();
			}
			throw e;
		}
	}

	private void runAsync(Runnable r) {
		executor.execute(r);
	}

}
