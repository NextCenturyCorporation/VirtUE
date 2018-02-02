import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/observable';
import { VirtueModel } from '../models/virtue.model';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class VirtuesService {

  statusUpdated = new EventEmitter<string>();

  // private jsondata = 'http://localhost:8080/admin/virtue/template';
  private jsondata = './assets/json/virtue_list.json';

  constructor(
    private httpClient: HttpClient
  ) {  }

  public createVirtue(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.post<VirtueModel>(this.jsondata, virtue);
  }

  public deleteVirtue(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.delete<VirtueModel>(`${this.jsondata}/${virtue.id}`);
  }

  public getVirtue(id: string): Observable<VirtueModel> {
    return this.httpClient.get<VirtueModel>(`${this.jsondata}/${id}`);
    // const virtue;
    // return this.httpClient.get<VirtueModel>(`${this.jsondata}`)
    //   .map(res => virtue = res);
  }
  // public getVirtue(id: string): Observable<VirtueModel> {
  //   return this.httpClient.get<VirtueModel>(this.jsondata,{
  //     params: new HttpParams().set("id", id)
  //   });
  // }


  public listVirtues(): Observable<Array<VirtueModel>> {
    return this.httpClient.get<Array<VirtueModel>>(`${this.jsondata}`);
  }

  public update(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.put<VirtueModel>(`${this.jsondata}/${virtue.id}`,virtue);
  }
}
