import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { JsonFilterPipe } from '../data/json-filter.pipe';
import { DataService } from '../data/data.service';

@Component({
  selector: 'app-dashboard',
  providers: [ DataService ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})

export class DashboardComponent implements OnInit {

  jsonData = [];
  form: FormGroup;
  sensorValue: string;
  sensors = [
    {value: ' 0000 ', viewValue: 'DEFAULT'},
    {value: 'CHR', viewValue: 'CHR'},
    {value: 'DIR', viewValue: 'DIR'},
    {value: 'FIFO', viewValue: 'FIFO'},
    {value: 'IPv4', viewValue: 'IPv4'},
    {value: 'REG', viewValue: 'REG'},
    {value: 'unix', viewValue: 'Unix'},
  ];

  constructor( private dataService: DataService ){}

  ngOnInit() {
    this.dataService.getData()
    .subscribe(resJsonData => this.jsonData = resJsonData);
  }
}
