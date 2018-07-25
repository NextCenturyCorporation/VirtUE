
import { Virtue } from '../../shared/models/virtue.model';

export class User {
  id: string;
  username: string;
  enabled: boolean;
  virtueIDs: any[];
  virtues: Virtue[];
  roles: any[];

  constructor() {
    this.virtues = new Array<Virtue>();
    this.virtueIDs = [];
    this.enabled = true;
    this.roles = [];
  }
}
