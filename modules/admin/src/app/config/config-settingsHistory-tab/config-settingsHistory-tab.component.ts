import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { MatDialog } from '@angular/material';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import {
  Column,
  TextColumn,
  IconColumn,
  SORT_DIR
} from '../../shared/models/column.model';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * @class
 * #unimplemented
 * Will be used to display a history of changes to global settings, once versioning and histories have been
 * figured out.
 *
 */
@Component({
  selector: 'app-config-settings-history-tab',
  templateUrl: './config-settingsHistory-tab.component.html',
  styleUrls: ['../config.component.css', './config-settingsHistory-tab.component.css']
})
export class ConfigSettingsHistoryTabComponent extends GenericDataTabComponent {

  /** #unimplemented */
  @ViewChild(GenericTableComponent) historyTable: GenericTableComponent<{foo: number, bar: string}>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      routerService: RouterService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(routerService, dataRequestService, dialog);
    this.tabLabel = "Settings History";
  }

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
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [];
  }

  setUpTable(): void {
    if (this.historyTable === undefined) {
      return;
    }
    this.historyTable.setUp({
      cols: this.getColumns(),
      tableWidth: 1,
      noDataMsg: "Not yet implemented."
    });
  }

  getColumns(): Column[] {
    return [
      new TextColumn("Settings Foo", 4, (s: {foo: string, bar: string}) => s.foo, SORT_DIR.ASC),
      new TextColumn("Settings Bar", 4, (s: {foo: string, bar: string}) => s.bar, SORT_DIR.ASC),
      new IconColumn("Revert button (placeholder)", 4, "restore", (obj) => {obj.foo++; })
    ];
  }
}
