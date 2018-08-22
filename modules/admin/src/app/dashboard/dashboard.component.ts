import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Component, OnInit, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { SensingModel } from '../shared/models/sensing.model';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { SensingService } from '../shared/services/sensing.service';
import { ItemService } from '../shared/services/item.service';

import { ConfigUrlEnum } from '../shared/enums/enums';
import { Column } from '../shared/models/column.model';
import { GenericListComponent } from '../shared/abstracts/gen-list/gen-list.component';


@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  providers: [ BaseUrlService, SensingService, ItemService ]
})

export class DashboardComponent extends GenericListComponent {

  //also not used, but proboably should be
  form: FormGroup;

  //also not used
  // columns = [
  //   {value: '', viewValue: 'Select Column'},
  //   {value: 'timestamp', viewValue: 'Timestamp'},
  //   {value: 'sensor_id', viewValue: 'ID'},
  //   {value: 'sensor', viewValue: 'Sensor Name'},
  //   {value: 'message', viewValue: 'Message'},
  //   {value: 'level', viewValue: 'level'}
  // ];

  //not used
  // sensorValue: string;

  //not used
  // sensors = [
  //   {value: '', viewValue: 'Select One'},
  //   {value: ' 0000 ', viewValue: 'DEFAULT'},
  //   {value: 'chr', viewValue: 'CHR'},
  //   {value: 'dir', viewValue: 'DIR'},
  //   {value: 'fifo', viewValue: 'FIFO'},
  //   {value: 'ipv4', viewValue: 'IPv4'},
  //   {value: 'reg', viewValue: 'REG'},
  //   {value: 'unix', viewValue: 'Unix'},
  // ];

  jsonResult: string;
  sensorData = [];

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog,
    private sensingService: SensingService,
  ) {
    super(router, baseUrlService, itemService, dialog);

  }

  ngOnInit() {
    this.cmnComponentSetup();
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.VIRTUES,
      neededDatasets: ["apps", "vms", "virtues"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Virtue Templates",
      itemName: "Virtue Template",
      pluralItem: "Virtues",
      domain: '/virtues'
    };
  }

  getNoDataMsg(): string {
    return "No sensor information is available at this time";

  }

  getTableFilters(): {text:string, value:string}[] {
    return [];
  }

  getColumns(): Column[] {
    return [
      {name: 'sensor_id',   prettyName: 'Sensor', isList: false, sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'virtue name', prettyName: 'Virtue', isList: false, sortDefault: 'asc', colWidth:3, formatValue: this.getVirtName},
      {name: 'kafka_topic', prettyName: 'Kafka Topic', isList: false, sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'has cert.',   prettyName: 'Certificate', isList: false, sortDefault: 'asc', colWidth:1, formatValue: this.hasCertificates},
      {name: 'updated_at',  prettyName: 'Last Update', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined}
      // {value: '', viewValue: 'Select Column'},
      // {value: 'timestamp', viewValue: 'Timestamp'},
      // {value: 'sensor_id', viewValue: 'ID'},
      // {value: 'sensor', viewValue: 'Sensor Name'},
      // {value: 'message', viewValue: 'Message'},
      // {value: 'level', viewValue: 'level'}
    ];
  }

  getVirtName(d: any) {
    return this.allVirtues.get(d.virtue_id).getName();
  }

  hasCertificates(d) {
    if (d.has_certificates) {
      return 'Yes';
    }
    return 'No';
  }

  onPullComplete() {
    this.getSensingData();
  }

  getSensingData() {
    this.sensingService.setBaseUrl(this.baseUrl);
    this.sensingService.getSensingLog().subscribe(data => {
      if (data.length > 0) {
        this.sensorData = data[0].sensors;
        console.log(this.sensorData);
        //TODO add formatted date here, of form:   d.updated_at|date:"MM/dd/yyyy, h:mm a"
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
