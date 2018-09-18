package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
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

	public S3ImageManager(VirtueAwsEc2Provider ec2Provider) {
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
		String key = path + "/" + filename;
		// This method has some specific limitations that need to be headed! check the
		// docs
		S3ObjectInputStream stream = null;
		try {
			logger.debug("Exporting image at " + path + " started.");
			S3Object obj = s3.getObject(bucketName, key);
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
	public void storeStreamAsImage(String path, String type, InputStream uncloseableStream) throws IOException {
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
					s3.putObject(bucketName, key, file);
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
