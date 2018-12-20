import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
// import { map } from 'rxjs/operators';
import 'rxjs/add/operator/map'

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

      console.log(`${this.baseUrl}login`, username, password);
      // return this.httpClient.post<any>(`${this.baseUrl}login`, {  params: params, headers: headers})
      // return this.httpClient.get<string>(`${this.baseUrl}login`, httpOptions)
      return this.httpClient.post(
            `${this.baseUrl}login`,
            JSON.stringify({username: username, password: password}),
            {
              // headers: {},
              observe: 'response',
              responseType: 'text'
            }
          )
      //       .pipe((data) => {console.log(JSON.stringify(data)); return data;});
      // return this.httpClient.post<string>(`${this.baseUrl}login`, JSON.stringify({username: username, password: password}), httpOptions)
        // .map((res:any, r2:any) => {
        //       console.log(res, r2);
        //        // res.json() //Convert response to JSON
        //        //OR
        //        return res.text(); //Convert response to a string
        //    })
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
        // localStorage.removeItem('currentUser');
    }

    // getJSessionId() {
    //   return "what on earth goes here";
    // }
}
