package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class BaseApplicationInstance implements IApplicationInstance {

	protected ApplicationDefinition applicationDefinition;

	public BaseApplicationInstance() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance#getApplicationDefinition()
	 */
	@Override
	public ApplicationDefinition getApplicationDefinition() {
		return applicationDefinition;
	}

	/* (non-Javadoc)
	 * @see com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance#setApplicationDefinition(com.ncc.savior.virtueadmin.model.ApplicationDefinition)
	 */
	@Override
	public void setApplicationDefinition(ApplicationDefinition applicationDefinition) {
		this.applicationDefinition = applicationDefinition;
	}

}