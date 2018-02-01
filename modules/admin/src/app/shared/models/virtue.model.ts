export class VirtueModel {
  id: string;
  name: string;
  version: string;
  vmTemplates: [
    { id: string, name: string, os: string, templatePath: string }
  ];
  awsTemplateName: string;
  applications: [
    { id: string, name: string, version: string, os: string }
  ];

  public VirtueTemplate(id: string, name: string, version: string, vmTemplates: any, awsTemplateName: string, applications: any) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.vmTemplates = vmTemplates;
		this.awsTemplateName = awsTemplateName;
    this.applications = applications;
	}
  constructor(){}
}
