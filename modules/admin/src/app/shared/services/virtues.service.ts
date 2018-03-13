import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Virtue } from '../models/virtue.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VirtuesService {

  // private jsondata = 'http://localhost:8080/admin/virtue/template';
  private jsondata = './assets/json/virtue_list.json';

  constructor(private httpClient: HttpClient) { }

  public getVirtues(): Observable<Array<Virtue>> {
    return this.httpClient.get<Array<Virtue>>(this.jsondata);
  }

  public getVirtue(id: string): Observable<any> {
    // const src = `${this.jsondata}/${id}`;
    const src = `${this.jsondata}/?id=${id}`;
    return this.httpClient.get<Virtue>(src);
  }

  public createVirtue(virtue: Virtue): Observable<any> {
    return this.httpClient.post(this.jsondata, virtue);
    // return this.httpClient.post<Virtue>(this.jsondata, virtue);
  }

  public updateVirtue(id: string, virtue: Virtue): Observable<any> {
    const src = `${this.jsondata}/?id=${id}`;
    return this.httpClient.put(src, virtue);
    // return this.httpClient.put<Virtue>(`${this.jsondata}/${virtue.id}`,virtue);
  }

  /**
    public deleteVirtue(virtue: Virtue): Observable<Virtue> {
      return this.httpClient.delete<Virtue>(`${this.jsondata}/${virtue.id}`);
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
