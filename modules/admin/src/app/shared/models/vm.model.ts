
import { Application } from '../../shared/models/application.model';


export class VirtualMachine {
  id: string;
  name: string;
  enabled: boolean;
  appIDs: any[];
  apps: Application[];

  os: string;
  templatePath: string;
  loginUser: string;
  lastModification: string;
  lastEditor: string;
  securityTag: string;

  constructor() {
    this.appIDs = [];
    this.enabled = true;
    this.apps = new Array<Application>();
  }
}
