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
import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../../shared/services/config-urls.enum';
import { Datasets } from '../../../shared/abstracts/gen-data-page/datasets.enum';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';

/**
* @class
 * This class represents a tab in [[VirtueComponent]], listing places this Virtue template has been used
 *
 * It holds two tables:
 *    - Users that have been granted this template
 *    - Virtue instances that have been built from this template (currently unimplemented)
 *
 * @extends [[GenericFormTabComponent]]
 */
@Component({
  selector: 'app-virtue-usage-tab',
  templateUrl: './virtue-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css']
})
export class VirtueUsageTabComponent extends GenericFormTabComponent implements OnInit {

  /** A table listing what users have been given access to this Virtue template */
  @ViewChild('parentTable') private parentTable: GenericTableComponent;

  /** re-classing item, to make it easier and less error-prone to work with. */
  protected item: Virtue;

  /**
   * see [[GenericFormTabComponent.constructor]] for inherited parameters
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Virtue Usage";

  }

  /**
   * See [[GenericFormTabComponent.init]] for generic info
   *
   * @param mode the [[Mode]] to set up the page in.
   */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpParentTable();
  }

  /**
   * See [[GenericFormTabComponent.setUp]] for generic info
   *
   * @param item a reference to the Item being viewed/edited in the [[VirtueComponent]] parent
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
   * See [[GenericFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well the contents of [[parentTable]]
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated, and/or an attribute `allUsers: DictList<Item>`
   *                if parentTable is to be updated.
   *                Either attribute is optional.
   */
  update(changes: any): void {
    if (changes.mode) {
      this.setMode(changes.mode);
      this.parentTable.colData = this.getParentColumns();
      this.parentTable.subMenuOptions = this.getParentSubMenu();
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
   * @return what columns should show up in [[parentTable]]
   *         Links to the parent and the parent's children should only be clickable if not in view mode.
   */
  getParentColumns(): Column[] {
    let cols = [
      new Column('enabled',      'Account Status',   4, 'desc',    this.formatStatus)
    ];

    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('childNames',  'Attached Virtues', 5, undefined, this.formatName, this.getChildren, (i: Item) => this.viewItem(i)));
      cols.unshift(new Column('name',        'Username',         3, 'asc',     undefined,       undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('childNames',  'Attached Virtues', 5, undefined, this.formatName, this.getChildren));
      cols.unshift(new Column('name',        'Username',         3, 'asc'));
    }
    return cols;
  }

  /**
   * @return a list of links to show up as a submenu on each parent. Links are to edit the parent, or
   *         view the parent. Only show this list if page is in view mode.
   */
  getParentSubMenu(): SubMenuOptions[] {
    if (this.mode === Mode.VIEW) {
      return [
        new SubMenuOptions("View", () => true, (i: Item) => this.viewItem(i)),
        new SubMenuOptions("Edit", () => true, (i: Item) => this.editItem(i))
      ];
    }
    else {
      return [];
    }
  }

  /**
   * Sets up the table of parents
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpParentTable(): void {
    this.parentTable.setUp({
      cols: this.getParentColumns(),
      opts: this.getParentSubMenu(),
      coloredLabels: false,
      filters: [],
      tableWidth: 8,
      noDataMsg: "No users have been assigned this Virtue at the moment.",
      hasCheckBoxes: false
    });
  }

  /**
   * Do nothing at the moment - nothing about item can be changed from this tab
   *
   * @return true
   */
  collectData(): boolean {
    return true;
  }

}
