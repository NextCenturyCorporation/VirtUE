import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { SensingModel } from '../shared/models/sensing.model';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { SensingService } from '../shared/services/sensing.service';
import { ItemService } from '../shared/services/item.service';

import { ConfigUrls, Datasets } from '../shared/enums/enums';
import { Column } from '../shared/models/column.model';
import { GenericListComponent } from '../shared/abstracts/gen-list/gen-list.component';

/**
 * Note: this class will be significantly overhauled soon, to actually implement connection to the sensors
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  providers: [ BaseUrlService, SensingService, ItemService ]
})
export class DashboardComponent extends GenericListComponent {

  // not used, but probably should be
  // form: FormGroup;

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

  /**
   * #uncommented
   */
  jsonResult: string;

  /**
   * #uncommented
   */
  sensorData = [];

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog,
    /**
     * #uncommented
     */
    private sensingService: SensingService,
  ) {
    super(router, baseUrlService, itemService, dialog);

  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VIRTUES,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES]
    };
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain?: string} {
    return {
      prettyTitle: "Sensor Information",
      itemName: "Sensor",
      pluralItem: "Sensors"
    };
  }

  /**
   * @return a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No sensor information is available at this time";

  }

  /**
   * @return an empty list; Apps can't be disabled, so nothing to filter
   */
  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[GenericListComponent]].
   */
  getColumns(): Column[] {
    return [
    new Column('sensor_id',   'Sensor',  3, 'asc'),
    new Column('virtue name', 'Virtue',  3, 'asc', this.getVirtName),
    new Column('kafka_topic', 'Kafka Topic', 3, 'asc'),
    new Column('has cert',    'Certificate', 1, 'asc', this.hasCertificates),
    new Column('updated_at',  'Last Update', 2, 'asc')
    ];
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getVirtName(d: any): string {
    return this.allVirtues.get(d.virtue_id).getName();
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  hasCertificates(d): string {
    if (d.has_certificates) {
      return 'Yes';
    }
    return 'No';
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  onPullComplete(): void {
    this.getSensingData();
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getSensingData(): void {
    this.sensingService.setBaseUrl(this.baseUrl);
    let sub = this.sensingService.getSensingLog().subscribe(sensorLog => {
      if (sensorLog.length > 0) {
        this.sensorData = sensorLog[0].sensors;
        console.log(this.sensorData);
        // TODO add formatted date here, of form:   d.updated_at|date:"MM/dd/yyyy, h:mm a"
      } else {
        console.log("Response received, but no data included.");
      }
    },
    error => {
      // get static data
      let staticSub = this.sensingService.getStaticList().subscribe(staticData => {
        this.sensorData = staticData[0].sensors;
      },
      () => { //on error
        staticSub.unsubscribe();
      },
      () => {// on complete
        staticSub.unsubscribe();
      });
      sub.unsubscribe();
    },
    () => {
      sub.unsubscribe();
    });
  }

  /**
   * #uncommented
   * what's this? TODO once we have data to display and know how it should be displayed.
   * @param
   *
   * @return
   */
  sensorlog(log) {
    //  console.log('sensorlog ... ' + this.sensorData.error);
    this.sensorData = log;
  }

}
