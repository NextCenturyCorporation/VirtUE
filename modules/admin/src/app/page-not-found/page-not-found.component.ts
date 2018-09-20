import { Component, OnInit } from '@angular/core';

/**
 * @class
 * Shows up when the app.routing.module has no entry for a requested url path.
 * Currently just displays "page-not-found-works!"
 * Needs to be changed to something meaningful, with perhaps a link to the main page or some other resource.
 */
@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html'
})
export class PageNotFoundComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
