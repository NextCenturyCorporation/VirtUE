
import { Item } from './item.model';
import { Virtue } from './virtue.model';

import { DictList } from './dictionary.model';

import { IndexedObj } from './indexedObj.model';
import { Datasets } from '../abstracts/gen-data-page/datasets.enum';

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
  virtueTemplateIDs: string[];

  /**
   * convert from whatever form the user object is in the database.
   *
   * @param userObj a user record, retrieved from the backend, which we want to convert into a User.
   */
  constructor(userObj) {
    super();
    if (userObj) {
      this.name = userObj.username;
      if (!userObj.authorities) {
        this.roles = [];
      }
      else {
        this.roles = userObj.authorities;
      }

      if (!userObj.virtueTemplateIds) {
        this.virtueTemplateIDs = [];
      }
      else {
        this.setChildIDs(userObj.virtueTemplateIds);
      }

      this.enabled = userObj.enabled;

      this.parentDomain = '/users';

      this.build = ['virtueTemplates']
    }
    else {
      this.roles = [];
    }
  }


  buildAttributes(childDatasets: DictList<(DictList<IndexedObj>)> ): void {

    let virtueDataset: DictList<IndexedObj> = childDatasets.get(Datasets.VIRTUES);

    this.setChildren(virtueDataset.getSubSet(this.getChildIDs()));
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



  /** @override [[Item.getChildIDs]] */
  getChildIDs(): string[] {
    return this.virtueTemplateIDs;
  }

  /** @override [[Item.getChildren]] */
  getChildren(): DictList<IndexedObj> {
    return this.virtueTemplates;
  }

  /** @override [[Item.setChildIDs]] */
  setChildIDs(newChildIDs: string[]): void {
    this.virtueTemplateIDs = newChildIDs;
  }

  /** @override [[Item.setChildren]] */
  setChildren(newChildren: DictList<IndexedObj>): void {
    this.virtueTemplates = newChildren;
  }
}
