import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-main-user-tab',
  templateUrl: './main-user-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})
export class UserMainTabComponent extends GenericMainTabComponent implements OnInit {

  private roleUser: boolean;
  private roleAdmin: boolean;

  private fullImagePath: string;

  // re-classing parent's object
  protected item: User;

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
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.USERS,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES, Datasets.USERS]
    };
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
  update(changes: any) {
    this.childrenTable.items = this.item.children.asList();

    if (changes.mode) {
      this.mode = changes.mode;
    }
  }

  /**
   * @param
   *
   * @return
   */
  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof User) ) {
      // TODO throw error
      console.log("item passed to main-user-tab which was not a User: ", item);
      return;
    }
    this.item = item as User;

    this.roleUser = this.item.roles.includes("ROLE_USER");
    this.roleAdmin = this.item.roles.includes("ROLE_ADMIN");
  }

  /**
   * @param
   *
   * @return
   */
  collectData(): boolean {
    this.item.roles = [];
    if (this.roleUser) {
      this.item.roles.push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item.roles.push('ROLE_ADMIN');
    }
    return true;
  }

  /**
   * @param
   *
   * @return
   */
  getColumns(): Column[] {
    return [
      new Column('name',    'Virtue Template Name',   3, 'asc',     undefined,       undefined,           (i: Item) => this.viewItem(i)),
      new Column('vms',     'Virtual Machines',       3, undefined, this.formatName, this.getChildren,    (i: Item) => this.viewItem(i)),
      new Column('apps',    'Assigned Applications',  3, undefined, this.formatName, this.getGrandchildren),
      new Column('version', 'Version',                2, 'asc'),
      new Column('status',  'Status',                 1, 'asc', this.formatStatus)
    ];
  }

  /**
   * @param
   *
   * @return
   */
  getSubMenu(): RowOptions[] {
    return [
       new RowOptions("Edit", () => true, (i: Item) => this.viewItem(i)),
       new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
    ];
  }

  /**
   * @param
   *
   * @return
   */
  getNoDataMsg(): string {
    return "No virtues have been added yet. To add a virtue, click on the button \"Add Virtue\" above.";
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
      filters: [], // don't allow filtering on the form's child table.
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
  getDialogRef(params: {height: string, width: string, data: any}) {
    return this.dialog.open( VirtueModalComponent, params);
  }
}
