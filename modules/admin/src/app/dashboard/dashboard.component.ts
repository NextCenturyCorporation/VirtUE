import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { DashboardService } from '../shared/services/dashboard.service';

import { JsonFilterPipe } from '../shared/json-filter.pipe';
import { CountFilterPipe } from '../shared/count-filter.pipe';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  providers: [ DashboardService ]
})

export class DashboardComponent implements OnInit {

  form: FormGroup;

  columnSearch: string;
  columns = [
    {value: '', viewValue: 'Select Column'},
    {value: 'timestamp', viewValue: 'Timestamp'},
    {value: 'sensor', viewValue: 'Sensor'},
    {value: 'message', viewValue: 'Message'},
    {value: 'level', viewValue: 'level'}
  ];

  sensorValue: string;
  sensors = [
    {value: '', viewValue: 'Select One'},
    {value: ' 0000 ', viewValue: 'DEFAULT'},
    {value: 'chr', viewValue: 'CHR'},
    {value: 'dir', viewValue: 'DIR'},
    {value: 'fifo', viewValue: 'FIFO'},
    {value: 'ipv4', viewValue: 'IPv4'},
    {value: 'reg', viewValue: 'REG'},
    {value: 'unix', viewValue: 'Unix'},
  ];

  jsonResult: string;
  jsonData = [];

  // constructor(){}
  constructor(
    private dashboardService: DashboardService
  ){}

  ngOnInit() {
    this.dashboardService.getList()
    .subscribe(data => this.jsonData = data);
  }
}
