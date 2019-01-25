import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';

import { IndexedObj } from '../../../shared/models/indexedObj.model';
import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { VirtualMachine } from '../../../shared/models/vm.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { RouterService } from '../../../shared/services/router.service';

import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';

import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { ItemFormMainTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-main-tab/item-form-main-tab.component';

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
 * @extends [[ItemFormMainTabComponent]]
 */
@Component({
  selector: 'app-main-user-tab',
  templateUrl: './main-user-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component.css']
})
export class UserMainTabComponent extends ItemFormMainTabComponent implements OnInit {

  /** whether or not this user has 'user' rights - I assume this is a temporary role #TODO
   * Must be public to be used in template html file in production mode.
   */
  public roleUser: boolean;

  /** whether or not this user has 'admin' rights over something
   * Must be public to be used in template html file in production mode.
   */
  public roleAdmin: boolean;

  /** re-classing parent's item object 
  * Must be public to be used in template html file in production mode.*/
  public item: User;

  /**
   * see [[ItemFormMainTabComponent.constructor]] for parameters
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog) {
    super(routerService, dialog);
    this.childDatasetName = DatasetNames.VIRTUES;
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
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
   * See [[ItemFormTabComponent.collectData]]
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
   * add colors to the child table defined in [[ItemFormMainTabComponent]], since here it will be showing Virtues.
   */
  customizeTableParams(params): void {
    params['coloredLabels'] = true;
    params['getColor'] = (v: Virtue) => v.color;
  }

  /**
   * In view mode, make labels in Virtue and VMs columns clickable.
   * @return what columns should show up in the user's virtue children table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Virtue Template Name', 3, (v: Virtue) => v.getName(), SORT_DIR.ASC, (v: Virtue) => this.viewItem(v),
                                                                                          () => this.getSubMenu()),
      new ListColumn('Virtual Machines', 3, (v: Virtue) => v.getVms(), this.formatName, (vm: VirtualMachine) => this.viewItem(vm)),
      new ListColumn('Available Apps', 4, (v: Virtue) => v.getVmApps(),  this.formatName),
      new TextColumn('Version', 1, (v: Virtue) => String(v.version), SORT_DIR.ASC),
    ];
  }

  /**
   * @return a string to be displayed in the children table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No virtues have been added yet. To add a virtue, click on the button \"Add Virtue\" above.";
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
