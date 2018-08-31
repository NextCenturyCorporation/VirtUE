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

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';
// import { GenericModalComponent } from '../../../modals/generic-modal/generic.modal';

@Component({
  selector: 'app-virtue-usage-tab',
  templateUrl: './virtue-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css']
})

export class VirtueUsageTabComponent extends GenericFormTabComponent implements OnInit {

  @ViewChild('parentTable') private parentTable: GenericTableComponent;

  protected item: Virtue;

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Virtue Usage";

  }

  update(newData?: any) {
    if (newData) {
      if (newData.allUsers) {
        let allUsers: DictList<Item> = newData.allUsers;
        this.parentTable.items = [];

        for (let u of allUsers.asList()) {
          if (u.children.has(this.item.getID())) {
            this.parentTable.items.push(u);
          }
        }
      }

      // other conditionals
    }
    else {
      // TODO show error
      console.log();
    }
  }

  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-usage-tab which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;
  }

  init() {
    this.setUpParentTable();
  }

  getParentColumns(): Column[] {
    return [
      new Column('name',        'Username',         undefined,        'asc',      3, undefined, (i: Item) => this.viewItem(i)),
      // new Column('childNamesHTML',  'Attached Virtues',  true, undefined, 5, this.getChildNamesHtml),
      new Column('childNames',  'Attached Virtues', this.getChildren, undefined,  5, this.formatName, (i: Item) => this.viewItem(i)),
      new Column('status',      'Account Status',   undefined,        'desc',     4, this.formatStatus)
    ];
  }

  getParentOptionsList(): RowOptions[] {
    return [
       new RowOptions("View", () => true, (i: Item) => this.viewItem(i))
    ];
  }

  setUpParentTable(): void {
    this.parentTable.setUp({
      cols: this.getParentColumns(),
      opts: [],
      coloredLabels: false,
      filters: [],
      tableWidth: 8,
      noDataMsg: "No users have been assigned this Virtue at the moment.",
      hasCheckBoxes: false
    });
  }

  collectData(): boolean {
    // do nothing at the moment - nothing about item can be changed from this
    // page, so no changes to collect.
    return true;
  }

}
