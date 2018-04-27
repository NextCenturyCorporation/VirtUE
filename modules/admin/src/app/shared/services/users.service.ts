import { EventEmitter, Injectable } from '@angular/core';
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
  private restApi = './assets/json/users.json';

  constructor( private httpClient: HttpClient ) {}

  // Get all users
  public getUsers( baseUrl: string ): Observable<any> {
    let awsServer = baseUrl + this.configUrl;
    return this.httpClient.get<any>(awsServer);
  }

  getUser(baseUrl: string, id: string): Observable<any> {
    let src = baseUrl + this.configUrl + id;
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

  deleteUser(baseUrl: string, username: string) {
    let awsServer = baseUrl + this.configUrl;
    console.log('Delete: ' + awsServer + username);
    return this.httpClient.delete(awsServer + username).subscribe(
      data => console.log(data),
      error => console.error('Error')
    );
  }

/*
  public update(user: User): Observable<any> {
    return this.http.put<User>(`${this.jsondata}/${user.id}`,user);
  }*/


}
