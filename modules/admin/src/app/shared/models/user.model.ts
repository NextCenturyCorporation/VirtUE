
import { Item } from './item.model';
import { Virtue } from './virtue.model';

import { DictList } from './dictionary.model';

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
        this.childIDs = [];
      }
      else {
        this.childIDs = userObj.virtueTemplateIds;
      }

      this.enabled = userObj.enabled;

      this.parentDomain = '/users';
    }
    else {
      this.roles = [];
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
}
