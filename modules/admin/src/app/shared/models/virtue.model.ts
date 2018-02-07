import { ApplicationModel } from './virtualmachine.model';

export class VirtueModel {
  id: string;
  name: string;
  version: string;
  vmTemplates: ApplicationModel[];
  enabled: boolean;
  lastModification: string;
  lastEditor: string;
  awsTemplateName: string;
  applications: string;

  public VirtueTemplate(
    id: string,
    name: string,
    version: string,
    vmTemplates: ApplicationModel[],
    enabled: boolean,
    lastModification: string,
    lastEditor: string,
    awsTemplateName:string,
    applications: string
  ) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
    this.enabled = enabled;
    this.lastModification = lastModification;
    this.lastEditor = lastEditor;
		this.awsTemplateName = awsTemplateName;
    this.applications = applications;
	}
  constructor(){}
}
