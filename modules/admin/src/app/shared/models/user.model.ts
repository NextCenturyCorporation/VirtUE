export class User {
  username: string;
  authorities: any[];
  virtueTemplateIds: any[];

  public User(
    username: string,
    authorities: any[],
    virtueTemplateIds: any[]
  ) {
    this.username = username;
    this.authorities = authorities;
    this.virtueTemplateIds = virtueTemplateIds;
  }
  constructor() { }
}
