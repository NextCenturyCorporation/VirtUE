package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class BaseApplicationInstance implements IApplicationInstance {

	protected ApplicationDefinition applicationDefinition;
	protected String hostname;
	protected int port;
	protected String userName;
	protected String privateKey;

	public BaseApplicationInstance(ApplicationDefinition appDef, String hostname, int sshPort, String userName,
			String privateKey) {
		applicationDefinition = appDef;
		this.hostname = hostname;
		port = sshPort;
		this.userName = userName;
		this.privateKey = privateKey;
	}

	protected BaseApplicationInstance() {
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

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

}
