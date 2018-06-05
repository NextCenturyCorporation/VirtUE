import { EventEmitter, Injectable, OnDestroy } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable } from 'rxjs/Observable';
import { User } from '../models/user.model';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()

export class UsersService {

  private configUrl = 'admin/user/';

  constructor( private httpClient: HttpClient ) {}

  // Get all users
  public getUsers( baseUrl: string ): Observable<any> {
    let awsServer = baseUrl + this.configUrl;
    return this.httpClient.get<any>(awsServer);
  }

  getUser(baseUrl: string, id: string): Observable<any> {
    let src = baseUrl + this.configUrl + id;
    // console.log(src);
    return this.httpClient.get<any>(src);
  }

  createUser( baseUrl: string, userData: any ): Observable<any> {
    let awsServer = baseUrl + this.configUrl;
    let newUser = userData;
    if (userData) {
      // console.log('Posting user: ' + newUser);
      return this.httpClient.post(awsServer, newUser, httpOptions);
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
    console.log('assignVirtues => ');
    console.log(username);
    console.log(virtue);

    let userRecord = baseUrl + this.configUrl + username + '/assign/' + virtue;
    return this.httpClient.post(userRecord, virtue, httpOptions).toPromise().then(data => {
      return true;
    },
    error => {
      console.log(error + ': users.service.ts (assignVirtues): looks like there\'s a problem posting virtue ' + virtue);
    });
  }

  deleteUser(baseUrl: string, username: string) {
    let awsServer = baseUrl + this.configUrl;
    return this.httpClient.delete(awsServer + username).subscribe(
    data => {
      return true;
    },
    error => {
      console.error('Error');
    });
  }

}
