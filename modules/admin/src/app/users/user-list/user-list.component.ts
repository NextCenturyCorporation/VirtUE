import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtueInstance } from '../../shared/models/virtue-instance.model';
import {  Column,
          TextColumn,
          ListColumn,
          SORT_DIR  } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { RouterService } from '../../shared/services/router.service';
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
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css']
})
export class UserListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(routerService, dataRequestService, dialog);

  }

  getDatasetToDisplay(): DatasetNames {
    return DatasetNames.USERS;
  }

  /**
   * @return a list of the columns to show up in the table. See details in [[ItemListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Username',           2, (u: User) => u.getName(), SORT_DIR.ASC, (u: User) => this.viewItem(u),
                                                                                                () => this.getSubMenu()),
      new ListColumn('Active Virtues',  3, (u: User) => u.getActiveVirtues(), (v: VirtueInstance) => v.getLabel(),
                                                                            (v: VirtueInstance) => this.toDetailsPage(v)),
      new ListColumn('Available Virtues',  3, (u: User) => u.getVirtues(), this.formatName, (v: Virtue) => this.viewItem(v)),
      new TextColumn('Authorized Roles',   2, this.formatRoles),
      new TextColumn('Account Status',     1, this.formatStatus, SORT_DIR.DESC)
    ];
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.VIRTUE_TS, DatasetNames.VIRTUES, DatasetNames.USERS];

  }

  /**
   * See [[ItemListComponent.getListOptions]] for details
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
    return user.roles.sort().join(", ");
  }

}
