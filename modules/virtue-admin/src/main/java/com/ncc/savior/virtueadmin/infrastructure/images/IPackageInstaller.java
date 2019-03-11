package com.ncc.savior.virtueadmin.infrastructure.images;

import java.io.IOException;
import java.util.Map;

import com.jcraft.jsch.JSchException;
import com.ncc.savior.virtueadmin.infrastructure.images.XenGuestImageGenerator.ImageBuildStage;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;

/**
 * Service that installs packages on a remote system.
 *
 */
public interface IPackageInstaller {
	void installPackages(ImageBuildStage stage, ImageDescriptor imageDescriptor, Map<String, Object> dataModel) throws JSchException, IOException, TemplateException;
}
