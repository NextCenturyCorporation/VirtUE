import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-virtual-machines',
  templateUrl: './virtual-machines.component.html',
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})
export class VirtualMachinesComponent implements OnInit {

  location: Location;

  constructor( location: Location ) {
    this.location = location;
  }

  ngOnInit() {
  }

}
