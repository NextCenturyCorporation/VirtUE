import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../../shared/services/baseUrl.service';
import { ItemService } from '../../../shared/services/item.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrlEnum } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

@Component({
  selector: 'app-main-user-tab',
  templateUrl: './main-user-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})

export class UserMainTabComponent extends GenericMainTabComponent implements OnInit {

  private roleUser: boolean;
  private roleAdmin: boolean;

  private fullImagePath: string;

  // re-classing parent's object
  protected item: User;

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["apps", "vms", "virtues", "users"]
    };
  }

  init() {
    this.setUpChildTable();
  }

  update(changes: any) {
    this.childrenTable.items = this.item.children.asList();

    if (changes.mode) {
      this.mode = changes.mode;
    }
  }

  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof User) ) {
      // TODO throw error
      console.log("item passed to main-user-tab which was not a User: ", item);
      return;
    }
    this.item = item as User;

    this.roleUser = this.item.roles.includes("ROLE_USER");
    this.roleAdmin = this.item.roles.includes("ROLE_ADMIN");
  }

  collectData(): boolean {
    this.item.roles = [];
    if (this.roleUser) {
      this.item.roles.push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item.roles.push('ROLE_ADMIN');
    }
    return true;
  }

  getColumns(): Column[] {
    return [
      // See note in gen-form getOptionsList
      new Column('name',    'Virtue Template Name',   undefined,       'asc',     3, undefined, (i: Item) => this.viewItem(i)),
      new Column('vms',     'Virtual Machines',       this.getChildren, undefined, 3, this.formatName, (i: Item) => this.viewItem(i)),
      new Column('apps',    'Assigned Applications',  this.getGrandchildren, undefined, 3, this.formatName),
      new Column('version', 'Version',                undefined,        'asc',     2),
      new Column('status',  'Status',                 undefined,        'asc',     1, this.formatStatus)
    ];
  }

  getOptionsList(): RowOptions[] {
    return [
       new RowOptions("Edit", () => true, (i: Item) => this.viewItem(i)),
       new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
    ];
  }

  getNoDataMsg(): string {
    return "No virtues have been added yet. To add a virtue, click on the button \"Add Virtue\" above.";
  }

  setUpChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    this.childrenTable.setUp({
      cols: this.getColumns(),
      opts: this.getOptionsList(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }

  getDialogRef(params: {height: string, width: string, data: any}) {
    return this.dialog.open( VirtueModalComponent, params);
  }
}
