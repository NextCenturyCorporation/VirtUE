export class Virtue {
  id: any;
  name: string;
  version: string;
  virtualMachineTemplateIds: any[];
  enabled: boolean;
  lastEditor: string;
  lastModification: any;
  awsTemplateName: string;
  applicationIds: any[];

  public Virtue(
    id: any,
    name: string,
    version: string,
    virtualMachineTemplateIds: any[],
    enabled: boolean,
    lastEditor: string,
    lastModification: any,
    awsTemplateName: string,
    applicationIds: any[]
  ) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.virtualMachineTemplateIds = virtualMachineTemplateIds;
    this.enabled = enabled;
    this.lastEditor = lastEditor;
    this.lastModification = lastModification;
    this.awsTemplateName = awsTemplateName;
    this.applicationIds = applicationIds;
  }
  constructor() { }
}
