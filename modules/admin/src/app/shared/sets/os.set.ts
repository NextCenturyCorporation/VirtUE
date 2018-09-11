
/**
 * The available OS types a vm can be instantiated with.
 * #uncommented
 * @class
 * @extends
 */
export class OSSet {
  readonly list: {prettyName: string, os: string}[] = [
    { prettyName: 'Debian', os: 'LINUX' },
    { prettyName: 'Windows', os: 'WINDOWS' }
  ];
  getList() {
    return this.list;
  }
}
