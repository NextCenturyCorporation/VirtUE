package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object (DTO) for templates.
 *
 * Note that while the workbench keeps track of ids, and generates the related
 * object lists as needed (e.g. vmTemplateIds vs vmTemplates), this keeps track
 * of vmTemplates, and generates their ids when requested. The ids are taken
 * from the front end, used to build the objects, and thereafter the objects are
 * the final authority. When passed to the frontend again, its the id list that
 * matters - it doesn't get rebuilt.
 */
@Entity
@Schema(description = "Object to describe a virtue template.")
public class VirtueTemplate {
	@Id
	@Schema(description = "ID of the virtue template.")
	private String id;
	@Schema(description = "Name of the virtue template.")
	private String name;
	@Schema(description = "Version of the virtue template.")
	private String version;
	@ColumnDefault("true")
	@Schema(description = "Boolean as to whether the virtue template is enabled and allowed to be provisioned.  May not be implemented.")
	private boolean enabled;

	@Schema(description = "Date of last modification for virtual machine template metadata.")
	private Date lastModification;
	@Schema(description = "Username of last editor of this virtual machine template metadata.")
	private String lastEditor;
	@Schema(description = "User who originally created this virtual machine template.")
	private String userCreatedBy;
	@Schema(description = "Time at which this virtual machine was originally created.")
	private Date timeCreatedAt;

	@Schema(description = "Unused")
	private String awsTemplateName;
	@Schema(description = "Color for which the virtue instance will be shown through the desktop application and workbench.")
	private String color;

	@ManyToMany()
	private Collection<VirtualMachineTemplate> vmTemplates;
	@ManyToMany()
	@Schema(description = "Unused")
	private Collection<Printer> printers;

	@ManyToMany()
	// @ElementCollection()
	private Collection<FileSystem> fileSystems;

	@ElementCollection()
	@Schema(description = "Unused")
	private Collection<String> allowedPasteTargetIds;

	@Transient
	@Schema(description = "Unused")
	private Collection<String> printerIds;
	@Transient
	@Schema(description = "List of IDs of the file systems that will be attached to the VMs when this virtue is provisioned.")
	private Collection<String> fileSystemIds;
	@Transient
	@Schema(description = "List of IDs of the virtual machine templates that will be provisioned when this virtue is provisioned.")
	private Collection<String> vmTemplateIds;

	// private Set<String> startingResourceIds;
	// private Set<String> startingTransducerIds;

