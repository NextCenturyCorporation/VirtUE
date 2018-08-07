import { EventEmitter, Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { User } from '../models/user.model';
import { Item } from '../models/item.model';
import { DictList } from '../models/dictionary.model';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class UsersService {

  private configUrl = 'admin/user/';

  constructor( private httpClient: HttpClient ) {}

  // Get all users
  getUsers(baseUrl: string): Observable<User[]> {
    let awsServer = baseUrl + this.configUrl;
    return this.httpClient.get<User[]>(awsServer);
  }
  // pullUserData( baseUrl: string ): Observable<any> {
  //
  //   let awsServer = baseUrl + this.configUrl;
  //   return this.httpClient.get<User[]>(awsServer);
  // }
  // getUsers(baseUrl: string): Observable<DictList<User>> {
  //   let usersDict = new DictList<User>();
  //   this.pullUserData(baseUrl).subscribe(users => {
  //     console.log(users.length);
  //     let user: User = null;
  //     for (let u of users) {
  //       user = new User(u);
  //       usersDict.add(user.getID(), user);
  //     }
  //     user = null;
  //     users = null;
  //     // console.log(usersDict);
  //     let temp = Observable.of(usersDict);
  //     console.log("leaving second", usersDict.length, temp);
  //     return temp;
  //   },
  //   error => {},
  //   () => {});
  //
  //
  //   console.log("leaving once");
  //   return Observable.of(usersDict);
  // }

  getUser(baseUrl: string, id: string): Observable<any> {
    let url = baseUrl + this.configUrl + id;
    // console.log('getUser => ');
    // console.log(url);
    return this.httpClient.get<any>(url);
  }

  createUser( baseUrl: string, userData: any ): Observable<any> {
    let url = baseUrl + this.configUrl;
    let newUser = userData;
    if (userData) {
      // console.log('Posting user: ' + newUser);
      return this.httpClient.post(url, newUser, httpOptions);
    } else {
      console.log('Sadness, there was a problem creating this user:');
      console.log(newUser);
    }
  }

  /*
  public update(user: User): Observable<any> {
    return this.http.put<User>(`${this.jsondata}/${user.id}`,user);
  }*/

  assignVirtues(baseUrl: string, username: string, virtue: string) {
    // console.log('assignVirtues => ');
    // console.log(username);
    // console.log(virtue);

    let userRecord = baseUrl + this.configUrl + username + '/assign/' + virtue;
    // console.log(userRecord);
    return this.httpClient.post(userRecord, virtue, httpOptions).toPromise().then(data => {
      return true;
    },
    error => {
      console.log(error + ': users.service.ts (assignVirtues): looks like there\'s a problem posting virtue ' + virtue);
    });
  }

  public updateUser(baseUrl: string, username: string, userData: any) {
    let url = baseUrl + this.configUrl + username;
    // console.log("updateUser");
    // console.log(url);
    return this.httpClient.put(url, userData, httpOptions);
  }

  //doesn't appear that enabling users works.
  //see backend files at:
  //  src/main/java/com/ncc/savior/virtueadmin/service/AdminService.java:325
  //  src/main/java/com/ncc/savior/virtueadmin/data/jpa/SpringJpaUserManager.java:71
  public setUserStatus(baseUrl: string, username: string, newStatus: string): Observable<any> {
    let url = baseUrl + this.configUrl + username + '/enable';
    // console.log("setUserStatus");
    console.log(url);
    return this.httpClient.post(url, newStatus);
  }


  deleteUser(baseUrl: string, username: string) {
    let url = baseUrl + this.configUrl;
    // console.log("deleteUser");
    // console.log(url);
    return this.httpClient.delete(url + username).subscribe(
    data => {
      console.log(data);
      return true;
    },
    error => {
      console.error('Error');
    });
  }

}
