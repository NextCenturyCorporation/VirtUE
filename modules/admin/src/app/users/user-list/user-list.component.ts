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

import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrlEnum } from '../../shared/enums/enums';

@Component({
  selector: 'user-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class UserListComponent extends GenericListComponent implements OnInit  {

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);


  }

  //called after all the datasets have loaded
  onPullComplete(): void {
    this.setItems(this.allUsers.asList());
  }

  getColumns(): Column[] {
    //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    //
    //Note: colWidths of all columns must add to exactly 12.
    //Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    //See note next to a line containing "mui-col-md-12" in gen-list.component.html
    return [
      {name: 'name', prettyName: 'Username', isList: false, sortDefault: 'asc', colWidth:2, formatValue: undefined, link:(i:Item) => this.editItem(i)},
      {name: 'roles', prettyName: 'Authorized Roles', isList: false, sortDefault: 'asc', colWidth:3, formatValue: this.formatRoles},
      {name: 'childNamesHTML', prettyName: 'Available Virtues', isList: true, sortDefault: undefined, colWidth:4, formatValue: this.getChildNamesHtml},
      {name: 'status', prettyName: 'Account Status', isList: false, sortDefault: 'desc', colWidth:3, formatValue: this.formatStatus}
    ];
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["virtues", "users"]
    };

  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Users",
      itemName: "User",
      pluralItem: "Users",
      domain: '/users'
    };

  }

  getNoDataMsg(): string {
    return "No users have been added at this time. To add a user, click on the button \"Add User\" above.";
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

    let sub = this.itemService.setItemStatus(this.serviceConfigUrl, u.getID(), !u.enabled).subscribe( () => {

    },
    () => {},
    () => {//when finished
      this.refreshData();
      sub.unsubscribe();
    });

  }

}
