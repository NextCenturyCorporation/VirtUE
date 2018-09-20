import { Component, OnInit, ViewChild } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { Column } from '../../models/column.model';
import { SubMenuOptions } from '../../models/subMenuOptions.model';
import { DictList } from '../../models/dictionary.model';

import { GenericDataPageComponent } from '../gen-data-page/gen-data-page.component';
import { GenericTableComponent } from '../gen-table/gen-table.component';

import { Item } from '../../models/item.model';
import { User } from '../../models/user.model';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';

import { Mode } from '../../enums/enums';

@Component({
  templateUrl: './gen-list.component.html',
  providers: [ BaseUrlService, ItemService, GenericTableComponent ]
})
export abstract class GenericListComponent extends GenericDataPageComponent implements OnInit {

  /** The table itself */
  @ViewChild(GenericTableComponent) table: GenericTableComponent;

  prettyTitle: string;
  itemName: string;
  pluralItem: string;

  /**
   * One of:
   *  '/users'
   *  '/virtues'
   *  '/vm-templates'
   *  '/applications'
   * Parsed out of url path
   * Only used in the list html page though, in the create-new-item button
   */
  domain: string;

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
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

    // pull the domain, e.g. '/users', '/virtues', etc., out of the url.
    // Used only when to navigate to the create page for this type of item, via the 'Add new {{itemName}}' button.
    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
    this.domain = '/' + url.split('/')[0];
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    this.fillTable();
  }


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
      selectedIDs: this.getSelectedIDs()
    });
  }

  /**
   * Most lists don't allow selection
   *
   * @return a list of item IDs that should be initialized as 'selected' when the table builds.
   */
  getSelectedIDs(): string[] {
    return [];
  }

  // overridden by everything that lists virtues
  hasCheckbox() {
    return false;
  }

  // abstracts away table from subclasses
  setItems(newItems: Item[]) {
    this.table.items = newItems;
  }

  /**
   * This specifies a set of filters for all the list pages, where
   * Currently filtering can only be applied based on the "enabled" column, but eventually
   * being able to apply (perhaps arbitrary) filters to any column would be useful.
   *
   * When a filter label is clicked, its value (as specified below) is passed to [[ListFilterPipe]] within the table
   * object, which will filter out any Item with a different status, unless the value is '*', in which case nothing
   * is filtered out, and the list is merely sorted.
   *
   * While this function may seem unnecessary given that acceptable filter values are hardcoded in [[ListFilterPipe]],
   * there are some pages which need no status filters ([[AppsListComponent]]), and override this function to return an empty list.
   * Eventually this should be fixed to allow arbitrary filters on any column, with ListFilterPipe changed accordingly.
   *
   * @return a list of filter options to be displayed at the top of the list page.
   */
  getTableFilters(): {value: string, text: string}[] {
    return [{value: '*', text: 'All ' + this.pluralItem},
            {value: 'true', text: 'Enabled ' + this.pluralItem},
            {value: 'false', text: 'Disabled ' + this.pluralItem}];
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

  abstract getListOptions(): {
      prettyTitle: string,
      itemName: string,
      /** used to reference collections of this type of Item, in a shortened form, in the filter labels #TODO*/
      pluralItem: string};

  // must be here so subclasses of list, which use table, can set table values.
  abstract getNoDataMsg(): string;

  /**
   * This defines the submenu that appears under each label in the first column of the table.
   *
   * see [[GenericPageComponent.openDialog]] for notes on that openDialog call.
   *
   * Overridden by apps-list and modals
   *
   * @return a set of clickable links which show up on every item in the table.
   */
  getSubMenu(): SubMenuOptions[] {
    return [
      new SubMenuOptions("Enable",  (i: Item) => !i.enabled, (i: Item) => this.toggleItemStatus(i)),
      new SubMenuOptions("Disable", (i: Item) => i.enabled, (i: Item) => this.toggleItemStatus(i)),
      new SubMenuOptions("Edit",    () => true,             (i: Item) => this.editItem(i)),
      new SubMenuOptions("Duplicate", () => true,           (i: Item) => this.dupItem(i)),
      new SubMenuOptions("Delete",  () => true,             (i: Item) => this.openDialog('Delete ' + i.getName(), () => this.deleteItem(i)))
    ];
  }

  /**
   * @return whether or not the items being listed have colored labels. True for, and only for, all tables that list virtues.
   * overridden by virtue-list, virtues-modal, main-user-tab, vm-usage-tab, and virtue-settings
   */
  hasColoredLabels(): boolean {
    return false;
  }

}
