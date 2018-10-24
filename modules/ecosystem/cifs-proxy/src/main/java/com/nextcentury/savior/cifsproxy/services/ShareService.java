package com.nextcentury.savior.cifsproxy.services;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.servlet.http.HttpSession;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nextcentury.savior.cifsproxy.controllers.MountController;
import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.model.FileShare.SharePermissions;

public class ShareService {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(ShareService.class);

	@Autowired
	private MountController mountController;

	NavigableSet<FileShare> shares = new ConcurrentSkipListSet<>();
	Map<FileShare, String> mountPoints = new ConcurrentHashMap<>();

	public NavigableSet<FileShare> getShares() {
		LOGGER.entry();
		LOGGER.exit(shares);
		return shares;
	}

	public void newShare(HttpSession session, FileShare share) {
		LOGGER.entry(session, share);
		Set<SharePermissions> permissions = share.getPermissions();
		if (permissions.isEmpty()) {
			IllegalArgumentException exception = new IllegalArgumentException("permissions cannot be empty");
			LOGGER.throwing(exception);
			throw exception;
		}
		String permissionsString;
		if (permissions.contains(SharePermissions.WRITE)) {
			permissionsString = "rw";
		} else {
			permissionsString = "r";
		}
		String mountPoint = getMountPoint(share);
		mountController.mountDirectory(session, mountPoint, share.getPath(), permissionsString, mountPoint);
		shares.add(share);
		LOGGER.exit();
	}

	private String getMountPoint(FileShare fs) {
		LOGGER.entry(fs);
		String mountPoint = mountPoints.get(fs);
		if (mountPoint == null) {
			mountPoint = fs.getName();
			mountPoints.put(fs, mountPoint);
		}
		LOGGER.exit(mountPoint);
		return mountPoint;
	}
}
