import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericTabComponent } from '../../shared/abstracts/gen-tab/gen-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import { SensingModel } from '../../shared/models/sensing.model';

import {
  Column,
  TextColumn,
  LabelColumn,
  RadioButtonColumn,
  SORT_DIR
} from '../../shared/models/column.model';

/**
 * Just for testing the radio button column
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
  styleUrls: ['./config-sensors-tab.component.css']
})
export class ConfigSensorsTabComponent extends GenericTabComponent implements OnInit {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) generalSensorTable: GenericTableComponent<Sensor>;


  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
    router: Router,
    dialog: MatDialog
  ) {
    super(router, dialog);
    this.tabName = "Global Sensors";
  }

  /**
   * #unimplemented
   */
  init(): void {
    this.setUpTable();
  }

  /**
   * #unimplemented
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
  update(changes: any): void {
    return;
  }


  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
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

  /** #unimplemented */
  collectData(): boolean {
    return true;
  }
}
