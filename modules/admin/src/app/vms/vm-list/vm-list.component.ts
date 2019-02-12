import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import {  Column,
          TextColumn,
          ListColumn,
          SORT_DIR  } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { RouterService } from '../../shared/services/router.service';
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
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css']
})
export class VmListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(routerService, dataRequestService, dialog);
  }

  getDatasetToDisplay(): DatasetNames {
    return DatasetNames.VM_TS;
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[ItemListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name',           2, (v: VirtualMachine) => v.getName(),  SORT_DIR.ASC,
                                                                          (i: Item) => this.viewItem(i), () => this.getSubMenu()),
      new TextColumn('OS',                      1, (v: VirtualMachine) => v.os,         SORT_DIR.ASC),
      new ListColumn('Assigned Applications',   4, (v: VirtualMachine) => v.getApps(), this.formatName, (i: Item) => this.viewItem(i)),
      new TextColumn('Last Editor',             2, (v: VirtualMachine) => v.lastEditor, SORT_DIR.ASC),
      // new TextColumn('Version',           1, (v: VirtualMachine) => String(v.version),  SORT_DIR.ASC),
      new TextColumn('Modification Date',       2, (v: VirtualMachine) => v.readableModificationDate,    SORT_DIR.DESC),
      new TextColumn('Status',                  1, this.formatStatus,                   SORT_DIR.ASC)

    ];
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS, DatasetNames.VM_TS];
  }


  /**
   * See [[ItemListComponent.getListOptions]] for details
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
