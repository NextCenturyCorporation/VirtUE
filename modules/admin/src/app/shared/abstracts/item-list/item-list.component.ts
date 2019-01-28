import { Component, OnInit, ViewChild } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { Column } from '../../models/column.model';
import { SubMenuOptions } from '../../models/subMenuOptions.model';
import { DictList } from '../../models/dictionary.model';

import { GenericDataPageComponent } from '../gen-data-page/gen-data-page.component';
import { GenericTableComponent } from '../gen-table/gen-table.component';

import { Item } from '../../models/item.model';
import { Toggleable } from '../../models/toggleable.interface';

import { RouterService } from '../../services/router.service';
import { BaseUrlService } from '../../services/baseUrl.service';
import { DataRequestService } from '../../services/dataRequest.service';

import { DatasetNames } from '../gen-data-page/datasetNames.enum';
import { Mode } from '../gen-form/mode.enum';

/**
* @class
 * This class represents a collection of Items in a table, to be viewed and interacted with.
 * It holds a GenericTableComponent, and allows for sorting, filtering, and selection.
 * The data displayed in each column for each item is defined by the subclass, and can be text, a list, or a
 * link.
 *
 * @extends GenericDataPageComponent because the derivative list pages need to load a known type of data from the backend.
 */
// @Component({
//   templateUrl: './item-list.component.html'
// })
export abstract class ItemListComponent extends GenericDataPageComponent implements OnInit {

  /** The table itself */
  @ViewChild(GenericTableComponent) table: GenericTableComponent<Item>;


  /** a string to appear as the list's title - preferably a full description */
  prettyTitle: string;

  /** used in a button label to create a new item: "Add {{itemName}}". It shouldn't be long. */
  itemName: string;

  /** used to reference collections of this type of Item in the filter option labels. Should be short. #TODO*/
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
  subdomain: string;

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(routerService, baseUrlService, dataRequestService, dialog);

    let params = this.getListOptions();

    this.prettyTitle = params.prettyTitle;
    this.itemName = params.itemName;
    this.pluralItem = params.pluralItem;

    // pull the top subdomain, e.g. '/users', '/virtues', etc., out of the url.
    // Used only when to navigate to the create page for this type of item, via the 'Add new {{itemName}}' button.
    this.subdomain = this.getSubdomain();
  }

  protected getSubdomain(): string {
    return '/' + this.routerService.getRouterUrlPieces()[0];
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    this.setUpTable();
  }

  /**
   * returns the default parameters for the table on a list page, using subclass-defined features.
   */
  defaultTableParams() {
    return {
      cols: this.getColumns(),
      filters: this.getTableFilters(),
      tableWidth: 1, // as a fraction of the parent object's width: a float in the range (0, 1].
      noDataMsg: this.getNoDataMsg(),
      elementIsDisabled: (i: Toggleable) => !i.enabled
    };
  }

  /**
   * Sets up the table of Items.
   *
   * If all subclass-lists' tables have an attribute, but require different values for it, then it should be set via a method in
   * [[defaultTableParams]].
   * If a page has a unique attribute that the other pages don't even need to see (like Virtue-list having a getColor field), then
   * that should be added in a customizeTableParams method, in that subclass. See [[VirtueListComponent.customizeTableParams]]
   */
  setUpTable(): void {
    if (this.table === undefined) {
      return;
    }
    let params = this.defaultTableParams();

    this.customizeTableParams(params);

    this.table.setUp(params);
  }

  /**
   * Allow children to customize the parameters passed to the table. By default, do nothing.
   * @param paramsObject the parameters to be passed to the table. see [[GenericTableComponent.setUp]]
   */
  customizeTableParams(paramsObject) {}


  /**
   * Populates the table with the input list of items.
   * Abstracts away table from subclasses
   *
   * @param newItems the list of items to be displayed in the table.
   */
  setItems(newItems: Item[]): void {
    this.table.populate(newItems);
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
   * This defines what columns show up in the table.
   *
   * Note: the summed widths of all columns must add to exactly 12.
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
      /** used to reference collections of this type of Item, in a shortened form, in the filter labels #TODO*/
      pluralItem: string};

  /**
   * @returns a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
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
      new SubMenuOptions("Enable",  (i: Item) => !i.enabled, (i: Item) => this.setItemAvailability(i, true)),
      new SubMenuOptions("Disable", (i: Item) => i.enabled, (i: Item) => this.setItemAvailability(i, false)),
      new SubMenuOptions("Edit",    () => true,             (i: Item) => this.editItem(i)),
      new SubMenuOptions("Duplicate", () => true,           (i: Item) => this.dupItem(i)),
      new SubMenuOptions("Delete",  () => true,             (i: Item) => this.openDialog('Delete ' + i.getName(), () => this.deleteItem(i)))
    ];
  }

}
