import { Component, OnInit, ViewChild } from '@angular/core';

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
 * This component allows the user (an admin) to set up activ directories. For something.
 * TODO ask about active directories
 * #uncommented, because this is a stub.
 * also #move to a tab on the global settings form
 */
@Component({
  selector: 'app-config-printer-tab',
  templateUrl: './config-printer-tab.component.html',
  styleUrls: ['./config-printer-tab.component.css']
})
export class ConfigPrinterTabComponent extends GenericTabComponent implements OnInit {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) printersTable: GenericTableComponent<{foo: number, bar: string}>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
    router: Router,
    dialog: MatDialog
  ) {
    super(router, dialog);
    this.tabName = "Printers";
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
    this.printersTable.populate([{foo: 1, bar: "A"}, {foo: 2, bar: "B"}, {foo: 3, bar: "C"}]);
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
    if (this.printersTable === undefined) {
      return;
    }
    this.printersTable.setUp({
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "Not yet implemented."
    });
  }

  /** #unimplemented */
  getColumns(): Column[] {
    return [
      new TextColumn("Printer number", 4, (s: {foo: string, bar: string}) => s.foo, SORT_DIR.ASC),
      new TextColumn("Printer name", 4, (s: {foo: string, bar: string}) => s.bar, SORT_DIR.ASC),
      new IconColumn("Remove printer (placeholder)", 4, "delete", (obj) => {obj.foo++; })
    ];
  }

  /** #unimplemented */
  collectData(): boolean {
    return true;
  }

}
