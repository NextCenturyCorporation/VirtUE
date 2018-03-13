package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.util.JavaUtil;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

public class AwsVmUpdater {
	private static final Logger logger = LoggerFactory.getLogger(AwsVmUpdater.class);
	private IUpdateNotifier notifier;
	private AmazonEC2 ec2;
	private SshKeyInjector sshKeyInjector;
	private Collection<VirtualMachine> networkingQueue;
	private ScheduledExecutorService executor;
	@SuppressWarnings("rawtypes")
	private Map<String, ScheduledFuture> reachableFutureMap;
	@SuppressWarnings("rawtypes")
	private Map<String, ScheduledFuture> namingFutureMap;
	private IKeyManager keyManager;

	@SuppressWarnings("rawtypes")
	public AwsVmUpdater(AmazonEC2 ec2, IUpdateNotifier notifier, IKeyManager keyManager) {
		this.ec2 = ec2;
		this.notifier = notifier;
		this.keyManager = keyManager;
		this.reachableFutureMap = Collections.synchronizedMap(new HashMap<String, ScheduledFuture>());
		this.namingFutureMap = Collections.synchronizedMap(new HashMap<String, ScheduledFuture>());
		this.networkingQueue = Collections.synchronizedCollection(new ArrayList<VirtualMachine>());
		this.sshKeyInjector = new SshKeyInjector();
		this.executor = Executors.newScheduledThreadPool(3, new ThreadFactory() {

			private int num = 1;

			@Override
			public synchronized Thread newThread(Runnable r) {
				String name = "aws-updated-" + num;
				num++;
				return new Thread(r, name);
			}
		});
		startProvisioningQueue();
		startDebug();
	}

	private void startDebug() {
		Runnable command = new Runnable() {
			@SuppressWarnings("rawtypes")
			@Override
			public void run() {
				ArrayList<VirtualMachine> net = new ArrayList<VirtualMachine>(networkingQueue);
				HashMap<String, ScheduledFuture> naming = new HashMap<String, ScheduledFuture>(namingFutureMap);
				HashMap<String, ScheduledFuture> reachable = new HashMap<String, ScheduledFuture>(reachableFutureMap);
				StringBuilder sb = new StringBuilder();
				sb.append("AWS UPDATER STATUS:\n").append("Naming:\n");
				int i = 1;
				for (Entry<String, ScheduledFuture> entry : naming.entrySet()) {
					sb.append("  ").append(i).append(". ").append(entry.getKey()).append(" - ").append(entry.getValue())
							.append("\n");
					i++;
				}
				i = 1;
				sb.append("Networking:\n");
				for (VirtualMachine n : net) {
					sb.append("  ").append(i).append(". ").append(n).append("\n");
					i++;
				}
				i = 1;
				sb.append("Reachability:\n");
				for (Entry<String, ScheduledFuture> entry : reachable.entrySet()) {
					sb.append("  ").append(i).append(". ").append(entry.getKey()).append(" - ").append(entry.getValue())
							.append("\n");
					i++;
				}
				logger.debug(sb.toString());
			}
		};
		// executor.scheduleAtFixedRate(command, 500, 5000, TimeUnit.MILLISECONDS);

	}

	/**
	 * Starts executor task for checking the networking queue and testing to see if
	 * networking is available from AWS for those VMs.
	 */
	private void startProvisioningQueue() {
		Runnable getNetworkingCommand = new Runnable() {
			@Override
			public void run() {
				ArrayList<VirtualMachine> copy = new ArrayList<VirtualMachine>(networkingQueue);
				updateNetworking(copy);
			}
		};
		executor.scheduleWithFixedDelay(getNetworkingCommand, 1000, 5000, TimeUnit.MILLISECONDS);
	}

	public void addVmToProvisionPipeline(ArrayList<VirtualMachine> vms) {
		logger.trace("Scheduling " + vms.size() + " vms to be renamed.");
		for (VirtualMachine vm : vms) {
			Runnable command = new Runnable() {
				@Override
				public void run() {
					try {
						nameVmInAws(vm);
					} catch (Exception e) {
						logger.trace("Naming in AWS failed for vm=" + vm.getId());
						return;
					}
					ScheduledFuture<?> future = namingFutureMap.get(vm.getId());
					future.cancel(false);
					namingFutureMap.remove(vm.getId());
					networkingQueue.add(vm);
				}
			};
			ScheduledFuture<?> future = executor.scheduleAtFixedRate(command, 1, 3, TimeUnit.SECONDS);
			namingFutureMap.put(vm.getId(), future);
		}
	}

