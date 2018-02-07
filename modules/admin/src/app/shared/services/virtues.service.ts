import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { VirtueModel } from '../models/virtue.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class VirtuesService {

  // private jsondata = 'http://localhost:8080/admin/virtue/template';
  private jsondata = './assets/json/virtue_list.json';

  constructor( private httpClient: HttpClient ) {  }

  public getVirtues(): Observable<Array<VirtueModel>> {
    return this.httpClient.get<Array<VirtueModel>>(this.jsondata);
  }

  public getVirtue(id: string): Observable<VirtueModel> {
    // return this.httpClient.get<VirtueModel>(`${this.jsondata}/${id}`);
    const url = `${this.jsondata}/${id}`;
    return this.httpClient.get<VirtueModel>(url).pipe(
      tap(_ => console.log(`fetched virtue id=${id}`)),
      catchError(this.handleError<VirtueModel>(`getVirtue id=${id}`))
    );
  }

  // public createVirtue(virtue: any[]): Observable<VirtueModel> {
  //   return this.httpClient.post<VirtueModel>(this.jsondata, virtue);
  // }

  public createVirtue(virtue: VirtueModel) {
    return this.httpClient.post( this.jsondata, virtue );
  }
/**
  public deleteVirtue(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.delete<VirtueModel>(`${this.jsondata}/${virtue.id}`);
  }

  public update(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.put<VirtueModel>(`${this.jsondata}/${virtue.id}`,virtue);
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
