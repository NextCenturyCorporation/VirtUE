
import { Item } from './item.model';
import { Virtue } from './virtue.model';

export class User extends Item {
  // id: string;
  username: string;
  // enabled: boolean;
  virtueIDs: any[];
  virtues: Virtue[];
  roles: any[];

  //whatever form the user object is in the database.
  constructor(userObj) {
    if (userObj) {
      super('', userObj.username);
      this.roles = userObj.authorities;
      this.enabled = userObj.enabled;
      this.status = userObj.enabled ? 'enabled' : 'disabled';
    }
    else {
      super('', '');
    }


    // console.log(this);
    this.virtues = new Array<Virtue>();
    this.virtueIDs = [];
    // this.enabled = true;
    this.roles = [];
  }

  setName(s: string) {
    this.username = s;
  }

  getName(): string {
    return this.username;
  }
}
