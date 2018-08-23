import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-vms',
  templateUrl: './vms.wrapper.component.html',
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})
export class VmsWrapperComponent implements OnInit {

  location: Location;

  constructor( location: Location ) {
    this.location = location;
  }

  ngOnInit() {
  }

}
