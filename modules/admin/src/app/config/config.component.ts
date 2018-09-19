import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';

/**
 * @class
 * Convert this to a generic form.
 *
 * #uncommented
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


  /**
   * #uncommented
   */
  constructor(
    /** #uncommented */
    protected location: Location
   ) {}

  /**
   * #uncommented
   */
  ngOnInit(): void {
  }

}
