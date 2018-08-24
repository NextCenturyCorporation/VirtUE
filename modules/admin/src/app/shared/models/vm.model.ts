
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';

import { DictList } from './dictionary.model';

/**
 * Represents a VirtualMachine.
 * Children are Application objects.
 *
 */
export class VirtualMachine extends Item {

  id: string;
  os: string;
  templatePath: string;
  loginUser: string;
  lastModification: string | Date;
  lastEditor: string;

  // convert from whatever form the vm object is in the database.
  constructor(vmObj) {
    super();
    if (vmObj) {
      this.id = vmObj.id;
      this.name = vmObj.name;
      this.enabled = vmObj.enabled;
      this.childIDs = vmObj.applicationIds;
      this.lastEditor = vmObj.lastEditor;
      this.lastModification = vmObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(vmObj.lastModification, 'short');
      this.os = vmObj.os;
      this.status = vmObj.enabled ? 'enabled' : 'disabled';

      // change 'edit' to 'view' once we have a view page set up.
      this.domain = '/vm-templates/edit/' + this.id;
    }

  }
}
