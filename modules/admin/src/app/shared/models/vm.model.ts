
import { Item } from './item.model';
import { Application } from './application.model';


export class VirtualMachine extends Item {
  // id: string;
  name: string;
  // enabled: boolean;
  appIDs: any[];
  apps: Application[];

  os: string;
  templatePath: string;
  loginUser: string;
  lastModification: string;
  lastEditor: string;
  securityTag: string;

  constructor(id: string, name: string) {
    super(id, name);
    this.appIDs = [];
    this.enabled = true;
    this.apps = new Array<Application>();
  }

  getName() {
    return this.name;
  }
}
