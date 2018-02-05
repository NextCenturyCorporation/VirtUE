import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/observable';
import { UserModel } from '../models/user.model';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class UsersService {

  selectedUser = new EventEmitter<UserModel>();

  // private jsondata = 'http://localhost:8080/admin/user/template';
  private adUsers = './assets/json/ad_users.json';
  private jsondata = './assets/json/app_users.json';

  constructor(
    private httpClient: HttpClient
  ) {  }

  public getAdUsers(): Observable<Array<UserModel>> {
    return this.httpClient.get<Array<UserModel>>(this.adUsers);
  }

  public createUser(user: UserModel): Observable<UserModel> {
    return this.httpClient.post<UserModel>(this.jsondata, user);
  }

  public deleteUser(user: UserModel): Observable<UserModel> {
    return this.httpClient.delete<UserModel>(`${this.jsondata}/${user.id}`);
  }

  public getUser(id: string): Observable<UserModel> {
    return this.httpClient.get<UserModel>(`${this.jsondata}/${id}`);
  }

  public listUsers(): Observable<Array<UserModel>> {
    return this.httpClient.get<Array<UserModel>>(`${this.jsondata}`);
  }

  public update(user: UserModel): Observable<UserModel> {
    return this.httpClient.put<UserModel>(`${this.jsondata}/${user.id}`,user);
  }

}
