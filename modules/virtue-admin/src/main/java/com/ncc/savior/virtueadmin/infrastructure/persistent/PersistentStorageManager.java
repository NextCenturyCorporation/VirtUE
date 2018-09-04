package com.ncc.savior.virtueadmin.infrastructure.persistent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeType;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.data.IPersistentStorageDao;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.model.VirtuePersistentStorage;

/**
 * Manages creating, deleting etc persistent storage and keeping the database
 * and AWS in sync.
 */
public class PersistentStorageManager {
	private static final Logger logger = LoggerFactory.getLogger(PersistentStorageManager.class);
	private AwsEc2Wrapper ec2Wrapper;
	private IPersistentStorageDao persistentStorageDao;
	private String snapshotIdForNewPersistentStorageDrive;
	private String availabilityZone;

	public PersistentStorageManager(AwsEc2Wrapper ec2Wrapper, IPersistentStorageDao persistentStorageDao,
			String snapshotIdForNewPersistentStorageDrive, String availabilityZone) {
		super();
		this.ec2Wrapper = ec2Wrapper;
		this.persistentStorageDao = persistentStorageDao;
		this.snapshotIdForNewPersistentStorageDrive = snapshotIdForNewPersistentStorageDrive;
		this.availabilityZone = availabilityZone;
	}

	/**
	 * Returns the volume ID for the persistent storage for a given user and
	 * template ID. The template name should also be provided in order to properly
	 * tag the volume. If no volume exists, a new one will be created.
	 * 
	 * @param username
	 * @param virtueTemplateId
	 * @param virtueTemplateName
	 * @return
	 */
	public String getOrCreatePersistentStorageForVirtue(String username, String virtueTemplateId,
			String virtueTemplateName) {
		VirtuePersistentStorage ps = persistentStorageDao.getPersistentStorage(username, virtueTemplateId);
		String volumeId = null;
		if (ps != null) {
			// storage exists already, use that one.
			AmazonEC2 ec2 = ec2Wrapper.getEc2();
			try {
				DescribeVolumesRequest describeVolumesRequest = new DescribeVolumesRequest();
				describeVolumesRequest.withVolumeIds(ps.getInfrastructureId());
				DescribeVolumesResult result = ec2.describeVolumes(describeVolumesRequest);
				List<Volume> volumes = result.getVolumes();

				if (volumes.size() != 1) {
					logger.error("Error attempting to find volume with id=" + ps.getInfrastructureId() + ". Found "
							+ volumes.size() + " volumes.");
				}
				if (volumes.size() > 0) {
					Volume volume = volumes.get(0);
					volumeId = volume.getVolumeId();
				}
			} catch (AmazonEC2Exception e) {
				if (AwsEc2Wrapper.AWS_NOT_FOUND_ERROR_CODE.equals(e.getErrorCode())) {
					persistentStorageDao.deletePersistentStorage(ps);
					ps = null;
				} else {
					logger.error("Error trying to use AWS volume", e);
				}
			}
		}
		if (ps == null) {
			// storage doesn't exist, create a new one.
			String volumeName = username + "-" + virtueTemplateName + "-" + virtueTemplateId;
			volumeId = getNewVolumeId(volumeName);
			VirtuePersistentStorage newPs = new VirtuePersistentStorage(username, volumeId, virtueTemplateId);
			persistentStorageDao.savePersistentStorage(newPs);
		}
		return volumeId;
	}

	/**
	 * Deletes persistent storage if found. Throws a {@link SaviorException} if not
	 * found.
	 * 
	 * @param username
	 * @param virtueTemplateId
	 */
	public void deletePersistentStorage(String username, String virtueTemplateId) {
		VirtuePersistentStorage ps = persistentStorageDao.getPersistentStorage(username, virtueTemplateId);
		if (ps == null) {
			throw new SaviorException(SaviorErrorCode.STORAGE_NOT_FOUND,
					"Unable to find persistent storage for username=" + username + " virtueTemplateId="
							+ virtueTemplateId);
		}
		deletePersistentStorage(ps);
	}

