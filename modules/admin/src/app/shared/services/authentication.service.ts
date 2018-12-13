import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';

import { BaseUrlService } from './baseUrl.service';

@Injectable()
export class AuthenticationService {
  baseUrl: string;

    constructor(
      private httpClient: HttpClient,
      private baseUrlService: BaseUrlService
    ) {
      setTimeout(() => {
        let sub = this.baseUrlService.getBaseUrl().subscribe( res => {
          this.baseUrl = res[0].virtue_server;
        }, error => {
          sub.unsubscribe();
        }, () => {
          sub.unsubscribe();
        });
      }, 100);
    }

    login(username: string, password: string) {
      // let headers = new HttpHeaders({
      //     'Accept':'text/html'
      //   });
      //
      // let params = new HttpParams();
      // params = params
      //   .set('username', username)
      //   // .set('response_type', 'code')
      //   .set('password', password)

      const httpOptions = {
        // headers: new HttpHeaders(
        //       { 'Content-Type': 'application/json',
        //         'responseType': 'json',
        //         'username': username, 'password': password
        //       })
        // headers: new HttpHeaders({ 'Content-Type': 'text/html', 'responseType': 'text' })
        headers: new HttpHeaders({ 'Content-Type': 'text/html' })
      };

      console.log(`${this.baseUrl}login`, username, password);
      // return this.httpClient.post<any>(`${this.baseUrl}login`, {  params: params, headers: headers})
      // return this.httpClient.get<string>(`${this.baseUrl}login`, httpOptions)
      return this.httpClient.post<string>(`${this.baseUrl}login`, JSON.stringify({username: username, password: password}), httpOptions)
          // .pipe((user: any) => {
          //   console.log("returned: ", user);
          //     // login successful if there's a jwt token in the response
          //     if (user && user.token) {
          //         // store user details and jwt token in local storage to keep user logged in between page refreshes
          //         localStorage.setItem('currentUser', JSON.stringify(user));
          //     }
          //
          //     return user;
          // })
        ;
    }

    logout() {
        // remove user from local storage to log user out
        localStorage.removeItem('currentUser');
    }
}
