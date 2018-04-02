import { Application } from './application.model';

export class VirtualMachine {
  id: string;
  name: string;
  os: string;
  templatePath: string;
  applicationIds: any[];
  enabled: boolean;
  lastModification: string;
  lastEditor: string;

  public VirtualMachine(
    id: string,
    name: string,
    os: string,
    templatePath: string,
    applicationIds: any[],
    enabled: boolean,
    lastModification: string,
    lastEditor: string
  ) {
    this.id = id;
    this.name = name;
    this.os = os;
    this.templatePath = templatePath;
    this.applicationIds = applicationIds;
    this.enabled = enabled;
    this.lastModification = lastModification;
    this.lastEditor = lastEditor;
  }
}
