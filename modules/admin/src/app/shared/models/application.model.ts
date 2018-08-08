
import { Item } from './item.model';

/**
 * Represents a Application.
 * Children are null.
 * At time of writing, launchCommand and iconKey are not used.
 */
export class Application extends Item{

  version: string;
  os: string;
  launchCommand: string;
  iconKey: string;

  //convert from whatever form the application object is in the database.
  constructor(appObj) {
    if (appObj) {
      super(appObj.id, appObj.name);
      this.os = appObj.os;
      this.version = appObj.version;
      this.launchCommand = appObj.launchCommand;
      this.iconKey = appObj.iconKey;
    }
    else {
      super('', '');
      this.os = '';
      this.launchCommand = '';
      this.iconKey = '';
    }

    //Apps can't be disabled.
    //this value is used in getSpecifiedItemsHTML().
    this.enabled = true;
    this.status = 'enabled';
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
