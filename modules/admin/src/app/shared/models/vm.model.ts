
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

  constructor(userObj) {
    if (userObj) {
      super('', userObj.username);
      this.enabled = userObj.enabled;
      this.status = userObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }
  }

  setName(s: string) {
    this.name = s;
  }

  getName(): string {
    return this.name;
  }
}
