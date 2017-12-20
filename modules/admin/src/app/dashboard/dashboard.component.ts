import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { DataService } from '../data/data.service';
import { JsonFilterPipe } from '../data/json-filter.pipe';
import { CountFilterPipe } from '../data/count-filter.pipe';

@Component({
  selector: 'app-dashboard',
  providers: [ DataService ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
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
  constructor( private dataService: DataService ){}

  ngOnInit() {
    this.dataService.getData()
    .subscribe(resJsonData => this.jsonData = resJsonData);
  }
}