	/**
	 *
	 * @param template
	 * @param templateId
	 */
	public VirtueTemplate(String templateId, VirtueTemplate template) {
		super();
		this.id = templateId;
		this.name = template.getName();
		this.version = template.getVersion();
		this.vmTemplates = template.getVmTemplates();
		this.vmTemplateIds = template.getVmTemplateIds();
		this.color = template.getColor();
		this.enabled = template.isEnabled();
		this.lastModification = template.getLastModification();
		this.lastEditor = template.getLastEditor();
		this.awsTemplateName = template.getAwsTemplateName();
		this.printers = template.getPrinters();
		this.printerIds = template.getPrinterIds();
		this.fileSystems = template.getFileSystems();
		this.fileSystemIds = template.getFileSystemIds();

		this.allowedPasteTargetIds = template.getAllowedPasteTargetIds();

		if (this.fileSystemIds == null) {
			this.fileSystemIds = new ArrayList<String>();
		}
		if (this.allowedPasteTargetIds == null) {
			this.allowedPasteTargetIds = new ArrayList<String>();
		}
	}

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName, String color, boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
		this.allowedPasteTargetIds = new ArrayList<String>();
	}

	public VirtueTemplate(String id, String name, String version, VirtualMachineTemplate vmTemplate,
			String awsTemplateName, String color, boolean enabled, Date lastModification, String lastEditor) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = new ArrayList<VirtualMachineTemplate>();
		vmTemplates.add(vmTemplate);
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
		this.allowedPasteTargetIds = new ArrayList<String>();
	}

	public VirtueTemplate(String id, String name, String version, String awsTemplateName, String color, boolean enabled,
			Date lastModification, String lastEditor, VirtualMachineTemplate... vmTemplates) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = new ArrayList<VirtualMachineTemplate>();
		for (VirtualMachineTemplate vmTemplate : vmTemplates) {
			this.vmTemplates.add(vmTemplate);
		}
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
		this.allowedPasteTargetIds = new ArrayList<String>();
	}

	public VirtueTemplate(String id, String name, String version, Collection<VirtualMachineTemplate> vmTemplates,
			String awsTemplateName, String color, boolean enabled, Date lastModification, String lastEditor,
			String userCreatedBy, Date timeCreatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.color = color;
		this.enabled = enabled;
		this.lastModification = lastModification;
		this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
		this.userCreatedBy = userCreatedBy;
		this.timeCreatedAt = timeCreatedAt;
		this.allowedPasteTargetIds = new ArrayList<String>();
	}

	/**
	 * Used for jackson deserialization
	 */
	protected VirtueTemplate() {
		super();
		this.allowedPasteTargetIds = new ArrayList<String>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@JsonIgnore
	public Set<ApplicationDefinition> getApplications() {
		return getVmTemplates().stream().flatMap(vmTemplate -> vmTemplate.getApplications().parallelStream())
				.collect(Collectors.toSet());
	}

	@JsonIgnore
	public Collection<VirtualMachineTemplate> getVmTemplates() {
		return vmTemplates;
	}

	@JsonIgnore
	public Collection<Printer> getPrinters() {
		return printers;
	}

	// below setters are used for jackson deserialization.
	public void setId(String id) {
		this.id = id;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setVersion(String version) {
		this.version = version;
	}

	public void setVmTemplates(Collection<VirtualMachineTemplate> vmTemplates) {
		this.vmTemplates = vmTemplates;
	}

	@Override
	public String toString() {
		return "VirtueTemplate [id=" + id + ", name=" + name + ", version=" + version + ", vmTemplates=" + vmTemplates
				+ ", color=" + color + ", enabled=" + enabled + ", lastModification=" + lastModification
				+ ", lastEditor=" + lastEditor + ", awsTemplateName=" + awsTemplateName + "]";
	}

	public String getAwsTemplateName() {
		return awsTemplateName;
	}

	public void setAwsTemplateName(String awsTemplateName) {
		this.awsTemplateName = awsTemplateName;
	}

	public String getColor() {
		return color;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Date getLastModification() {
		return lastModification;
	}

	public void setLastModification(Date lastModification) {
		this.lastModification = lastModification;
	}

	public String getLastEditor() {
		return lastEditor;
	}

	public void setLastEditor(String lastEditor) {
		this.lastEditor = lastEditor;
	}

	@JsonGetter
	public Collection<String> getVmTemplateIds() {
		if (vmTemplates != null) {
			vmTemplateIds = new ArrayList<String>();
			for (VirtualMachineTemplate vmt : vmTemplates) {
				vmTemplateIds.add(vmt.getId());
			}
		}
		return vmTemplateIds;
	}

	@JsonGetter
	public Collection<String> getApplicationIds() {
		Collection<String> applicationIds = new ArrayList<String>();
		if (vmTemplates != null) {
			for (VirtualMachineTemplate vmt : vmTemplates) {
				applicationIds.addAll(vmt.getApplicationIds());
			}
		}
		return applicationIds;
	}

	@JsonGetter
	public Collection<String> getPrinterIds() {
		if (printers != null) {
			printerIds = new ArrayList<String>();
			for (Printer p : printers) {
				printerIds.add(p.getId());
			}
		}
		return printerIds;
	}

	@JsonGetter
	public Collection<String> getFileSystemIds() {
		if (fileSystems != null) {
			fileSystemIds = new ArrayList<String>();
			for (FileSystem fs : fileSystems) {
				fileSystemIds.add(fs.getId());
			}
		}
		// return new ArrayList<String>();
		return fileSystemIds;
	}

	@JsonIgnore
	public Collection<FileSystem> getFileSystems() {
		return fileSystems;
	}

	@JsonGetter
	public Collection<String> getAllowedPasteTargetIds() {
		return allowedPasteTargetIds;
	}

	@JsonSetter
	public void setVmTemplateIds(Collection<String> vmTemplateIds) {
		this.vmTemplates = null;
		this.vmTemplateIds = vmTemplateIds;
	}

	@JsonSetter
	public void setFileSystemIds(Collection<String> fileSystemIds) {
		this.fileSystemIds = fileSystemIds;
	}

	public void setFileSystems(Collection<FileSystem> fileSystems) {
		this.fileSystems = fileSystems;
	}

	@JsonSetter
	public void setAllowedPasteTargetIds(Collection<String> allowedPasteTargetIds) {
		this.allowedPasteTargetIds = allowedPasteTargetIds;
	}

	public Date getTimeCreatedAt() {
		return timeCreatedAt;
	}

	public void setTimeCreatedAt(Date timeCreatedAt) {
		this.timeCreatedAt = timeCreatedAt;
	}

	public String getUserCreatedBy() {
		return userCreatedBy;
	}

	public void setUserCreatedBy(String userCreatedBy) {
		this.userCreatedBy = userCreatedBy;
	}

	public void setPrinters(Collection<Printer> printers) {
		this.printers = printers;
	}

	public void addPrinter(Printer newPrinter) {
		if (printers == null) {
			printers = new ArrayList<Printer>();
		}
		if (printerIds == null) {
			printerIds = new ArrayList<String>();
		}
		printers.add(newPrinter);
		printerIds.add(newPrinter.getId());
	}

	public void addFileSystem(FileSystem newFileSystem) {
		if (fileSystems == null) {
			fileSystems = new ArrayList<FileSystem>();
		}
		if (fileSystemIds == null) {
			fileSystemIds = new ArrayList<String>();
		}
		fileSystems.add(newFileSystem);
		fileSystemIds.add(newFileSystem.getId());
	}

	public void removePrinter(Printer printer) {
		Iterator<Printer> itr = getPrinters().iterator();
		while (itr.hasNext()) {
			Printer p = itr.next();
			if (p.getId().equals(printer.getId())) {
				itr.remove();
				break;
			}
		}
	}

	public void removeFileSystem(FileSystem fileSystem) {
		Iterator<FileSystem> itr = getFileSystems().iterator();
		while (itr.hasNext()) {
			FileSystem fs = itr.next();
			if (fs.getId().equals(fileSystem.getId())) {
				itr.remove();
				break;
			}
		}
	}

	public static final Comparator<? super VirtueTemplate> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();

	private static class CaseInsensitiveNameComparator implements Comparator<VirtueTemplate> {
		@Override
		public int compare(VirtueTemplate o1, VirtueTemplate o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
		}
	}
}
