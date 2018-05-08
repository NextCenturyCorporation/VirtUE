import { Injector, Injectable } from '@angular/core';
import { AsyncPipe, JsonPipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Virtue } from '../models/virtue.model';
import { MessageService } from './message.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VirtuesService {

  private configUrl = 'admin/virtue/template/';
  // private restApi = './assets/json/virtue_list.json';

  constructor(
    private httpClient: HttpClient,
    private messageService: MessageService
  ) {}

  getVirtues(baseUrl: string): Observable<Virtue[]> {
    let src = `${baseUrl + this.configUrl}`;
    return this.httpClient.get<Virtue[]>(src);
      // .pipe(
      //   tap(virtues => this.log(`fetched virtues`)),
      //   catchError(this.handleError('getVirtues', []))
      // );
  }

  public getVirtue(baseUrl: string, id: string): Observable<any> {
    let url = `${baseUrl + this.configUrl}/${id}`;
    return this.httpClient.get<Virtue>(url);
  }

  public createVirtue(baseUrl: string, virtueData: any): Observable<any> {
    let url = `${baseUrl + this.configUrl}`;
    if (virtueData) {
      console.log(virtueData);
      return this.httpClient.post(url, virtueData, httpOptions);
    } else {
      console.log('Sadness, there was a problem creating this virtue:');
      console.log(virtueData);
    }
  }

  public deleteVirtue(baseUrl: string, id: string) {
    let url = baseUrl + this.configUrl + id;
    console.log('Deleting... ' + url);

    return this.httpClient.delete(url).toPromise().then(
      data => {
        return true;
      },
      error => {
      console.log(error.message);
    });
  }
/**

  public updateVirtue(id: string, virtue: Virtue): Observable<any> {
    const src = `${this.restApi}/?id=${id}`;
    return this.http.put(src, virtue);
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

      // send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // better job of transforming error for user consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
  /** Log a HeroService message with the MessageService */
  private log (message: string) {
    this.messageService.add('VirtueService: ' + message);
  }
}
