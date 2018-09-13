
import { Item } from './item.model';
import { Virtue } from './virtue.model';

import { DictList } from './dictionary.model';

/**
 * #uncommented
 * @class
 * @extends
 *
 * Represents a User.
 * Children are Virtue objects.
 */
export class User extends Item {

  /** #uncommented */
  roles: string[];


  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // convert from whatever form the user object is in the database.
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
      this.status = userObj.enabled ? 'enabled' : 'disabled';

      this.parentDomain = '/users';
    }
    else {
      this.roles = [];
    }
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // Overrides Item
  getID(): string {
    return this.name;
  }
}
