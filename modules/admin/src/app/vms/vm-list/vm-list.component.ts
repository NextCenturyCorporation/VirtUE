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
  selector: 'vm-list',
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

    this.serviceConfigUrl = ConfigUrlEnum.VMS;

    this.neededDatasets = ["apps", "vms"];

    this.prettyTitle = "Virtual Machine Templates";
    this.itemName = "Vm Template";
    this.pluralItem = "VMs";
    this.noDataMessage = "No vms have been added at this time. To add a vm, click on the button \"Add " + this.itemName +  "\" above.";
    this.domain = '/vm-templates';
  }

  getColumns(): Column[] {
    //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    //
    //Note: colWidths of all columns must add to exactly 12.
    //Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    //See note next to a line containing "mui-col-md-12" in gen-list.component.html
    return [
    {name: 'name', prettyName: 'Template Name', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined, link:(i:Item) => this.editItem(i)},
    {name: 'os', prettyName: 'OS', isList: false, sortDefault: 'asc', colWidth:1, formatValue: undefined},
    {name: 'childNamesHTML', prettyName: 'Assigned Applications', isList: true, sortDefault: undefined, colWidth:4, formatValue: this.getChildNamesHtml},
    {name: 'lastEditor', prettyName: 'Last Modified By', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
    {name: 'modDate', prettyName: 'Modified Date', isList: false, sortDefault: 'desc', colWidth:2, formatValue: undefined},
    {name: 'status', prettyName: 'Status', isList: false, sortDefault: 'asc', colWidth:1, formatValue: this.formatStatus}
    ];
  }
  //called after all the datasets have loaded
  onPullComplete(): void {
    setItems(this.allVms.asList());
  }

}
