import { Injector, Injectable } from '@angular/core';
import { AsyncPipe, JsonPipe } from '@angular/common';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Virtue } from '../models/virtue.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VirtuesService {

  baseUrl: string;
  configUrl: 'admin/virtue/template';
  restApi: './assets/json/virtue_list.json';

  constructor( private httpClient: HttpClient ) {}

  // getBaseUrl() {
  //   return this.httpClient.get(this.configUrl);
  // }

  getVirtues(): Observable<Array<Virtue>> {
    return this.httpClient.get<Array<Virtue>>(this.restApi);
  }

  public getVirtue(id: string): Observable<any> {
    // const src = `${this.jsondata}/${id}`;
    let src = `${this.restApi}/?id=${id}`;
    return this.httpClient.get<Virtue>(src);
  }

/**
  public createVirtue(virtue: Virtue): Observable<any> {
    return this.http.post(this.restApi, virtue);
  }

  public updateVirtue(id: string, virtue: Virtue): Observable<any> {
    const src = `${this.restApi}/?id=${id}`;
    return this.http.put(src, virtue);
  }

  public deleteVirtue(virtue: Virtue): Observable<Virtue> {
    return this.http.delete<Virtue>(`${this.jsondata}/${virtue.id}`);
  }
*/
  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @ param operation - name of the operation that failed
   * @ param result - optional value to return as the observable result
   */
  private handleError<T>(operation = 'operation', result?: T) {
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
