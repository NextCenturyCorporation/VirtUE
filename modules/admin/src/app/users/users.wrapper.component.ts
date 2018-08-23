import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-users',
  templateUrl: './users.wrapper.component.html',
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})

export class UsersWrapperComponent implements OnInit {

  location: Location;

  constructor( location: Location ) {
    this.location = location;
  }

  ngOnInit() {}
}
