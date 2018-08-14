import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { GeneralListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrlEnum } from '../../shared/enums/enums';

@Component({
  selector: 'app-user-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class UserListComponent extends GeneralListComponent {

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);

    //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    //
    //Note: colWidths of all columns must add to exactly 12.
    //Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    //See note next to a line containing "mui-col-md-12" in gen-list.component.html
    this.colData = [
      {name: 'name', prettyName: 'Username', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'roles', prettyName: 'Authorized Roles', isList: false, sortDefault: 'asc', colWidth:3, formatValue: this.formatRoles},
      {name: 'childNamesAsHtmlList', prettyName: 'Available Virtues', isList: true, sortDefault: undefined, colWidth:4, formatValue: undefined},
      {name: 'status', prettyName: 'Account Status', isList: false, sortDefault: 'desc', colWidth:3, formatValue: this.formatStatus}
    ];

    this.serviceConfigUrl = ConfigUrlEnum.USERS;

    this.updateFuncQueue = [this.pullVirtues, this.pullUsers];
    this.neededDatasets = ["virtues", "users"];

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
    if (u.getName().toUpperCase() === "ADMIN") {
      this.openDialog('disable', u);
      //// TODO: Remove this message when this no longer happens. When we stop funneling all requests through admin.
      return;
    }

    this.itemService.setItemStatus(this.serviceConfigUrl, u.getID(), !u.enabled).subscribe();
    this.refreshData();
  }

  //called after all the datasets have loaded
  //scope is what you'd expect "this" to be. 'this' refers to the TODO here though.
  //TODO put above message on other onComplete definitions
  onComplete(scope): void {
    scope.items = scope.allUsers.asList();
  }

}
