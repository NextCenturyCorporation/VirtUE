import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()

export class DataService {

  private jsonfile: string = './assets/json/sample_data.json';
  constructor( private http: Http ){}

  getData() : Observable<any> {
    return this.http.get(this.jsonfile)
      .map((res: Response) => res.json())
      .catch((error:any) => Observable.throw(error.json().error || 'Server error'));;
  }
}

export class PagingService {
  getResults() {}

}
