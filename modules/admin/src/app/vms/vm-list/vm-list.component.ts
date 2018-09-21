import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { Datasets } from '../../shared/abstracts/gen-data-page/datasets.enum';
import { ConfigUrls } from '../../shared/services/configUrls.enum';

/**
 * @class
 * This class represents a table of Virtual Machines, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 * Also allows the creation of new Virtual Machines.
 *
 * Currently, all the applications available to each VM are listed, but apps don't have a view page yet so their names are
 * only displayed as text, instead of links.
 *
 * @extends GenericListComponent
 */
@Component({
  selector: 'app-vm-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class VmListComponent extends GenericListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
  }

  /**
   * called after all the datasets have loaded. Pass the vm list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.allVms.asList());
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[GenericListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new Column('name',        'Template Name',         2, 'asc', undefined, undefined, (i: Item) => this.viewItem(i)),
      new Column('os',          'OS',                    1, 'asc'),
      new Column('childNames',  'Assigned Applications', 4, undefined, this.formatName, this.getChildren),
      new Column('lastEditor',  'Last Modified By',      2, 'asc'),
      new Column('modDate',     'Modified Date',         2, 'desc'),
      new Column('enabled',      'Status',                1, 'asc', this.formatStatus)
    ];
  }

  /**
   * See [[GenericPageComponent.getPageOptions]]
   * @return child-specific information needed by the generic page functions when loading data.
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VMS,
      neededDatasets: [Datasets.APPS, Datasets.VMS]
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
