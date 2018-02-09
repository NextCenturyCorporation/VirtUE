import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

import { Application } from '../models/application.model';
import { VirtualMachine } from '../models/vm.model';

const httpHeader = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class VirtualMachineService {

  private jsondata = 'http://localhost:8080/admin/virtualMachine/template';
  // private jsondata = './assets/json/vm_list.json';

  constructor( private httpClient: HttpClient ) {  }

  public getVmList(): Observable<Array<VirtualMachine>> {
    return this.httpClient.get<Array<VirtualMachine>>(this.jsondata);
    console.log(this.jsondata);
  }

  public getVm(id: string): Observable<any> {
    const src = `${this.jsondata}/${id}`;
    return this.httpClient.get<VirtualMachine>(src);
  }

  public getApps(id: string): Observable<any> {
    const src = `${this.jsondata}/${id}`;
    return this.httpClient.get<Application>(src);
  }
  // public createVirtue(vm: ApplicationModel): Observable<ApplicationModel> {
  //   return this.httpClient.post<ApplicationModel>(this.jsondata, vm);
  // }

  public createVM(vm: VirtualMachine) {
    return this.httpClient.post( this.jsondata, vm );
  }

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
