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

  private jsonfile = './assets/json/baseUrl.json';

  constructor( private httpClient: HttpClient ) { }

  getBaseUrl() {
    return this.httpClient.get(this.jsonfile);
  }

}
