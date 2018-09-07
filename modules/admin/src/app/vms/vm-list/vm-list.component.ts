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

import { ConfigUrlEnum } from '../../shared/enums/enums';

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
      // {name: str, prettyName: str, isList: bool, sortDefault: str, colWidth: num, formatValue?: func, link?: func}
      new Column('name',        'Template Name',        undefined, 'asc',     2, undefined, (i: Item) => this.editItem(i)),
      new Column('os',          'OS',                   undefined, 'asc',     1),
      new Column('childNames',  'Assigned Applications', this.getChildren, undefined, 4, this.formatName),
      new Column('lastEditor',  'Last Modified By',     undefined, 'asc',     2, undefined),
      new Column('modDate',     'Modified Date',        undefined, 'desc',    2, undefined),
      new Column('status',      'Status',               undefined, 'asc',     1, this.formatStatus)
    ];
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.VMS,
      neededDatasets: ["apps", "vms"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Virtual Machine Templates",
      itemName: "Vm Template",
      pluralItem: "VMs",
      domain: '/vm-templates'
    };
  }

  getNoDataMsg(): string {
    return  "No vms have been added at this time. To add a vm, click on the button \"Add Vm Template\" above.";
  }


}
