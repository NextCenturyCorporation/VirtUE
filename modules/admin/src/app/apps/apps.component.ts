import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'app-apps',
  templateUrl: './apps.component.html',
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})
export class AppsComponent implements OnInit {

  location: Location;

  constructor(
    location: Location,
    private router: ActivatedRoute
  ) {
    this.location = location;
  }

  ngOnInit() {
  }

}
