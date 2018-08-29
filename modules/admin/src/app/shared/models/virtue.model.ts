
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

  id: string;
  version: string;
  lastEditor: string;
  lastModification: string | Date;
  awsTemplateName: string;
  color: string;

  // convert from whatever form the virtue object is in the database.
  constructor(virtueObj) {
    super();
    if (virtueObj) {
      this.id = virtueObj.id;
      this.name = virtueObj.name;
      this.childIDs = virtueObj.virtualMachineTemplateIds;
      this.enabled = virtueObj.enabled;
      this.version = virtueObj.version;
      this.lastEditor = virtueObj.lastEditor;
      this.lastModification = virtueObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');
      this.color = virtueObj.color;
      this.status = virtueObj.enabled ? 'enabled' : 'disabled';
    }

    if (! this.color) {
      // TODO - is it better to have everything default to no color, or to something
      // like silver (#C0C0C0), to let the user know that colors are available?
      this.color = "transparent";
    }
  }

}
