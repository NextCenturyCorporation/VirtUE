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

  baseUrl: string;

  constructor(
    private httpClient: HttpClient
  ) { }

  private jsonfile = './assets/json/sensing.json';
  private configUrl = 'admin/sensing';

  public setBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  public getSensingLog(): Observable<any> {
    return this.httpClient.get<any>(this.baseUrl + this.configUrl);
  }

  public getStaticList(): Observable<any> {
    // console.log('using static data');
    return this.httpClient.get<any>(this.jsonfile);
  }

}
