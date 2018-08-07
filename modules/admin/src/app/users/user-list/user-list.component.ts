import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList, Dict } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { ApplicationsService } from '../../shared/services/applications.service';

import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { GeneralListComponent } from '../../gen-list/gen-list.component';

@Component({
  selector: 'app-user-list',
  templateUrl: '../../gen-list/gen-list.component.html',
  styleUrls: ['../../gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, UsersService, VirtuesService, VirtualMachineService, ApplicationsService  ]
})
export class UserListComponent extends GeneralListComponent {

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
      {name: 'username', prettyName: 'Username', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'roles', prettyName: 'Authorized Roles', isList: false, sortDefault: 'asc', colWidth:3, formatValue: this.formatRoles},
      {name: 'virtues', prettyName: 'Available Virtues', isList: true, sortDefault: undefined, colWidth:4, formatValue: this.getChildrenListHTMLstring},
      {name: 'status', prettyName: 'Account Status', isList: false, sortDefault: 'desc', colWidth:3, formatValue: this.formatStatus}
    ];

    this.updateFuncQueue = [this.pullVirtues, this.pullUsers];

    this.prettyTitle = "Users";
    this.itemName = "User";
    this.pluralItem = "Users";
    this.noDataMessage = "No users have been added at this time. To add a user, click on the button \"Add " + this.itemName +  "\" above.";
    this.domain = '/users';
  }

  formatRoles( user: User ): string {
    if (!user.roles) {
      return '';
    }
    return user.roles.sort().toString();
  }

  // Overrides parent
  toggleItemStatus(u: User) {
    console.log(u);
    let newStatus = u.enabled ? 'disable': 'enable';
    if (u.getName().toUpperCase() === "ADMIN") {
      this.openDialog('disable', u);
      //// TODO: Remove this message when this no longer happens. When we stop funneling all requests through admin.
      return;
    }
    console.log("trying to", newStatus, u.getName());
    this.usersService.setUserStatus(this.baseUrl, u.getID(), newStatus).subscribe();

    this.refreshData();
  }

}
