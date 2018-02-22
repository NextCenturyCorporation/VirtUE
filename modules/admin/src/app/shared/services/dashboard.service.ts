import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class DashboardService {

  private jsonfile = './assets/json/sample_data.json';

  constructor( private httpClient: HttpClient ) {  }

  getList(): Observable<any> {
    return this.httpClient.get<any>(this.jsonfile);
  }

}
