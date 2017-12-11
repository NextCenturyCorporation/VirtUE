import { Component, OnInit } from '@angular/core';
import { DataService } from '../data/data.service';

@Component({
  selector: 'app-dashboard',
  providers: [ DataService ],
  //template: '<h2>Json Example</h2><ul *ngFor="let d of jsonData"><li>{{d.timestamp}}</li></ul>'
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})

export class DashboardComponent implements OnInit {

  jsonData = [];
  constructor( private dataService: DataService ){}

  ngOnInit() {
    this.dataService.getData()
    .subscribe(resJsonData => this.jsonData = resJsonData);
  }


}
