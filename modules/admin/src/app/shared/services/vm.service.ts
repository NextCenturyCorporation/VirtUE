import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpHeaders, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';

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
  }

  public updateVM(baseUrl: string, id: string, vmData: any) {
    let url = baseUrl + this.configUrl + id;
    return this.httpClient.put(url, vmData, httpOptions)
           .toPromise().then(data => {
             return data;
           }, error => {
             console.log(error);
           });
  }

/**

  public deleteVirtue(virtue: Virtue): Observable<Virtue> {
    return this.httpClient.delete<Virtue>('this.jsondata}/${virtue.id');
  }
*/
}
