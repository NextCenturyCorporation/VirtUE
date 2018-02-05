import { VirtueModel } from './virtue.model';

export class UserModel {
  id: any;
  name: string;
  ad_id: string;
  virtues: VirtueModel[];
  modifiedDate: string;
  status: boolean;

  public UserTemplate(id: any, name: string, ad_id: string, virtues: VirtueModel[], modifiedDate: string, status: boolean) {
		this.id = id;
		this.name = name;
		this.ad_id = ad_id;
		this.virtues = virtues;
		this.modifiedDate = modifiedDate;
    this.status = status;
	}
  constructor(){}
}
