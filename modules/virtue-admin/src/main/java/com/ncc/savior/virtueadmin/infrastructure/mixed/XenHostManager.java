package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.model.InstanceType;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.virtueadmin.data.IActiveVirtueDao;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.IUpdateListener;
import com.ncc.savior.virtueadmin.infrastructure.aws.AwsEc2Wrapper;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;
import com.ncc.savior.virtueadmin.model.VirtueInstance;
import com.ncc.savior.virtueadmin.model.VmState;
import com.ncc.savior.virtueadmin.util.JavaUtil;

public class XenHostManager {
	private static final Logger logger = LoggerFactory.getLogger(XenAwsMixCloudManager.class);
	private VirtualMachineTemplate xenVmTemplate;
	private IUpdateListener<VirtualMachine> notifier;
	private AwsEc2Wrapper ec2Wrapper;
	private Collection<String> securityGroups;
	private String xenKeyName;
	private InstanceType xenInstanceType;
	private XenHostVmUpdater updater;
	protected IActiveVirtueDao vmDao;
	private IKeyManager keyManager;
	private static final String VM_PREFIX = "VRTU-";


	
	protected Map<String, XenVirtueCreationPackage> packs = new HashMap<String, XenVirtueCreationPackage>();

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> notifier, Collection<String> securityGroups, String xenKeyName,
			InstanceType xenInstanceType) {
		this.notifier = notifier;
		this.ec2Wrapper = ec2Wrapper;
		this.securityGroups = securityGroups;
		this.xenKeyName = xenKeyName;
		this.xenInstanceType = xenInstanceType;
		this.vmDao = xenVmDao;
		this.keyManager = keyManager; 
		
		IUpdateListener<VirtualMachine> xenListener = new IUpdateListener<VirtualMachine>() {
			@Override
			public void updateElements(Collection<VirtualMachine> elements) {
				xenVmDao.updateVms(elements);
			}
		};
		this.updater = new XenHostVmUpdater(ec2Wrapper.getEc2(), xenListener, keyManager);
	}

	public XenHostManager(IKeyManager keyManager, AwsEc2Wrapper ec2Wrapper, IActiveVirtueDao xenVmDao,
			IUpdateListener<VirtualMachine> notifier, String securityGroupsCommaSeparated, String xenKeyName,
			String xenInstanceType) {
		this(keyManager, ec2Wrapper, xenVmDao, notifier, splitOnComma(securityGroupsCommaSeparated), xenKeyName,
				InstanceType.fromValue(xenInstanceType));
	}

	private static Collection<String> splitOnComma(String securityGroupsCommaSeparated) {
		Collection<String> groups = new ArrayList<String>();
		if (securityGroupsCommaSeparated != null) {
			for (String group : securityGroupsCommaSeparated.split(",")) {
				groups.add(group.trim());
			}
		}
		return groups;
	}

	public void provisionXenHost(VirtueInstance virtue, Collection<VirtualMachineTemplate> linuxVmts) {
		VirtualMachine xenVm = ec2Wrapper.provisionVm(xenVmTemplate, "Xen-" + virtue.getUsername(), securityGroups,
				xenKeyName, xenInstanceType);
		
		
		xenVm.setId(virtue.getId());
		ArrayList<VirtualMachine> xenVms = new ArrayList<VirtualMachine>();
		xenVms.add(xenVm);
		vmDao.updateVms(xenVms);
		updater.addVmToProvisionPipeline(xenVms);
		final String id = virtue.getId();
		for (VirtualMachineTemplate vmt : linuxVmts) {
			
			String keyName = xenVm.getPrivateKeyName(); 
			String myKey = keyManager.getKeyByName(keyName); 
			File privateKeyFile = keyManager.getKeyFileByName(keyName);
			String ipAddress = "0.0.0.0"; 
			
			JSch ssh = new JSch();
			ChannelExec channel = null;
			Session session = null;
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			String domainUUID = UUID.randomUUID().toString(); 
			String clientUser = virtue.getUsername();
			String name = VM_PREFIX + clientUser + "-" + virtue.getUsername() + "-" + domainUUID;
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
				ps.println("./create.sh " + name);
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

			/*
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), "",
					new ArrayList<ApplicationDefinition>(), VmState.CREATING, vmt.getOs(), "", "", 22, "", "", "", "");
		    */
			
			
			VirtualMachine vm = new VirtualMachine(UUID.randomUUID().toString(), name, vmt.getApplications(),
					VmState.CREATING, vmt.getOs(), domainUUID, dnsAddress, 22,
					loginUsername, "", keyName, ipAddress );
			
			virtue.getVms().add(vm);
			
		}
		
		Runnable r = new Runnable() {

			@Override
			public void run() {
				Optional<VirtualMachine> vm = vmDao.getXenVm(id);
				while (!vm.isPresent() || VmState.RUNNING.equals(vm.get().getState())) {
					JavaUtil.sleepAndLogInterruption(2000);
					vm = vmDao.getXenVm(id);
				}
				// TODO Create vms from templates but use the VM instances already stored in the
				// virtue.

				// TODO make sure VM status is updated via notifier

				logger.info("Create vms here " + linuxVmts);
			}
		};
		Thread t = new Thread(r, "XenProvisioner-" + id);
		t.start();
	}

	public void deleteVirtue(String id, Collection<VirtualMachine> linuxVms) {
		// TODO get XenVmManager for id
		// TODO tell XenManager to delete itself
		// TODO schedule once Vm's are deleted, XenManager will delete itself.

	}

	public void startVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}

	public void stopVirtue(VirtueInstance virtueInstance, Collection<VirtualMachine> linuxVms) {
		// TODO Auto-generated method stub

	}
	
	
	   /**
	    * @param input
	    * @param channel
	    */
	   private static String getIpAddress(InputStream input,
	                                   Channel channel) throws Exception
	   {
		  String virtue_ip = "0.0.0.0";
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
		             return virtue_ip; 
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
