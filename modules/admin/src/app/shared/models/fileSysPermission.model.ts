
/**
 * #uncommented
 * @class
 * @extends
 */
export class FileSysPermission {

  /** #uncommented */
  location: string;

  /** #uncommented */
  enabled: boolean = false;

  /** #uncommented */
  read: boolean = false;

  /** #uncommented */
  write: boolean = false;

  /** #uncommented */
  execute: boolean = false;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(loc: string) {
    this.location = loc;
  }
}
