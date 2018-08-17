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
import { GenericTable } from '../gen-table/gen-table.component';

import { Item } from '../../models/item.model';
import { User } from '../../models/user.model';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';


@Component({
  selector: 'gen-list',
  templateUrl: './gen-list.component.html',
  providers: [ BaseUrlService, ItemService, GenericTable ]
})
export class GenericListComponent extends GenericPageComponent implements OnInit {

  prettyTitle: string;
  itemName: string;
  pluralItem: string;

  //this list is what gets displayed in the table.
  items: Item[];

  //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
  // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
  colData: Column[];

  //The table itself
  //must be set in derived classes.
  @ViewChild(GenericTable) table: GenericTable;

  rowOptions: RowOptions[];

  domain: string; // like '/users', '/virtues', etc.

  noDataMessage: string;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  //show on all pages except apps-list
  showSortingAndEditOptions:boolean = true;

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);

    this.items = [];

    this.colData = this.getColumns();
    //default, overwritten by app-list
    this.rowOptions = this.getOptionsList();

  }

  ngOnInit() {
    this.sortColumn = this.colData[0];
    this.cmnComponentSetup();

    this.fillTable();
  }

  fillTable(): void {
    if (this.table === undefined) {
      return;
    }
    this.table.colData = this.getColumns();
    this.table.rowOptions = this.getOptionsList();
    this.table.hasColoredLabels = this.hasColoredLabels();
    this.table.noDataMessage = this.noDataMessage;
    this.table.filterOptions = [
      {value:'*', text:'All ' + this.pluralItem},
      {value:'enabled', text:'Enabled ' + this.pluralItem},
      {value:'disabled', text:'Disabled ' + this.pluralItem}];

    // this.table.setUp(stuff)
  }

  setItems(newItems: Item[]) {
    this.items = newItems;
    this.table.items = newItems;
  }

  //overridden by all children
  getColumns(): Column[] {
    return [];
  }

  //overridden by app-list
  getOptionsList(): RowOptions[] {
    return [
      new RowOptions("Enable", (i:Item) => !i.enabled, (i:Item) => this.toggleItemStatus(i)),
      new RowOptions("Disable", (i:Item) => i.enabled, (i:Item) => this.toggleItemStatus(i)),
      new RowOptions("Edit", () => true, (i:Item) => this.editItem(i)),
      new RowOptions("Duplicate", () => true, (i:Item) => this.dupItem(i)),
      new RowOptions("Delete", () => true, (i:Item) => this.openDialog('delete', i))
  ];
  }

  callback(action: {(i:Item): any}, item:Item) {
    console.log("here");
    action(item);
  }

  //overridden by virtues
  hasColoredLabels() {
    return false;
  }

  //used by many children to display their status
  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  //see comment by Item.childNamesHTML
  getChildNamesHtml( item: Item) {
    return item.childNamesHTML;
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

  openDialog(action: string, target: Item): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          actionType: action,
          targetObject: target
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    // control goes here after either "Ok" or "Cancel" are clicked on the dialog
    const dialogResults = dialogRef.componentInstance.dialogEmitter.subscribe((targetObject) => {

      if (targetObject !== 0 ) {

        if ( action === 'delete') {
          this.deleteItem(targetObject);
        }
        if (action === 'disable') {
          this.setItemStatus(targetObject, false);
        }
      }
    });
  }

  editItem(i: Item) {
    console.log("here");
    this.router.navigate([this.domain +"/edit/" + i.getID()]);
  }

  dupItem(i: Item) {
    console.log("here");
    this.router.navigate([this.domain +"/duplicate/" + i.getID()]);
  }

  deleteItem(i: Item) {
    this.itemService.deleteItem(this.serviceConfigUrl, i.getID());
    this.refreshData();
  }

  //overriden by user-list, to perform function of setItemStatus method.
  //TODO Change backend so everything works the same way.
  //Probably just make every work via a setStatus method, and remove the toggle.
  toggleItemStatus(i: Item) {
    this.itemService.toggleItemStatus(this.serviceConfigUrl, i.getID()).subscribe();
  }

  setItemStatus(i: Item, newStatus: boolean) {
    this.itemService.setItemStatus(this.serviceConfigUrl, i.getID(), newStatus).subscribe();
    this.refreshData();
  }

  //overridden by children
  onPullComplete(): void {}
}
