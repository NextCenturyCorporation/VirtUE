package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.virtueadmin.infrastructure.IKeyManager;
import com.ncc.savior.virtueadmin.infrastructure.images.XenGuestImageGenerator.ImageBuildStage;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;

public class AnsiblePackageInstaller implements IPackageInstaller {
	private static final Logger logger = LoggerFactory.getLogger(AnsiblePackageInstaller.class);

	@Autowired
	ITemplateService templateService;

	@Autowired
	IKeyManager keyManager;

	@Value("${virtue.ansible.hostname}")
	private String ansibleHostname;
	@Value("${virtue.ansible.port}")
	private int ansiblePort;
	@Value("${virtue.ansible.keyName}")
	private String ansibleKeyName;
	@Value("${virtue.ansible.username}")
	private String ansibleUsername;

	public void init() {

	}

	@Override
	public void installPackages(ImageBuildStage stage, ImageDescriptor imageDescriptor, Map<String, Object> dataModel)
			throws JSchException, IOException, TemplateException {
		File key = keyManager.getKeyFileByName(ansibleKeyName);
		VirtualMachine vm = new VirtualMachine("id", "ansible", null, null, null, null, ansibleHostname, ansiblePort,
				ansibleUsername, null, ansibleKeyName, null);
		// try {
		dataModel.put("initFile", UUID.randomUUID().toString());
		Session session = SshUtil.getConnectedSessionWithRetries(vm, key, 3, 1000);
		List<String> lines = SshUtil.runScriptFromFile(templateService, session,
				"image/ansible/ansible-" + stage.toString() + ".tpl", dataModel);
		logger.debug("output: ");
		for (String line:lines) {
			logger.debug("  "+line);
		}
		dataModel.remove("initFile");
		// } catch (JSchException | IOException | TemplateException e) {
		// throw
		// }
	}

}
