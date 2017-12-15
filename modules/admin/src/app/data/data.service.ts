import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()

export class DataService {

  //private jsonfile: string = './assets/json/example.json';
  private jsonfile: string = './assets/json/sample_data.json';
  constructor( private http: Http ){}

  getData() : Observable<any> {
    return this.http.get(this.jsonfile)
      .map( (res: Response) => {
        const data = res.json();
        return data;
      })
      .catch( (error:any) => {
        return Observable.throw('Oops. There is a problem getting the data');
      })
  }
}
