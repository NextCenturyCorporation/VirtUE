import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { SensingModel } from '../models/sensing.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * #uncommented
 * @class
 * @extends
 */
@Injectable()
export class SensingService {

  /** #uncommented */
  baseUrl: string;
  /** #uncommented */
  private jsonfile = './assets/json/sensing.json';
  /** #uncommented */
  private configUrl = 'sensing';

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(
    private httpClient: HttpClient
  ) { }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public setBaseUrl( url: string ): void {
    this.baseUrl = url;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public getSensingLog(): Observable<any> {
    return this.httpClient.get<any>(this.baseUrl + this.configUrl);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public getStaticList(): Observable<any> {
    // console.log('using static data');
    return this.httpClient.get<any>(this.jsonfile);
  }

}
