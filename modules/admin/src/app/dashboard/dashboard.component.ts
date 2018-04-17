import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { SensingModel } from '../shared/models/sensing.model';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { SensingService } from '../shared/services/sensing.service';
import { VirtuesService } from '../shared/services/virtues.service';


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  providers: [ BaseUrlService, SensingService, VirtuesService ]
})

export class DashboardComponent implements OnInit {

  form: FormGroup;

  columnSearch: string;
  columns = [
    {value: '', viewValue: 'Select Column'},
    {value: 'timestamp', viewValue: 'Timestamp'},
    {value: 'sensor_id', viewValue: 'ID'},
    {value: 'sensor', viewValue: 'Sensor Name'},
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
  sensorData = [];
  virtues = [];
  virtueName: string;

  // constructor(){}
  constructor(
    private baseUrlService: BaseUrlService,
    private sensingService: SensingService,
    private virtuesService: VirtuesService
  ) {}

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getVirtueInfo(awsServer);
      this.getSensingData(awsServer);
    });
  }

  getSensingData(baseUrl: string) {
    this.sensingService.getSensingLog(baseUrl).subscribe(data => {
      if (data.length > 0) {
        this.sensorData = data[0].sensors;
        console.log(this.sensorData);
      } else {
        this.getStaticData();
      }
    });
  }

  getStaticData() {
    this.sensingService.getStaticList().subscribe(data => {
      // console.log(data[0].sensors);
      this.sensorData = data[0].sensors;
      // console.log('sensing data not found...');
    });
  }

  sensorlog(log) {
    // console.log('sensorlog ... ' + this.sensorData.error);
    this.sensorData = log;
  }

  getVirtueInfo(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl).subscribe(virtues => {
      this.virtues = virtues;
    });
  }

  getSensorInfo(value: string, prop: string) {
    if (prop === 'virtue_id') {
      let sensorValue = this.getVirtueName(value);
      // return this.virtueName;
      return sensorValue;

    }  else if (prop === 'certs') {
      if (value) {
        return 'Yes';
      } else {
        return 'No';
      }
    }
  }

  getVirtueName(id: string) {
    for (let virtue of this.virtues) {
      if (id === virtue.id) {
        // console.log(virtue.name);
        return virtue.name;
      } else {
        return id;
      }
    }
  }

}
