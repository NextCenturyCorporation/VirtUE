import { Component, OnInit } from '@angular/core';
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

import { Item } from '../../models/item.model';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';


/********************************************
 *
 * #uncommented
 * @class
 * @extends
 * 
 * Using this table needs three things:
 *   1. Have a table object. Include it in the html file via:
 *           <app-item-table #table></app-item-table>
 *         and in the parent .ts
 *           @ViewChild(GenericTable) table: GenericTable;
 *     --remember doing it this way means the table gets instantiated when the
 *       containing component's ngOnInit runs, not during the component's constructor.
 *
 *   2. call table.setUp(), once the necessary data is available
 *       (generally is by the time of ngOnInit)
 *
 *   3. set 'items' to an Item[], once the desired item list is available. It's expected
 *       that the data won't be there instantaneously, but angular should update the table
 *       whenever it arrives.
 *
 *
 * ********************************************/
@Component({
  selector: 'app-item-table',
  templateUrl: './gen-table.component.html',
  styleUrls: ['../gen-page/gen-page.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class GenericTableComponent {

  // This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
  //  to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
  colData: Column[];

  // this is a list of the links/options that show up under the element in the
  // first column of each row in the table
  rowOptions: RowOptions[];

  // this list is what gets displayed in the table.
  items: Item[];

  // used to put a colored bar for everywhere virtues show up
  hasColoredLabels: boolean;

  // used to put checkboxes on the table and allow selection, within modals
  hasCheckbox: boolean;

  filterOptions: {text: string, value: string}[];

  noDataMessage: string;

  tableWidth: number;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  // For modals and other possible components which allow the user to select
  // things from the list
  selectedIDs: string[];

  /**
   *
   */
  constructor() {
    // create meaningless empty column to prevent exceptions before createTable() is called by ngOnInit
    this.sortColumn = new Column("", "", 0);
    this.colData = [this.sortColumn];
    this.filterOptions = [];
    this.items = [];
    this.rowOptions = [];
    this.tableWidth = 12; // default to take up full space in container
    this.hasCheckbox = false;
    this.selectedIDs = [];
  }

  /**
   * Must be called by the object holding this table, passing in the parameters the table
   * needs. 'items' isn't passed in, because it usually isn't available when the table is built.
   *
   * @param params is a single object so the callee can (has to) see what element they're setting to what,
   * and because most elements are necessary.
   *
   */
  setUp(params: {
    cols: Column[];
    opts: RowOptions[];
    coloredLabels: boolean;
    filters: {value: string, text: string}[];
    tableWidth: number,
    noDataMsg: string,
    hasCheckBoxes: boolean,
    selectedIDs?: string[]}
  ): void {
    this.colData = params.cols;
    this.rowOptions = params.opts;
    this.hasColoredLabels = params.coloredLabels;
    this.filterOptions = params.filters;
    this.noDataMessage = params.noDataMsg;
    this.tableWidth = params.tableWidth;
    this.hasCheckbox = params.hasCheckBoxes;

    if (params.selectedIDs) {
      this.selectedIDs = params.selectedIDs;
    }

    this.sortColumn = this.colData[0];
  }

  /**
   * @param id the id of the Item that we want to know is selected or not
   *
   * @return true iff the Item is currently selected
   */
  isSelected(id: string) {
    return this.selectedIDs.includes(id);
  }

  /**
   * Called whenever the user checks or unchecks the "master" checkbox in the table's header.
   * Either adds all items to selectedIDs, or removes them all, as appropriate.
   *
   * @param checked true if the user just checked the box, false if the user unchecked it.
   */
  selectAll(checked): void {
    if (checked) {
      for (let i of this.items) {
        this.selectedIDs.push(i.getID());
      }
    } else {
      this.clearSelections();
    }
  }

  /**
   * Called whenever the user checks or unchecks the checkbox for a particular item.
   * Adds or removes the Item with the given id from this.selectedIDs
   *
   * @param checked true if the user just checked the box, false if the user unchecked it.
   * @param id the id of the Item whose checkbox was just clicked
   */
  checkClicked(checked: boolean, id: string): void {
    if (checked === true) {
      this.selectedIDs.push(id);
    } else {
      this.selectedIDs.splice(this.selectedIDs.indexOf(id), 1);
    }
  }

  /**
   * Empties this.selectedIDs
   */
  clearSelections() {
    this.selectedIDs = [];
  }

  /**
   * sets the watched attribute filterValue, causing angular to refresh the page
   * and run the filter/sorter again - which is called via the pipe '|' character
   * in gen-table.html
   *
   * @param filterValue the new value to filter the list by. Only matching items are kept, unless value is '*'.
   */
  filterList(filterValue: string): void {
    this.filterValue = filterValue;
  }

  /**
   * Called when the user clicks a sortable (non-list, with default sort specified) column's header label.
   * Sets sort direction to column default if the column is different from the current column being sorted on,
   * and reverses the sort direction if it's the same.
   *
   * @param sortColumn the column to sort the table by
   *
   */
  setColumnSortDirection(sortColumn: Column): void {
    // If the user clicked the currently-active column
    if (this.sortColumn === sortColumn) {
      this.reverseSorting();
    } else {
      this.sortColumn = sortColumn;
    }
  }

  /**
   * Toggles sortDirection between 'asc' and 'desc'. Could benefit from being made into enums.
   */
  reverseSorting(): void {
    if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortDirection = 'asc';
    }
  }

}
