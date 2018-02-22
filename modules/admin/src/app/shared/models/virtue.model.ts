import { Application } from './application.model';
import { Users } from './users.model';
import { VirtualMachine } from './vm.model';

export class Virtue {
  id: string;
  name: string;
  version: string;
  vmTemplates: VirtualMachine[];
  users: Users[];
  enabled: boolean;
  lastModification: any;
  lastEditor: string;
  awsTemplateName: string;
  applications: Application[];

  public Virtue(
    id: string,
    name: string,
    version: string,
    vmTemplates: VirtualMachine[],
    users: Users[],
    enabled: boolean,
    lastModification: any,
    lastEditor: string,
    awsTemplateName: string,
    applications: Application[]
  ) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.vmTemplates = vmTemplates;
    this.users = users;
    this.enabled = enabled;
    this.lastModification = lastModification;
    this.lastEditor = lastEditor;
    this.awsTemplateName = awsTemplateName;
    this.applications = applications;
}
  constructor() {}
}
