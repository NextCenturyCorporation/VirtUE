import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Params } from '@angular/router';

import { Location } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Application } from '../models/application.model';
import { Globals } from '../globals';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VmAppsService {
  restApi: string;
  constructor(
    private httpClient: HttpClient,
    private hostname: Globals
   ) {  }

  private getBaseUrl() {
   let baseUrl = './assets/json/baseUrl.json';
   // let jsondata = './assets/json/vm_apps.json';
   return this.httpClient.get(baseUrl);
  }

  public getAppsList(): Observable<Application[]> {
    this.getBaseUrl().subscribe( resp => {
      let info = resp[0].url;
      this.restApi = `${info}admin/application`;
      console.log(this.restApi);
    });
    return this.httpClient.get<Application[]>(this.restApi);
  }

  public getApp(id: string): Observable<Application[]> {
    const src = `${this.restApi}/?id=${id}`;

    // console.log('getApp ID: ' + id + ' @ ' + src);

    return this.httpClient.get<Application[]>(src);
  }

  public addApp(vm: Application) {
    return this.httpClient.post( this.restApi, vm );
  }
  public updateStatus(id: string): Observable<any> {
    const src = `${this.restApi}`;
    // /?id=${id}
    return this.httpClient.get<Application[]>(src);

    // return this.httpClient.put<Application[]>(src, JSON.stringify(app));
  }
  /**
  public updateStatus(id: string, app: Application): Observable<any> {
    return this.httpClient.put(`${this.restApi}?id=${id}`,app);
  }

  public deleteVirtue(virtue: Virtue): Observable<Virtue> {
    return this.httpClient.delete<Virtue>(`${this.restApi}/${virtue.id}`);
  }
  */

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @ param operation - name of the operation that failed
   * @ param result - optional value to return as the observable result
   */
  private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      // this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
