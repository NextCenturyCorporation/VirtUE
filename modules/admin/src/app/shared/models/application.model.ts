
import { Item } from './item.model';

export class Application extends Item{
  // id: string;
  name: string;

  version: string;
  os: string;
  launchCommand: string;

  constructor(id: string) {
    super(id, '');
    // this.enabled = undefined; //probably unnecessary. Apps can't be disabled.
  }

  getName() {
    return this.name;
  }
}
