import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import 'rxjs/add/operator/map';
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

    login(username: string, password: string) {

      const formBody = new HttpParams()
          .set('username', username)
          .set('password', password);


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
          .pipe(map((response: any) => {
            if (response && response.status === 200) {
                this.authenticated = true;
            }
            else {
              this.authenticated = false;
            }
            return response;
          }))
        ;
    }

    logout() {
        this.authenticated = false;
        return this.httpClient.post(
          `/logout`, // note that this redirects to /login?logout - calling the latter directly works, but gives an error.
          {},
          {
            headers: new HttpHeaders().set(InterceptorRemoteDestinationHeader, ''),
            observe: 'response',
            responseType: 'text'
          }
        ).subscribe(
          something => {
            console.log("Logging out");
            this.routerService.goToPage('/login');
          });
    }

    isAuthenticated() {
      return this.authenticated;
    }

    markUnauthenticated() {
      this.authenticated = false;
      this.routerService.loginRedirect();
    }
}
