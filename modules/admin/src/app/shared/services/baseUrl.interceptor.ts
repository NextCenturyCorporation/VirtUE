import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpInterceptor, HttpHandler, HttpRequest, HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';
import 'rxjs/add/operator/mergeMap';
import { Observable } from 'rxjs/Observable';

export const InterceptorRemoteDestinationHeader = 'X-Add-baseUrl-Interceptor';

/**
 * @class
 * This class makes a request for a local json file, holding the address it should request data from and save data to.
 * json object in that file must contain ` "virtue_server": "http://localhost:8080/" `, with the location of wherever
 * virtue-admin is running.
 */
@Injectable()
export class BaseUrlInterceptor implements HttpInterceptor {

  /**
   * @param httpClient what makes the local query
   */
  constructor( private httpClient: HttpClient ) { }

  /** the path to the json file holding the base url */
  private baseUrlFilePath = './assets/json/baseUrl.json';
  private baseUrl = "";

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Note that the call to get the baseUrl uses httpClient, which will try to apply all interceptors, including this one.
    // Use and check for a custom header to prevent infinite recursion here.
    // https://stackoverflow.com/questions/46469349/how-to-make-an-angular-module-to-ignore-http-interceptor-added-in-a-core-module

    // Note also that HttpBackend exists. https://stackoverflow.com/a/49013534/3015812.
    // It could be used here to prevent any call from here from being intercepted again, but depending on the order of things that may
    // prevent it from being intercepted by the other, not-yet-run, interceptors on the first pass.
    // It could instead be added to the classes which make local calls, but the calls made to load local icons are automatic and so can't
    // (probably) be changed.

    if (request.headers.has(InterceptorRemoteDestinationHeader)) {
      const headers = request.headers.delete(InterceptorRemoteDestinationHeader);
      request = request.clone({ headers })
      // if the baseUrl hasn't been set yet, get it, and then make the call
      if (this.baseUrl === "") {
        return this.httpClient.get(this.baseUrlFilePath).mergeMap((jsonPacket: any) => {
                                                          this.baseUrl = jsonPacket[0].virtue_server;

                                                          if (this.baseUrl.slice(-1) !== "/") {
                                                            this.baseUrl = this.baseUrl + "/";
                                                          }

                                                          return this.mutateCall(request, next);
                                                        });
      }
      else {
        return this.mutateCall(request, next);
      }
    }
    // else: when making a local request.
    return next.handle(request);

  }

  private mutateCall(request: HttpRequest<any>, next: HttpHandler) {
    let baseUrl = this.baseUrl;
    // just so we don't end up with requests to `www.homepage.com//login`. Note that the requestUrl is immutable.
    if (request.url.slice(0,1) === "/") {
      baseUrl = baseUrl.slice(0, -1);
    }
    const correctedRequest = request.clone({ url: `${baseUrl}${request.url}` });

    return next.handle(correctedRequest);
  }

}
