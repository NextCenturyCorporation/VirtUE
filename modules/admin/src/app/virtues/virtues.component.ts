import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'app-virtues',
  templateUrl: './virtues.component.html'
})

export class VirtuesComponent implements OnInit {

  constructor( private router: ActivatedRoute ) {}

  ngOnInit() {  }

}
