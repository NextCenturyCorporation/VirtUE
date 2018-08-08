
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

import { DictList } from './dictionary.model';

/**
 * Represents a Virtue.
 * Children are VirtualMachine objects.
 *
 * This needs an html list (as a string) representing it's child vms' apps for
 * the virtue-list page, where both the children (vms) and children's children
 * (apps) need to be listed.
 */
export class Virtue extends Item {

  version: string;
  lastEditor: string;
  lastModification: any;
  awsTemplateName: string;
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
      this.modDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');
      this.color = virtueObj.color;
      this.status = virtueObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }

    if (! this.color) {
      this.color = "#B2B2B2";
    }

    this.children = new DictList<VirtualMachine>();
    this.appsListHTML = "";
  }

  generateAppListHtml(allVms: DictList<VirtualMachine>, allApps: DictList<Application>): void {
    let allChildApps: string[] = [];
    for (let vmID of this.childIDs) {
      allChildApps = allChildApps.concat(allVms.get(vmID).childIDs)
    }
    this.appsListHTML = this.getSpecifiedItemsHTML(allChildApps, allApps);
  }

  getRepresentation(): {} {
    if (! this.version) {
      this.version = '1.0';
    }

    return {
      'name': this.name,
      'version': this.version,
      'enabled': this.enabled,
      'color' : this.color,
      'virtualMachineTemplateIds': this.childIDs
    };
  }
}
