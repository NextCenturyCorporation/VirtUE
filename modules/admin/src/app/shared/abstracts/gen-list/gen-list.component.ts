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
import { UsersService } from '../../services/users.service';
import { VirtuesService } from '../../services/virtues.service';
import { ItemService } from '../../services/item.service';
import { VirtualMachineService } from '../../services/vm.service';
import { ApplicationsService } from '../../services/applications.service';

@Component({
  selector: 'app-gen-list',
  templateUrl: './gen-list.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ]
})
export class GeneralListComponent extends GenericPageComponent implements OnInit {

  prettyTitle: string;
  itemName: string;
  pluralItem: string;

  //this list is what gets displayed in the table.
  items: Item[];

  colData: Column[];

  domain: string; // like '/users', '/virtues', etc.

  noDataMessage: string;

  baseUrl: string;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  // protected router: Router;
  // protected baseUrlService: BaseUrlService;
  // protected usersService: UsersService;
  // protected virtuesService: VirtuesService;
  // protected vmService: VirtualMachineService;
  // protected appsService: ApplicationsService;
  // public dialog: MatDialog;

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    usersService: UsersService,
    virtuesService: VirtuesService,
    vmService: VirtualMachineService,
    appsService: ApplicationsService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);

    this.updateFuncQueue = [];
    this.items = [];
  }

  ngOnInit() {
    this.sortColumn = this.colData[0];
    this.baseUrlService.getBaseUrl().subscribe( res => {
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);

      this.pullDatasets();
    });

    //do we need this?
    // this.refreshData();
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

  //all the list pages need a full dataset on some set of item types; they don't
  //need to do any extra winnowing.
  pullData(): void {
    this.pullDatasets();
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
          this.disableItem(targetObject);
        }
      }
    });
  }

  deleteItem(i: Item) {}

  disableItem(i: Item) {}
}
