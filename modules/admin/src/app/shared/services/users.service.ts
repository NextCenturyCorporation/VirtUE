import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { User } from '../models/user.model';
import { Virtue } from '../models/virtue.model';
import { VirtuesService } from '../services/virtues.service';
import { Globals } from '../globals';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class UsersService {

  constructor(
    private http: HttpClient,
    private virtue: VirtuesService,
    private hostname: Globals
  ) { }

  // private jsondata = 'http://localhost:8080/admin/user/template';
  private adUsers = './assets/json/ad_users.json';
  private jsondata = this.hostname.serverUrl + './admin/user';

  private userVirtues = [];

  // use this when running locally only
  private userData = [];
  // User services
  public getAdUsers(): Observable<Array<User>> {
    return this.http.get<Array<User>>(this.adUsers);
  }

  public getUsers(): Observable<Array<User>> {
    return this.http.get<Array<User>>(this.jsondata);
  }

  public getUser(id: string): Observable<User> {
    const src = `${this.jsondata}/?id=${id}`;
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
