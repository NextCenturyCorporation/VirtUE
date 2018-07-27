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

    this.prettyTitle = "Users";

    this.colData = [
      {name: 'username', prettyName: 'Username', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'roles', prettyName: 'Authorized Roles', isList: false, sortDefault: 'asc', colWidth:3, formatValue: this.formatRoles},
      {name: 'virtues', prettyName: 'Available Virtues', isList: true, sortDefault: undefined, colWidth:4, formatValue: this.getChildrenListHTMLstring},
      {name: 'status', prettyName: 'Account Status', isList: false, sortDefault: 'desc', colWidth:3, formatValue: this.formatStatus}
    ];

    this.prettyTitle = "Users";
    this.itemName = "User";
    this.noDataMessage = "No users have been added at this time. To add a user, click on the button \"Add User\" above.";
    this.domain = '/users';
    // this.showAllMessage = "All User Accounts";
    // this.showEnabledMessage = "Enabled Accounts";
    // this.showDisabledMessage = "Disabled Accounts";


  }

  // Overrides parent
  pullData() {
    // console.log("starting pull");
    // this.virtuesService.getVirtues(this.baseUrl).subscribe( virtues => {
    //     console.log("start virts");
    //     this.allVirtues.clear() // not sure if this is needed
    //     this.allVirtues = new DictList<Virtue>();
    //     let virt = null;
    //     for (let v of virtues) {
    //       virt = new Virtue(v);
    //       this.allVirtues.add(v.id, virt);
    //     }
    //     virt = null;
    //     virtues = null;
    //     console.log("done virts", this.allVirtues.length);
    //   },
    //   error => {},
    //   () => this.pullUsers()
    // );

    //this doesn't necessarily guarantee that users will be loaded afer the virtues
    //are.
    //It seems to always work though.
    //Could use a settimeout of 0 and call pullUsers on the completion though.

    this.pullVirtues(true);
    this.pullUsers(false);

  }

  formatRoles( user: User ): string {

    if (!user.roles) {
      return '';
    }
    return user.roles.sort().toString();
  }

  formatStatus( user: User ): string {
    return user.enabled ? 'Enabled' : 'Disabled';
  }

  getChildrenListHTMLstring( user: User ): string {
    return user.childrenListHTMLstring;
  }

  // Overrides parent
  toggleItemStatus(u: User) {
    console.log(u);
    let newStatus = u.enabled ? 'disable': 'enable';
    // console.log("here");
    if (u.getName().toUpperCase() === "ADMIN") {
      //// TODO: Remove this message when this no longer happens. When we stop funneling all requests through admin.
      // this.openDialog('disable', username + " and lock everyone out of the system?");
      if (!confirm("Are you sure to disable "+ u.getName() + " and lock everyone out of the system, including you?")) {
        console.log("Leaving " + u.getName() + " intact.");
        return;
      }
      console.log("Request to disable " + u.getName() + " ignored.");
      //we're not disabling that
      return;
    }
    //I don't know what 'data' is here, but it isn't a list of users.
    this.usersService.setUserStatus(this.baseUrl, u.getName(), newStatus).subscribe();//data => {
    //   this.users = data;
    //   // console.log(data);
    // });

    this.refreshData();
  }

  deleteUser(username: string) {
    // console.log(username);
    this.usersService.deleteUser(this.baseUrl, username);
    this.refreshData();
  }

}
