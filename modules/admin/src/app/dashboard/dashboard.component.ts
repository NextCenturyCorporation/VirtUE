import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
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

  // not used, but probably should be
  form: FormGroup;

  // not used, but might need the info when setting this up later
  //  columns = [
  //    {value: '', viewValue: 'Select Column'},
  //    {value: 'timestamp', viewValue: 'Timestamp'},
  //    {value: 'sensor_id', viewValue: 'ID'},
  //    {value: 'sensor', viewValue: 'Sensor Name'},
  //    {value: 'message', viewValue: 'Message'},
  //    {value: 'level', viewValue: 'level'}
  //  ];

  // also not used
  //  sensors = [
  //    {value: '', viewValue: 'Select One'},
  //    {value: ' 0000 ', viewValue: 'DEFAULT'},
  //    {value: 'chr', viewValue: 'CHR'},
  //    {value: 'dir', viewValue: 'DIR'},
  //    {value: 'fifo', viewValue: 'FIFO'},
  //    {value: 'ipv4', viewValue: 'IPv4'},
  //    {value: 'reg', viewValue: 'REG'},
  //    {value: 'unix', viewValue: 'Unix'},
  //  ];

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

  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  getColumns(): Column[] {
    return [
    new Column('sensor_id',   'Sensor', false, 'asc', 3),
    new Column('virtue name', 'Virtue', false, 'asc', 3, this.getVirtName),
    new Column('kafka_topic', 'Kafka Topic', false, 'asc', 3),
    new Column('has cert',    'Certificate', false, 'asc', 1, this.hasCertificates),
    new Column('updated_at',  'Last Update', false, 'asc', 2)
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
    this.sensingService.getSensingLog().subscribe(sensorLog => {
      if (sensorLog.length > 0) {
        this.sensorData = sensorLog[0].sensors;
        console.log(this.sensorLog);
        // TODO add formatted date here, of form:   d.updated_at|date:"MM/dd/yyyy, h:mm a"
      } else {
        // get static data
        this.sensingService.getStaticList().subscribe(staticData => {
          this.sensorData = staticData[0].sensors;
        });
      }
    });
  }

  // what's this? TODO once we have data to display and know how it should be displayed.
  sensorlog(log) {
    //  console.log('sensorlog ... ' + this.sensorData.error);
    this.sensorData = log;
  }

}
