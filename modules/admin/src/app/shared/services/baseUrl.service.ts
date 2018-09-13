import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * #uncommented
 * @class
 * @extends
 */
@Injectable()
export class BaseUrlService {

  /** #uncommented */
  private jsonfile = './assets/json/baseUrl.json';

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor( private httpClient: HttpClient ) { }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getBaseUrl(): Observable<any> {
    return this.httpClient.get(this.jsonfile);
  }

}
