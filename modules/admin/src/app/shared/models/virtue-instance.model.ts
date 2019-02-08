
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';
import { VirtualMachineInstance } from './vm-instance.model';
import { Virtue } from './virtue.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

import { DictList } from './dictionary.model';

import { NetworkPermission } from './networkPerm.model';
import { FileSystem } from './fileSystem.model';
import { Printer } from './printer.model';

export enum VirtueState {
  ERROR = 10,
  RUNNING = 20,
  RESUMING = 30,
  LAUNCHING = 40,
  CREATING = 50,
  PAUSED = 60,
  PAUSING = 70,
  STOPPING = 80,
  STOPPED = 90,
  DELETING = 100,
  DELETED = 110,
  UNPROVISIONED = 120
}

/**
 * @class
 * Represents a template for a Virtue.
 * A virtue is a collection of virtual machines that can generally interact with each other, but
 *  not (by default) with any vm in a different Virtue. Each Virtue has a plethora of settings that can be defined,
 *  which allow the admin to decide what small set of ways this collection of virtual machines is allowed to interact
 *  with the outside world.
 *
 * Virtue instances also have software sensors built in, which can be monitored to detect possible intrusions or attacks.
 *
 * Virtues are supposed to represent a role, a single cohesive bundle of activities. So there might be a vm that has MS office on it,
 * and another that has LibreOffice and gedit on it. Those two together might make an "office" Virtue.
 * A standard office worker might have:
 *  - that office virtue
 *  - an internet virtue, with browsers, that's open to the world but closed to all other virtues
 *  - an intranet virtue with corporate email and other internal services
 *
 * @extends [[Item]]
 */
export class VirtueInstance extends IndexedObj {

  // these are vm instances
  vmIds: string[] = [];
  vms: DictList<VirtualMachineInstance> = new DictList<VirtualMachineInstance>();

  // note that these are just links to definitions of apps. Not any running instance in particular.
  appIds: string[] = [];
  apps: DictList<Application> = new DictList<Application>();

  templateId: string = "";
  template: Virtue;

  state: VirtueState;

  name: string = "";
  user: string = "";
  color: string = "transparent";
  parentDomain: string = "/";

  /**
   * convert from whatever form the virtue object is in the database.
   *
   * Note that there doesn't currently appear to be any good way to compare if two objects have the same
   * attributes, without simply iterating through them all and checking. 'instanceof' appears to only work
   * for items created from the same prototype.
   *
   * TODO should this be durable, or fail-fast?
   *
   * @param virtueObj a virtue record, retrieved from the backend, which we want to convert into a Virtue.
   */
  constructor(virtueObj?) {
    super();
    this.parentDomain = '/virtue/instances';

    if (virtueObj) {
      this.vmIds = virtueObj.virtualMachineIds;
      this.name = virtueObj.name;
      this.user = virtueObj.username;
      this.templateId = virtueObj.templateId;
      // maybe temporary. It could be actually pulled in, but at the moment all we need is to able to navigate to it.
      this.template = new Virtue({id: virtueObj.templateId});
      this.appIds = virtueObj.applicationIds;
      this.state = virtueObj.state;
      this.color = virtueObj.color;
    }
  }

  getName(): string {
    return this.name;
  }

  getTemplateVersion(): string {
    return String(this.template ? this.template.version : "");
  }

  getID(): string {
    return this.templateId;
  }

  getDatasetName(): string {
    return DatasetNames.VIRTUES;
  }

  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {
    if (datasetName === DatasetNames.VMS) {
      this.vms = dataset.getSubset(this.vmIds) as DictList<VirtualMachineInstance>;
    }
    else if (datasetName === DatasetNames.APPS) {
      this.apps = dataset.getSubset(this.appIds) as DictList<Application>;
    }
    else if (datasetName === DatasetNames.VIRTUE_TS) {
      this.template = dataset.get(this.templateId) as Virtue;
    }
  }

  getVms(): IndexedObj[] {
    return this.getChildren(DatasetNames.VMS);
  }

  isStopped(): boolean {
    // ERROR = 10,
    // RUNNING = 20,
    // RESUMING = 30,
    // LAUNCHING = 40,
    // CREATING = 50,
    // PAUSED = 60,
    // PAUSING = 70,
    // STOPPING = 80,
    // STOPPED = 90,
    // DELETING = 100,
    // DELETED = 110,
    // UNPROVISIONED = 120
    return ! (this.state >= 20 && this.state <= 70);
  }

  stop(): void {
    //
  }

  stopVm(vm: VirtualMachine): void {
    //
  }


  /** @override [[Item.getRelatedDict]] */
  getRelatedDict(datasetName: DatasetNames): DictList<IndexedObj> {
    switch (datasetName) {
      case DatasetNames.VMS:
        return this.vms;
      default:
        console.log("You shouldn't be here. Requested dataset: ", datasetName);
        return null;
    }
  }

  /**
   * Currently Users only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedIDList]]
   */
  getRelatedIDList(datasetName: DatasetNames): string[] {
    switch (datasetName) {
      case DatasetNames.VMS:
        return this.vmIds;
    }
    console.log("You shouldn't be here. Requested dataset: ", datasetName);
    return [];
  }

}
