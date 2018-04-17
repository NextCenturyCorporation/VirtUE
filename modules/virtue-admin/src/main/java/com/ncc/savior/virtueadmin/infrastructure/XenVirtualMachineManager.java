/* 
*  XenLibvirtManager.java
*  
*  VirtUE - Savior Project
*  Created by womitowoju  Mar 8, 2018
*  
*  Copyright (c) 2018 Next Century Corporation. All rights reserved.
*/

package com.ncc.savior.virtueadmin.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.InstanceType;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Channel; 
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2VmManager;
import com.ncc.savior.virtueadmin.infrastructure.aws.VirtueAwsEc2Provider;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueUser;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.SshKeyInjector;
import com.ncc.savior.virtueadmin.util.SshUtil;

public class XenVirtualMachineManager extends BaseVmManager {

	private static final Logger logger = LoggerFactory.getLogger(AwsEc2VmManager.class);
	private static final int SSH_PORT = 22;
	private static final String VM_PREFIX = "VRTU-";
	private String privateKey;
	private SshKeyInjector sshKeyInjector;
	private String serverUser;
	private String defaultLoginUsername;
	
	private IKeyManager keyManager;
	private String region;
	private String awsProfile;

	private InstanceType instanceType;

	private String defaultTemplate;
	
	VirtueAwsEc2Provider ec2Provider; 

	public XenVirtualMachineManager() {
		// TODO Auto-generated constructor stub
	}

	public XenVirtualMachineManager(IKeyManager keyManager,  VirtueAwsEc2Provider ec2Provider) {

		//this.privateKey = SshUtil.getKeyFromFile(privatekeyfile);
		this.defaultLoginUsername = "admin";
		this.sshKeyInjector = new SshKeyInjector();
		//this.privateKey = SshUtil.getKeyFromFile(privatekeyfile);
		this.serverUser = System.getProperty("user.name");
		
		this.keyManager = keyManager;
		//this.region = region;
		//this.awsProfile = awsProfile; 

		this.ec2Provider = ec2Provider; 
		
	}

	@Override
	public VirtualMachine provisionVirtualMachineTemplate(VirtueUser user, VirtualMachineTemplate vmt) {
		
		int i = 0; 
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
	/*	
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmTemplates.size());
		for (VirtualMachineTemplate vmt : vmTemplates) {

			String clientUser = user.getUsername();
			String name = VM_PREFIX + clientUser + "-" + serverUser + "-" + instance.getInstanceId();
			String loginUsername = vmt.getLoginUser();
			//String keyName = instance.getKeyName();

			String uuidVirtue = UUID.randomUUID().toString(); 
			String domainName = "Virtue-" + unique-name; 


			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
					VmState.CREATING, vmt.getOs(), instance.getInstanceId(), instance.getPublicDnsName(), SSH_PORT,
					loginUsername, privateKey, keyName, instance.getPublicIpAddress());
			vms.add(vm);

		}
	*/
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>(vmTemplates.size());
		for (VirtualMachineTemplate vmt : vmTemplates) {
			
			String keyName = "virginiatech_ec2"; 
			String myKey = keyManager.getKeyByName(keyName); 
			File privateKeyFile = keyManager.getKeyFileByName("virginiatech_ec2");
			String ipAddress = "0.0.0.0"; 

			JSch ssh = new JSch();
			ChannelExec channel = null;
			Session session = null;
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			String domainUUID = UUID.randomUUID().toString(); 
			String clientUser = user.getUsername();
			String name = VM_PREFIX + clientUser + "-" + serverUser + "-" + domainUUID;
			String loginUsername = vmt.getLoginUser();
			
			try {
				ssh.addIdentity(privateKeyFile.getAbsolutePath());

				session = ssh.getSession("ec2-user","ec2-35-172-226-43.compute-1.amazonaws.com", 22);
				session.setConfig("PreferredAuthentications", "publickey");
				session.setConfig("StrictHostKeyChecking", "no");
				session.setTimeout(500);
				session.connect();


				Channel myChannel = session.openChannel("shell");
				OutputStream ops = myChannel.getOutputStream();
				PrintStream ps = new PrintStream(ops, true);

				myChannel.connect();
				InputStream input = myChannel.getInputStream();

				//commands
				ps.println("cd ./app-domains");
				ps.println("./create.sh dom2");
				ps.println("sudo xl console " + name);

				//ps.println("exit");
				ps.close();
				ipAddress = getIpAddress(input, myChannel);

				myChannel.disconnect();
				session.disconnect();				

			} catch (JSchException e) {
				logger.trace("Vm is not reachable yet: " + e.getMessage());
				//return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (channel != null) {
					channel.disconnect();
				}
				if (session != null) {
					session.disconnect();
				}
				//			JavaUtil.closeIgnoreErrors(reader, ereader);
			}		

			String dnsAddress = ""; //we don't have dns name yet. 
 
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
					VmState.CREATING, vmt.getOs(), domainUUID, dnsAddress, SSH_PORT,
					loginUsername, privateKey, keyName, ipAddress );
			
			vms.add(vm);

		}
		return null;
	}

	@Override
	public void deleteVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<VirtualMachine> startVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<VirtualMachine> stopVirtualMachines(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	   /**
	    * @param input
	    * @param channel
	    */
	   private static String getIpAddress(InputStream input,
	                                   Channel channel) throws Exception
	   {
		  String virtue_ip = "";
	      int SIZE = 1024;
	      byte[] tmp = new byte[SIZE];
	      while (true)
	      {
	         while (input.available() > 0)
	         {
	            int i = input.read(tmp, 0, SIZE);
	            if(i < 0)
	               break;
	            
	             String myIP = new String(tmp, 0, i); 
	             //System.out.print(myIP);

	             if(myIP.contains("virtue-ip"))
	             {
		             System.out.print(findIP(myIP));
		             virtue_ip = findIP(myIP); 
	             }
	         }
	         if(channel.isClosed())
	         {
	            System.out.println("exit-status: " + channel.getExitStatus());
	            break;
	         }
	         try
	         {
	            Thread.sleep(300);
	         }
	         catch (Exception ee)
	         {
	         }
	      }
	      
	      return virtue_ip;
	   }
	   
	   
	   private static String findIP(String substring)
	   {
		   String IPADDRESS_PATTERN = 
			        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

			Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
			Matcher matcher = pattern.matcher(substring);
			if (matcher.find()) {
			    return matcher.group();
			} else{
			    return "0.0.0.0";
			}

        }

}
