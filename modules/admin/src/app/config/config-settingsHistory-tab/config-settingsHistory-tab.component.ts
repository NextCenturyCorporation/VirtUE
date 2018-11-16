import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import {
  Column,
  TextColumn,
  IconColumn,
  SORT_DIR
} from '../../shared/models/column.model';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * @class
 * #unimplemented
 *
 */
@Component({
  selector: 'app-config-settings-history-tab',
  templateUrl: './config-settingsHistory-tab.component.html',
  styleUrls: ['../config.component.css', './config-settingsHistory-tab.component.css']
})
export class ConfigSettingsHistoryTabComponent extends GenericDataTabComponent {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) historyTable: GenericTableComponent<{foo: number, bar: string}>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(router, baseUrlService, dataRequestService, dialog);
    this.tabName = "Settings History";
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
  onPullComplete(): void {
    // this.historyTable.populate(this.datasets[DatasetNames.GLOBAL_SETTINGS_HISTORY].asList());
  }

  /**
   * #unimplemented
   * See [[GenericDataPageComponent.getDataPageOptions]]() for details on return values
   */
  getDataPageOptions(): {
      neededDatasets: DatasetNames[]} {
    return {
      neededDatasets: []
    };
  }


  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
  setUpTable(): void {
    if (this.historyTable === undefined) {
      return;
    }
    this.historyTable.setUp({
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "Not yet implemented."
    });
  }

  /** #unimplemented */
  getColumns(): Column[] {
    return [
      new TextColumn("Settings Foo", 4, (s: {foo: string, bar: string}) => s.foo, SORT_DIR.ASC),
      new TextColumn("Settings Bar", 4, (s: {foo: string, bar: string}) => s.bar, SORT_DIR.ASC),
      new IconColumn("Revert button (placeholder)", 4, "restore", (obj) => {obj.foo++; })
    ];
  }
}
