import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import {  Column,
          TextColumn,
          ListColumn,
          SORT_DIR  } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { ItemListComponent } from '../../shared/abstracts/item-list/item-list.component';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * This class represents a table of Virtual Machines, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 * Also allows the creation of new Virtual Machines.
 *
 * Currently, all the applications available to each VM are listed, but apps don't have a view page yet so their names are
 * only displayed as text, instead of links.
 *
 * @extends ItemListComponent
 */
@Component({
  selector: 'app-vm-list',
  templateUrl: '../../shared/abstracts/item-list/item-list.component.html',
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css'],
  providers: [ BaseUrlService, DataRequestService  ]
})
export class VmListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, dataRequestService, dialog);
  }

  /**
   * called after all the datasets have loaded. Pass the vm list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.datasets[DatasetNames.VMS].asList());
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[GenericListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name',           2, (v: VirtualMachine) => v.getName(),  SORT_DIR.ASC,
                                                                          (i: Item) => this.viewItem(i), () => this.getSubMenu()),
      new TextColumn('OS',                      1, (v: VirtualMachine) => v.os,         SORT_DIR.ASC),
      new ListColumn('Assigned Applications',   4, (v: VirtualMachine) => this.getApps(v), this.formatName, (i: Item) => this.viewItem(i)),
      new TextColumn('Last Editor',             2, (v: VirtualMachine) => v.lastEditor, SORT_DIR.ASC),
      // new TextColumn('Version',           1, (v: VirtualMachine) => String(v.version),  SORT_DIR.ASC),
      new TextColumn('Modification Date',       2, (v: VirtualMachine) => v.modDate,    SORT_DIR.DESC),
      new TextColumn('Status',                  1, this.formatStatus,                   SORT_DIR.ASC)

    ];
  }

  /**
   * See [[GenericDataPageComponent.getDataPageOptions]]
   * @return child-specific information needed by the generic page functions when loading data.
   */
  getDataPageOptions(): {
      neededDatasets: DatasetNames[]} {
    return {
      neededDatasets: [DatasetNames.APPS, DatasetNames.VMS]
    };
  }


  /**
   * See [[GenericListComponent.getListOptions]] for details
   * @return child-list-specific information needed by the generic list page functions.
   */
  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string} {
    return {
      prettyTitle: 'Virtual Machine Templates',
      itemName: 'Vm Template',
      pluralItem: 'VMs'
    };
  }

  /**
   * @return a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return  "No vms have been added at this time. To add a vm, click on the button \"Add Vm Template\" above.";
  }


}
