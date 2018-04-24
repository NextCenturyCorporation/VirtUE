package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * 
 */
public class AwsRenamingCompletableFutureService
		extends BaseIndividualScheduledCompletableFutureService<VirtualMachine, VirtualMachine, Void> {
	private static final Logger logger = LoggerFactory.getLogger(AwsRenamingCompletableFutureService.class);
	private AmazonEC2 ec2;

	public AwsRenamingCompletableFutureService(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, true, 1000, 3000);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(Wrapper wrapper) {
		VirtualMachine vm = wrapper.param;
		try {
			boolean success = nameVmInAws(vm);
			if (success) {
				onSuccess(vm.getId(), vm, wrapper.future);
			}
		} catch (Exception e) {
			logger.trace("Naming in AWS failed for vm=" + vm.getId());
			return;
		}
	}

	/**
	 * Renames the VM based on the {@link VirtualMachine#getName()} method. The name
	 * is set earlier in the provision process.
	 * 
	 * @param vm
	 * @return
	 */
	private boolean nameVmInAws(VirtualMachine vm) {
		CreateTagsRequest ctr = new CreateTagsRequest();
		ctr.withResources(vm.getInfrastructureId());
		Collection<Tag> tags = new ArrayList<Tag>();
		// TODO ?? tags.add(new Tag("Autogen-Virtue-VM", serverUser));
		tags.add(new Tag("Name", vm.getName()));
		ctr.setTags(tags);
		CreateTagsResult result = ec2.createTags(ctr);
		return result.getSdkHttpMetadata().getHttpStatusCode() < 300;
	}

	@Override
	protected String getId(BaseCompletableFutureService<VirtualMachine, VirtualMachine, Void>.Wrapper wrapper) {
		return wrapper.param.getId();
	}

	@Override
	public void onServiceStart() {
		// do nothing
	}

	@Override
	protected String getServiceName() {
		return "AwsRenamingService";
	}

}
