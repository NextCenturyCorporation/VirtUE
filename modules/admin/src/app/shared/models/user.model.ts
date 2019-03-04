
import { Item } from './item.model';
import { Virtue } from './virtue.model';
import { VirtualMachine } from './vm.model';
import { VirtueInstance } from './virtue-instance.model';

import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
* @class
 * Represents a User.
 *
 * @extends [[Item]]
 */
export class User extends Item {

  /** What roles this User is granted - currently can be 'User' and/or 'Admin' */
  roles: string[] = [];

  activeVirtues: DictList<VirtueInstance> = new DictList<VirtueInstance>();
  virtueTemplates: DictList<Virtue> = new DictList<Virtue>();

  virtueTemplateIds: string[] = [];

  /**
   * convert from whatever form the user object is in the database.
   *
   * @param userObj a user record, retrieved from the backend, which we want to convert into a User.
   */
  constructor(userObj?) {
    super();
    this.parentDomain = '/users';

    if (userObj) {
      this.name = userObj.username;
      this.roles = userObj.authorities;
      this.enabled = userObj.enabled;
      this.virtueTemplateIds = userObj.virtueTemplateIds;
    }
  }

  getDatasetName(): string {
    return DatasetNames.USERS;
  }

  /**
   * #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {
    if (datasetName === DatasetNames.VIRTUE_TS) {
      this.virtueTemplates = dataset.getSubset(this.virtueTemplateIds) as DictList<Virtue>;
    }
    else if (datasetName === DatasetNames.VIRTUES) {
      this.activeVirtues = new DictList<VirtueInstance>();
      for (let v of dataset.asList() as VirtueInstance[]) {
        if (v.user === this.getName()) {
          this.activeVirtues.add(v.getID(), v);
        }
      }
    }
  }

  /**
   * Overrides [[Item.getID]]
   * @return the user's username - its ID.
   */
  getID(): string {
    return this.name;
  }

  /**
   * Overrides [[Item.setID]]
   *
   * @param id the new username (which functions as ID) to give this User.
   */
  setID(id: string) {
    this.name = id;
  }


  /** @override [[Item.getRelatedDict]] */
  getRelatedDict(datasetName: DatasetNames): DictList<IndexedObj> {
    if (datasetName === DatasetNames.VIRTUE_TS) {
      return this.virtueTemplates;
    }
    if (datasetName === DatasetNames.VIRTUES) {
      return this.activeVirtues;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.VIRTUE_TS or VIRTUES, was", datasetName);
    return undefined;
  }

  /**
   * Currently Users only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedIDList]]
   */
  getRelatedIDList(datasetName: DatasetNames): string[] {

    if (datasetName === DatasetNames.VIRTUE_TS) {
      return this.virtueTemplateIds;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.VIRTUE_TS or VIRTUES, was", datasetName);
    return [];
  }

  getActiveVirtues(): VirtueInstance[] {
    return this.getChildren(DatasetNames.VIRTUES) as VirtueInstance[];
  }

  getVirtues(): Virtue[] {
    return this.getChildren(DatasetNames.VIRTUE_TS) as Virtue[];
  }

  getVirtueVms(): VirtualMachine[] {
    return this.getGrandChildren(DatasetNames.VIRTUE_TS, DatasetNames.VM_TS) as VirtualMachine[];
  }

  removeUnspecifiedChild(childObj: IndexedObj): void {
    if (childObj instanceof Virtue) {
      this.removeVirtue(childObj);
    }
    else {
      console.log("The given object doesn't appear to be a Virtue.");
    }
  }

  removeVirtue(virtue: Virtue) {
    this.removeChild(virtue.getID(), DatasetNames.VIRTUE_TS);
  }

  protected getInBackendFormat() {
    let user = {
      username: this.name,
      authorities: this.roles,
      enabled: this.enabled,
      virtueTemplateIds: this.virtueTemplateIds
    };

    return user;
  }
}
