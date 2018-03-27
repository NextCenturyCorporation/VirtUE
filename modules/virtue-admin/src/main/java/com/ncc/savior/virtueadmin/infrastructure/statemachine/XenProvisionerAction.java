package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;

public class XenProvisionerAction implements Action<ProvisionStates, ProvisionEvents> {

	@Autowired
	private AwsEc2Wrapper awsWrapper;

	@Autowired
	private ScheduledExecutorService executor;

	private VirtualMachineTemplate vmt;

	@Value(value = "")
	private String templatePath;

	public XenProvisionerAction() {
		vmt = new VirtualMachineTemplate(UUID.randomUUID().toString(), "XenTemplate", OS.LINUX, templatePath, null,
				"admin", false, new Date(), "System");
	}

	@Override
	public void execute(StateContext<ProvisionStates, ProvisionEvents> context) {
		Map<Object, Object> vars = context.getExtendedState().getVariables();
		VirtueUser user = (VirtueUser) vars.get(StateMachineKeys.KEY_USER);

		// VirtualMachine vm = awsWrapper.provisionVm(vmt, namePrefix, securityGroups,
		// serverKeyName, instanceType);

	}

}
