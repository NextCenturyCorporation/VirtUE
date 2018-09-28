import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { Column } from '../../../shared/models/column.model';

import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../../shared/services/config-urls.enum';
import { Datasets } from '../../../shared/abstracts/gen-data-page/datasets.enum';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

/**
 * @class
 * This class represents the main tab for a User form - [[UserComponent]]
 *
 * From here, the user (as in the system admin using this system, not [[User]] as in the object)
 * can view/add/remove the [[User]]'s attached virtues, can add/remove the User's roles, and can enabled/disable the User.
 *
 * Note that usernames have to be unique. This is currently only enforced on this front-end, which
 * should be rectified. The backend just takes usernames and makes a new entry if it hasn't seen that name before,
 * and overwrites the current entry for that name if it has.
 *
 * @extends [[GenericMainTabComponent]]
 */
@Component({
  selector: 'app-main-user-tab',
  templateUrl: './main-user-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})
export class UserMainTabComponent extends GenericMainTabComponent implements OnInit {

  /** whether or not this user has 'user' rights - I assume this is a temporary role #TODO */
  private roleUser: boolean;

  /** whether or not this user has 'admin' rights over something */
  private roleAdmin: boolean;

  /** re-classing parent's item object */
  protected item: User;

  /**
   * see [[GenericMainTabComponent.constructor]] for parameters
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
  }

  /**
   * See [[GenericFormTabComponent.setUp]] for generic info
   *
   * @param item a reference to the Item being displayed by this tab's parent form.
   */
  setUp(item: Item): void {
    if ( !(item instanceof User) ) {
      // TODO throw error
      console.log("item passed to main-user-tab which was not a User: ", item);
      return;
    }
    this.item = item as User;

    this.roleUser = this.item.roles.includes("ROLE_USER");
    this.roleAdmin = this.item.roles.includes("ROLE_ADMIN");
  }

  /**
   * See [[GenericFormTabComponent.collectData]]
   * records the user's roles.
   * #TODO add a check for username in create mode, to at least check for uniqueness.
   *
   * @return true always at the moment
   */
  collectData(): boolean {
    this.item.roles = [];
    if (this.roleUser) {
      this.item.roles.push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item.roles.push('ROLE_ADMIN');
    }
    return true;
  }

  /**
   * In view mode, make labels in Virtue and VMs columns clickable.
   * @return what columns should show up in the user's virtue children table
   */
  getColumns(): Column[] {
    let cols: Column[] = [
      new Column('apps',    'Assigned Applications',  3, undefined, this.formatName, this.getGrandchildren),
      new Column('version', 'Version',                2, 'asc'),
      new Column('enabled',  'Status',                 1, 'asc', this.formatStatus)
    ];

    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('vms', 'Virtual Machines', 3, undefined, this.formatName, this.getChildren, (i: Item) => this.viewItem(i)));
      cols.unshift(new Column('name', 'Virtue Template Name', 3, 'asc', this.formatName, undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('vms', 'Virtual Machines', 3, undefined, this.formatName, this.getChildren));
      cols.unshift(new Column('name', 'Virtue Template Name', 3, 'asc'));
    }

    return cols;
  }

  /**
   * @return a string to be displayed in the children table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No virtues have been added yet. To add a virtue, click on the button \"Add Virtue\" above.";
  }

  /**
   * @return true always, since this table holds virtues.
   */
  hasColoredLabels(): boolean {
    return true;
  }

  /**
   * Loads an VirtueModalComponent
   * @param parameters to be passed into the modal
   */
  getDialogRef(params: {
                          /** the height of the modal, in pixels */
                          height: string,
                          /** the width of the modal, in pixels */
                          width: string,
                          /** some type of data object to be passed into the modal - a container */
                          data: any
                        }) {
    return this.dialog.open( VirtueModalComponent, params);
  }
}
