import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericPageComponent } from '../../shared/abstracts/gen-page/gen-page.component';
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
  ){}
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
 * This is kept because the html has a list of sensor types - in case we need those. Probably don't.
 *
 * #delete
 *
 */
@Component({
  selector: 'app-config-sensors',
  templateUrl: './config-sensors.component.html',
  styleUrls: ['./config-sensors.component.css']
})
export class ConfigSensorsComponent extends GenericPageComponent implements OnInit {

  /** #uncommented */
  @ViewChild(GenericTableComponent) sensorTable: GenericTableComponent<Sensor>;


  /**
   *
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
    router: Router,
    dialog: MatDialog
  ) {
    super(router, dialog);
  }

  /**
   *
   */
  ngOnInit(): void {
    this.setUpTable();
    this.sensorTable.populate([ new Sensor("In-resource (Unikernel)", VigilenceLevel.OFF),
                                new Sensor("In-Virtue Controller", VigilenceLevel.OFF),
                                new Sensor("Logging - Aggregate", VigilenceLevel.OFF),
                                new Sensor("Logging - Archive", VigilenceLevel.OFF),
                                new Sensor("Certificates Infrastructure", VigilenceLevel.OFF)]);
  }


  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
  setUpTable(): void {
    if (this.sensorTable === undefined) {
      return;
    }
    this.sensorTable.setUp({
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "No sensor data at the moment. It's not yet implemented."
    });
  }

  /** */
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
