import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { SubMenuOptions } from '../../shared/models/subMenuOptions.model';

import { VirtualMachineInstance, VmState } from '../../shared/models/vm-instance.model';
import { VirtueInstance, VirtueState } from '../../shared/models/virtue-instance.model';

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
 * This class represents a table of running Virtues
 *
 *
 * @extends ItemListComponent
 */
@Component({
  selector: 'app-virtue-instance-list',
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css'],
  template: `
  <div id="content-container">
    <div id="content-header" class="titlebar">
      <h1 class="titlebar-title">{{prettyTitle}}</h1>
    </div>
    <div id="content-main">
      <div id="content">
      <app-table #table></app-table>
      </div>
    </div>
  </div>`
})
export class VmInstanceListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(routerService, baseUrlService, dataRequestService, dialog);
  }

  getDatasetToDisplay(): DatasetNames {
    return DatasetNames.VMS;
  }

  defaultTableParams() {
    return {
      cols: this.getColumns(),
      filters: this.getTableFilters(),
      tableWidth: 1, // as a fraction of the parent object's width: a float in the range (0, 1].
      noDataMsg: this.getNoDataMsg()
    };
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[ItemListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name',   5, (v: VirtualMachineInstance) => v.getName(), SORT_DIR.ASC,  undefined,
                                                                                                    () => this.getSubMenu()),
      new TextColumn('State',           1,  (v: VirtualMachineInstance) => String(v.state), SORT_DIR.ASC),
      new TextColumn('os',              1,  (v: VirtualMachineInstance) => v.os,       SORT_DIR.ASC),
      new TextColumn('Hostname',           3,  (v: VirtualMachineInstance) => String(v.hostname), SORT_DIR.ASC)
    ];
  }

  getTableFilters(): {value: string, text: string}[] {
    return [];
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS, DatasetNames.VM_TS, DatasetNames.VMS];
  }

  getSubMenu(): SubMenuOptions[] {
    return [
      // new SubMenuOptions("View Template details",  () => true, (v: VirtueInstance) => {}),
      new SubMenuOptions("Stop",   (v: VirtualMachineInstance) => !v.isStopped(), (v: VirtualMachineInstance) => v.stop()),
    ];
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
      prettyTitle: 'Virtual Machine Instances',
      itemName: 'Vm',
      pluralItem: 'Vms'
    };
  }

  getNoDataMsg(): string {
    return "No virtual machine instances exist at this time.";
  }

}
