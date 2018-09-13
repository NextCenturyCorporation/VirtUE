import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * #uncommented
 * @class
 * @extends
 */
@Injectable()
export class DashboardService {

  /** #uncommented */
  private jsonfile = './assets/json/sensing.json';

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
  getList(): Observable<any> {
    return this.httpClient.get<any>(this.jsonfile);
  }

}
