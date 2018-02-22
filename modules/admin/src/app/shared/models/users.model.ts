export class Users {
  username: string;
  authorities: any[];

  public UserTemplate(
    username: string,
    authorities: any[]
  ) {
      this.username = username;
      this.authorities = authorities;
	   }
  constructor() {}
}
