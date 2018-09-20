import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

@Component({
  selector: 'app-main-user-tab',
  templateUrl: './main-user-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})
export class UserMainTabComponent extends GenericMainTabComponent implements OnInit {

  private roleUser: boolean;

  /** whether or not this user has 'admin' rights over something */
  private roleAdmin: boolean;

  // re-classing parent's object
  protected item: User;

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
