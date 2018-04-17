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

  constructor(
    private httpClient: HttpClient
  ) { }

  private jsonfile = './assets/json/sensing.json';
  private configUrl = 'admin/sensing';

  public getList(baseUrl: string): Observable<any> {
    let src = baseUrl + this.configUrl;
    return this.httpClient.get<any>(src);
  }

  public getStaticList(): Observable<any> {
    return this.httpClient.get<any>(this.jsonfile);
  }

}
