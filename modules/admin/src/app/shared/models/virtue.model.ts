import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

export class Virtue {
  id: string;
  name: string;
  version: string;
  vmTemplates: VirtualMachine[];
  enabled: boolean;
  lastModification: string;
  lastEditor: string;
  awsTemplateName: string;
  applications: Application[];

  public VirtueTemplate(
    id: string,
    name: string,
    version: string,
    vmTemplates: VirtualMachine[],
    enabled: boolean,
    lastModification: string,
    lastEditor: string,
    awsTemplateName: string,
    applications: Application[]
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
  constructor() {}
}