	/**
	 * Deletes persistent storage. If found in database, but not in AWS, the
	 * database entry will be deleted with no error. If the volume is in use, a
	 * {@link SaviorException} will be thrown.
	 * 
	 * @param ps
	 */
	public void deletePersistentStorage(VirtuePersistentStorage ps) {
		DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest(ps.getInfrastructureId());
		try {
			ec2Wrapper.getEc2().deleteVolume(deleteVolumeRequest);
			persistentStorageDao.deletePersistentStorage(ps);
		} catch (AmazonEC2Exception e) {
			if (AwsEc2Wrapper.AWS_NOT_FOUND_ERROR_CODE.equals(e.getErrorCode())) {
				persistentStorageDao.deletePersistentStorage(ps);
			} else if (AwsEc2Wrapper.AWS_VOLUME_IN_USE_ERROR_CODE.equals(e.getErrorCode())) {
				throw new SaviorException(SaviorErrorCode.VOLUME_IN_USE,
						"Cannot delete volume because it is currently in use", e);
			} else {
				String msg = "Error attempting to delete persistent storage=" + ps;
				logger.error(msg, e);
				throw new SaviorException(SaviorErrorCode.AWS_ERROR, msg, e);
			}
		}

	}

	public Iterable<VirtuePersistentStorage> getAllPersistentStorage() {
		return persistentStorageDao.getAllPersistentStorage();
	}

	private String getNewVolumeId(String name) {
		AmazonEC2 ec2 = ec2Wrapper.getEc2();
		CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest();
		createVolumeRequest.setSnapshotId(snapshotIdForNewPersistentStorageDrive);
		createVolumeRequest.setAvailabilityZone(availabilityZone);
		createVolumeRequest.setVolumeType(VolumeType.Gp2);
		TagSpecification ts = new TagSpecification();
		Collection<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("Name", name));
		ts.setTags(tags);
		// createVolumeRequest.withTagSpecifications(ts);
		CreateVolumeResult result = ec2.createVolume(createVolumeRequest);

		Volume volume = result.getVolume();
		String id = volume.getVolumeId();

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(id).withTags(new Tag("Name", name));
		ec2.createTags(createTagsRequest);
		return id;
	}

	/**
	 * Attempts to delete all persistent storage.  
	 */
	public void deleteAllPersistentStorage() {
		Iterable<VirtuePersistentStorage> aps = getAllPersistentStorage();
		Map<VirtuePersistentStorage, SaviorException> failedDeletes = new TreeMap<VirtuePersistentStorage, SaviorException>();
		for (VirtuePersistentStorage ps : aps) {
			try {
				deletePersistentStorage(ps);
			} catch (SaviorException e) {
				failedDeletes.put(ps, e);
			}
		}
		if (!failedDeletes.isEmpty()) {
			if (failedDeletes.size() == 1) {
				VirtuePersistentStorage ps = failedDeletes.keySet().iterator().next();
				SaviorException e = failedDeletes.get(ps);
				throw new SaviorException(e.getErrorCode(), e.getLocalizedMessage(), e);
			} else {
				throw new SaviorException(SaviorErrorCode.MULTIPLE_STORAGE_ERROR, "Failed to delete "
						+ failedDeletes.size() + " persistent storage volumes.  Volumes=" + failedDeletes);
			}
		}
	}

	public Iterable<VirtuePersistentStorage> getPersistentStorageForUser(String username) {
		return persistentStorageDao.getPersistentStorageForUser(username);
	}

	public VirtuePersistentStorage getPersistentStorage(String username, String virtueTemplateId) {
		VirtuePersistentStorage ps = persistentStorageDao.getPersistentStorage(username, virtueTemplateId);
		return ps;
	}
}
