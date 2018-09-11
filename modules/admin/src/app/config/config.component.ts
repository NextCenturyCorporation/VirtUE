import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css'],
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})
export class ConfigComponent implements OnInit {

  location: Location;

  constructor( location: Location ) {
    this.location = location;
  }

  ngOnInit() {
  }

}
