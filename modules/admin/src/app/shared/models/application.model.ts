
import { Item } from './item.model';

/**
 * Represents a Application.
 * Children are null.
 * At time of writing, launchCommand and iconKey are not used.
 */
export class Application extends Item{

  id: string;
  version: string;
  os: string;
  launchCommand: string;
  iconKey: string;

  //convert from whatever form the application object is in the database.
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
