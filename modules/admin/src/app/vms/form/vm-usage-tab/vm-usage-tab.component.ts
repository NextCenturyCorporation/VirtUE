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

  @ViewChild('parentTable') private parentTable: GenericTableComponent;

  // usageTable would show the running virtues that have been built from this template.
  // This may be unnecessary/unteneble. It could be a lot.
  // Tables need filters.
  @ViewChild('usageTable') private usageTable: GenericTableComponent;

  protected item: VirtualMachine;

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Virtual Machine Usage";

  }

  update(changes: any) {
    if (changes.allVirtues) {
      let allVirtues: DictList<Item> = changes.allVirtues;
      this.parentTable.items = [];

      for (let u of allVirtues.asList()) {
        if (u.children.has(this.item.getID())) {
          this.parentTable.items.push(u);
        }
      }
    }

    if (changes.mode) {
      this.mode = changes.mode;
    }
  }

  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof VirtualMachine) ) {
      // TODO throw error
      console.log("item passed to vm-usage-tab which was not a VirtualMachine: ", item);
      return;
    }
    this.item = item as VirtualMachine;
  }

  init() {
    this.setUpParentTable();
  }

  getParentColumns(): Column[] {
    return [
      new Column('name',        'Template Name',    undefined, 'asc',     4, undefined, (i: Item) => this.viewItem(i)),
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

  collectData(): boolean {
    // nothing about item can be changed from this
    // page at the moment, so no changes to collect.
    return true;
  }

}
