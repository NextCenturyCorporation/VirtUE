import { Component, OnInit } from '@angular/core';

/**
 * @class
 * This component currently only consists of a static html block, and gets displayed at the bottom of all pages.
 * Could be extended, perhaps with links to NextCentury's page, or commonly-used materials, or a guide, or FAQ.
 */
@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html'
})
export class FooterComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
