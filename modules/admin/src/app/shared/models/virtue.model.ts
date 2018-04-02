import { Application } from './application.model';
import { Users } from './users.model';
import { VirtualMachine } from './vm.model';

export class Virtue {
  id: any;
  name: string;
  version: string;
  virtualMachineTemplateIds: any[];
  users: Users[];
  enabled: boolean;
  lastEditor: string;
  awsTemplateName: string;
  applications: Application[];

  public Virtue(
    id: any,
    name: string,
    version: string,
    virtualMachineTemplateIds: any[],
    users: Users[],
    enabled: boolean,
    lastEditor: string,
    awsTemplateName: string,
    applications: Application[]
  ) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.virtualMachineTemplateIds = virtualMachineTemplateIds;
    this.users = users;
    this.enabled = enabled;
    this.lastEditor = lastEditor;
    this.awsTemplateName = awsTemplateName;
    this.applications = applications;
  }
  constructor() { }
}
