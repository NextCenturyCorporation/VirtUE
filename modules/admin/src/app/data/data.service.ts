import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()

export class DataService {
  // DATA SOURCES
  private virtueList : string = './assets/json/virtue_list.json';
  private vmList : string = './assets/json/vm_list.json';
  private activeDirectory : string = './assets/json/ad_users.json';
  private appUsers : string = './assets/json/app_users.json';
  private dashboardData : string = './assets/json/sample_data.json';
  private jsonfile: string;
  // private jsonfile: string = './assets/json/sample_data.json';
  constructor( private http: Http ){}

  getDataSrc(dataSrc) {
    switch(dataSrc) {
    case 'virtues':
        return this.jsonfile = this.virtueList;
        break;
    case 'vms':
        return this.jsonfile = this.vmList;
        break;
    case 'adUsers':
        return this.jsonfile = this.activeDirectory;
        break;
    case 'app-users':
        return this.jsonfile = this.appUsers;
        break;
    case 'dashboard':
        return this.jsonfile = this.dashboardData;
        break;
    default:
        return this.jsonfile = './assets/json/sample_data.json';
    }
    console.log(data);
  }

  getData(jsonDataSrc) : Observable<any> {
    this.getDataSrc(jsonDataSrc);
    return this.http.get(this.jsonfile)
      .map( (res: Response) => {
        const data = res.json();
        return data;
      })
      .catch( (error:any) => {
        return Observable.throw('Oops. There is a problem getting the data ');
      })
  }
}
