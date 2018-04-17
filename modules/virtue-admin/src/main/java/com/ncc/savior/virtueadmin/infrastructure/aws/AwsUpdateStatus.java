package com.ncc.savior.virtueadmin.infrastructure.aws;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.BaseGroupedVmPipelineComponent;
import com.ncc.savior.virtueadmin.infrastructure.pipelining.PipelineWrapper;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class AwsUpdateStatus extends BaseGroupedVmPipelineComponent<VirtualMachine> {

	private Collection<VmState> successStatus;
	private AmazonEC2 ec2;

	public AwsUpdateStatus(ScheduledExecutorService executor, AmazonEC2 ec2, Collection<VmState> successStatus) {
		super(executor, true, 2000, 2500);
		this.ec2 = ec2;
		this.successStatus = successStatus;
	}

	@Override
	protected void onExecute(Collection<PipelineWrapper<VirtualMachine>> wrappers) {
		try {
		AwsUtil.updateStatusOnVms(ec2, unwrap(wrappers));
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
				doOnFailure(wrappers);
			}
		}
		Iterator<PipelineWrapper<VirtualMachine>> itr = wrappers.iterator();
		while (itr.hasNext()) {
			PipelineWrapper<VirtualMachine> wrapper = itr.next();
			if (!successStatus.contains(wrapper.get().getState())) {
				// remove all of the unsuccessful statuses
				itr.remove();
			}
		}
		doOnSuccess(wrappers);
	}
}
