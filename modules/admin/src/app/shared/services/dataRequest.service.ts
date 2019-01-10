import { Injector, Injectable } from '@angular/core';
import { AsyncPipe, JsonPipe } from '@angular/common';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';

import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { IndexedObj } from '../models/indexedObj.model';
import { MessageService } from './message.service';

import { InterceptorRemoteDestinationHeader } from './baseUrl.interceptor';

/**
 * define the html headers to go on the post requests
 */
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json'}).set(InterceptorRemoteDestinationHeader, '')
};

/**
 * @class
 * This class is what shapes the queries to the back-end for all the User/Virtue-template/VM-tempalte/Application  data.
 * The baseUrl must be set first, but after that each query takes some relevant parameters and a path, which directs which
 * location within the baseUrl should be given the request.
 */
@Injectable()
export class DataRequestService {

  /** the root URL to query - the location of our virtue-admin server. */
  private baseUrl: string = "/";

  /**
   * Just sets up those two parameters as attributes
   */
  constructor(
    /** @param httpClient injected, is what makes the http requests */
    private httpClient: HttpClient,

    /** @param messageService, injected, TODO may not be needed. Used to collect message from any part of the app. */
    private messageService: MessageService,
  ) {}

  /**
   * sets the url to use, to make requests to the virtue-admin server
   * @param url the url of the virtue-admin server
   */
  public setBaseUrl(url: string): void {
    // this.baseUrl = url;
    return;
  }

  /**
   * Request all items in the dataset at {subdomain}
   * The most commonly-used function - see [[GenericDataPageComponent.recursivePullData]]
   *
   * @param subdomain the path describing to virtue-admin what set of data we're requesting
   *
   * @return a subscription that will return a list of objects, if/when available.
   */
  public getRecords(subdomain: string): Observable<IndexedObj[]> {
    let url = this.baseUrl + subdomain;
    return this.httpClient.get<IndexedObj[]>(url, httpOptions).catch(this.errorHandler);
  }

  /**
   * Request an item with the specified ID, in the dataset at {subdomain}
   * At the moment, never used.
   * If we ever need to cut down on requests and processing, we can call this when building the top-level sets in in the user,
   * vm, and application forms instead of [[getRecords]]. That is, if we're looking at a User, request only the current User's data,
   * instead of requesting all of them and just picking out the one we want to view/edit/duplicate. Note that virtues is not
   * included in that list, because virtue's settings tab requires a list of all other virtues.
   *
   * @param subdomain the path describing to virtue-admin what set of data we're requesting
   * @param id the identifying key for the item we're requesting.
   *
   * @return a subscription that will return the requested object, if it exists.
   */
  public getRecord(subdomain: string, id: string): Observable<IndexedObj> {
    let url = this.baseUrl + subdomain + id;
    return this.httpClient.get<IndexedObj>(url, httpOptions).catch(this.errorHandler);
  }

  /**
   * @param subdomain the path describing to virtue-admin what set of data to save this item to
   * @param itemData a JSON.stringify-ed Item. Must have the attributes the backend expects to see.
   *                 Those are set in each item-form's [[finalizeItem]] method.
   *
   * @return a subscription that will return the saved object as it exists on the backend.
   */
  public createRecord(subdomain: string, itemData: string): Observable<IndexedObj> {
    let url = this.baseUrl + subdomain;
    return this.httpClient.post(url, itemData, httpOptions).catch(this.errorHandler);
  }

  /**
   * Delete the item with the given id, in the dataset at {subdomain}
   * @param subdomain the path describing to virtue-admin what set of data we're requesting to access
   * @param id the identifying key for the item to be deleted.
   *
   * @return a Promise that will return nothing
   *
   * I suspect that this will need to be removed or no longer be used.
   * It seems more advisable to keep a copy of everything, and just move it to some section for deleted Items.
   * Perhaps that region could allow deletion? There's gotta be a balance between recording everything and making
   * logs and histories to cluttered to be useful.
   *
   * Note that this works correctly, using promises. It'd be cleaner if everything else could be changed eventually to use them,
   * instead of subscriptions we only use once.
   * See https://codecraft.tv/courses/angular/http/http-with-promises/
   */
  public deleteRecord(subdomain: string, id: string): Promise<any> {

    let url = this.baseUrl + subdomain + id;

    console.log('Deleting item at:', url);

    return this.httpClient.delete(url, httpOptions).toPromise().catch(this.errorHandler);
  }

  /**
   * Update the item with the given id, in the dataset at {subdomain}
   * @param subdomain the path describing to virtue-admin what set of data we're requesting to access
   * @param id the identifying key for the item to be updated.
   * @param itemData a JSON.stringify-ed Item, to be saved to the backend.
   *                 Must have the attributes the backend expects to see. Those are set in each item-form's [[finalizeItem]] method.
   *
   * @return a subscription that will return the updated object as it exists on the backend.
   *
   */
  public updateRecord(subdomain: string, id: string, itemData: string): Observable<IndexedObj> {
    let url = this.baseUrl + subdomain + id;
    return this.httpClient.put(url, itemData, httpOptions).catch(this.errorHandler);
  }

  /**
   * Set the status of the item with the given id, in the dataset at {subdomain}, to the given value.
   *
   * @param subdomain the path describing to virtue-admin what set of data we're requesting to access
   * @param id the identifying key for the item to be enabled/disabled.
   * @param newStatus the new true/false status this item should have.
   *
   * @return a subscription that will return the updated object as it exists on the backend.
   */
  public setRecordAvailability(subdomain: string, id: string, newStatus: boolean): Observable<IndexedObj> {
    let url = this.baseUrl + subdomain + id + '/setStatus';

    return this.httpClient.put(url, newStatus, httpOptions).catch(this.errorHandler);
  }

  /**
   * #uncommented
   * TODO Is this set up correctly, how does it work, and do we need it?
   * If yes then, should '.catch(this.errorHandler);' be appended to all requests?
   * Did so anyway.
   *
   * @return an observable that does something. Figure this out when implementing error handling
   *
   */
   errorHandler(error: HttpErrorResponse): Observable<any> {
     return Observable.throw(error.message || 'Server Error');
   }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed.
   * @param result - optional value to return as the observable result TODO improve this when fixing error handling
   *
   * @return an observable that logs error messages when they happen, and then returns/passes along the original
   *         return value.
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
   * Log a message with the MessageService - called on errors.
   *
   * @param massage the string to be saved to the log
   */
  private log (message: string): void {
    this.messageService.add('DataRequestService: ' + message);
  }
}
