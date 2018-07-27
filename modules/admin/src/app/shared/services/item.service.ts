import { Injector, Injectable } from '@angular/core';
import { AsyncPipe, JsonPipe } from '@angular/common';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';

import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/catch';
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

export class ItemService {

  private configUrl = 'admin/virtue/template/';
  // private restApi = './assets/json/virtue_list.json';

  constructor(
    private httpClient: HttpClient,
    private messageService: MessageService
  ) {}

  getItems(baseUrl: string): Observable<Virtue[]> {
    let src = baseUrl + this.configUrl;
    // console.log('getVirtueList() => ');
    // console.log(src);
    return this.httpClient.get<Virtue[]>(src);
      // .pipe(
      //   tap(virtues => this.log(`fetched virtues`)),
      //   catchError(this.handleError('getVirtues', []))
      // );
  }

  public getVirtue(baseUrl: string, id: string): Observable<any> {
    let url = baseUrl + this.configUrl + id;
    // console.log('getVirtue() => ');
    // console.log(url);
    return this.httpClient.get<Virtue>(url);
  }

  public createVirtue(baseUrl: string, virtueData: any): Observable<any> {
    // console.log('createVirtue() => ' + baseUrl);
    let url = baseUrl + this.configUrl;
    // console.log('createVirtue() => ');
    // console.log(url);
    console.log(virtueData);
    // return "";
    return this.httpClient.post(url, virtueData, httpOptions);
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

  public updateVirtue(baseUrl: string, id: string, virtueData: any) {
    let url = baseUrl + this.configUrl + id;
    // console.log("updateVirtue");
    // console.log(url);
    return this.httpClient.put(url, virtueData, httpOptions)
            .catch(this.errorHandler);
  }

  toggleVirtueStatus(baseUrl: string, id: string) {
    let url = baseUrl + this.configUrl + id + '/toggle';
    // console.log("toggleVirtueStatus");
    // console.log(url);
    return this.httpClient.get(url);
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @ param operation - name of the operation that failed
   * @ param result - optional value to return as the observable result
   */
   errorHandler(error: HttpErrorResponse) {
     return Observable.throw(error.message || 'Server Error');
   }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // better job of transforming error for user consumption
      this.log('operation} failed: ${error.message');

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
  /** Log a HeroService message with the MessageService */
  private log (message: string) {
    this.messageService.add('VirtueService: ' + message);
  }
}
