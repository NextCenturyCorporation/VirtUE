package com.ncc.savior.virtueadmin.infrastructure.future;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsUtil;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Service will get the status of the AWS VM and update the state in
 * {@link VirtualMachine}. This service will retry until the VM has the given
 * success state and then complete the future.
 * 
 *
 */
public class AwsUpdateStatusCompletableFutureService
		extends BaseGroupedScheduledCompletableFutureService<VirtualMachine, VirtualMachine, VmState> {

	private AmazonEC2 ec2;

	protected AwsUpdateStatusCompletableFutureService(ScheduledExecutorService executor, AmazonEC2 ec2,
			int timeoutMillis) {
		super(executor, true, 2000, 2500, timeoutMillis);
		this.ec2 = ec2;
	}

	@Override
	protected void onExecute(Collection<Wrapper> wrappers) {
		Collection<VirtualMachine> vms = super.unwrapParameter(wrappers);
		try {
			AwsUtil.updateStatusOnVms(ec2, vms);
		} catch (AmazonEC2Exception e) {
			for (BaseCompletableFutureService<VirtualMachine, VirtualMachine, VmState>.Wrapper wrapper : wrappers) {
				VirtualMachine vm = wrapper.param;
				if (e.getErrorMessage().contains(vm.getName())) {
					onFailure(wrapper, e);
				}
				//If we are trying to delete an already deleted vm, AWS won't find it.
				if (e.getErrorCode().equals("InvalidInstanceID.NotFound") && VmState.DELETED.equals(wrapper.extra)&& e.getErrorMessage().contains(vm.getInfrastructureId())) {
					vm.setState(VmState.DELETED);
				}
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
