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

import { ConfigUrls, Datasets } from '../../shared/enums/enums';

/**
 * @class
 * This class represents a table of users, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 * Links to the view pages for each user's assigned virtues are listed.
 *
 * Also allows the creation of new Users.
 *
 * @extends GenericListComponent
 */
@Component({
  selector: 'app-user-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class UserListComponent extends GenericListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
  }

  /**
   * called after all the datasets have loaded. Pass the user list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.allUsers.asList());
  }

  /**
   * @return a list of the columns to show up in the table. See details in [[GenericListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new Column('name',        'Username',           3, 'asc',     undefined, undefined, (i: Item) => this.viewItem(i)),
      new Column('childNames',  'Available Virtues',  4, undefined, this.formatName, this.getChildren, (i: Item) => this.viewItem(i)),
      new Column('roles',       'Authorized Roles',   3, 'asc',     this.formatRoles),
      new Column('status',      'Account Status',     2, 'desc',    this.formatStatus)
    ];
  }

  /**
   * See [[GenericPageComponent.getPageOptions]]
   * @return child-specific information needed by the generic page functions when loading data.
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.USERS,
      neededDatasets: [Datasets.VIRTUES, Datasets.USERS]
    };

  }

  /**
   * See [[GenericListComponent.getListOptions]] for details
   * @return child-list-specific information needed by the generic list page functions.
   */
  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string} {
    return {
      prettyTitle: 'Users',
      itemName: 'User',
      pluralItem: 'Users'
    };

  }

  /**
   * @return a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No users have been added at this time. To add a user, click on the button \"Add User\" above.";
  }

  /**
   * Used in a table column. Needs to be sorted before turned into a string, so that all entries in
   * that column follow the same order, and therefore sorting that column groups all users with the
   * same role-set together.
   *
   * @param user the user whose roles we want to format
   *
   * @return a string made from a sorted list of this user's roles.
   */
  formatRoles( user: User ): string {
    if (!user.roles) {
      return '';
    }
    return user.roles.sort().toString();
  }

  /**
   * Overrides parent, [[GenericPageComponent.toggleItemStatus]]. On the backend, vms/virtues/apps all only have a toggle function,
   * but users only have a setStatus function. So our itemService has both, and we have to call the right
   * one based on what type of item we're trying to toggle the status of.
   * That should be fixed, but is not critical.
   *
   * @param user the user whose status we wish to toggle.
   */
  toggleItemStatus(user: User): void {
    console.log(user);
    if (user.getName().toUpperCase() === 'ADMIN' && user.enabled) {
      this.openDialog('Disable ' + user.getName(), (() => this.setItemStatus(user, false)));
      // TODO: Remove this message when/if this is no longer applicable.
      return;
    }

    let sub = this.itemService.setItemStatus(this.serviceConfigUrl, user.getID(), !user.enabled).subscribe( () => {

    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      this.refreshData();
      sub.unsubscribe();
    });

  }

}
