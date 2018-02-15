import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/observable';
import { User } from '../models/user.model';
import { Virtue } from '../models/virtue.model';
import { VirtuesService } from '../services/virtues.service';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class UsersService {

  constructor(
    private http: HttpClient,
    private virtue: VirtuesService
  ) {  }

  // private jsondata = 'http://localhost:8080/admin/user/template';
  private adUsers = './assets/json/ad_users.json';
  private jsondata = './assets/json/app_users.json';

  private userVirtues = [];
  // virtueListChanged = new EventEmitter<UserVirtue[]>();

  // use this when running locally only
  private userData = [];
  //User services
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

  public addUser(user: User): Observable<User> {
    return this.http.post<User>(this.jsondata, user);
  }

  public deleteUser(user: User): Observable<User> {
    return this.http.delete<User>(`${this.jsondata}/${user.id}`);
  }

  public update(user: User): Observable<User> {
    return this.http.put<User>(`${this.jsondata}/${user.id}`,user);
  }


  // Virtue dialog service
  // public getLocalObj(objId: string): Observable<any> {
    // const data = this.userData;
    // this.virtue
    // for (var i in data) {
    //     if (data[i].id === objId) {
    //       console.log(i);
    //       return data[i];
    //       // console.log(data[i].slice());
    //       break;
    //     }
    // }
  // }
  // public addUserVirtues(userVirtue: virtue): Observable<Array<Virtue>> {
  //   // return this.http.post<User>(this.jsondata, user);
  //   this.userVirtues.push[userVirtue];
  //   console.log(userVirtue);
  // }
  //
  // public getSelectedVirtues(){
  //   return this.virtues.slice();
  // }


}
