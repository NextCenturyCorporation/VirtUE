
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';

import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';
import { Subdomains } from '../services/subdomains.enum';

/**
 * Represents a virtual machine template.
 */
export class VirtualMachine extends Item {

  /** The operating system this VM should be set up to have */
  os: string;

  /** what version of edit this template currently is at */
  version: number = 1;

  applications: DictList<Application> = new DictList<Application>();

  applicationIds: string[] = [];

  // /** #uncommented what is this? (templatePath)*/
  // templatePath: string;

  // /** #uncommented what is this? how should it be set or used? (loginUser)*/
  // loginUser: string;

  /** #TODO do we need this? Can anyone else edit templates, besides the admin? Or will there be multiple, distinguishable, admins? */
  lastEditor: string;

  /** A full Date, of the last time this record was changed on the backend. */
  lastModification: Date;

  /**
   * convert from whatever form the vm object is in the database.
   * @param vmObj a virtual machine record, retrieved from the backend, which we want to convert into a VirtualMachine.
   */
  constructor(vmObj?) {
    super();
    this.parentDomain = '/vm-templates';

    if (vmObj) {
      this.id = vmObj.id;
      this.name = vmObj.name;
      this.enabled = vmObj.enabled;
      this.os = vmObj.os;
      this.applicationIds = vmObj.applicationIds;
      this.version = vmObj.version;
      this.lastEditor = vmObj.lastEditor;
      this.modificationDate = vmObj.lastModification;
      this.readableModificationDate = new DatePipe('en-US').transform(vmObj.lastModification, 'short');
    }

  }


  /**
   * @return the VMS subdomain
   */
  getSubdomain(): string {
    return Subdomains.VMS;
  }

  /**
   * #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {

    if (datasetName === DatasetNames.APPS) {
      this.applications = dataset.getSubset(this.applicationIds) as DictList<Application>;
    }

  }

  /**
   * Currently Vms only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedDict]]
   */
  getRelatedDict(datasetName: DatasetNames): DictList<IndexedObj> {
    if (datasetName === DatasetNames.APPS) {
      return this.applications;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.APPS, was", datasetName);
    return new DictList<IndexedObj>();
  }

  /**
   * Currently Vms only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedIDList]]
   */
  getRelatedIDList(datasetName: DatasetNames): string[] {

    if (datasetName === DatasetNames.APPS) {
      return this.applicationIds;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.APPS, was", datasetName);
    return [];
  }

  getApps(): IndexedObj[] {
    return this.getChildren(DatasetNames.APPS);
  }

  removeUnspecifiedChild(childItem: IndexedObj): void {
    if (childItem instanceof Application) {
      this.removeApp(childItem);
    }
    else {
      console.log("The given object doesn't appear to be a Application.");
    }
  }

  removeApp(childApp: Application): void {
    this.removeChild(childApp.getID(), DatasetNames.APPS);
  }

  protected getInBackendFormat() {
    let vm = {
      id: this.id,
      name: this.name,
      enabled: this.enabled,
      applicationIds: this.applicationIds,
      version: this.version,
      lastModification: this.modificationDate,
      os: this.os,
      lastEditor: 'administrator',
      loginUser: 'system' // TODO does this still exist on the backend?
    };

    return vm;
  }
}
