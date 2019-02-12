import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';
import { Toggleable } from './toggleable.interface';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * This class represents a file system, which a virtue may be given various forms of access to.
 *
 * Specifically, there will be a set of these that are defined by the admin, with default values for enabled/r/w/e.
 *
 * A virtue can have a FileSystem added to it, and then can change those default settings. Those settings are saved with
 * the Virtue. So it doesn't work like the other IndexedObj objects - while the FileSystem dataset needs to be pulled on the
 * pages listed below, Virtues shouldn't use those values to set any of their attributes.
 *
 *  Does it need to be pulled? On anything besides the modal and the config tab?
 *    I guess - so we can show some marker when a value is different from the default.
 *
 * Used in [[ConfigFileSysTabComponent]] [[VirtueSettingsTabComponent]] and [[Virtue]]
 */
export class FileSystem extends IndexedObj implements Toggleable {

  /** the id given by the backend */
  id: string = "12345"; // default to something recognizable so it's clear if something goes wrong.

  address: string;

  name: string;

  /** whether this FS can be accessed at all */
  enabled: boolean = true;

  readPerm: boolean = false;

  writePerm: boolean = false;

  /**
   * TODO is this execution privileges on the FS, or from it? Both? Either?
   */
  executePerm: boolean = false;

  constructor( fs?: {id?: string, name: string, address: string} | FileSystem ) {
    super();

    if (fs) {
      if (fs.id !== undefined) {
        this.id = fs.id;
      }

      this.name = fs.name;
      this.address = fs.address;

      if ( 'enabled' in fs) {
        this.enabled = fs.enabled;
      }
      if ( 'readPerm' in fs) {
        this.readPerm = fs.readPerm;
      }
      if ( 'writePerm' in fs) {
        this.writePerm = fs.writePerm;
      }
      if ( 'executePerm' in fs) {
        this.executePerm = fs.executePerm;
      }
    }
  }

  getID(): string {
    return this.id;
  }

  getDatasetName(): string {
    return DatasetNames.FILE_SYSTEMS;
  }

  formatPerms() {
    return (this.readPerm ? " R " : "") + (this.writePerm ? " W " : "") + (this.executePerm ? " X " : "");
  }
}
