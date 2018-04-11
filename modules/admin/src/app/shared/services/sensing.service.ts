import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { SensingModel } from '../models/sensing.model';
import { Globals } from '../globals';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class SensingService {

  constructor(
    private httpClient: HttpClient,
    private hostname: Globals
  ) { }

  private jsonfile = './assets/json/sample_data.json';
  private restApi = this.hostname.serverUrl + '/admin/sensing';

  public getList(): Observable<any> {
    return this.httpClient.get<any>(this.restApi);
  }

}
