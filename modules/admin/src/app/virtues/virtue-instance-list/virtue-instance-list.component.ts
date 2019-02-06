import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { User } from '../../shared/models/user.model';
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
export class VirtueInstanceListComponent extends ItemListComponent {

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
    return DatasetNames.VIRTUES;
  }

  defaultTableParams() {
    return {
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1, // as a fraction of the parent object's width: a float in the range (0, 1].
      noDataMsg: this.getNoDataMsg()
    };
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[ItemListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name', 2, (v: VirtueInstance) => v.getName(), SORT_DIR.ASC,
                                                        (v: VirtueInstance) => this.toDetailsPage(v), () => this.getSubMenu()),
      new TextColumn('User',          2, (v: VirtueInstance) => v.user, SORT_DIR.ASC, (v: VirtueInstance) => this.viewUser(v)),
      new ListColumn('Active VMs',        2, (v: VirtueInstance) => v.getVms(),  this.formatName,
                                                        (vm) => this.viewVmInstance(vm)),
      // new ListColumn('Applications',      2, (v: VirtueInstance) => []),

      // put this in once you actually pull the template in
      // new TextColumn('Template Version',  1, (v: VirtueInstance) => v.getTemplateVersion(),  SORT_DIR.ASC),
      new TextColumn('State',            1,  (v: VirtueInstance) => String(v.state), SORT_DIR.ASC)
    ];
  }

  viewVmInstance(vm): void {
    this.toDetailsPage(new VirtualMachineInstance(vm));
  }

  viewUser(v: VirtueInstance): void {
    let owningUser = new User({username: v.user});
    this.viewItem(owningUser);
  }

  /**
   * add colors to the table defined in [[ItemListComponent]], since here it will be showing Virtues.
   */
  customizeTableParams(params): void {
    params['coloredLabels'] = true;
    params['getColor'] = (v: VirtueInstance) => v.color;
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS, DatasetNames.VMS, DatasetNames.VIRTUE_TS, DatasetNames.VIRTUES];
  }

  getSubMenu(): SubMenuOptions[] {
    return [
      new SubMenuOptions("View Template details",  () => true, (v: VirtueInstance) => this.viewItem(v.template)),
      new SubMenuOptions("Stop",   (v: VirtueInstance) => !v.isStopped(), (v: VirtueInstance) => this.stopVirtue(v)),
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
      prettyTitle: 'Virtue Instances',
      itemName: 'Virtue',
      pluralItem: 'Virtues'
    };
  }

  /**
   * @return a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No virtues are running at this time.";
  }

}
