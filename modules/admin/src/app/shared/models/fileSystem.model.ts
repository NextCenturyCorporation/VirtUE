import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';
import { Toggleable } from './toggleable.interface';

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
 * Used in [[ConfigFileSysTabComponent]] [[VirtueSettingsTabComponent]] and [[Virtue]]
 */
export class FileSystem extends IndexedObj implements Toggleable {

  /** the id given by the backend */
  id: string;

  /** Can this FS be accessed at all? */
  enabled: boolean = false;

  /** Can the virtue read data on this FS? */
  read: boolean = false;

  /** Can the virtue write data to this FS? */
  write: boolean = false;

  /** Can the virtue execute files on this FS?
   * TODO on, or from? The 2x2 of executable source and execution location*/
  execute: boolean = false;

  /**
   * Location is defined by the parameter, everything else defaults to false.
   */
  constructor(
    /** @param location the address of this FS, relative to something, TODO */
    public location: string
  ) {
    super();
    this.id = "location" + "12345";
  }

  /** #uncommented */
  getID(): string {
    return this.id;
  }

}
