import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'app-virtues',
  templateUrl: './virtues.wrapper.component.html',
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})

export class VirtuesWrapperComponent implements OnInit {

  location: Location;

  constructor(
    location: Location,
    private router: ActivatedRoute
  ) {
    this.location = location;
  }

  ngOnInit() {  }

}
