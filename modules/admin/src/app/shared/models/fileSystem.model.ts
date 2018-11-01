import { DictList } from './dictionary.model';
import { IndexedObj } from './indexedObj.model';

/**
 * @class
 * This class represents a file system, which a virtue may be given various forms of access to.
 *
 * Used in [[VirtueSettingsTabComponent]] and [[Virtue]]
 */
export class FileSystem extends IndexedObj {

  /** the id given by the backend */
  id: string;

  /** Can this FS be access at all? */
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
    super("location" + "12345");
  }

  /** #uncommented */
  getID(): string {
    return this.id;
  }

  /** doesn't depend on anything else, and so nothing needs to be built */
  buildAttributes(childDatasets: DictList<(DictList<IndexedObj>)> ): void {}

}
