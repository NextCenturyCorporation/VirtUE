import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

/**
 * @class
 * This class makes a request for a local json file, holding the address it should request data from and save data to.
 * json object in that file must contain ` "virtue_server": "http://localhost:8080/" `, with the location of wherever
 * virtue-admin is running.
 */
@Injectable()
export class BaseUrlService {

  /** the path to the json file holding the base url */
  private baseUrlFilePath = './assets/json/baseUrl.json';

  /**
   * @param httpClient what makes the local query
   */
  constructor( private httpClient: HttpClient ) { }

  /**
   * Request the address of the virtue-admin server, as specified in {[[baseUrlFilePath]]}
   * @return an observable that should be a promise #TODO, from which the address for virtue_server can be gotten.
   */
  getBaseUrl(): Observable<any> {
    return this.httpClient.get(this.baseUrlFilePath);
  }

}
