import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/observable';
import { VirtueModel } from '../models/virtue.model';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class VirtuesService {
  // DATA SOURCES

  // private readonly jsonfile = 'http://localhost:8080/admin/virtue/template';
  private jsonfile = './assets/json/virtue_list.json';

  constructor(
    private httpClient: HttpClient
  ) {  }

  public create(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.post<VirtueModel>(this.jsonfile, virtue);
  }

  public delete(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.delete<VirtueModel>(`${this.jsonfile}/${virtue.id}`);
  }

  public get(id: string): Observable<VirtueModel> {
    return this.httpClient.get<VirtueModel>(`${this.jsonfile}/${id}`);
  }

  public list(): Observable<Array<VirtueModel>> {
    return this.httpClient.get<Array<VirtueModel>>(this.jsonfile);
  }

  public update(virtue: VirtueModel): Observable<VirtueModel> {
    return this.httpClient.put<VirtueModel>(`${this.jsonfile}/${virtue.id}`,virtue);
  }
}
