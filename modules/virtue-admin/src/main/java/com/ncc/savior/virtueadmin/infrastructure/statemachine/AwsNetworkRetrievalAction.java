package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

public class AwsNetworkRetrievalAction implements Action<ProvisionStates, ProvisionEvents> {

	@Override
	public void execute(StateContext<ProvisionStates, ProvisionEvents> context) {
		// context.get

	}

}
