import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { SensingModel } from '../models/sensing.model';
import { InterceptorRemoteDestinationHeader } from './baseUrl.interceptor';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }).set(InterceptorRemoteDestinationHeader, '')
};

/**
 * @class
 * This class will query the backend (or wherever?) for sensor data, and return for processing and display.
 *
 * Will likely change drastically.
 * TODO
 * #uncommented
 */
@Injectable()
export class SensingService {

  /** #uncommented */
  baseUrl: string = "";
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
   * Don't do it this way! This class is really just a skeleton, but you should set method through httpClient's setBaseUrl
   * method, and just use relative urls to make requests. If the baseUrl is just the virtue-admin server, then you don't need
   * to set anything, because it's already been set in the main body of the code. If it isn't though, you'll need to define
   * a provider for HttpClient for just this class, so this class can use a different instance of httpclient and therefore be
   * able to set a different baseUrl without screwing other thigns up.
   * @param
   *
   * @return
   */
  public setBaseUrl( url: string ): void {
  console.log(url);
    if (url) {
      this.baseUrl = url;
    }
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public getSensingLog(): Observable<any> {
    return this.httpClient.get<any>(this.baseUrl + this.configUrl, httpOptions);
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
