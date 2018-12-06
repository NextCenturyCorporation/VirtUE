import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
// import { MatTabsModule } from '@angular/material/tabs';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { RouterService } from '../../../shared/services/router.service';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { ItemFormTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component';

/**
* @class
 * This class represents a tab in [[VirtueComponent]], listing places this Virtue template has been used
 *
 * It holds two tables:
 *    - Users that have been granted this template
 *    - Virtue instances that have been built from this template (currently unimplemented)
 *
 * @extends [[ItemFormTabComponent]]
 */
@Component({
  selector: 'app-virtue-usage-tab',
  templateUrl: './virtue-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component.css']
})
export class VirtueUsageTabComponent extends ItemFormTabComponent implements OnInit {

  /** A table listing what users have been given access to this Virtue template */
  @ViewChild('parentTable') private parentTable: GenericTableComponent<User>;

  /** #uncommented unimplemented */
  @ViewChild('usageTable') private usageTable: GenericTableComponent<Number>;

  /** re-classing item, to make it easier and less error-prone to work with. */
  protected item: Virtue;

  /**
   * see [[ItemFormTabComponent.constructor]] for inherited parameters
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog) {
    super(routerService, dialog);
    this.tabName = "Virtue Usage";

  }

  /**
   * See [[ItemFormTabComponent.init]] for generic info
   *
   * @param mode the [[Mode]] to set up the page in.
   */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpParentTable();
    this.setUpUsageTable();
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
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
   * See [[ItemFormTabComponent.update]] for generic info
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
      this.setUpParentTable();
    }

    if (changes.allUsers) {
      let items: Item[] = [];
      let allUsers: DictList<User> = changes.allUsers;

      for (let u of allUsers.asList()) {
        if (u.virtueTemplates.has(this.item.getID())) {
          items.push(u);
        }
      }
      this.parentTable.populate(items);
    }

  }

  /**
   * @return what columns should show up in [[parentTable]]
   *         Links to the parent and the parent's children should only be clickable if not in view mode.
   */
  getParentColumns(): Column[] {
    return [
      new TextColumn('Username',  3, (u: User) => u.getName(), SORT_DIR.ASC, (u: User) => this.viewItem(u),
                                                                               () => this.getParentSubMenu()),
      new ListColumn('Attached Virtues', 5, (u: User) => u.getVirtues(),  this.formatName, (v: Virtue) => this.viewItem(v)),
      new TextColumn('Status',  4, this.formatStatus, SORT_DIR.ASC)
    ];

  }

  /**
   * @return a list of links to show up as a submenu on each parent. Links are to edit the parent, or
   *         view the parent. Only show this list if page is in view mode.
   */
  getParentSubMenu(): SubMenuOptions[] {
    if (this.mode === Mode.VIEW) {
      return [
        new SubMenuOptions("View", () => this.inViewMode(), (i: Item) => this.viewItem(i)),
        new SubMenuOptions("Edit", () => this.inViewMode(), (i: Item) => this.editItem(i))
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
      filters: [],
      tableWidth: 0.66,
      noDataMsg: "No users have been assigned this Virtue at the moment.",
      elementIsDisabled: (u: User) => !u.enabled,
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
   * Sets up the table of parents
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpUsageTable(): void {
    this.usageTable.setUp({
      cols: [],
      filters: [],
      tableWidth: 0.66,
      noDataMsg: "Not yet implemented",
      editingEnabled: () => !this.inViewMode()
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
