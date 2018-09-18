
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';

import { DictList } from './dictionary.model';

/**
 * @class
 * @extends
 *
 * Represents a VirtualMachine.
 * Children are Application objects.
 *
 */
export class VirtualMachine extends Item {

  /** The operating system this VM should be set up to have */
  os: string;

  /** what version of edit this template currently is at */
  version: string;

  // /** #uncommented what is this? */
  // templatePath: string;

  // /** #uncommented what is this? how should it be set or used? */
  // loginUser: string;

  /** #TODO do we need this? Can anyone else edit templates, besides the admin? Or will there be multiple, distinguishable, admins? */
  lastEditor: string;

  /** A full Date, of the last time this record was changed on the backend. */
  lastModification: Date;

  /**
   * convert from whatever form the vm object is in the database.
   * @param vmObj a virtual machine record, retrieved from the backend, which we want to convert into a VirtualMachine.
   */
  constructor(vmObj) {
    super();
    if (vmObj) {
      this.id = vmObj.id;
      this.name = vmObj.name;
      this.enabled = vmObj.enabled;
      this.childIDs = vmObj.applicationIds;
      this.version = vmObj.version;
      if (! this.version) {
        this.version = '1';
      }
      this.lastEditor = vmObj.lastEditor;
      this.lastModification = vmObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(vmObj.lastModification, 'short');
      this.os = vmObj.os;

      this.parentDomain = '/vm-templates';
    }

  }
}
