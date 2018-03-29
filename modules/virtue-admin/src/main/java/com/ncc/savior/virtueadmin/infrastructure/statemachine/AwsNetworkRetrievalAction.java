package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

public class AwsNetworkRetrievalAction implements Action<ProvisionStates, StateEvents> {

	@Override
	public void execute(StateContext<ProvisionStates, StateEvents> context) {
		// context.get

	}

}
