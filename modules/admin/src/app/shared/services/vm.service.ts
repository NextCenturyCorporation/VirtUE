import { Injectable } from '@angular/core';
<<<<<<< HEAD
import { HttpClient, HttpHeaders } from '@angular/common/http';
=======
import { HttpClient, HttpEvent, HttpHeaders, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
>>>>>>> VRTU-349

import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import { ISubscription } from 'rxjs/Subscription';

import { VirtualMachine } from '../models/vm.model';
import { MessageService } from './message.service';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VirtualMachineService {

  private configUrl = 'admin/virtualMachine/template/';

  constructor(
    private httpClient: HttpClient
   ) {  }

  public getVmList(baseUrl: string): Observable<any> {
    let src = baseUrl + this.configUrl;
    return this.httpClient.get<any>(src);
  }

  public getVM(baseUrl: string, id: string): Observable<any> {
    let src = baseUrl + this.configUrl + id;
    return this.httpClient.get<VirtualMachine>(src);
  }

  public createVM(baseUrl: string, vmData: any) {
    let url = baseUrl + this.configUrl;
    console.log('createVM() => ');
    console.log(vmData);
    return this.httpClient.post(url, vmData, httpOptions)
           .toPromise().then(data => {
             return data;
           }, error => {
             console.log(error);
           });

  updateStatus(baseUrl: string, id: string, isEnabled: boolean): Observable<VirtualMachine> {
    let url = baseUrl + this.configUrl + id;
    console.log(url);
    let body = {
      "enabled": isEnabled
    };
    console.log(body);
    return this.httpClient.put<VirtualMachine>(url, JSON.stringify(body), httpOptions);
  }

/**
  public update(virtue: Virtue): Observable<Virtue> {
    return this.httpClient.put<Virtue>('this.jsondata}/${virtue.id',virtue);
  }
  public deleteVirtue(virtue: Virtue): Observable<Virtue> {
    return this.httpClient.delete<Virtue>('this.jsondata}/${virtue.id');
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
      console.error(error.message); // log to console instead

      // TODO: better job of transforming error for user consumption
      // this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
