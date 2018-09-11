import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { AppsModalComponent } from '../../../modals/apps-modal/apps-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

import { OSSet } from '../../../shared/sets/os.set';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-vm-main-tab',
  templateUrl: './vm-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ OSSet ]
})
export class VmMainTabComponent extends GenericMainTabComponent implements OnInit {

  private newVersion: number;

  protected item: VirtualMachine;

  constructor(
      protected osOptions: OSSet,
      router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";
  }

  update(changes: any) {
    this.childrenTable.items = this.item.children.asList();

    if (changes.mode) {
      this.mode = changes.mode;
    }
  }

  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof VirtualMachine) ) {
      // TODO throw error
      console.log("item passed to vm-main-tab which was not a VirtualMachine: ", item);
      return;
    }
    this.item = item as VirtualMachine;

    this.newVersion = Number(this.item.version);

    if (this.mode !== Mode.VIEW && this.mode !== Mode.CREATE) {
      this.newVersion++;
    }

  }

  getColumns(): Column[] {
    return [
      new Column('name',    'Application Name', 5, 'asc'),
      new Column('version', 'Version',          3, 'asc'),
      new Column('os',      'Operating System', 4, 'desc')
    ];
  }

  getSubMenu(): RowOptions[] {
    return [
      // add the below once (or if) apps are given their own form page
      // new RowOptions("View", () => true, (i: Item) => this.viewItem(i)),
      new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
    ];
  }

  getNoDataMsg(): string {
    return 'No applications have been added yet. To add a template, click on the button "Add/Remove Application Packages" above.';
  }

  init() {
    this.setUpChildTable();
  }

  setUpChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    this.childrenTable.setUp({
      cols: this.getColumns(),
      opts: this.getSubMenu(),
      coloredLabels: false,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }

  collectData(): boolean {
    this.item.version = String(this.newVersion);
    return true;
  }

  getDialogRef(params: {height: string, width: string, data: any}) {
    return this.dialog.open( AppsModalComponent, params);
  }
}
