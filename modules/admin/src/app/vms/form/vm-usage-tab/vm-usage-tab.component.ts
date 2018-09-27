import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../../shared/services/baseUrl.service';
import { ItemService } from '../../../shared/services/item.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';

import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../../shared/services/config-urls.enum';
import { Datasets } from '../../../shared/abstracts/gen-data-page/datasets.enum';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';

/**
* @class
 * This class represents a tab in [[VMComponent]], listing places this VM template has been used
 *
 * It holds two tables:
 *    - Virtues that have been assigned this template
 *    - Running VMs that have been built from this template (currently unimplemented)
 *
 * @extends [[GenericFormTabComponent]]
 */
@Component({
  selector: 'app-vm-usage-tab',
  templateUrl: './vm-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css']
})
export class VmUsageTabComponent extends GenericFormTabComponent implements OnInit {

  /** A table listing what virtues have been given access to this VM template */
  @ViewChild('parentTable') private parentTable: GenericTableComponent;

  /**
   * this would show the running virtues that have been built from this template.
   * This may be unnecessary/unteneble. It could be a lot.
   * Tables may need filters.
   */
  @ViewChild('usageTable') private usageTable: GenericTableComponent;

  /** re-classing item, to make it easier and less error-prone to work with. */
  protected item: VirtualMachine;

  /**
   * see [[GenericFormTabComponent.constructor]] for inherited parameters
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Virtual Machine Usage";

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
   * @param item a reference to the Item being viewed/edited in the [[VirtualMachineComponent]] parent
   */
  setUp(item: Item): void {
    if ( !(item instanceof VirtualMachine) ) {
      // TODO throw error
      console.log("item passed to vm-usage-tab which was not a VirtualMachine: ", item);
      return;
    }
    this.item = item as VirtualMachine;
  }

  /**
   * See [[GenericFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well the contents of [[parentTable]]
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated, and/or an attribute `allVirtues: DictList<Item>`
   *                if parentTable is to be updated.
   *                Either attribute is optional.
   */
  update(changes: any): void {
    if (changes.allVirtues) {
      let allVirtues: DictList<Item> = changes.allVirtues;
      this.parentTable.items = [];

      for (let v of allVirtues.asList()) {
        if (v.children.has(this.item.getID())) {
          this.parentTable.items.push(v);
        }
      }
    }

    if (changes.mode) {
      this.setMode(changes.mode);
      this.parentTable.colData = this.getParentColumns();
      this.parentTable.subMenuOptions = this.getParentSubMenu();
    }
  }

  /**
   * @return what columns should show up in [[parentTable]]
   *         Links to the parent and the parent's children should only be clickable if not in view mode.
   */
  getParentColumns(): Column[] {
    let cols = [
      new Column('version',     'Version',       2, 'asc'),
      new Column('enabled',      'Status',        3, 'asc',    this.formatStatus)
    ];
    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('childNames',  'Attached VMs',  3, undefined, this.formatName, this.getChildren, (i: Item) => this.viewItem(i)));
      cols.unshift(new Column('name',        'Template Name', 4, 'asc',    undefined, undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('childNames',  'Attached VMs',  3, undefined, this.formatName, this.getChildren));
      cols.unshift(new Column('name',        'Template Name', 4, 'asc'));
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
      coloredLabels: true,
      filters: [],
      tableWidth: 8,
      noDataMsg: "No virtue template has been assigned this virtual machine template at the moment.",
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
