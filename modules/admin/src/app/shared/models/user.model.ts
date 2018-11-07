
import { Item } from './item.model';
import { Virtue } from './virtue.model';

import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
* @class
 * Represents a User.
 * Children are Virtue objects.
 *
 * @extends [[Item]]
 */
export class User extends Item {

  /** What roles this User is granted - currently can be 'User' and/or 'Admin' */
  roles: string[];

  /** #uncommented */
  virtueTemplates: DictList<IndexedObj>;

  /** #uncommented */
  virtueTemplateIds: string[];

  /**
   * convert from whatever form the user object is in the database.
   *
   * @param userObj a user record, retrieved from the backend, which we want to convert into a User.
   */
  constructor(userObj) {
    super();
    if (userObj) {
      this.name = userObj.username;
      this.roles = userObj.authorities;
      this.enabled = userObj.enabled;
      this.virtueTemplateIds = userObj.virtueTemplateIds;

    }

    if ( !this.roles) {
      this.roles = [];
    }
    if ( !this.virtueTemplateIds) {
      this.virtueTemplateIds = [];
    }

    this.parentDomain = '/users';
  }

  /**
   * #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {
    if (datasetName === DatasetNames.VIRTUES) {
      this.virtueTemplates = dataset.getSubset(this.virtueTemplateIds) as DictList<Virtue>;
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
    if (datasetName === DatasetNames.VIRTUES) {
      return this.virtueTemplates;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.VIRTUES, was", datasetName);
    return undefined;
  }

  /**
   * Currently Users only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedIDList]]
   */
  getRelatedIDList(datasetName: DatasetNames): string[] {

    if (datasetName === DatasetNames.VIRTUES) {
      return this.virtueTemplateIds;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.VIRTUES, was", datasetName);
    return [];
  }
}
