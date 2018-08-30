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

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';

@Component({
  selector: 'app-vm-usage-tab',
  templateUrl: './vm-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css']
})

export class VmUsageTabComponent extends GenericFormTabComponent implements OnInit {

  @ViewChild('parentTable') parentTable: GenericTableComponent;

  // This may be unnecessary. It'd be a lot. Like if each user has an average of
  // 3, and you have 30 users, that's almost a hundred to scroll through.
  // Tables need filters.
  @ViewChild('usageTable') usageTable: GenericTableComponent;

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Virtual Machine Usage";

  }

  update(newData?: any) {
    if (newData) {
      if (newData.allVirtues) {
        let allVirtues: DictList<Item> = newData.allVirtues;
        this.parentTable.items = [];

        for (let u of allVirtues.asList()) {
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
    this.item = item;
  }

  init() {
    this.setUpParentTable();
  }

  getParentColumns(): Column[] {
    return [
      new Column('name',        'Template Name',    undefined, 'asc',     4, undefined, (i: Item) => this.viewItem(i)),
      // new Column('childNamesHTML',  'Attached VMs',     true, undefined,  3, this.getChildNamesHtml),
      new Column('childNames',  'Attached VMs', this.getChildren, undefined, 3, this.formatName, (i: Item) => this.viewItem(i)),
      new Column('version',     'Version',          undefined, 'asc',     2),
      new Column('status',      'Status',           undefined, 'asc',     3, this.formatStatus)
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
      opts: this.getParentOptionsList(),
      coloredLabels: true,
      filters: [],
      tableWidth: 8,
      noDataMsg: "No virtue template has been assigned this virtual machine template at the moment.",
      hasCheckBoxes: false
    });
  }

  collectData() {
    // do nothing at the moment - nothing about item can be changed from this
    // page, so no changes to collect.
  }

}
