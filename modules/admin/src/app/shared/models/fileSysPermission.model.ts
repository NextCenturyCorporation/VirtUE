

export class FileSysPermission {
  location: string;
  enabled: boolean = false;
  read: boolean = false;
  write: boolean = false;
  execute: boolean = false;

  constructor(loc: string) {
    this.location = loc;
  }
}
