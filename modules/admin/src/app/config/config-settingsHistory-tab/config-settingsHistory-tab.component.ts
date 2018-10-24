import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericTabComponent } from '../../shared/abstracts/gen-tab/gen-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import {
  Column,
  TextColumn,
  IconColumn,
  SORT_DIR
} from '../../shared/models/column.model';

/**
 * @class
 * #unimplemented
 *
 */
@Component({
  selector: 'app-config-settings-history-tab',
  templateUrl: './config-settingsHistory-tab.component.html',
  styleUrls: ['./config-settingsHistory-tab.component.css']
})
export class ConfigSettingsHistoryTabComponent extends GenericTabComponent implements OnInit {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) historyTable: GenericTableComponent<{foo: number, bar: string}>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
    router: Router,
    dialog: MatDialog
  ) {
    super(router, dialog);
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
  setUp(): void {
    this.historyTable.populate([{foo: 1, bar: "A"}, {foo: 2, bar: "B"}, {foo: 3, bar: "C"}]);
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

  /** #unimplemented */
  collectData(): boolean {
    return true;
  }
}
