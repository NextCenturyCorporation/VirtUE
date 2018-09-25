
/**
 * @class
 * This class represents a file system, which a virtue may be given various forms of access to.
 *
 * Used in [[VirtueSettingsTabComponent]] and [[Virtue]]
 */
export class FileSysPermission {

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
  ) {}
}
