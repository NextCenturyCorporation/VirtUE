import { Injector, Injectable } from '@angular/core';
import { AsyncPipe, JsonPipe } from '@angular/common';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';

import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Item } from '../models/item.model';
import { MessageService } from './message.service';

/**
 * #uncommented
 */
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * #uncommented
 * @class
 * @extends
 */
@Injectable()
export class ItemService {

  /** #uncommented */
  private baseUrl: string;
  // private restApi = './assets/json/virtue_list.json';

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(
    private httpClient: HttpClient,
    private messageService: MessageService,
  ) {}

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public getItems(configUrl: string): Observable<Item[]> {
    let src = this.baseUrl + configUrl;
    return this.httpClient.get<Item[]>(src).catch(this.errorHandler);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public getItem(configUrl: string, id: string): Observable<any> {
    let url = this.baseUrl + configUrl + id;
    return this.httpClient.get<Item>(url).catch(this.errorHandler);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public createItem(configUrl: string, itemData: string): Observable<any> {
    let url = this.baseUrl + configUrl;

    return this.httpClient.post(url, itemData, httpOptions).catch(this.errorHandler);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  /**
   * This works correctly, using promises. Everything else should be changed eventually to use them,
   * instead of subscriptions we only use once.
   * See https://codecraft.tv/courses/angular/http/http-with-promises/
   */
  public deleteItem(configUrl: string, id: string): Promise<any> {

    let url = this.baseUrl + configUrl + id;

    console.log('Deleting item at:', url);

    return this.httpClient.delete(url).toPromise().catch(this.errorHandler);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public updateItem(configUrl: string, id: string, itemData: string): Observable<any> {
    let url = this.baseUrl + configUrl + id;
    // console.log("updateItem");

    return this.httpClient.put(url, itemData, httpOptions).catch(this.errorHandler);

  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public toggleItemStatus(configUrl: string, id: string): Observable<any> {
    let url = this.baseUrl + configUrl + id + '/toggle';
    // console.log("toggleItemStatus");
    // console.log(url);
    return this.httpClient.get(url).catch(this.errorHandler);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  public setItemStatus(configUrl: string, id: string, newStatus: boolean): Observable<any> {
    let url = this.baseUrl + configUrl + id + '/enable';
    // console.log("setUserStatus");
    // console.log(url);
    return this.httpClient.post(url, newStatus).catch(this.errorHandler);
  }

  /**
   * #uncommented
   // TODO Is this set up correctly, how does it work, and do we need it?
   // If yes then, should '.catch(this.errorHandler);' be appended to all requests?
   // Did so anyway.
   * @return ?
   *
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
   errorHandler(error: HttpErrorResponse): Observable<any> {
     return Observable.throw(error.message || 'Server Error');
   }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  private handleError<T>(operation = 'operation', result?: T): any {
    return (error: any): Observable<T> => {

      // send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // better job of transforming error for user consumption
      this.log('operation} failed: ${error.message');

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  /** Log a message with the MessageService */
  private log (message: string): void {
    this.messageService.add('ItemService: ' + message);
  }
}
