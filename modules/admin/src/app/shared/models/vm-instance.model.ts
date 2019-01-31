
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';

import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { VirtualMachine } from './vm.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';
import { Subdomains } from '../services/subdomains.enum';


export enum VmState {
	CREATING = "CREATING",
  STOPPED = "STOPPED",
  LAUNCHING = "LAUNCHING",
  RUNNING = "RUNNING",
  PAUSING = "PAUSING",
  PAUSED = "PAUSED",
  RESUMING = "RESUMING",
  STOPPING = "STOPPING",
  DELETING = "DELETING",
  ERROR = "ERROR",
  DELETED = "DELETED"
}

/**
 * Represents a virtual machine instance.
 */
export class VirtualMachineInstance extends IndexedObj {

  // these aren't returned by the backend at the moment
  // applications: DictList<Application> = new DictList<Application>();
  // applicationIds: string[] = [];

  name: string = "";
  id: string;
  os: string;
  state: VmState;
  parentDomain: string = "/";
  hostname: string;

  /**
   * convert from whatever form the vm object is in the database.
   * @param vmObj a virtual machine record, retrieved from the backend, which we want to convert into a VirtualMachine.
   */
  constructor(vmObj?) {
    super();
    this.parentDomain = '/vm-instances';

    if (vmObj) {
      this.id = vmObj.id;
      this.name = vmObj.name;
      this.os = vmObj.os;
      if (vmObj.state in VmState) {
        this.state = vmObj.state;
      }
      this.hostname = vmObj.hostname;

      // this.applicationIds = vmObj.applicationIds;
    }
  }

  getID(): string {
    return this.id;
  }

  getName(): string {
    // return this.name.split("-").slice(0, 5).join("-");
    return this.name.split("-").slice(1).join("-");
  }

  getSubdomain(): string {
    return Subdomains.VMS;
  }

  isStopped(): boolean {
    return  this.state === VmState.STOPPED ||
            this.state === VmState.STOPPING ||
            this.state === VmState.DELETING ||
            this.state === VmState.DELETED;
            // this.state === VmState.CREATING ||
            // this.state === VmState.LAUNCHING ||
            // this.state === VmState.RUNNING ||
            // this.state === VmState.PAUSING ||
            // this.state === VmState.PAUSED ||
            // this.state === VmState.RESUMING ||
            // this.state === VmState.ERROR
  }

  stop(): void {
    //
  }
  //
  // getApps(): IndexedObj[] {
  //   return this.getChildren(DatasetNames.APPS);
  // }

}
