import { Component, OnInit, ViewChild } from '@angular/core';

/**
 * @class
 * This is the main class for this Angular app, and is the entry point for everything.
 * Every page uses the below template.
 * An object of this type is automatically instantiated by Angular.
 */
@Component({
  selector: 'app-root',
  template: `
<app-header></app-header>
<app-breadcrumbs></app-breadcrumbs>
<content>
  <router-outlet></router-outlet>
</content>
<app-footer></app-footer>
`
})
export class AppComponent implements OnInit {
  title = 'Savior Adminstrator Workbench';

  /** do nothing extra on initialization */
  constructor() { }

  /** do nothing extra on render */
  ngOnInit() {}
}
