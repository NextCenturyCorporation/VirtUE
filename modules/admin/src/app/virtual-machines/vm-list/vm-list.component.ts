import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { ApplicationsService } from '../../shared/services/applications.service';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { GeneralListComponent } from '../../gen-list/gen-list.component';

@Component({
  selector: 'app-vm-list',
  templateUrl: '../../gen-list/gen-list.component.html',
  styleUrls: ['../../gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, UsersService, VirtuesService, VirtualMachineService, ApplicationsService ]
})
export class VmListComponent extends GeneralListComponent {

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    usersService: UsersService,
    virtuesService: VirtuesService,
    vmService: VirtualMachineService,
    appsService: ApplicationsService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);

    //Note: colWidths of all columns must add to exactly 12.
    //Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    //See note next to a line containing "mui-col-md-12" in gen-list.component.html
    this.colData = [
      {name: 'name', prettyName: 'Template Name', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'os', prettyName: 'OS', isList: false, sortDefault: 'asc', colWidth:1, formatValue: undefined},
      {name: 'apps', prettyName: 'Assigned Applications', isList: true, sortDefault: undefined, colWidth:3, formatValue: this.getChildrenListHTMLstring},
      {name: 'lastEditor', prettyName: 'Last Modified By', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'securityTag', prettyName: 'Security', isList: false, sortDefault: 'asc', colWidth:1, formatValue: undefined},
      {name: 'modDate', prettyName: 'Modified Date', isList: false, sortDefault: 'desc', colWidth:2, formatValue: undefined},
      {name: 'status', prettyName: 'Status', isList: false, sortDefault: 'asc', colWidth:1, formatValue: this.formatStatus}
    ];

    this.updateFuncQueue = [this.pullApps, this.pullVms];

    this.prettyTitle = "Virtual Machine Templates";
    this.itemName = "Vm Template";
    this.pluralItem = "VMs";
    this.noDataMessage = "No vms have been added at this time. To add a vm, click on the button \"Add " + this.itemName +  "\" above.";
    this.domain = '/vm-templates';
  }

  deleteItem(i: Item) {
    this.vmService.deleteVM(this.baseUrl, i.getID());
    this.refreshData();
  }

  // Overrides parent
  toggleItemStatus(vm: VirtualMachine) {
    this.vmService.toggleVmStatus(this.baseUrl, vm.getID()).subscribe();
    this.refreshData();
  }

}
