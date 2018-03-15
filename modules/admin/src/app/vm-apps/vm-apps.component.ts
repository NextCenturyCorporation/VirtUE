import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'app-vm-apps',
  templateUrl: './vm-apps.component.html'
})
export class VmAppsComponent implements OnInit {

  constructor( private router: ActivatedRoute ) { }

  ngOnInit() {
  }

}
