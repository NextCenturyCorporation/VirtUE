import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { SensingModel } from '../models/sensing.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class SensingService {

  private jsonfile = './assets/json/sample_data.json';
  private restApi = 'http://sensing-api.savior.internal:17141/api/v1/sensors/all/inspect';

  constructor( private httpClient: HttpClient ) { }

  getList(): Observable<any> {
    return this.httpClient.get<any>(this.restApi);
  }

}
