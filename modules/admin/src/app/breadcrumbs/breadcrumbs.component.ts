import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivationEnd, NavigationEnd, ActivatedRoute, RouterModule, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs/Subscription';

import { Breadcrumb } from '../shared/models/breadcrumb.model';
import { BreadcrumbProvider } from '../shared/providers/breadcrumb';

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html'
})
export class BreadcrumbsComponent implements OnInit {

  breadcrumbs: Breadcrumb[] = [];
  routerSub: Subscription;

  constructor(
    private router: Router,
    private breadcrumbProvider: BreadcrumbProvider,
    private activatedRoute: ActivatedRoute
  ) {
    // let tempCrumbs: Breadcrumb[] = [];
    // this.router.events.subscribe(
    //   e => {
    //   console.log(e);
    //   if (e instanceof ActivationEnd) {
    //     console.log("\t++", e.snapshot.data.breadcrumb);
    //     if (e.snapshot.data.breadcrumb) {
    //       tempCrumbs.unshift(e.snapshot.data.breadcrumb);
    //       // this.breadcrumbs = Object.assign([], e.snapshot.data.breadcrumbs);
    //     // } else if (this.breadcrumbs.length <= 0 && e.snapshot.data.defaultBreadcrumbs) {
    //     }
    //     this.breadcrumbs = tempCrumbs;
    //     console.log("\t..", this.breadcrumbs);
    //   }
    //
    //   //ugly
    //   //Navigation end appears to be one of the last events in the chain that get
    //   //get fired off when you navigate to a new link. So when you see that, reset
    //   //tempCrumbs, so the next navigation starts with a clean slate.
    //   if (e instanceof NavigationEnd) {
    //     tempCrumbs = [];
    //   }
    // });

    // this.breadcrumbProvider._addItem.subscribe(breadcrumb => {
    //   this.breadcrumbs.push(breadcrumb);
    //   console.log(this.breadcrumbs);
    // });
  }

  ngOnInit() {
    let tempCrumbs: Breadcrumb[] = [];
    this.routerSub = this.router.events.subscribe(
      e => {
      console.log(e);
      if (e instanceof ActivationEnd) {
        console.log("\t++", e.snapshot.data.breadcrumb);
        if (e.snapshot.data.breadcrumb) {
          tempCrumbs.unshift(e.snapshot.data.breadcrumb);
          // this.breadcrumbs = Object.assign([], e.snapshot.data.breadcrumbs);
        // } else if (this.breadcrumbs.length <= 0 && e.snapshot.data.defaultBreadcrumbs) {
        }
        this.breadcrumbs = tempCrumbs;
        console.log("\t..", this.breadcrumbs);
      }

      //ugly
      //Navigation end appears to be one of the last events in the chain that get
      //get fired off when you navigate to a new link. So when you see that, reset
      //tempCrumbs, so the next navigation starts with a clean slate.
      if (e instanceof NavigationEnd) {
        tempCrumbs = [];
      }
    });
    // this.router.events.pipe(
    //   filter(event => event instanceof NavigationEnd)
    // ).subscribe(() => {
    //   console.log(this.activatedRoute)
    //   this.buildBreadCrumb(this.activatedRoute.root);
    // });
  }

  // buildBreadCrumb(route: ActivatedRoute, url: string = '',
  //                 breadcrumbs: Array<Breadcrumb> = []): Array<Breadcrumb> {
  //     console.log(route.routeConfig);
  //     //If no routeConfig is avalailable we are on the root path
  //     const path = route.routeConfig ? route.routeConfig.path : '';
  //     if (route.routeConfig && route.routeConfig.path === '') {
  //       console.log(breadcrumbs)
  //       return breadcrumbs;
  //     }
  //     const label = route.routeConfig ? route.routeConfig.data[ 'breadcrumbs' ] : '';
  //     //In the routeConfig the complete path is not available,
  //     //so we rebuild it each time
  //     const nextUrl = `${url}${path}/`;
  //     const breadcrumb = new Breadcrumb(label, nextUrl);
  //
  //     const newBreadcrumbs = [ ...breadcrumbs, breadcrumb ];
  //     if (route.firstChild) {
  //         //If we are not on our current path yet,
  //         //there will be more children to look after, to build our breadcumb
  //         return this.buildBreadCrumb(route.firstChild, nextUrl, newBreadcrumbs);
  //     }
  //     return newBreadcrumbs;
  // }
}
