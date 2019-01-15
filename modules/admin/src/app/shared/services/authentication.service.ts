import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import 'rxjs/add/operator/map'
import { catchError } from 'rxjs/operators/catchError';

import { InterceptorRemoteDestinationHeader } from './baseUrl.interceptor';
import { RouterService } from './router.service';

@Injectable()
export class AuthenticationService {
  baseUrl: string;
  authenticated: boolean = true;

    constructor(
      private httpClient: HttpClient,
      private routerService: RouterService
    ) { }

    // check expires header

    login(username: string, password: string) {
      console.log(username, password);


      // const jsonBody = JSON.stringify({username: username, password: password});

      const formBody = new HttpParams()
          .set('username', username)
          .set('password', password)//.toString();


      // return this.httpClient.post<any>(`${this.baseUrl}login`, {  params: params, headers: headers})
      // return this.httpClient.get<string>(`${this.baseUrl}login`, httpOptions)
      return this.httpClient.post(
            `/login`,
            formBody,
            {
              headers: new HttpHeaders()
                            .set(InterceptorRemoteDestinationHeader, '')
                            .set('Content-Type', 'application/x-www-form-urlencoded'),
              observe: 'response',
              responseType: 'text'
            }
          )
          // .pipe(map((response: any) => {
          //   console.log('returned: ', response);
          //   if (response && response.status === 200) {
          //       // store user details, including sessionIDs, in localStorage to keep user logged in between page refreshes
          //       // localStorage.setItem('currentUser', JSON.stringify(response));
          //       // .... /permanently. Is this safe?
          //       this.authenticated = true;
          //   }
          //   else {
          //     this.authenticated = false;
          //   }
          //
          //   return response;
          // }))
        ;
    }

    logout() {
        this.authenticated = false;
        return this.httpClient.post(
          `/logout`,
          {},
          {
            headers: new HttpHeaders().set(InterceptorRemoteDestinationHeader, ''),
            observe: 'response',
            responseType: 'text'
          }
        );
    }

    isAuthenticated() {
      console.log(this.authenticated);
      return this.authenticated;
    }

    markUnauthenticated() {
      this.authenticated = false;
      // reload this page, in case it shouldn't be viewed.
      this.routerService.goToPage(this.routerService.getRouterUrl());
    }
}
