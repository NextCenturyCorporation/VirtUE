import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivationEnd, NavigationStart, ActivatedRoute, RouterModule, Router } from '@angular/router';
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
  ) { }

  ngOnInit() {
    let tempCrumbs: Breadcrumb[] = [];
    this.routerSub = this.router.events.subscribe(
      e => {
      // This function gets called many times on each navigation.

      // NavigationStart appears to be one of the first events in the chain that get
      // get fired off when you navigate to a new link. So when you see that event, reset
      // tempCrumbs, so the breadcrumb list starts with a clean slate.
      if (e instanceof NavigationStart) {
        tempCrumbs = [];
      }
      // ActivationEnd events occur once for every page that was hit while navigating to
      // this one. For each such event, add a link to the corresponding page.
      else if (e instanceof ActivationEnd) {
        if (e.snapshot.data.breadcrumb) {
          tempCrumbs.unshift(e.snapshot.data.breadcrumb);
        }
        this.breadcrumbs = tempCrumbs;
      }
    });
  }
}
