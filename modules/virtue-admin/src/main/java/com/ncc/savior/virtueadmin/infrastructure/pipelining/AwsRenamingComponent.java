package com.ncc.savior.virtueadmin.infrastructure.pipelining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtualMachine;

import net.bytebuddy.agent.VirtualMachine;

/**
 * Component of an {@link IUpdatePipeline} that will rename a VM instance in
 * AWS. The name is based on {@link VirtualMachine#getName()}.
 */
public class AwsRenamingComponent extends BaseIndividualVmPipelineComponent {
	private static final Logger logger = LoggerFactory.getLogger(AwsRenamingComponent.class);
	private AmazonEC2 ec2;

	public AwsRenamingComponent(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, true, 1000, 3000);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(JpaVirtualMachine vm) {
		try {
			nameVmInAws(vm);
			doOnSuccess(vm);
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
	 */
	private void nameVmInAws(JpaVirtualMachine vm) {
		CreateTagsRequest ctr = new CreateTagsRequest();
		ctr.withResources(vm.getInfrastructureId());
		Collection<Tag> tags = new ArrayList<Tag>();
		// TODO ?? tags.add(new Tag("Autogen-Virtue-VM", serverUser));
		tags.add(new Tag("Name", vm.getName()));
		ctr.setTags(tags);
		ec2.createTags(ctr);
	}

}
