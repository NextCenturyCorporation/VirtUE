package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

public class AwsUpdateStatusCompletableFutureService
		extends BaseGroupedScheduledCompletableFutureService<VirtualMachine, VirtualMachine, VmState> {

	private AmazonEC2 ec2;

	protected AwsUpdateStatusCompletableFutureService(ScheduledExecutorService executor, AmazonEC2 ec2) {
		super(executor, true, 2000, 2500);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(Collection<Wrapper> wrappers) {
		Collection<VirtualMachine> vms = super.unwrapParameter(wrappers);
		try {
			AwsUtil.updateStatusOnVms(ec2, vms);
		} catch (AmazonEC2Exception e) {
			if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
				// TODO
				return;
			}
		}
		Iterator<Wrapper> itr = wrappers.iterator();
		while (itr.hasNext()) {
			Wrapper wrapper = itr.next();
			VmState successState = wrapper.extra;
			if (successState.equals(wrapper.param.getState())) {
				wrapper.result = wrapper.param;
			} else {
				// remove all of the unsuccessful statuses
				itr.remove();
			}
		}
		onSuccess(wrappers);

	}

	@Override
	protected String getServiceName() {
		return "AwsUpdateService";
	}

}
