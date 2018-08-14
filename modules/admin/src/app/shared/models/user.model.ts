
import { Item } from './item.model';
import { Virtue } from './virtue.model';

import { DictList } from './dictionary.model';
/**
 * Represents a User.
 * Children are Virtue objects.
 *
 */
export class User extends Item {

  roles: string[];


  //convert from whatever form the user object is in the database.
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
    }
    else {
      this.roles = [];
    }
  }


  //Overrides Item
  getID(): string {
    return this.name;
  }
}
