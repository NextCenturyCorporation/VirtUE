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
export abstract class GenericListComponent extends GenericPageComponent implements OnInit {

  // The table itself
  @ViewChild(GenericTableComponent) table: GenericTableComponent;

  prettyTitle: string;
  itemName: string;
  pluralItem: string;
  domain: string; // like '/users', '/virtues', etc.

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
    this.domain = params.domain;
  }

  ngOnInit() {
    this.cmnComponentSetup();
    this.fillTable();
  }


  fillTable(): void {
    if (this.table === undefined) {
      return;
    }
    this.table.setUp({
      cols: this.getColumns(),
      opts: this.getOptionsList(),
      coloredLabels: this.hasColoredLabels(),
      filters: this.getTableFilters(),
      tableWidth: 12,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: this.hasCheckbox(),
      selectedIDs: this.getSelectedIDs()
    });
  }

  // most lists don't allow selection
  getSelectedIDs() {
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

  // not used by all subclasses - some don't have reason to filter
  getTableFilters(): {text: string, value: string}[] {
    return [{value: '*', text: 'All ' + this.pluralItem},
            {value: 'enabled', text: 'Enabled ' + this.pluralItem},
            {value: 'disabled', text: 'Disabled ' + this.pluralItem}];
  }

  abstract onPullComplete(): void;

  abstract getColumns(): Column[];

  abstract getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string};

  // must be here so subclasses of list, which use table, can set table values.
  abstract getNoDataMsg(): string;

  // overridden by app-list and modals
  getOptionsList(): RowOptions[] {
    return [
      new RowOptions("Enable",  (i: Item) => !i.enabled, (i: Item) => this.toggleItemStatus(i)),
      new RowOptions("Disable", (i: Item) => i.enabled, (i: Item) => this.toggleItemStatus(i)),
      new RowOptions("Edit",    () => true,             (i: Item) => this.editItem(i)),
      new RowOptions("Duplicate", () => true,           (i: Item) => this.dupItem(i)),
      new RowOptions("Delete",  () => true,             (i: Item) => this.openDialog('delete', i))
    ];
  }

  // overridden by virtues
  hasColoredLabels() {
    return false;
  }

  editItem(i: Item) {
    this.router.navigate([i.getPageRoute(Mode.EDIT)]);
  }

  dupItem(i: Item) {
    this.router.navigate([i.getPageRoute(Mode.DUPLICATE)]);
  }

  deleteItem(i: Item) {
    this.itemService.deleteItem(this.serviceConfigUrl, i.getID());
    this.refreshData(true);
  }

  // overriden by user-list, to perform function of setItemStatus method.
  // TODO Change backend so everything works the same way.
  // Probably just make every work via a setStatus method, and remove the toggle.
  toggleItemStatus(i: Item) {
    let sub = this.itemService.toggleItemStatus(this.serviceConfigUrl, i.getID()).subscribe(() => {
      this.refreshData();
    },
    error => {

    },
    () => {
      sub.unsubscribe();
    });
  }

  setItemStatus(i: Item, newStatus: boolean) {
    let sub = this.itemService.setItemStatus(this.serviceConfigUrl, i.getID(), newStatus).subscribe(() => {
      this.refreshData();
    },
    error => {

    },
    () => {
      sub.unsubscribe();
    });
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

    //  control goes here after either "Ok" or "Cancel" are clicked on the dialog
    let sub = dialogRef.componentInstance.dialogEmitter.subscribe((targetObject) => {

      if (targetObject !== 0 ) {

        if ( action === 'delete') {
          this.deleteItem(targetObject);
        }
        if (action === 'disable') {
          this.setItemStatus(targetObject, false);
        }
      }
    },
    () => {},
    () => {
      sub.unsubscribe();
    });
  }
}
