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

@Component({
  selector: 'item-table',
  templateUrl: './gen-table.component.html',
  styleUrls: ['../gen-list/gen-list.component.css'],
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

  hasColoredLabels: boolean;

  filterOptions: {text:string, value:string}[];

  noDataMessage: string;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  constructor( ){
    //prevent error, until createTable() is called by
    this.sortColumn = new Column();
    this.filterOptions = [];
    this.items = [];
    this.rowOptions = [];

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
