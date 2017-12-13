import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Pipe, PipeTransform } from '@angular/core';
import { DataService } from '../data/data.service';
import { JsonFilterPipe } from '../data/json-filter.pipe';

@Component({
  selector: 'app-dashboard',
  providers: [ DataService ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})

export class DashboardComponent implements OnInit {

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

  jsonResult: string;
  jsonData = [
   {
     "timestamp": "2017-12-05T19:57:01.052901",
     "sensor": "f8f6b0bf-eef4-448a-b42a-54f403f839c0",
     "message": "{python:1,root:cwd,DIR:'0,128 4096 1095483 /usr/src/app\n'}",
     "level": "debug"
   },
   {
     "timestamp": "2017-12-05T19:57:01.061852",
     "sensor": "b860f302-f8a4-4db5-88ba-70db3fb2fb1f",
     "message": "{python:1,root:'mem',REG:'8,1 26258 396447 /usr/lib/x86_64-linux-gnu/gconv/gconv-modules.cache\n'}",
     "level": "debug"
   },
   {
     "timestamp": "2017-12-05T19:57:01.062093",
     "sensor": "b860f302-f8a4-4db5-88ba-70db3fb2fb1f",
     "message": "{python:1,root:'6u',unix:'0x0000000000000000 0t0 1104363 socket\n'}",
     "level": "info"
   },
   {
     "timestamp": "2017-12-05T19:57:01.062237",
     "sensor": "b860f302-f8a4-4db5-88ba-70db3fb2fb1f",
     "message": "{python:1,root:'10u',IPv4:'1104367 0t0',TCP:'35553d5def9c:11001 (LISTEN)\n'}",
     "level": "info"
   }
  ]
  if (this.jsonData[2].message.indexOf('unix') > 0){
    console.log('UNIX info message exists.')
  }
  constructor(){}
  //constructor( private dataService: DataService ){}

  ngOnInit() {

    // this.dataService.getData()
    // .subscribe(resJsonData => this.jsonData = resJsonData);
  }
}
