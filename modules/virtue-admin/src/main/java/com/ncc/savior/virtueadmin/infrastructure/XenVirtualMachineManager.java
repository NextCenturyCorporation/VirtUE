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
				int i = 0; 
		String myKey = keyManager.getKeyByName("virginiatech_ec2"); 
		File privateKeyFile = keyManager.getKeyFileByName("virginiatech_ec2");

		//cert =/Users/womitowoju/workspace/VirtUE/modules/virtue-admin/certs
		
		
		JSch ssh = new JSch();
		ChannelExec channel = null;
		Session session = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		
		try {
			//ssh.addIdentity(myKey);
			ssh.addIdentity(privateKeyFile.getAbsolutePath());

			session = ssh.getSession("ec2-user","ec2-35-172-226-43.compute-1.amazonaws.com", 22);
			session.setConfig("PreferredAuthentications", "publickey");
			session.setConfig("StrictHostKeyChecking", "no");
			session.setTimeout(500);
			session.connect();
			
			if(false)
			{
				channel = (ChannelExec) session.openChannel("exec");
				//channel.setCommand("echo 'Testing reachability of VM'");
				
				channel.setCommand("cd ./app-domains; ./create.sh dom2;  sudo xl console dom2"); 
				//channel.setCommand("sudo xl console dom2"); 
	
				channel.connect(10000);
				
	  		    //channel.setOutputStream(baos);//This prints on console. Need 2 capture in String somehow?
	
				channel.setOutputStream(System.out); 
				// InputStreamReader stream = new InputStreamReader(channel.getInputStream());
				// reader = new BufferedReader(stream);
				// InputStreamReader estream = new InputStreamReader(channel.getErrStream());
				// ereader = new BufferedReader(estream);
				// String line;
				// logger.debug("should read line soon");
				// while ((line = reader.readLine()) != null || (line = ereader.readLine()) !=
				// null) {
				// logger.trace(line);
				// }
				//return true;
	  		   String myOutput = new String(baos.toByteArray());
	  		   channel.sendSignal("2"); // CTRL + C - interrupt
			}
			else
			{
			      Channel myChannel = session.openChannel("shell");
			      OutputStream ops = myChannel.getOutputStream();
			      PrintStream ps = new PrintStream(ops, true);

			      myChannel.connect();
			      InputStream input = myChannel.getInputStream();

			      //commands
			      ps.println("cd ./app-domains");
			      ps.println("./create.sh dom2");
			      ps.println("sudo xl console dom2");


			      //ps.println("exit");
			      ps.close();
			      printResult(input, myChannel);

			      myChannel.disconnect();
			      session.disconnect();				
			}
				

			
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
	   private static String printResult(InputStream input,
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
