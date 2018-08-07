
import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

import { DictList } from './dictionary.model';

export class Virtue extends Item {

  vmIDs: any[];
  vms: VirtualMachine[];
  version: string;
  lastEditor: string;
  lastModification: any;
  awsTemplateName: string;
  appIDs: any[];
  color: string;

  appsListHTML: string;

  //convert from whatever form the virtue object is in the database.
  constructor(virtueObj) {
    if (virtueObj) {
      super(virtueObj.id, virtueObj.name);
      this.childIDs = virtueObj.virtualMachineTemplateIds;
      this.enabled = virtueObj.enabled;
      this.version = virtueObj.version;
      this.lastEditor = virtueObj.lastEditor;
      this.lastModification = virtueObj.lastModification;
      this.modDate = virtueObj.modDate;
      this.color = virtueObj.color;
      this.status = virtueObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }
    this.vms = [];
    this.appsListHTML = "";
  }

  formatAppListHtml(allVms: DictList<VirtualMachine>, allApps: DictList<Application>): void {
    let allChildApps: string[] = [];
    for (let vmID of this.childIDs) {
      allChildApps = allChildApps.concat(allVms.get(vmID).childIDs)
    }
    this.appsListHTML = this.getSpecifiedItemsHTML(allChildApps, allApps);
  }

  getName() {
    return this.name;
  }
}
