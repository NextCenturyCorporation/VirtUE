
/**
 * The available OS types a vm can be instantiated with.
 * #uncommented
 * @class
 * @extends
 */
export class OSSet {
  /** #uncommented */
  readonly list: {prettyName: string, os: string}[] = [
    { prettyName: 'Debian', os: 'LINUX' },
    { prettyName: 'Windows', os: 'WINDOWS' }
  ];

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getList(): {prettyName: string, os: string}[] {
    return this.list;
  }
}
