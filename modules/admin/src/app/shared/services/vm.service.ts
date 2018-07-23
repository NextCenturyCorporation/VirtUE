import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import 'rxjs/add/observable/throw';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/toPromise';
import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';

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
    // console.log('getVmList() => ');
    // console.log(src);
    return this.httpClient.get<any>(src);
  }

  public getVM(baseUrl: string, id: string): Observable<any> {
    let src = baseUrl + this.configUrl + id;
    // console.log('getVM() => ');
    // console.log(src);
    return this.httpClient.get<VirtualMachine>(src);
  }

  public createVM(baseUrl: string, vmData: any) {
    let url = baseUrl + this.configUrl;
    // console.log('createVM() => ');
    // console.log(url);
    // console.log(vmData);
    return this.httpClient.post(url, vmData, httpOptions)
           .toPromise().then(data => {
             return data;
           }, error => {
             console.log(error);
           });
 }

  public updateVM(baseUrl: string, id: string, vmData: any) {
    let url = baseUrl + this.configUrl + id;
    // console.log('updateVM() => ');
    // console.log(url);
    return this.httpClient.put(url, vmData, httpOptions)
           .toPromise().then(data => {
             return data;
           }, error => {
             console.log(error);
           });
  }

  public toggleVmStatus(baseUrl: string, id: string): Observable<any> {
    let url = baseUrl + this.configUrl + id + '/toggle';
    console.log('toggleVmStatus() => ');
    console.log(url);
    return this.httpClient.get(url);
  }

  public updateVmStatus(baseUrl: string, id: string): Observable<any> {
    let url = baseUrl + this.configUrl + id;
    console.log('updateVmStatus() => ');
    console.log(url);
    return this.httpClient.get(url);
  }

  public deleteVM(baseUrl: string, id: string) {
    let url = baseUrl + this.configUrl + id;
    console.log('deleteVM() => ');
    return this.httpClient.delete(url)
    .toPromise().then(data => {
       return data;
     }, error => {
       console.log(error);
     });
  }
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
