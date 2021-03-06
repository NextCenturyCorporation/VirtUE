/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
/**
 * 
 */
package com.nextcentury.savior.cifsproxy.services;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

import com.ncc.savior.virtueadmin.template.FreeMarkerTemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;
import com.nextcentury.savior.cifsproxy.ActiveDirectorySecurityConfig;
import com.nextcentury.savior.cifsproxy.BaseSecurityConfig;
import com.nextcentury.savior.cifsproxy.model.Printer;

/**
 * @author clong
 *
 */
@Service
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class PrinterService {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(PrinterService.class);

	/**
	 * Required: Active Directory security domain.
	 */
	@Value("${savior.security.ad.domain}")
	private String adDomain;

	@Autowired
	private VirtueService virtueService;

	@Autowired
	private SambaConfigManager sambaConfigManager;

	@Autowired
	private FreeMarkerTemplateService templateService;

	private Map<String, Printer> printers = new HashMap<>();

	@Value("${savior.cifsproxy.printerconfig:printer-config.tpl}")
	private String printerConfigTemplate;

	public Collection<Printer> getPrinters() {
		LOGGER.entry();
		LOGGER.exit(printers.values());
		return printers.values();
	}

	public Printer getPrinter(String name) {
		LOGGER.entry(name);
		Printer printer = printers.get(name);
		LOGGER.exit(printer);
		return printer;
	}

	public Printer newPrinter(HttpSession session, Printer printer)
			throws IllegalArgumentException, TemplateException, IOException {
		LOGGER.entry(session, printer);
		if (printer.getName() == null || printer.getName().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("name cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (printer.getVirtueId() == null || printer.getVirtueId().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("virtueId cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (virtueService.getVirtue(printer.getVirtueId()) == null) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Virtue not found for id: " + printer.getVirtueId());
			LOGGER.throwing(e);
			throw e;
		}
		if (printer.getServer() == null || printer.getServer().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("server cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (printers.containsKey(printer.getName())) {
			IllegalArgumentException e = new IllegalArgumentException(
					"printer '" + printer.getName() + "' already exists");
			LOGGER.throwing(e);
			throw e;
		}
		cacheCredentials(session, printer);
		validatePrinter(printer);
		boolean unregisterName = true;
		try {
			sambaConfigManager.initExportedName(printer);

			String domainUser = (String) session.getAttribute(ActiveDirectorySecurityConfig.USERNAME_ATTRIBUTE);
			addPrinterConfiguration(printer, domainUser);
			printers.put(printer.getName(), printer);
			unregisterName = false;
		} finally {
			if (unregisterName) {
				sambaConfigManager.unregisterExportedName(printer.getExportedName());
			}
		}

		LOGGER.exit(printer);
		return printer;
	}

	/**
	 * Get a Kerberos ticket for the printer and cache it for later use.
	 * 
	 * @param printer
	 * @param session
	 */
	private void cacheCredentials(HttpSession session, Printer printer) {
		LOGGER.entry(session, printer);
		String username = (String) session.getAttribute(ActiveDirectorySecurityConfig.USERNAME_ATTRIBUTE);
		Path ccachePath = (Path) session.getAttribute(ActiveDirectorySecurityConfig.CCACHE_PATH_ATTRIBUTE);
		String ccacheFilename = ccachePath.toAbsolutePath().toString();
		Path keytabPath = (Path) session.getAttribute(ActiveDirectorySecurityConfig.KEYTAB_PATH_ATTRIBUTE);
		String keytabFilename = keytabPath.toAbsolutePath().toString();

		Path intermediateCCache = null;
		try {
			intermediateCCache = Files.createTempFile("cifsproxy-intermediate-ccache", "");
			String intermediateCCacheFilename = intermediateCCache.toAbsolutePath().toString();
			KerberosUtils.initCCache(intermediateCCacheFilename, keytabFilename, adDomain);
			KerberosUtils.importCredentials(intermediateCCacheFilename, ccacheFilename);
			KerberosUtils.getServiceTicket(intermediateCCacheFilename, username, printer.getServer(), keytabFilename);
			intermediateCCache.toFile().setReadable(true, false);

			KerberosUtils.switchPrincipal(intermediateCCacheFilename,
					virtueService.getVirtue(printer.getVirtueId()).getUsername(), username);
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException("could not create temporary file", e);
			LOGGER.throwing(wse);
			throw wse;
		} finally {
			if (intermediateCCache != null && !LOGGER.isDebugEnabled()) {
				LOGGER.trace("deleting temporary mount file: " + intermediateCCache);
				intermediateCCache.toFile().delete();
			}
		}
		LOGGER.exit(intermediateCCache);
	}

	/**
	 * Confirm that we can connect to a {@link Printer}
	 * 
	 * @param printer
	 */
	private void validatePrinter(Printer printer) {
		LOGGER.entry(printer);
		ProcessBuilder processBuilder = KerberosUtils.createProcessBuilder(null);
		processBuilder.command("sudo", "-u", virtueService.getVirtue(printer.getVirtueId()).getUsername(), "smbclient",
				"-k", printer.getServiceName(), "-c", "queue");
		KerberosUtils.runProcess(processBuilder, "print queue");
		LOGGER.exit(processBuilder);
	}

	private void addPrinterConfiguration(Printer printer, String domainUser) throws TemplateException, IOException {
		LOGGER.entry(printer, domainUser);
		StringWriter stringWriter = new StringWriter();
		Map<String, Object> printerConfigParams = new HashMap<>();
		printerConfigParams.put("domainUser", domainUser);
		printerConfigParams.put("exportedName", printer.getExportedName());
		printerConfigParams.put("localUser", virtueService.getVirtue(printer.getVirtueId()).getUsername());
		printerConfigParams.put("name", printer.getName());
		printerConfigParams.put("serviceName", printer.getServiceName());
		templateService.processTemplate(printerConfigTemplate, stringWriter, printerConfigParams);
		String configContents = stringWriter.toString();
		sambaConfigManager.writeShareConfig(printer.getName(), printer.getVirtueId(), configContents);
		LOGGER.exit();
	}

	public void removePrinter(String name) throws IllegalArgumentException, IOException {
		LOGGER.entry(name);
		Printer printer = printers.get(name);
		if (printer == null) {
			IllegalArgumentException ise = new IllegalArgumentException("unknown printer '" + name + "'");
			LOGGER.throwing(ise);
			throw ise;
		}
		sambaConfigManager.removeConfigFile(name, printer.getVirtueId());
		printers.remove(name);
		LOGGER.exit();
	}

}
