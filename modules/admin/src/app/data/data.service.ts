import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import 'rxjs/add/operator/map';

@Injectable()

export class DataService {

  private jsonfile: string = './assets/json/sample_data.json';
  constructor( private http: Http ){}

  getData() {
    return this.http.get(this.jsonfile)
    .map((res: Response) => res.json());
  }
}
