
import { Item } from './item.model';
import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * Represents an Application.
 * Currently not decided how applications will be installed, or described, or loaded, or anything.
 * Right now they're just a name on a VM template.
 *
 * Could have a repository of OK'd programs, self-contained units.
 * So I'd want a form page for Applications, where you can:
 *    - upload some sort of executable or package.
 *    - define/load a set-up/installation script, to be run when the program is installed. Must install w/o internet, from that package.
 *      - Does that therefore mean you'd need to list and define all dependencies? Ugh.
 *    - maybe define a post-installation script
 *    - define a launch command (what if different VMs should have different commands? multiples of program seems wasteful)
 *    - maybe allow the program state to be saved and reloaded in some cases, instead of installed fresh each time?
 *      - Seems like it'd be really annoying and time-consuming, to have to re-set up everything, every time you log on.
 *
 * #TODO make apps meaningful
 *
 * At time of writing (early Aug 2018), version means nothing, and launchCommand and iconKey are not used.
 *
 * @extends [[Item]]
 */
export class Application extends Item {

  /** #uncommented (version) #TODO is this the application's version? unlike Virtue.version and vm.version*/
  version: string = '';

  /** The operating system which this particular application is able to run */
  os: string = '';

  /** #uncommented #TODO not sure how to change this, but it probably will need to change. (launchCommand)
   * Are these commands? are they defined here?
   * See notes at top of class
   */
  launchCommand: string = '';

  /** #uncommented TODO (iconKey) */
  iconKey: string = '';

  /**
   * convert from whatever form the application object is in the database.
   * @param appObj an application record, retrieved from the backend, which we want to convert into an Application.
   */
  constructor(appObj) {
    super();
    this.parentDomain = '/apps';

    if (appObj) {
      this.id = appObj.id;
      this.name = appObj.name;
      this.os = appObj.os;
      this.version = appObj.version;
      this.launchCommand = appObj.launchCommand;
      this.iconKey = appObj.iconKey;

    }
  }

  getDatasetName(): string {
    return DatasetNames.APPS;
  }

  removeUnspecifiedChild(obj): void {}
}
