import { Injectable, Output, EventEmitter } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Location } from '@angular/common';

import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/pairwise';

import { Router, ActivationEnd, NavigationStart, NavigationEnd, RoutesRecognized } from '@angular/router';

import { Breadcrumb } from '../../shared/models/breadcrumb.model';


/**
 * @class
 */
@Injectable()
export class RouterService {

  // The component really pivots around this variable
  private fullCrumb: Breadcrumb = new Breadcrumb(undefined, undefined);

  // private previousUrl: string;

  private history: Breadcrumb[] = [];

  @Output()
  public onNewPage: EventEmitter<Breadcrumb> = new EventEmitter<Breadcrumb>();

  constructor(
    private router: Router,
    private location: Location
  ) {
    // this.router.events
    //     .filter(e => e instanceof RoutesRecognized)
    //     .pairwise()
    //     .subscribe((event: any[]) => {
    //       this.previousUrl = event[0].urlAfterRedirects;
    //     });

    // Tell angular to load a fresh, new, component every time the router navigates to a URL, even if the user has been there before.
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

    // make the page reload if the user clicks on a link to the same page they're on.
    this.router.events.subscribe((event) => {

      if (event instanceof NavigationEnd) {
        this.router.navigated = false;
        window.scrollTo(0, 0);
        this.setBreadcrumbHref(event.url);
      }
    });
  }
  setBreadcrumbHref( href: string ): void {
    this.fullCrumb.href = href;
  }

  setBreadcrumbLabel( label: string ): void {
    // This function will be called when data returns from the backend to whatever component is loaded.
    // fullCrumb's href is set around render time.
    // Therefore we can be pretty confident this function will always be called second.

    // It can, apparently, get called more than once though. So guard against that - otherwise you'll have extra breadcrumbs
    if (this.fullCrumb.href !== undefined && this.fullCrumb.label === undefined) {
      this.fullCrumb.label = label;
      this.fullCrumb = this.cleanCrumb(this.fullCrumb);
      this.onNewPage.emit(this.fullCrumb);
      this.updateHistory(this.fullCrumb);
      this.fullCrumb = new Breadcrumb(undefined, undefined);
    }
  }

  // just have pages notify the router manually of their title.
  public submitPageTitle( title: string ): void {
    this.setBreadcrumbLabel(title);
  }

  updateHistory( newPage: Breadcrumb ): void {
    let i: number = this.history.length - 1;
    for (; i >= 0; i-- ) {
      if (newPage.href === this.history[i].href) {
        break;
      }
    }

    if (i !== -1) {
      this.history = this.history.slice(0, i);
    }

    this.history.push(newPage);
  }

  cleanCrumb(crumb: Breadcrumb): Breadcrumb {
    // Navigation should only happen from a view page, so if you aren't on a view page and navigate,
    // go back to the view page. To prevent accidentally making extra duplicates, and to prevent weird circles
    // with the breadcrumbs, like: View U1 > View V1 > View U2 > Edit V1 > Edit U1 > Duplicate V2 > Edit U2
    // With, of course, much longer names.
    crumb.href = crumb.href.replace("edit", "view")
                           .replace("duplicate", "view");
    return crumb;
  }

  private hasPreviousPage(): boolean {
    return this.history.length > 1;
  }

  private getPreviousPage(): string {
    return this.history[this.history.length - 2].href;
  }

  toPreviousPage(): void {
    if (this.hasPreviousPage()) {
      this.goToPage(this.getPreviousPage());
    }
    else {
      this.toTopDomainPage();
    }
  }

  isTopDomainPage(url: string): boolean {
    return this.getUrlPieces( url ).length === 1;
  }

  toTopDomainPage(): void {
    this.goToPage(this.getRouterUrlPieces()[0]);
  }


  /**
   * @param targetPath the subdomain path to navigate to.
   */
  goToPage(targetPath: string, params?: string[]): void {
    if (params) {
      params.unshift(targetPath);
      this.router.navigate(params);
    }
    else {
      this.router.navigate([targetPath]);
    }
  }

  changeUrlWithoutNavigation( newURL: string ): void {
    this.location.go(newURL);
  }

  getRouterUrlPieces(): string[] {
    return this.getUrlPieces( this.getRouterUrl() );
  }

  getUrlPieces(url: string): string[] {
    if (url[0] === '/') {
      url = url.substr(1);
    }
    return url.split('/');
  }

  getRouterUrl(): string {
    return this.router.routerState.snapshot.url;
  }

  loginRedirect() {
    console.log(this.getRouterUrl().split('?'));
    if (this.getRouterUrl().split('?')[0] !== '/login') {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.getRouterUrl().split("?")[0] }});
    }
  }
}
