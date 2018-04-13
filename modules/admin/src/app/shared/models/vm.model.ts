export class VirtualMachine {
  id: string;
  name: string;
  os: string;
  templatePath: string;
  loginUser: string;
  enabled: boolean;
  lastModification: string;
  lastEditor: string;
  applicationIds: any[]

  public VirtualMachine(
    id: string,
    name: string,
    os: string,
    templatePath: string,
    loginUser: string,
    enabled: boolean,
    lastModification: string,
    lastEditor: string,
    applicationIds: any[]
  ) {
    this.id = id;
    this.name = name;
    this.os = os;
    this.templatePath = templatePath;
    this.loginUser = loginUser;
    this.enabled = enabled;
    this.lastModification = lastModification;
    this.lastEditor = lastEditor;
    this.applicationIds = applicationIds;
  }
}
