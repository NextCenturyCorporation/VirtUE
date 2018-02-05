import { ApplicationModel } from './virtualmachine.model';

export class VirtueModel {
  id: string;
  name: string;
  version: string;
  vmTemplates: ApplicationModel[];
  awsTemplateName: string;
  applications: string;

  public VirtueTemplate(id: string, name: string, version: string, vmTemplates: ApplicationModel[], awsTemplateName:string, applications: string) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.awsTemplateName = awsTemplateName;
    this.applications = applications;
	}
  constructor(){}
}
