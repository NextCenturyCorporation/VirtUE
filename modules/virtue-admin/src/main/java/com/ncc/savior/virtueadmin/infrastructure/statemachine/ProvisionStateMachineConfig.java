package com.ncc.savior.virtueadmin.infrastructure.statemachine;

import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class ProvisionStateMachineConfig extends EnumStateMachineConfigurerAdapter<ProvisionStates, StateEvents> {
	private static final Logger logger = LoggerFactory.getLogger(ProvisionStateMachineConfig.class);
	protected static Random rand;
	protected static ScheduledExecutorService executor;

	static {
		ProvisionStateMachineConfig.rand = new Random();
		ProvisionStateMachineConfig.executor = Executors.newScheduledThreadPool(3);
	}

	@Override
	public void configure(StateMachineStateConfigurer<ProvisionStates, StateEvents> states) throws Exception {
		states.withStates().initial(ProvisionStates.UNPROVISIONED).states(EnumSet.allOf(ProvisionStates.class));
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<ProvisionStates, StateEvents> transitions) throws Exception {
		// @formatter:off
		ExternalTransitionConfigurer<ProvisionStates, StateEvents> t = transitions.withExternal()
			.source(ProvisionStates.UNPROVISIONED).target(ProvisionStates.XEN_INITIATED)
			.event(StateEvents.SUCCESS).action(new TestAction(1,17,1000)).and()
		.withExternal().source(ProvisionStates.XEN_INITIATED).target(ProvisionStates.XEN_NETWORKING_OK)
			.event(StateEvents.SUCCESS).action(new TestAction(2,12,2000)).and()
		.withExternal().source(ProvisionStates.XEN_NETWORKING_OK).target(ProvisionStates.XEN_READY)
			.event(StateEvents.SUCCESS).action(new TestAction(3,7,5000));
		// @formatter:on
		for (ProvisionStates state : ProvisionStates.values()) {
			t.and().withExternal().source(state).target(state).event(StateEvents.FAILURE);
		}

	}

	private class TestAction implements Action<ProvisionStates, StateEvents> {
		private final Logger logger = LoggerFactory.getLogger(TestAction.class);
		private int thresh;
		private int i;
		private long wait;

		public TestAction(int i, int thresh, long wait) {
			this.i = i;
			this.thresh = thresh;
			this.wait = wait;
		}

		@Override
		public void execute(StateContext<ProvisionStates, StateEvents> context) {
			final String action = "action" + i;
			final int threshold = thresh;
			ScheduledFuture<?> future;
			Runnable r = new Runnable() {

				@Override
				public void run() {
					logger.debug(action);
					int val = rand.nextInt(20);
					if (val > threshold) {
						logger.debug("action=" + action + " run=" + context.getStateMachine().getId() + " val=" + val
								+ " success!");
						context.getStateMachine().sendEvent(StateEvents.SUCCESS);
					} else {
						context.getStateMachine().sendEvent(StateEvents.FAILURE);
						logger.debug("action=" + action + " run=" + context.getStateMachine().getId() + " val=" + val
								+ " failure!");

					}
				}
			};
			future = executor.scheduleAtFixedRate(r, wait, wait, TimeUnit.MILLISECONDS);
		}
	}

	// private class TestAction1 implements Action<ProvisionStates, StateEvents> {
	// private final Logger logger = LoggerFactory.getLogger(TestAction1.class);
	//
	// @Override
	// public void execute(StateContext<ProvisionStates, StateEvents> context) {
	// String action = "action1";
	// logger.debug(action);
	// int val = rand.nextInt(20);
	// if (val > 16) {
	// logger.debug("action=" + action + " val=" + val + " success!");
	// context.getStateMachine().sendEvent(StateEvents.SUCCESS);
	// } else {
	// context.getStateMachine().sendEvent(StateEvents.FAILURE);
	// logger.debug("action=" + action + " val=" + val + " failure!");
	// }
	// }
	// }
	//
	// private class TestAction2 implements Action<ProvisionStates, StateEvents> {
	// private final Logger logger = LoggerFactory.getLogger(TestAction2.class);
	//
	// @Override
	// public void execute(StateContext<ProvisionStates, StateEvents> context) {
	// String action = "action2";
	// logger.debug(action);
	// int val = rand.nextInt(20);
	// if (val > 10) {
	// logger.debug("action=" + action + " val=" + val + " success!");
	// context.getStateMachine().sendEvent(StateEvents.SUCCESS);
	// } else {
	// context.getStateMachine().sendEvent(StateEvents.FAILURE);
	// logger.debug("action=" + action + " val=" + val + " failure!");
	// }
	//
	// }
	// }
	//
	// private class TestAction3 implements Action<ProvisionStates, StateEvents> {
	// private final Logger logger = LoggerFactory.getLogger(TestAction3.class);
	//
	// @Override
	// public void execute(StateContext<ProvisionStates, StateEvents> context) {
	// String action = "action3";
	// logger.debug(action);
	// int val = rand.nextInt(20);
	// if (val > 8) {
	// logger.debug("action=" + action + " val=" + val + " success!");
	// context.getStateMachine().sendEvent(StateEvents.SUCCESS);
	// } else {
	// context.getStateMachine().sendEvent(StateEvents.FAILURE);
	// logger.debug("action=" + action + " val=" + val + " failure!");
	// }
	//
	// }
	// }
}
