import { Component, OnInit } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Column } from '../../models/column.model';
import { SubMenuOptions } from '../../models/subMenuOptions.model';
import { DictList } from '../../models/dictionary.model';

import { GenericPageComponent } from '../gen-page/gen-page.component';

import { Item } from '../../models/item.model';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';


/********************************************
Using this table needs three things:
  1. Have a table object. Include it in the html file via:
          <app-item-table #table></app-item-table>
        and in the parent .ts
          @ViewChild(GenericTable) table: GenericTable;
    --remember doing it this way means the table gets instantiated when the
      containing component's ngOnInit runs, not during the component's constructor.

  2. call table.setUp(), once the necessary data is available
      (generally is by the time of ngOnInit)

  3. set 'items' to an Item[], once the desired item list is available. It's expected
      that the data won't be there instantaneously, but angular should update the table
      whenever it arrives.


********************************************/
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

  /**
   * This is a list of the clickable links/options that show up under the element in the
   * first column of each row in the table
   */
  subMenuOptions: SubMenuOptions[];

  /**
   * This list is what gets actually displayed in the table.
   *
   * Table will automatically update once this is set, and will display an no-data-message in the meantime.
   */
  items: Item[];

  // used to put a colored bar for everywhere virtues show up
  hasColoredLabels: boolean;

  // used to put checkboxes on the table and allow selection, within modals
  hasCheckbox: boolean;

  filterOptions: {text: string, value: string}[];

  /**
   * Currently only the status/enabled column can be filtered on.
   */
  filterColumn: string = 'enabled';

  /** The current value the table should be filtered by - only matching items are displayed. */
  filterValue: string = '*';

  filterCondition = (attribute: any) => {return String(attribute) === this.filterValue || this.filterValue === '*'};

  /**
   * Call to re-render the table on a change to filterValue.
   * this just gets toggled, and is passed into the listFilterSort pipe where it is ignored.
   * The fact that its value changes though, makes angular re-render the table, filtering it based on the currect criteria.
   */
  update: boolean = false;

  /** The message that should show up intead of any table data, when [[items]] is undefined or empty. */
  noDataMessage: string;

  tableWidth: number;

  // these are the default properties the list sorts by
  sortColumn: Column;

  /**
   * Whether the table should be sorted in an ascending or descending pattern (valid values are 'asc'|'desc')
   * Should/could be an enum. Change when the generic filter is implemented.
   */
  sortDirection: string = 'asc';

  // For modals and other possible components which allow the user to select
  // things from the list
  selectedIDs: string[];

  constructor() {
    // create meaningless empty column to prevent exceptions before setUp() is called by the parent component's ngOnInit
    this.sortColumn = new Column("", "", 0);
    this.colData = [this.sortColumn];
    this.filterOptions = [];
    this.items = [];
    this.subMenuOptions = [];
    this.tableWidth = 12; // default to take up full space in container
    this.hasCheckbox = false;
    this.selectedIDs = [];
  }

  // must be called by containing object, passing in all attributes the table
  // needs. items isn't passed in, because it usually isn't available when the table is built.
  // parameter is a single object so the callee has to see what element they're setting to what,
  // and because most elements are necessary.
  setUp(params: {
    cols: Column[];

    /** see this.[[subMenuOptions]] */
    opts: SubMenuOptions[];

    /** see this.[[hasColoredLabels]] */
    coloredLabels: boolean;
    filters: {value: string, text: string}[];
    tableWidth: number,
    noDataMsg: string,
    hasCheckBoxes: boolean,
    selectedIDs?: string[]}
  ): void {
    this.colData = params.cols;
    this.subMenuOptions = params.opts;
    this.hasColoredLabels = params.coloredLabels;
    this.filterOptions = params.filters;
    this.noDataMessage = params.noDataMsg;
    if ((params.tableWidth >= 1 && params.tableWidth <= 12)) {
      this.tableWidth = params.tableWidth;
    }

    this.hasCheckbox = params.hasCheckBoxes;

    if (params.selectedIDs) {
      this.selectedIDs = params.selectedIDs;
    }

    this.sortColumn = this.colData[0];
  }

  /**
   * @param id the id of the [[Item]] that we want to know is selected or not
   *
   * @return true iff the item with the given id is currently selected
   */
  isSelected(id: string): boolean {
    return this.selectedIDs.includes(id);
  }

  selectAll(checked) {
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
   * Adds or removes the Item with the given id from this.[[selectedIDs]]
   *
   * @param checked true if the user just checked the box, false if the user unchecked it.
   * @param id the id of the Item whose checkbox was just clicked
   */
  selectionChange(checked: boolean, id: string): void {
    if (checked === true) {
      this.selectedIDs.push(id);
    } else {
      this.selectedIDs.splice(this.selectedIDs.indexOf(id), 1);
    }
  }

  /**
   * Empties [[selectedIDs]]
   */
  clearSelections(): void {
    this.selectedIDs = [];
  }

  /**
   * sets the watched attribute [[filterValue]], causing angular to refresh the page
   * and run the filter/sorter again - which is called via the pipe '|' character
   * in gen-table.component.html
   *
   * @param filterValue the new value to filter the list by. Only matching items are kept, unless value is '*'.
   */
  filterList(filterValue: string): void {
    this.filterValue = filterValue;
    // The value of [[update]] is irrelevant - merely the fact that its value changes will refresh the table.
    this.update = !this.update;
  }

  /**
   * Called when the user clicks a sortable column's header label.
   * Columns are sortable iff their entries do not contain a list, and they have a default sort specified.
   * Sets sort direction to column default if the column is different from the current column being sorted on,
   * and reverses the sort direction if it's the same.
   *
   * @param sortColumn the column to sort the table by
   */
  setColumnSortDirection(sortColumn: Column): void {
    // If the user clicked the currently-active column
    if (this.sortColumn === sortColumn) {
      this.reverseSorting();
    } else {
      this.sortColumn = sortColumn;
    }
  }

  reverseSorting(): void {
    if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortDirection = 'asc';
    }
  }

}
