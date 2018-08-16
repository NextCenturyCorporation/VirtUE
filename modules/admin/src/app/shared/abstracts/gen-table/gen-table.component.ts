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
  selector: 'gen-list',
  templateUrl: './gen-table.component.html',
  providers: [ BaseUrlService, ItemService  ]
})
export class GenericTable {


  //this list is what gets displayed in the table.
  items: Item[];

  noDataMessage: string;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  constructor(
    //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    protected colData: Column[],
    //this is a list of the links/options that show up under the element in the
    //first column of each row in the table
    protected rowOptions: RowOptions[]
  ) {

    this.items = [];

    this.sortColumn = this.colData[0];
  }


  //overridden by virtues
  hasColoredLabels() {
    return false;
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
