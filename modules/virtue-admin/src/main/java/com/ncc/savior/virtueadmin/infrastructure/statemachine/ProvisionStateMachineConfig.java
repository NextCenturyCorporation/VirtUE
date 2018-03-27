package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

@Configuration
@EnableStateMachineFactory
public class ProvisionStateMachineConfig extends EnumStateMachineConfigurerAdapter<ProvisionStates, ProvisionEvents> {
	@Override
	public void configure(StateMachineStateConfigurer<ProvisionStates, ProvisionEvents> states) throws Exception {
		states.withStates().initial(ProvisionStates.XEN_INITIATED).states(EnumSet.allOf(ProvisionStates.class));
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<ProvisionStates, ProvisionEvents> transitions)
			throws Exception {
		// @formatter:off
		transitions.withExternal()
			.source(ProvisionStates.UNPROVISIONED).target(ProvisionStates.XEN_INITIATED)
			.event(ProvisionEvents.PROVISIONED_XEN).action(new XenProvisionerAction()).and()
		.withExternal().source(ProvisionStates.XEN_INITIATED).target(ProvisionStates.XEN_NETWORKING_OK)
			.event(ProvisionEvents.XEN_NETWORKING_OBTAINED).action(new AwsNetworkRetrievalAction());
		// @formatter:on
	}

	private static class TestGuard implements Guard<ProvisionStates, ProvisionEvents> {

		@Override
		public boolean evaluate(StateContext<ProvisionStates, ProvisionEvents> context) {
			boolean ok = null != context.getExtendedState().getVariables()
					.get(StateMachineCloudManager.KEY_XEN_INFRASTRUCTURE_ID);
			return ok;
		}

	}

	private static class LogAction implements Action<ProvisionStates, ProvisionEvents> {
		private static final Logger logger = LoggerFactory.getLogger(ProvisionStateMachineConfig.class);

		@Override
		public void execute(StateContext<ProvisionStates, ProvisionEvents> context) {
			logger.debug("action");
			context.getStateMachine().sendEvent(ProvisionEvents.PROVISIONED_XEN);

		}
	}
}
