import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { VirtualMachine } from '../models/vm.model';
import { Globals } from '../globals';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VirtualMachineService {

  private configUrl = 'admin/virtualMachine/template/';
  // private restApi = './assets/json/vm_list.json';

  constructor(
    private httpClient: HttpClient,
    private hostname: Globals
   ) {  }

  public getVmList(baseUrl: string): Observable<VirtualMachine[]> {
    let src = baseUrl + this.configUrl;
    return this.httpClient.get<VirtualMachine[]>(src);
  }

  public getVM(baseUrl: string, id: string): Observable<any> {
    let src = `${baseUrl + this.configUrl}/${id}`;
    return this.httpClient.get<VirtualMachine>(src);
  }

   createVirtue(baseUrl: string, id: string, vmData: any[]): Observable<any> {
    let url = baseUrl + this.configUrl;
    let body = {

    };
    return this.httpClient.post(url, body, httpOptions);
  }

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
  public createVirtue(vm: VirtualMachine) {
    let src = baseUrl + this.configUrl;
    return this.httpClient.post(src, vm );
  }

  public deleteVirtue(virtue: Virtue): Observable<Virtue> {
    return this.httpClient.delete<Virtue>(`${this.jsondata}/${virtue.id}`);
  }

    public update(virtue: Virtue): Observable<Virtue> {
      return this.httpClient.put<Virtue>(`${this.jsondata}/${virtue.id}`,virtue);
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
