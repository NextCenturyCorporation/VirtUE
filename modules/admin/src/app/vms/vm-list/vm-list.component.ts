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

import { ConfigUrls, Datasets } from '../../shared/enums/enums';

@Component({
  selector: 'app-vm-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class VmListComponent extends GenericListComponent {

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
  }

  // called after all the datasets have loaded
  onPullComplete(): void {
    this.setItems(this.allVms.asList());
  }

  getColumns(): Column[] {
    // This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    //  to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    //
    // Note: colWidths of all columns must add to exactly 12.
    // Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    return [
      new Column('name',        'Template Name',         2, 'asc', undefined, undefined, (i: Item) => this.viewItem(i)),
      new Column('os',          'OS',                    1, 'asc'),
      new Column('childNames',  'Assigned Applications', 4, undefined, this.formatName, this.getChildren),
      new Column('lastEditor',  'Last Modified By',      2, 'asc'),
      new Column('modDate',     'Modified Date',         2, 'desc'),
      new Column('enabled',      'Status',                1, 'asc', this.formatStatus)
    ];
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VMS,
      neededDatasets: [Datasets.APPS, Datasets.VMS]
    };
  }

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

  getNoDataMsg(): string {
    return  "No vms have been added at this time. To add a vm, click on the button \"Add Vm Template\" above.";
  }


}
