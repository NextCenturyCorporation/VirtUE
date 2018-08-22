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
Using this table needs three things:
  1. Have a table object. Include it in the html file via:
          <item-table #table></item-table>
        and in the parent .ts
          @ViewChild(GenericTable) table: GenericTable;
    -remember doing it this way means the table gets instantiated when the parent's
     ngInit runs, not during the parent's constructor.

  2. call setUp(), once the necessary data is available
      (generally by the same time of ngInit)

  3. set 'items' to an Item[], once the item data has come back. It's expected
      that this won't be done instantaneously.


********************************************/
@Component({
  selector: 'item-table',
  templateUrl: './gen-table.component.html',
  styleUrls: ['./gen-table.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class GenericTable {

  //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
  // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
  colData: Column[];

  //this is a list of the links/options that show up under the element in the
  //first column of each row in the table
  rowOptions: RowOptions[];

  //this list is what gets displayed in the table.
  items: Item[];

  //used to put a colored bar for everywhere virtues show up
  hasColoredLabels: boolean;

  //used to put a checkbox on modals
  hasCheckbox: boolean;

  filterOptions: {text:string, value:string}[];

  noDataMessage: string;

  tableWidth: number;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  //For modals and other possible components which allow the user to select
  //things from the list
  selectedIDs: string[];

  constructor( ){
    //prevent error, until createTable() is called by ngOnInit
    this.sortColumn = new Column();
    this.colData = [this.sortColumn];
    this.filterOptions = [];
    this.items = [];
    this.rowOptions = [];
    this.tableWidth = 12; //default to take up full space in container
    this.hasCheckbox = false;
    this.selectedIDs = [];
  }

  //must be called by containing object, passing in all attributes the table
  //needs, except for items, which usually isn't available when the table is built
  //Passes an object so the callee has to see what element they're setting to what,
  //and because all elements are necessary.
  setUp(params:{
    cols: Column[];
    opts: RowOptions[];
    coloredLabels: boolean;
    filters: {value:string, text:string}[];
    tableWidth: number,
    noDataMsg:string,
    hasCB: boolean,
    selectedIDs?: string[]}
  ) {
    this.colData = params.cols;
    this.rowOptions = params.opts;
    this.hasColoredLabels = params.coloredLabels;
    this.filterOptions = params.filters;
    this.noDataMessage = params.noDataMsg;
    this.tableWidth = params.tableWidth;
    this.hasCheckbox = params.hasCB;

    if (params.selectedIDs) {
      this.selectedIDs = params.selectedIDs;
    }

    this.sortColumn = this.colData[0];
  }

  isSelected(id: string) {
    return this.selectedIDs.includes(id);
  }

  selectAll(checked) {
    if (checked) {
      for (let i of this.items) {
        this.selectedIDs.push(i.id);
      }
    } else {
      this.clearSelections();
    }
  }

  //called upon check/uncheck
  checkItem(checked: boolean, id: string) {
    if (checked === true) {
      this.selectedIDs.push(id);
    } else {
      this.selectedIDs.splice(this.selectedIDs.indexOf(id), 1);
    }
  }

  clearSelections() {
    this.selectedIDs = [];
  }

  callback(action: {(i:Item): any}, item:Item) {
    action(item);
  }

  //sets the watched attribute filterValue, causing angular to refresh the page
  //and run the filter/sorter again - which is called via the pipe '|' character
  // in gen-list.html
  filterList(filterValue: string): void {
    this.filterValue = filterValue;
  }

  setColumnSortDirection(sortColumn: Column, sortDirection: string): void {
    //If the user clicked the currently-active column
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
