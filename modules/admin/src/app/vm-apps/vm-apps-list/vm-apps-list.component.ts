import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { ApplicationsService } from '../../shared/services/applications.service';

import { Application } from '../../shared/models/application.model';
import { AddVmAppComponent } from '../add-vm-app/add-vm-app.component';

import { GeneralListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

@Component({
  selector: 'app-vm-apps-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, UsersService, VirtuesService, VirtualMachineService, ApplicationsService  ]
})
export class VmAppsListComponent extends GeneralListComponent {

  file: string;
  url: string;

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
      {name: 'name', prettyName: 'App Name', isList: false, sortDefault: 'asc', colWidth:5, formatValue: undefined},
      {name: 'version', prettyName: 'Version', isList: false, sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'os', prettyName: 'Operating System', isList: false, sortDefault: 'desc', colWidth:4, formatValue: undefined}
    ];

    this.updateFuncQueue = [this.pullVirtues, this.pullUsers];

    this.prettyTitle = "Available Applications";
    this.itemName = "Application";
    this.pluralItem = "Applications";
    this.noDataMessage = "No apps appear to be available at this time. To add an application, click on the button \"Add "
                          + this.itemName +  "\" above.";
    this.domain = '/applications';

    this.updateFuncQueue = [this.pullApps];

    this.showSortingAndEditOptions = false;
  }

  openAppsDialog(): void {
    let dialogRef = this.dialog.open(AddVmAppComponent, {
      width: '480px',
      data: { file: this.file, url: this.url }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
}
