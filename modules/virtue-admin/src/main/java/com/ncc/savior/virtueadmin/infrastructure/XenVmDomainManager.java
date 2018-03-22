/* 
*  XenLibvirtManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Mar 8, 2018
*  
*  Copyright (c) 2018 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;


import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.Library;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;


public class XenVmDomainManager implements IVmManager {

	
	private static final Logger logger = LoggerFactory.getLogger(AwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = "VRTU-";
	private String privateKey;
	private SshKeyInjector sshKeyInjector;
	private String serverUser;
	private String serverKeyName;
	private String defaultLoginUsername;

	private String defaultTemplate; 
	
	private Connect xenConnection; 
	
	
	public XenVmDomainManager() {
		// TODO Auto-generated constructor stub
	}

	public XenVmDomainManager(File privatekeyfile) throws LibvirtException {

		logger.info("Enter XenVmDomainManager ctor");
		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);
		this.defaultLoginUsername = "admin";
		this.sshKeyInjector = new SshKeyInjector();
		this.privateKey = StaticMachineVmManager.getKeyFromFile(privatekeyfile);
		this.serverUser = System.getProperty("user.name");

		//Let's initialize libvirt
		init(); 
	}
	
	private void init() throws LibvirtException {

		logger.info("Enter XenVmDomainManager->init()");

        try {
        	
            Library.initEventLoop();
        } catch (LibvirtException e) {
            logger.debug("Libvirt initEventLoop failed", e);
        }	
        	
        xenConnection = new Connect("xen:///", false);
        XenErrorCallBack xenErrorCallBack = new XenErrorCallBack(); 
        xenConnection.setConnectionErrorCallback(xenErrorCallBack);
        
        logger.info("Connection type = " +  xenConnection.getType()); 
        logger.info("Connection getURI = " +  xenConnection.getURI()); 
        logger.info("Connection getMaxVcpus = " +  xenConnection.getMaxVcpus("xen")); 
        logger.info("Connection getHostName = " +  xenConnection.getHostName()); 
        logger.info("Connection getLibVersion = " +  xenConnection.getLibVersion()); 
        logger.info("Connection getVersion = " +  xenConnection.getVersion()); 
        logger.info("Connection isAlive = " +  xenConnection.isAlive());        
	}


	@Override
	public void addStateUpdateListener(IStateUpdateListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeStateUpdateListener(IStateUpdateListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user, VirtualMachineTemplate vmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine startVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VirtualMachine stopVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteVirtualMachine(VirtualMachine vm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VmState getVirtualMachineState(VirtualMachine vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<VirtualMachine> provisionVirtualMachineTemplates(VirtueUser user,
			Collection<VirtualMachineTemplate> vmTemplates) {

		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmTemplates.size());
		int domainID = 0; 
		String domainName = ""; 
		Random rand = new Random(); 
		
	    for (VirtualMachineTemplate vmt : vmTemplates) {

			UUID uuID = UUID.randomUUID(); 
			domainName = "savoir-" + rand.nextInt(500); 
	        String domainXml = "<domain type='xen' >\n" + 
					"  <name>" + domainName + "</name>\n" + 
					"<uuid>"  + uuID.toString() + "</uuid>\n" + 
	        		"  <memory unit='KiB'>1048576</memory>\n" + 
	        		"  <currentMemory unit='KiB'>1048576</currentMemory>\n" + 
	        		"  <vcpu placement='static'>1</vcpu>\n" + 
	        		"  <os>\n" + 
	        		"    <type arch='x86_64' machine='xenpv'>linux</type>\n" + 
	        		"    <kernel>/home/admin/app-domains/master/vmlinuz-4.2.0-42-generic</kernel>\n" + 
	        		"    <initrd>/home/admin/app-domains/master/initrd.img-4.2.0-42-generic</initrd>\n" + 
	        		"    <cmdline>root=/dev/xvda2 ro elevator=noop</cmdline>\n" + 
	        		"  </os>\n" + 
	        		"  <clock offset='utc' adjustment='reset'/>\n" + 
	        		"  <on_poweroff>destroy</on_poweroff>\n" + 
	        		"  <on_reboot>restart</on_reboot>\n" + 
	        		"  <on_crash>restart</on_crash>\n" + 
	        		"  <devices>\n" + 
	        		"    <disk type='file' device='disk'>\n" + 
	        		"      <driver name='tap' type='qcow2'/>\n" + 
	        		"      <source file='/home/admin/app-domains/master/disk.qcow2'/>\n" + 
	        		"      <target dev='xvda2' bus='xen'/>\n" + 
	        		"    </disk>\n" + 
	        		"    <disk type='file' device='disk'>\n" + 
	        		"      <driver name='tap' type='qcow2'/>\n" + 
	        		"      <source file='/home/admin/app-domains/master/swap.qcow2'/>\n" + 
	        		"      <target dev='xvda1' bus='xen'/>\n" + 
	        		"    </disk>\n" + 
	        		
	        		" <network> " + 
	        		
	        		"  <name>host-bridge</name> " +
	        		"<ip address='192.168.0.10' netmask='255.255.255.0'>" +
	        	    //"<dhcp>" + 
	        	    //  "<range start='192.168.0.2' end='192.168.0.254'/>" +
	        	    //"</dhcp>" + 
	        	    "</ip>" +
	        	    
	        		"  <forward mode='nat'/> " +
	        		"  <bridge name='virbr0'/>" +
	        		" </network>" +
	        		
	        		
	        		" <interface type='bridge'> " +
	        		"     <source bridge='xenbr0'/> " + 
	        		"     <mac address=" + randomMACAddress() + "/>" +
	        		"     <script path='vif2'/>" + 
	        		"  </interface> " +    
	        		
	        		
	        		"    <console type='pty' tty='/dev/pts/7'>\n" + 
	        		"      <source path='/dev/pts/7'/>\n" + 
	        		"      <target type='xen' port='0'/>\n" + 
	        		"    </console>\n" + 
	        		"    <input type='mouse' bus='xen'/>\n" + 
	        		"    <input type='keyboard' bus='xen'/>\n" + 
	        		"  </devices>\n" + 
	        		"</domain>\n"; 	

       	   	Domain dom1;
			try {
				dom1 = xenConnection.domainCreateLinux(domainXml, 0);
				domainID = dom1.getID(); 
				domainName = dom1.getName(); 
				
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
					   
 		   String loginUsername = defaultLoginUsername;
	  
		   VirtualMachine vm = new VirtualMachine(uuID.toString(), domainName, vmt.getApplications(), VmState.CREATING, vmt.getOs(), Integer.toString(domainID) , "", SSH_PORT, loginUsername, privateKey, "");	
		   vms.add(vm);

		}
		
		//modifyVms(vms);
		return vms;
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub
		
	}
	
	public String getServerKeyName() {
		return serverKeyName;
	}

	public void setServerKeyName(String serverKeyName) {
		this.serverKeyName = serverKeyName;
	}

	/**
	 * @return the defaultTemplate
	 */
	public String getDefaultTemplate() {


				
		return null;
	}

	/**
	 * @param defaultTemplate the defaultTemplate to set
	 */
	public void setDefaultTemplate(String defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}
	
	
	private String randomMACAddress(){
	    Random rand = new Random();
	    byte[] macAddr = new byte[6];
	    rand.nextBytes(macAddr);

	    macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated

	    StringBuilder sb = new StringBuilder(18);
	    for(byte b : macAddr){

	        if(sb.length() > 0)
	            sb.append(":");

	        sb.append(String.format("%02x", b));
	    }


	    return sb.toString();
	}
	
	
	

}