	/**
	 * Attempts to update networking from AWS. The VMs that have been successfully
	 * been updated are removed from the networking queue and added to reachability.
	 * 
	 * @param vms
	 */
	private void updateNetworking(Collection<VirtualMachine> vms) {
		ArrayList<VirtualMachine> updated = new ArrayList<VirtualMachine>();
		AwsUtil.updateNetworking(ec2, vms);
		for (VirtualMachine vm : vms) {
			if (isNotEmpty(vm.getHostname())) {
				networkingQueue.remove(vm);
				File privateKeyFile = keyManager.getKeyFileByName(vm.getPrivateKey());
				addToReachabilityAndAddRsaQueue(vm, privateKeyFile);
				updated.add(vm);
			}
		}
		if (!updated.isEmpty()) {
			notifier.notifyUpdatedVms(updated);
		}
	}

	/**
	 * Since each VM is tested for reachability independently, this test is done as
	 * its own task and the future is stored in a map. On success, the future is
	 * canceled.
	 * 
	 * @param vm
	 */
	private void addToReachabilityAndAddRsaQueue(VirtualMachine vm, File privateKeyFile) {
		Runnable command = new Runnable() {
			@Override
			public void run() {
				testReachabilityAndAddRsaKey(vm, privateKeyFile);
			}
		};
		ScheduledFuture<?> future = executor.scheduleAtFixedRate(command, 25, 1, TimeUnit.MILLISECONDS);
		reachableFutureMap.put(vm.getId(), future);

	}

	/**
	 * Tests the reachability of a VM. If reachable, add a new unique RSA key and
	 * add it to the VM and notify via the notifier.
	 * 
	 * @param vm
	 */
	protected void testReachabilityAndAddRsaKey(VirtualMachine vm, File privateKeyFile) {
		if (SshUtil.isVmReachable(vm, privateKeyFile)) {
			int numberOfAttempts = 3;
			if (OS.LINUX.equals(vm.getOs())) {
				String newPrivateKey = null;
				// do while so we always attempt at least once
				do {
					try {
						numberOfAttempts--;
						newPrivateKey = sshKeyInjector.injectSshKey(vm, privateKeyFile);
						break;
					} catch (Exception e) {
						logger.error("Injecting new SSH key failed.  Clients will not be able to login.", e);
					} finally {

					}
				} while (numberOfAttempts > 0);
				vm.setPrivateKey(newPrivateKey);
				reachableFutureMap.get(vm.getId()).cancel(false);
				reachableFutureMap.remove(vm.getId());
				notifier.notifyUpdatedVm(vm);
			}
		}
	}

	private boolean isNotEmpty(String stringToTest) {
		return stringToTest != null && !stringToTest.trim().equals("");
	}

	/**
	 * Renames the VMs based on the {@link VirtualMachine#getName()} method. The
	 * name is set earlier in the provision process.
	 * 
	 * @param vms
	 */
	private void nameVmsInAws(ArrayList<VirtualMachine> vms) {
		vms = new ArrayList<VirtualMachine>(vms);
		int tries = 3;
		while (!vms.isEmpty() && tries > 0) {
			Iterator<VirtualMachine> itr = vms.iterator();
			while (itr.hasNext()) {
				VirtualMachine vm = itr.next();
				try {
					nameVmInAws(vm);
					itr.remove();
				} catch (Exception e) {
					logger.warn("failed to rename AWS machine for VM='" + vm.getName() + "': " + e.getMessage());
				}
				JavaUtil.sleepAndLogInterruption(750);
			}
			tries--;
		}
	}

	private void nameVmInAws(VirtualMachine vm) {
		CreateTagsRequest ctr = new CreateTagsRequest();
		ctr.withResources(vm.getInfrastructureId());
		Collection<Tag> tags = new ArrayList<Tag>();
		// TODO ?? tags.add(new Tag("Autogen-Virtue-VM", serverUser));
		tags.add(new Tag("Name", vm.getName()));
		ctr.setTags(tags);
		ec2.createTags(ctr);
	}

	public static interface IUpdateNotifier {
		void notifyUpdatedVms(Collection<VirtualMachine> vm);

		void notifyUpdatedVm(VirtualMachine vm);
	}

}
