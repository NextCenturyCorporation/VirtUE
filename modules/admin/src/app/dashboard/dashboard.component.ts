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
    this.sensingService.getList(baseUrl).subscribe(data => {
      this.sensorlog(data);
    });
  }

  sensorlog(log){
    this.sensorData = log;
  }

  getVirtueInfo(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl).subscribe(virtues => {
      this.virtues = virtues;
    });
  }

  getSensorInfo(sensor: any[], prop: string) {
    if (prop === 'sensor_id') {
      return sensor[0].sensor_id;

    } else if (prop === 'virtue_id') {
      // console.log(sensor[0].virtue_id);
      this.getVirtueName(sensor[0].virtue_id);
      // this.virtueName = `${this.virtueName} (${sensor[0].virtue_id})`;
      return this.virtueName;

    } else if (prop === 'kafka_topic') {
      return sensor[0].kafka_topic;

    } else if (prop === 'has_certificates') {
      if (sensor[0].has_certificates === true) {
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
        this.virtueName = virtue.name;
      }
    }
  }

}
