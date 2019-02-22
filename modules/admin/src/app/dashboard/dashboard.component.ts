import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { SensorRecord } from '../shared/models/sensor.model';

import { RouterService } from '../shared/services/router.service';
import { BaseUrlService } from '../shared/services/baseUrl.service';
import { SensingService } from '../shared/services/sensing.service';
import { DataRequestService } from '../shared/services/dataRequest.service';

import { DatasetNames } from '../shared/abstracts/gen-data-page/datasetNames.enum';
import { Subdomains } from '../shared/services/subdomains.enum';
import { Column, TextColumn, SORT_DIR } from '../shared/models/column.model';
import { ItemListComponent } from '../shared/abstracts/item-list/item-list.component';

/**
 * Note: This class currently just displays the last-reported-status of the known sensors.
 * If hundreds of sensors are connected at once, performance may not be pleasant.
 *
 * Eventually (that is, in a later phase), this would be significantly overhauled to show data analytics and
 * stuff. Until then, this will just inherit from Itemlist and show some simple information.
 *
 * @class
 * @extends ItemListComponent for now
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent extends ItemListComponent {

  // sensorData = [];

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    dataRequestService: DataRequestService,
    dialog: MatDialog,
    private sensingService: SensingService,
  ) {
    super(routerService, dataRequestService, dialog);

  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[ItemListComponent]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Sensor ID',   2, (sensor) => sensor.sensor_id, SORT_DIR.ASC, (sensor) => this.toDetailsPage(sensor)),
      new TextColumn('Virtue ID',   2, (sensor) => this.getVirtName(sensor), SORT_DIR.ASC),
      new TextColumn('Username',    1, (sensor) => sensor.username, SORT_DIR.ASC),
      new TextColumn('Port',        1, (sensor) => sensor.port, SORT_DIR.ASC),
      new TextColumn('Kafka Topic', 3, (sensor) => sensor.kafka_topic, SORT_DIR.ASC),
      new TextColumn('Certificate', 1, (sensor) => this.hasCertificates(sensor),  SORT_DIR.ASC),
      new TextColumn('Last Update', 2, (sensor) => sensor.updated_at, SORT_DIR.ASC),
      // new TextColumn('Tim',   2, (sensor) => sensor.timestamp, SORT_DIR.ASC)
    ];
  }

  customizeTableParams(paramsObject) {
    delete paramsObject.elementIsDisabled;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getVirtName(d: any): string {
    return d.virtue_id;
    // return this.datasets[DatasetNames.VIRTUES].get(d.virtue_id).getName();
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
   *
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   * @return
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.VM_TS, DatasetNames.VMS, DatasetNames.VIRTUE_TS, DatasetNames.VIRTUES, DatasetNames.SENSORS];
  }

  getDatasetToDisplay(): DatasetNames {
    return DatasetNames.SENSORS;
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
  getTableFilters(): {objectField: string, options: {value: string, text: string}[] } {
    return undefined;
  }
}
