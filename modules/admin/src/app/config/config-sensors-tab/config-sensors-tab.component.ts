import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { MatDialog } from '@angular/material';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import { SensingModel } from '../../shared/models/sensing.model';

import {
  Column,
  TextColumn,
  LabelColumn,
  RadioButtonColumn,
  SORT_DIR
} from '../../shared/models/column.model';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * Temporary, just for showcasing the radio button column
 */
class Sensor {
  public status: string;
  constructor (
    public name: string,
    public level: VigilenceLevel
  ) { }
}

/**
 * This is also temporary, but will probably eventually just get moved to its own file.
 */
enum VigilenceLevel {
  OFF = "off",
  DEFAULT = "default",
  LOW = "low",
  HIGH = "high",
  ADVERSARIAL = "adversarial"
}

/**
 * @class
 * #unimplemented
 *
 */
@Component({
  selector: 'app-config-sensors-tab',
  templateUrl: './config-sensors-tab.component.html',
  styleUrls: ['../config.component.css', './config-sensors-tab.component.css']
})
export class ConfigSensorsTabComponent extends GenericDataTabComponent implements OnInit {

  @ViewChild(GenericTableComponent) generalSensorTable: GenericTableComponent<Sensor>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      routerService: RouterService,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(routerService, baseUrlService, dataRequestService, dialog);
    this.tabLabel = "Global Sensors";
  }

  init(): void {
    this.setUpTable();
  }

  /**
   * called once data is available - mocked here. #unimplemented
   */
  setUp(): void {
    this.generalSensorTable.populate([ new Sensor("In-resource (Unikernel)", VigilenceLevel.OFF),
                              new Sensor("In-Virtue Controller", VigilenceLevel.OFF),
                              new Sensor("Logging - Aggregate", VigilenceLevel.OFF),
                              new Sensor("Logging - Archive", VigilenceLevel.OFF),
                              new Sensor("Certificates Infrastructure", VigilenceLevel.OFF)]);
  }

  /**
   * #unimplemented
   */
  onPullComplete(): void {
    // this.generalSensorTable.populate(this.datasets[DatasetNames.SENSORS].asList());
  }

  /**
   * #unimplemented
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [];
  }

  setUpTable(): void {
    if (this.generalSensorTable === undefined) {
      return;
    }
    this.generalSensorTable.setUp({
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "No sensor data at the moment. It's not yet implemented."
    });
  }

  /** #unimplemented */
  getColumns(): Column[] {
    return [
      new TextColumn("Sensor Context", 3, (s: Sensor) => s.name, SORT_DIR.ASC),
      new RadioButtonColumn("Off",          1, "level", VigilenceLevel.OFF),
      new RadioButtonColumn("Default",      1, "level", VigilenceLevel.DEFAULT),
      new RadioButtonColumn("Low",          1, "level", VigilenceLevel.LOW),
      new RadioButtonColumn("High",         1, "level", VigilenceLevel.HIGH),
      new RadioButtonColumn("Adversarial",  2, "level", VigilenceLevel.ADVERSARIAL),
      new RadioButtonColumn("On",           1, "status", "ON"),
      new RadioButtonColumn("Off",          1, "status", "OFF")
    ];
  }
}
