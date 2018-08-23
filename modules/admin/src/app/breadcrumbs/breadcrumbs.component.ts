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
  ) { }

  ngOnInit() {
    let tempCrumbs: Breadcrumb[] = [];
    this.routerSub = this.router.events.subscribe(
      e => {
      console.log(e);
      if (e instanceof ActivationEnd) {
        console.log("\t++", e.snapshot.data.breadcrumb);
        if (e.snapshot.data.breadcrumb) {
          tempCrumbs.unshift(e.snapshot.data.breadcrumb);
        }
        this.breadcrumbs = tempCrumbs;
        console.log("\t..", this.breadcrumbs);
      }

      // ugly
      // Navigation end appears to be one of the last events in the chain that get
      // get fired off when you navigate to a new link. So when you see that, reset
      // tempCrumbs, so the next navigation starts with a clean slate.
      if (e instanceof NavigationEnd) {
        tempCrumbs = [];
      }
    });
  }
}
