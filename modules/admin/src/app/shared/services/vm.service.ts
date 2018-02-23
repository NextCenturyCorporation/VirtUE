import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { VirtualMachine } from '../models/vm.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class VirtualMachineService {

  // private jsondata = 'http://localhost:8080/admin/virtualMachine/template';
  private jsondata = './assets/json/vm_list.json';

  constructor( private httpClient: HttpClient ) {  }

  public getVmList(): Observable<Array<VirtualMachine>> {
    return this.httpClient.get<Array<VirtualMachine>>(this.jsondata);
  }

  public getVM(id: string): Observable<any> {
    const src = `${this.jsondata}/${id}`;
    return this.httpClient.get<VirtualMachine>(src);
  }

  // public createVirtue(virtue: any[]): Observable<Virtue> {
  //   return this.httpClient.post<Virtue>(this.jsondata, virtue);
  // }

  public createVirtue(vm: VirtualMachine) {
    return this.httpClient.post( this.jsondata, vm );
  }
/**
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
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      // this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
