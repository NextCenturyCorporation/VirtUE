
import { Item } from './item.model';

/**
 * Represents an Application.
 * Children are null.
 * At time of writing (early Aug 2018), launchCommand and iconKey are not used.
 */
export class Application extends Item {

  id: string;
  version: string;
  os: string;
  launchCommand: string;
  iconKey: string;

  // convert from whatever form the application object is in the database.
  constructor(appObj) {
    super();
    if (appObj) {
      this.id = appObj.id;
      this.name = appObj.name;
      this.os = appObj.os;
      this.version = appObj.version;
      this.launchCommand = appObj.launchCommand;
      this.iconKey = appObj.iconKey;
    }
    else {
      this.os = '';
      this.launchCommand = '';
      this.iconKey = '';
    }

    this.children = undefined;
  }
}
