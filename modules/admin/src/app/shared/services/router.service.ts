import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Location } from '@angular/common';

import { Router, ActivationEnd, NavigationStart, NavigationEnd, RoutesRecognized } from '@angular/router';


/**
 * @class
 */
@Injectable()
export class RouterService {

  private previousUrl: string;

  private history: string;

  constructor(
    private router: Router,
    private location: Location
  ) {
    this.router.events
        .filter(e => e instanceof RoutesRecognized)
        .pairwise()
        .subscribe((event: any[]) => {
          this.previousUrl = event[0].urlAfterRedirects;
        });

    // Tell angular to load a fresh, new, component every time a URL that needs this component loads,
    // even if the user has been on that page before
    this.router.routeReuseStrategy.shouldReuseRoute = function(){
        return false;
    };

    // make the page reload if the user clicks on a link to the same page they're on.
    this.router.events.subscribe((event) => {
        if (event instanceof NavigationEnd) {
            this.router.navigated = false;
            window.scrollTo(0, 0);
        }
    });
  }


  getRouteHistory(): string {
    return this.history;
  }

  getPreviousUrl(): string {
    return this.previousUrl;
  }

  toPreviousPage(): void {
    if (this.previousUrl) {
      this.goToPage(this.previousUrl);
    }
    else {
      this.toTopDomainPage();
    }
  }

  toTopDomainPage(): void {
    this.goToPage(this.getRouterUrlPieces()[0]);
  }

  /**
   * @param targetPath the subdomain path to navigate to.
   */
  goToPage(targetPath: string): void {
    this.router.navigate([targetPath]);
  }

  changeUrlWithoutNavigation( newURL: string ): void {
    this.location.go(newURL);
  }

  getRouterUrlPieces(): string[] {
    let url = this.getRouterUrl();
    if (url[0] === '/') {
      url = url.substr(1);
    }
    return url.split('/');
  }

  getRouterUrl(): string {
    return this.router.routerState.snapshot.url;
  }

}
