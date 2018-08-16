import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { SensingModel } from '../shared/models/sensing.model';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { SensingService } from '../shared/services/sensing.service';
import { ItemService } from '../shared/services/item.service';
import { GenericModal } from '../modals/generic-modal/generic.modal';


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  providers: [ BaseUrlService, SensingService, ItemService ]
})

export class DashboardComponent extends GenericModal {

  form: FormGroup;

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

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog,
    private sensingService: SensingService,
  ) {
    super(router, baseUrlService, itemService, dialog, undefined, undefined);

    this.neededDatasets["virtues"];
  }

  ngOnInit() {
    this.cmnComponentSetup();
  }

  onPullComplete() {
    this.getSensingData();
  }

  hasCertificates(d) {
    if (d.has_certificates) {
      return 'Yes';
    }
    return 'No';
  }

  getSensingData() {
    this.sensingService.setBaseUrl(this.baseUrl);
    this.sensingService.getSensingLog().subscribe(data => {
      if (data.length > 0) {
        this.sensorData = data[0].sensors;
        console.log(this.sensorData);
      } else {
        //get static data
        this.sensingService.getStaticList().subscribe(data => {
          this.sensorData = data[0].sensors;
        });
      }
    });
  }

  //what's this?
  sensorlog(log) {
    // console.log('sensorlog ... ' + this.sensorData.error);
    this.sensorData = log;
  }

}
