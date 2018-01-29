import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable } from 'rxjs/observable';
import { of } from 'rxjs/observable/of';
import { catchError, map, tap } from 'rxjs/operators';

const httpOption = {
  headers: new HttpHeaders({ 'Content-Type':'application/json' })
}

@Injectable()

export class JsondataService {
  // DATA SOURCES
  private virtueList : string = './assets/json/virtue_list.json';
  private vmList : string = './assets/json/vm_list.json';
  private activeDirectory : string = './assets/json/ad_users.json';
  private appUsers : string = './assets/json/savior_users.json';
  private dashboardData : string = '../assets/json/sample_data.json';
  private jsonfile: string = './assets/json/ad_users.json';


  getDataSrc(data) {
    switch(data) {
    case 'virtues':
        this.jsonfile = this.virtueList;
        break;
    case 'vms':
        this.jsonfile = this.vmList;
        break;
    case 'adUsers':
        this.jsonfile = this.activeDirectory;
        break;
    case 'appUsers':
        this.jsonfile = this.appUsers;
        break;
    case 'dashboard':
        this.jsonfile = this.dashboardData;
        break;
    default:
        return;
    }
    console.log(data);
  }

  constructor(
    private http: HttpClient
  ) {  }

  getJSON(dataSource:string): Observable<any> {
    this.getDataSrc(dataSource);
    // return this.http.get(this.jsonfile);
    return this.http.get<any>(this.jsonfile);
  }

  getDataById(id:number, dataSource:string): Observable<any> {
    this.getDataSrc(dataSource);

    console.log('SOURCE: ' + dataSource);
    console.log('JSON File: ' + this.jsonfile);

    return this.http.get(this.jsonfile);

    // .pipe(tap(_ => this.log(`fetched item id=${id}`)));
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   */
  private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  /** Log a HeroService message with the MessageService */
  private log(message: string) {
    // this.messageService.add('HeroService: ' + message);
  }

}
