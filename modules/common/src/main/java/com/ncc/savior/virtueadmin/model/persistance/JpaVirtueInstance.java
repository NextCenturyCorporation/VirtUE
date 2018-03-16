package com.ncc.savior.virtueadmin.model.persistance;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.BaseVirtueInstance;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.VmState;

/**
 * Virtue class models a virtual unit with the user, applications etc.
 * 
 * 
 */
@Entity
public class JpaVirtueInstance extends BaseVirtueInstance {
	@OneToMany
	private Collection<JpaVirtualMachine> vms;
	// private Set<String> transducers;
	@ManyToMany
	private Collection<ApplicationDefinition> applications;

	public JpaVirtueInstance(String id, String name, String username, String templateId,
			Collection<ApplicationDefinition> apps, Collection<JpaVirtualMachine> vms) {
		super(id, name, username, templateId);
		this.applications = apps;
		this.vms = vms;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected JpaVirtueInstance() {

	}

	public JpaVirtueInstance(JpaVirtueTemplate template, String username) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				getApplicationsFromTemplate(template), new HashSet<JpaVirtualMachine>());

	}

	public JpaVirtueInstance(JpaVirtueTemplate template, String username, Collection<JpaVirtualMachine> vms) {
		this(UUID.randomUUID().toString(), template.getName(), username, template.getId(),
				getApplicationsFromTemplate(template), vms);

	}

	private static Collection<ApplicationDefinition> getApplicationsFromTemplate(JpaVirtueTemplate template) {
		Collection<ApplicationDefinition> list = new HashSet<ApplicationDefinition>();
		for (JpaVirtualMachineTemplate vmTemp : template.getVmTemplates()) {
			list.addAll(vmTemp.getApplications());
		}
		return list;
	}

	public Collection<JpaVirtualMachine> getVms() {
		return vms;
	}

	@Override
	public String toString() {
		return "JpaVirtueInstance [vms=" + vms + ", state=" + state + ", applications=" + applications + ", id=" + id
				+ ", name=" + name + ", username=" + username + ", templateId=" + templateId + "]";
	}

	public Collection<ApplicationDefinition> getApplications() {
		return applications;
	}

	protected void setVms(Collection<JpaVirtualMachine> vms) {
		this.vms = vms;
	}

	protected void setApplications(Collection<ApplicationDefinition> applications) {
		this.applications = applications;
	}

	public JpaVirtualMachine findVmByApplicationId(String applicationId) {
		for (JpaVirtualMachine vm : vms) {
			if (vm.findApplicationById(applicationId) != null) {
				return vm;
			}
		}
		return null;
	}

	public VirtueState getState() {
		return getVirtueStateFrom(getVms());
	}

	private VirtueState getVirtueStateFrom(Collection<JpaVirtualMachine> vms) {
		// TODO this should probably be handled elsewhere
		state = VirtueState.RUNNING;
		boolean creating = false;
		boolean launching = false;
		boolean deleting = false;
		for (JpaVirtualMachine vm : vms) {
			VmState s = vm.getState();
			switch (s) {
			case RUNNING:
				// do nothing
				break;
			case CREATING:
				creating = true;
				break;
			case LAUNCHING:
				creating = true;
				break;
			case DELETING:
				deleting = true;
				break;
			case ERROR:
				setState(null);
				return null;
			case PAUSED:
				break;
			case PAUSING:
				break;
			case RESUMING:
				break;
			case STOPPED:
				break;
			case STOPPING:
				break;
			default:
				break;
			}
		}
		if ((creating || launching) && deleting) {
			return (null);
		}

		if ((creating || launching)) {
			return (VirtueState.LAUNCHING);
		}
		if (deleting) {
			return (VirtueState.DELETING);
		}
		return VirtueState.RUNNING;
	}
}
