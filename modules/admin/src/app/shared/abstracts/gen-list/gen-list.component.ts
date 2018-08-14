import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Column } from '../../models/column.model';
import { DictList } from '../../models/dictionary.model';

import { GenericPageComponent } from '../gen-page/gen-page.component';

import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Item } from '../../models/item.model';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';

@Component({
  selector: 'gen-list',
  templateUrl: './gen-list.component.html',
  providers: [ BaseUrlService, ItemService  ]
})
export class GeneralListComponent extends GenericPageComponent implements OnInit {

  prettyTitle: string;
  itemName: string;
  pluralItem: string;

  //this list is what gets displayed in the table.
  items: Item[];

  //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
  // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
  colData: Column[];

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
    // super(router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);

    this.updateFuncQueue = [];
    this.items = [];

  }

  ngOnInit() {
    this.sortColumn = this.colData[0];
    this.baseUrlService.getBaseUrl().subscribe( res => {
      let awsServer = res[0].aws_server;
      this.itemService.setBaseUrl(awsServer);

      //no onComplete() is needed
      this.pullDatasets();
    });

    this.resetRouter();
  }

  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  // Can't use this until I find a way to access the {x}-list component from
  // within a format function. Passing in the scope to all format functions
  // seems like a hack, and it'd only be needed for this one function.
  // formatDate( item: Item): string {
  //     return scope.datePipe.transform(item.lastModification, 'short');
  // }

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

  //nothing to be done but pull the data, and then set 'this.items' in onComplete
  pullData(): void {
    this.pullDatasets();
    // this.pullDatasets2(this.onComplete);
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
        // console.log('Dialog Emitter: ' + targetObject.getID());
        if ( action === 'delete') {
          this.deleteItem(targetObject);
        }
        if (action === 'disable') {
          this.setItemStatus(targetObject, false);
        }
      }
    });
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
}
