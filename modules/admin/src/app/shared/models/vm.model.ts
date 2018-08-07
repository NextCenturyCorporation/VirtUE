
import { Item } from './item.model';
import { Application } from './application.model';


export class VirtualMachine extends Item {

  apps: Application[];
  os: string;
  templatePath: string;
  loginUser: string;
  lastModification: string;
  lastEditor: string;
  securityTag: string;

  //convert from whatever form the vm object is in the database.
  constructor(vmObj) {
    if (vmObj) {
      super(vmObj.id, vmObj.name);
      this.enabled = vmObj.enabled;
      this.childIDs = vmObj.applicationIds;
      this.lastEditor = vmObj.lastEditor;
      this.lastModification = vmObj.lastModification;
      this.modDate = vmObj.modDate;
      this.securityTag = vmObj.securityTag;
      this.os = vmObj.os;
      this.status = vmObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }
    this.apps = [];
  }
}
