import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
// import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';

import { BaseUrlService } from '../../../shared/services/baseUrl.service';
import { ItemService } from '../../../shared/services/item.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-virtue-usage-tab',
  templateUrl: './virtue-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css']
})
export class VirtueUsageTabComponent extends GenericFormTabComponent implements OnInit {

  /** #uncommented */
  @ViewChild('parentTable') private parentTable: GenericTableComponent;

  /** #uncommented */
  protected item: Virtue;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Virtue Usage";

  }

  /**
  * #uncommented
  * See [[GenericFormTabComponent.init]] for generic info
  * @param
  *
  * @return
  */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpParentTable();
  }

  /**
   * #uncommented
   * See [[GenericFormTabComponent.setUp]] for generic info
   * @param
   *
   * @return
   */
  setUp(item: Item): void {
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-usage-tab which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;
  }

  /**
  * #uncommented
   * See [[GenericFormTabComponent.update]] for generic info
  * @param
  *
  * @return
  */
  update(changes: any): void {
    if (changes.mode) {
      this.setMode(changes.mode);
    }

    if (changes.allUsers) {
      let allUsers: DictList<Item> = changes.allUsers;
      this.parentTable.items = [];

      for (let u of allUsers.asList()) {
        if (u.children.has(this.item.getID())) {
          this.parentTable.items.push(u);
        }
      }
    }

  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getParentColumns(): Column[] {
    return [
      new Column('name',        'Username',         3, 'asc',     undefined,       undefined, (i: Item) => this.viewItem(i)),
      new Column('childNames',  'Attached Virtues', 5, undefined, this.formatName, this.getChildren, (i: Item) => this.viewItem(i)),
      new Column('status',      'Account Status',   4, 'desc',    this.formatStatus)
    ];
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getParentOptionsList(): RowOptions[] {
    return [
       new RowOptions("View", () => true, (i: Item) => this.viewItem(i))
    ];
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  setUpParentTable(): void {
    this.parentTable.setUp({
      cols: this.getParentColumns(),
      opts: [],
      coloredLabels: false,
      filters: [],
      tableWidth: 8,
      noDataMsg: "No users have been assigned this Virtue at the moment.",
      hasCheckBoxes: false
    });
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  collectData(): boolean {
    // do nothing at the moment - nothing about item can be changed from this
    // page at the moment, so no changes to collect.
    return true;
  }

}
