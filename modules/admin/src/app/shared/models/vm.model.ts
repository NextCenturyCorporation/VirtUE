
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';

import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

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
  version: number;

  /** #uncommented */
  applications: DictList<IndexedObj>;

  /** #uncommented */
  applicationIDs: string[];

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
  constructor(vmObj) {
    super();
    if (vmObj) {
      this.id = vmObj.id;
      this.name = vmObj.name;
      this.enabled = vmObj.enabled;
      this.applicationIDs = vmObj.applicationIds;
      this.version = vmObj.version;
      if (! this.version) {
        this.version = 1;
      }
      this.lastEditor = vmObj.lastEditor;
      this.lastModification = vmObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(vmObj.lastModification, 'short');
      this.os = vmObj.os;

      this.parentDomain = '/vm-templates';
    }

  }


  /**
   * #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {

    if (datasetName === DatasetNames.APPS) {
      this.applications = dataset.getSubset(this.applicationIDs) as DictList<Application>;
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
  }

  /**
   * Currently Vms only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedIDList]]
   */
  getRelatedIDList(datasetName: DatasetNames): string[] {

    if (datasetName === DatasetNames.APPS) {
      return this.applicationIDs;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.APPS, was", datasetName);
  }
}
