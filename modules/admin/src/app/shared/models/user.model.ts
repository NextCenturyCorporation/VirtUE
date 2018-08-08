
import { Item } from './item.model';
import { Virtue } from './virtue.model';

import { DictList } from './dictionary.model';
/**
 * Represents a User.
 * Children are Virtue objects.
 *
 *
 */
export class User extends Item {

  username: string;
  virtues: Virtue[];
  roles: string[];


  //convert from whatever form the user object is in the database.
  constructor(userObj) {
    if (userObj) {
      super('', userObj.username);
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
      super('', '');
      this.childIDs = [];
      this.roles = [];
    }

    this.children = new DictList<Virtue>();
    this.virtues = [];
  }

  setName(s: string) {
    this.username = s;
  }

  //Overrides Item
  getName(): string {
    return this.username;
  }

  //Overrides Item
  getID(): string {
    return this.username;
  }

  getRepresentation(): {} {
    return {
      // 'name': this.name,
      // 'version': this.version,
      // 'enabled': this.enabled,
      // 'color' : this.color,
      // 'virtualMachineTemplateIds': this.childIDs
    };
  }
}
