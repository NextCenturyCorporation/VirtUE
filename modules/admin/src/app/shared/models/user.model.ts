import { Virtue } from './virtue.model';

export class User {
  id: string;
  name: string;
  ad_id: string;
  virtues: Virtue[];
  modifiedDate: string;
  status: boolean;

  public UserTemplate(
    id: string,
    name: string,
    ad_id: string,
    virtues: Virtue[],
    modifiedDate: string,
    status: boolean
  ) {
		this.id = id;
		this.name = name;
		this.ad_id = ad_id;
		this.virtues = virtues;
		this.modifiedDate = modifiedDate;
    this.status = status;
	}
  constructor(){}
}
