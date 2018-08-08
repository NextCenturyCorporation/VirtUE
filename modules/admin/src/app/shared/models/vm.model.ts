
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
      this.modDate = new DatePipe('en-US').transform(vmObj.lastModification, 'short');
      this.securityTag = vmObj.securityTag;
      this.os = vmObj.os;
      this.status = vmObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }

    this.children = new DictList<Application>();
  }

  getRepresentation(): {} {
    return {
      'name': this.name,
      'os': this.os,
      'loginUser': 'system',
      'enabled': this.enabled,
      'applicationIds': this.childIDs,
      'securityTag': this.securityTag
    };
  }
}
