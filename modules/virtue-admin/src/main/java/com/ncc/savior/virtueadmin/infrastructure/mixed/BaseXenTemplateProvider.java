package com.ncc.savior.virtueadmin.infrastructure.mixed;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ncc.savior.virtueadmin.data.jpa.VirtualMachineTemplateRepository;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtualMachineTemplate;

public class BaseXenTemplateProvider {
	private static final Logger logger = LoggerFactory.getLogger(BaseXenTemplateProvider.class);
	private static final String XEN_ID = "XEN_CURRENT";
	@Value("${virtue.aws.xen.ami}")
	private String amiFromProperties;
	@Value("${virtue.aws.xen.loginUser}")
	private String userFromProperties;
	@Value("${virtue.aws.xen.vmKey}")
	private String keyNameFromProperties;

	@Autowired
	private VirtualMachineTemplateRepository vmTemplateRepo;

	private VirtualMachineTemplate cachedTemplate;

	protected void init() {
		VirtualMachineTemplate persistedTemplate = getPersistedTemplate();
		if (persistedTemplate == null) {
			VirtualMachineTemplate vmtFromProperties = new VirtualMachineTemplate(
					"Xen-" + amiFromProperties + "-" + UUID.randomUUID().toString(), "XenTemplate", OS.LINUX,
					amiFromProperties, new ArrayList<ApplicationDefinition>(), userFromProperties, false, new Date(0),
					"system");
			setPersistedTemplate(vmtFromProperties);
			persistedTemplate = vmtFromProperties;
		}
		this.cachedTemplate = persistedTemplate;
	}

	public VirtualMachineTemplate getXenVmTemplate() {
		return cachedTemplate;
	}

	public void setXenVmTemplate(VirtualMachineTemplate xenVmTemplate) {
		VirtualMachineTemplate old = this.cachedTemplate;
		this.cachedTemplate = xenVmTemplate;
		try {
			setPersistedTemplate(xenVmTemplate);
		} catch (Exception e) {
			logger.error("Unable to save new xen template=" + xenVmTemplate + "!  Reverting to " + old);
			this.cachedTemplate = old;
			throw e;
		}
	}

	protected VirtualMachineTemplate getPersistedTemplate() {
		Optional<VirtualMachineTemplate> ret = vmTemplateRepo.findById(XEN_ID);
		return ret.orElse(null);
	}

	protected void setPersistedTemplate(VirtualMachineTemplate template) {
		template.setId(XEN_ID);
		vmTemplateRepo.save(template);
	}

	public void setXenVmTemplateFromAmi(String ami) {
		VirtualMachineTemplate vmt = new VirtualMachineTemplate(
				"Xen-" + amiFromProperties + "-" + UUID.randomUUID().toString(), "XenTemplate", OS.LINUX, ami,
				new ArrayList<ApplicationDefinition>(), userFromProperties, false, new Date(0), "system");
		setXenVmTemplate(vmt);
	}

	public void deleteXenVmTemplate() {
		cachedTemplate = null;
		vmTemplateRepo.deleteById(XEN_ID);

	}
}
