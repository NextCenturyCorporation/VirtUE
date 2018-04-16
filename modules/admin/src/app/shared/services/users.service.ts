import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { User } from '../models/user.model';
import { Globals } from '../globals';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class UsersService {

  private subscription: Subscription;
  private configUrl = 'admin/user';
  private restApi = './assets/json/users.json';


  constructor(
    private http: HttpClient,
    private hostname: Globals
  ) {
  }

  // User services
  public getUsers( baseUrl: string ): Observable<Array<User>> {
    let awsServer = baseUrl + this.configUrl;
    return this.http.get<Array<User>>(awsServer);
  }

  public getUser(id: string): Observable<User> {
    const src = `${this.restApi}/?id=${id}`;
    return this.http.get<User>(src);
  }
/*
  public addUser(user: User): Observable<User> {
    return this.http.post<User>(this.jsondata, user);
  }

  public deleteUser(user: User): Observable<User> {
    return this.http.delete<User>(`${this.jsondata}/${user.id}`);
  }

  public update(user: User): Observable<any> {
    return this.http.put<User>(`${this.jsondata}/${user.id}`,user);
  }*/


}
