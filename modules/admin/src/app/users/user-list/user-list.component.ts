import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import {  Column,
          TextColumn,
          ListColumn,
          SORT_DIR  } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ItemListComponent } from '../../shared/abstracts/item-list/item-list.component';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * This class represents a table of users, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 * Links to the view pages for each user's assigned virtues are listed.
 *
 * Also allows the creation of new Users.
 *
 * @extends ItemListComponent
 */
@Component({
  selector: 'app-user-list',
  templateUrl: '../../shared/abstracts/item-list/item-list.component.html',
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css'],
  providers: [ BaseUrlService, DataRequestService  ]
})
export class UserListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, dataRequestService, dialog);
  }

  /**
   * called after all the datasetNames have loaded. Pass the user list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.datasets[DatasetNames.USERS].asList());
  }

  /**
   * @return a list of the columns to show up in the table. See details in [[GenericListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Username',           3, (i: Item) => i.getName(), SORT_DIR.ASC, (i: Item) => this.viewItem(i),
                                                                                                () => this.getSubMenu()),
      new ListColumn('Available Virtues',  4, (i: Item) => this.getVirtues(i), this.formatName, (i: Item) => this.viewItem(i)),
      new TextColumn('Authorized Roles',   3, this.formatRoles, SORT_DIR.ASC),
      new TextColumn('Account Status',     2, this.formatStatus, SORT_DIR.DESC)
    ];
  }

  /**
   * See [[GenericDataPageComponent.getDataPageOptions]]
   * @return child-specific information needed by the generic page functions when loading data.
   */
  getDataPageOptions(): {
      neededDatasets: DatasetNames[]} {
    return {
      neededDatasets: [DatasetNames.VIRTUES, DatasetNames.USERS]
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

}
