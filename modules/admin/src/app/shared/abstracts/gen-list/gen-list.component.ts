import { Component, OnInit, ViewChild } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Column } from '../../models/column.model';
import { RowOptions } from '../../models/rowOptions.model';
import { DictList } from '../../models/dictionary.model';

import { GenericPageComponent } from '../gen-page/gen-page.component';
import { GenericTableComponent } from '../gen-table/gen-table.component';

import { Item } from '../../models/item.model';
import { User } from '../../models/user.model';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';

import { Mode } from '../../enums/enums';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  templateUrl: './gen-list.component.html',
  providers: [ BaseUrlService, ItemService, GenericTableComponent ]
})
export abstract class GenericListComponent extends GenericPageComponent implements OnInit {

  /** The table itself */
  @ViewChild(GenericTableComponent) table: GenericTableComponent;

  /** #uncommented */
  prettyTitle: string;

  /** #uncommented */
  itemName: string;

  /** #uncommented */
  pluralItem: string;

  /**
   * One of:
   *  '/users'
   *  '/virtues'
   *  '/vm-templates'
   *  '/applications'
   * Only used in the list html page though, in the create-new-item button
   */
  domain: string;

  /**
   * see parent
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
    let params = this.getListOptions();

    this.prettyTitle = params.prettyTitle;
    this.itemName = params.itemName;
    this.pluralItem = params.pluralItem;


    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
    this.domain = '/' + url.split('/')[0];
    console.log
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    this.cmnComponentSetup();
    this.fillTable();
  }

  /**
   * #uncommented
   */
  fillTable(): void {
    if (this.table === undefined) {
      return;
    }
    this.table.setUp({
      cols: this.getColumns(),
      opts: this.getSubMenu(),
      coloredLabels: this.hasColoredLabels(),
      filters: this.getTableFilters(),
      tableWidth: 12,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: this.hasCheckbox(),
      selectedIDs: []
    });
  }

  /**
   * @return whether or not the table needs checkboxes. False is default.
   * Override to change.
   */
  hasCheckbox(): boolean {
    return false;
  }


  /**
   * abstracts away table from subclasses
   *
   * @param newItems the list of items to be displayed in the table.
   */
  setItems(newItems: Item[]): void {
    this.table.items = newItems;
  }

  /**
   * This specifies a set of filters for all the list pages, where
   * Currently filtering can only be applied based on the "status" column, but eventually
   * being able to apply (perhaps arbitrary) filters to any column would be useful.
   *
   * When a filter label is clicked, its value (as specified below) is passed to ListPipe within the table
   * object, which will filter out any Item with a different status, unless the value is '*', in which case nothing
   * is filtered out, and the list is merely sorted.
   *
   * While this function may seem unnecessary given that the possible filter values are hardcoded in listPipe,
   * there are some pages which need no status filters (apps-list), and override this function to return an empty list.
   * Eventually this should be fixed to allow arbitrary filters on any column, with ListPipe changed accordingly.
   *
   * @return a list of filter options to be displayed at the top of the list page.
   */
  getTableFilters(): {text: string, value: string}[] {
    return [{value: '*', text: 'All ' + this.pluralItem},
            {value: 'enabled', text: 'Enabled ' + this.pluralItem},
            {value: 'disabled', text: 'Disabled ' + this.pluralItem}];
  }

  /**
   * This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
   * to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
   *
   * Note: colWidths of all columns must add to exactly 12.
   * Too low will not scale to fit, and too large will cause columns to wrap, within each row.
   *
   * @return a list of columns to be displayed within the table of Items.
   */
  abstract getColumns(): Column[];

  /**
   * This must be defined by every child that extends this class.
   * Allows the definition of child-list-specific information used in the list's html code.
   * @return object holding three strings
   */
  abstract getListOptions(): {
      /** a string to appear as the list's title - preferably a full description */
      prettyTitle: string,
      /** used in a label on a button to create a new item: "Add {{itemName}}". It shouldn't be long. */
      itemName: string,
      /** used to reference collections of this type of item, in a shortened form, in the filter labels #TODO filters*/
      pluralItem: string};

  /**
   * @returns a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  abstract getNoDataMsg(): string;

  /**
   * This defines the submenu that appears under each label in the first column of the table.
   *
   * Overridden by apps-list and modals
   *
   * @return a set of clickable links which show up on every item in the table.
   */
  getSubMenu(): RowOptions[] {
    return [
      new RowOptions("Enable",  (i: Item) => !i.enabled, (i: Item) => this.toggleItemStatus(i)),
      new RowOptions("Disable", (i: Item) => i.enabled, (i: Item) => this.toggleItemStatus(i)),
      new RowOptions("Edit",    () => true,             (i: Item) => this.editItem(i)),
      new RowOptions("Duplicate", () => true,           (i: Item) => this.dupItem(i)),
      new RowOptions("Delete",  () => true,             (i: Item) => this.openDialog('delete', i))
    ];
  }

  /**
   * @return whether or not the items being listed have colored labels. True for, and only for, all tables that list virtues.
   * overridden by virtue-list, virtues-modal, main-user-tab, vm-usage-tab, and virtue-settings
   */
  hasColoredLabels(): boolean {
    return false;
  }

  /**
   * overriden by user-list, to perform function of setItemStatus method.
   * TODO Change backend so everything works the same way.
   * Probably just make every work via a setStatus method, and remove the toggle.
   *
   * @param i the item we're toggling the status of.
   */
  toggleItemStatus(i: Item): void {
    let sub = this.itemService.toggleItemStatus(this.serviceConfigUrl, i.getID()).subscribe(() => {
      this.refreshData();
    },
    error => {

    },
    () => {
      sub.unsubscribe();
    });
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  setItemStatus(i: Item, newStatus: boolean): void {
    let sub = this.itemService.setItemStatus(this.serviceConfigUrl, i.getID(), newStatus).subscribe(() => {
      this.refreshData();
    },
    error => {

    },
    () => {
      sub.unsubscribe();
    });
  }

  /**
   * #uncommented
   * @param action what's being done: e.g. 'delete', 'disable'
   * @param target the item upon which the stated action would be performed.
   */
  openDialog(action: string, target: Item): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          actionType: action,
          targetName: target.getName()
        }
    });

    // TODO ask why the dialog is deliberately off-centered.
    dialogRef.updatePosition({ top: '15%', left: '36%' });

    //  control goes here after either "Ok" or "Cancel" are clicked on the dialog
    let sub = dialogRef.componentInstance.getResponse.subscribe((shouldProceed) => {
      console.log(shouldProceed);
      if (shouldProceed) {
        if ( action === 'delete') {
          this.deleteItem(target);
        }
        if (action === 'disable') {
          this.setItemStatus(target, false);
        }
      }
    },
    ()=>{},
    ()=>{
      sub.unsubscribe();
    });
  }
}
