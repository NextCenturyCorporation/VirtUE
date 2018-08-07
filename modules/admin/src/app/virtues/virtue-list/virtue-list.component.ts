import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
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
  selector: 'app-virtue-list',
  templateUrl: '../../gen-list/gen-list.component.html',
  styleUrls: ['../../gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, UsersService, VirtuesService, VirtualMachineService, ApplicationsService  ]
})

export class VirtueListComponent extends GeneralListComponent {

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
      {name: 'vms', prettyName: 'Virtual Machines', isList: true, sortDefault: undefined, colWidth:2, formatValue: this.getChildrenListHTMLstring},
      {name: 'apps', prettyName: 'Applications', isList: true, sortDefault: undefined, colWidth:2, formatValue: this.getAppsListHTMLstring},
      {name: 'lastEditor', prettyName: 'Last Editor', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'version', prettyName: 'Version', isList: false, sortDefault: 'asc', colWidth:1, formatValue: undefined},
      {name: 'modDate', prettyName: 'Modification Date', isList: false, sortDefault: 'desc', colWidth:2, formatValue: undefined},
      {name: 'status', prettyName: 'Status', isList: false, sortDefault: 'asc', colWidth:1, formatValue: this.formatStatus}
    ];

    this.updateFuncQueue = [this.pullApps, this.pullVms, this.pullVirtues];

    this.prettyTitle = "Virtue Templates";
    this.itemName = "Virtue Template";
    this.pluralItem = "Virtues";
    this.noDataMessage = "No virtues have been added at this time. To add a virtue, click on the button \"Add " + this.itemName +  "\" above.";
    this.domain = '/virtues'
  }

  getAppsListHTMLstring(v: Virtue) {
    return v.appsListHTML;
  }

  toggleItemStatus(v: Virtue) {
    this.virtuesService.toggleVirtueStatus(this.baseUrl, v.getID()).subscribe();
    this.refreshData();
  }

  deleteItem(i: Item) {
    this.virtuesService.deleteVirtue(this.baseUrl, i.getID());
    this.refreshData();
  }
}
