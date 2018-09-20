

export class FileSysPermission {

  /** Can this FS be access at all? */
  enabled: boolean = false;
  read: boolean = false;
  write: boolean = false;
  execute: boolean = false;

  /**
   * Location is defined by the parameter, everything else defaults to false.
   */
  constructor(
    /** @param location the address of this FS, relative to something, TODO */
    public location: string
  ) {}
}
