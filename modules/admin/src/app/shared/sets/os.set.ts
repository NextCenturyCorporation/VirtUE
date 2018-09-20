// The available OS types a vm can be instantiated with.
export class OSSet {
  /** a static list of OS's that VM's can be set up to use. */
  readonly list: OS[] = [
    { prettyName: 'Debian Linux', os: 'LINUX' },
    { prettyName: 'Windows', os: 'WINDOWS' }
  ];

  /**
   * @return the static list of available operating systems
   */
  getList(): OS[] {
    return this.list;
  }
}

/** @class
 * represents an operating system. Just the name though.
 */
export class OS {
  /** What the OS should be labelled when displayed to the user */
  prettyName: string;
  /** whatever string is convenient for representing that OS on the back-end */
  os: string;
}
