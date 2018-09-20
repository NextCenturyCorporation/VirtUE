import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivationEnd, NavigationStart, ActivatedRoute, RouterModule, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs/Subscription';

import { Breadcrumb } from '../shared/models/breadcrumb.model';

/**
 * @class
 * Creates and displays to the user a list of links, one for each stop someone could take from the main page
 * in order to navigate to the page they're currently on.
 *
 * Currently that list is irrelevant to how the user actually got there. TODO It may be useful to make
 * that list build up as the user navigates.
 *
 *  - Clicking a link in your breadcrumbs should truncate the list to that link though, and using
 *  the back button in the browser shouldn't add a new link. Ideally it should revert the list to the state it was on the last page.
 *  Which may not be trivial. May at least entail using the route reuse policy thing to reload the previously used
 *  breadcrumb component.
 *  - Perhaps disabling the route resuse thing is why I can't see the previous steps taken by the route in activatedRoute.
 * Thought of a potentially better way though. Create a BC service that every page sends their breadcrumb to, and have
 * BreadcrumbsComponent subscribe to it. When a root page's breadcrumb comes in, restart the list with it. If the new breadcrumb
 * is already in the list, then truncate the list to that entry. Otherwise, just tack on the newest breadcrumb to the end.
 *
 *
 * @implements OnInit  in order to start this process as soon as the item is rendered
 * @implements OnDestroy   to kill the subscription when this object is destroyed.
 */
@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html'
})
export class BreadcrumbsComponent implements OnInit, OnDestroy {

  /** The list of breadcrumbs to be displayed on each page. Most recent page at the end. */
  breadcrumbs: Breadcrumb[] = [];

  /**
   * The subscription which persists across the lifetime of the page, collecting navigation events as they occur,
   * and being cut once this component is destroyed.
   */
  routerSub: Subscription;

  constructor(
    /** Handles the navigation to/from different pages. Injected, and so is constant across components. */
    private router: Router,
    /** Injected, records information about the route that may be relevant when we change breadcrumbs to build up. */
    private activatedRoute: ActivatedRoute
  ) { }

  /**
   * on render, start a subscription that tracks each router event upon navigation, and chains each stop together into a
   * list of breadcrumbs that can be displayed to the user as links.
   * Currently, you end up with a list of how the app navigated to that page, according to app-router's specifications, not
   * how the *user* got to that page. Should be changed eventually - see note at top of class.
   */
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

  /**
   * explicitly unsubscribe when this component is destroyed.
   */
  ngOnDestroy() {
    this.routerSub.unsubscribe();
  }
}
