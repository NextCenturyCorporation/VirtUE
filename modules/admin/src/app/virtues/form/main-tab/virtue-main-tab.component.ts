import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-virtue-main-tab',
  templateUrl: './virtue-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})
export class VirtueMainTabComponent extends GenericMainTabComponent implements OnInit {

  private newVersion: number;

  protected item: Virtue;

  /**
   * @param
   *
   * @return
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";
  }

  /**
   * @param
   *
   * @return
   */
  update(changes: any) {
    this.childrenTable.items = this.item.children.asList();

    if (changes.mode) {
      this.setMode(changes.mode);
      this.childrenTable.colData = this.getColumns();
      this.childrenTable.rowOptions = this.getSubMenu();
    }
  }

  /**
   * @param
   *
   * @return
   */
  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-main-tab which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;

    this.setMode(mode);
  }

  /**
   * @param
   *
   * @return
   */
  setMode(newMode: Mode) {
    this.mode = newMode;

    this.newVersion = Number(this.item.version);

    if (this.mode !== Mode.VIEW && this.mode !== Mode.CREATE) {
      this.newVersion++;
    }
  }

  /**
   * @param
   *
   * @return
   */
  // TODO start implementing view page from this.
  getColumns(): Column[] {
    let cols: Column[] = [
      new Column('os',          'OS',                    2, 'asc'),
      new Column('childNames',  'Assigned Applications', 4, undefined,  this.formatName, this.getChildren),
      new Column('status',      'Status',                2, 'asc',      this.formatStatus)
    ];
    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('name', 'VM Template Name', 4, 'asc', undefined, undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('name', 'VM Template Name', 4, 'asc'));
    }

    return cols;
  }

  /**
   * @param
   *
   * @return
   */
  getSubMenu(): RowOptions[] {
    if (this.mode === Mode.VIEW) {
      return [
         new RowOptions("View", () => true, (i: Item) => this.viewItem(i))
      ];
    }
    else {
      return [
         new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
      ];
    }
  }

  /**
   * @param
   *
   * @return
   */
  getNoDataMsg(): string {
    return "No virtual machine templates have been given to this Virtue yet. \
To add a virtual machine template, click on the button \"Add VM\" above.";
  }

  /**
   * @param
   *
   * @return
   */
  init() {
    this.setUpChildTable();
  }

  /**
   * @param
   *
   * @return
   */
  setUpChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    this.childrenTable.setUp({
      cols: this.getColumns(),
      opts: this.getSubMenu(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table. ?
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }

  /**
   * @param
   *
   * @return
   */
  collectData(): boolean {
    this.item.version = String(this.newVersion);
    return true;
  }

  /**
   * @param
   *
   * @return
   */
  getDialogRef(params: {height: string, width: string, data: any}) {
    return this.dialog.open( VmModalComponent, params);
  }
}
