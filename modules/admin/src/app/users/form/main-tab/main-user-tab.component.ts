import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { IndexedObj } from '../../../shared/models/indexedObj.model';
import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { Virtue } from '../../../shared/models/virtue.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';


import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../../shared/services/config-urls.enum';
import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { BaseUrlService } from '../../../shared/services/baseUrl.service';
import { DataRequestService } from '../../../shared/services/dataRequest.service';

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

  /** whether or not this user has 'user' rights - I assume this is a temporary role #TODO */
  private roleUser: boolean;

  /** whether or not this user has 'admin' rights over something */
  private roleAdmin: boolean;

  /** re-classing parent's item object */
  protected item: User;

  /**
   * see [[ItemFormMainTabComponent.constructor]] for parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(router, baseUrlService, dataRequestService, dialog);
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
      new TextColumn('Virtue Template Name', 3, (v: Virtue) => v.getName(), SORT_DIR.ASC, (i: Item) => this.viewItem(i),
                                                                                          () => this.getSubMenu()),
      new ListColumn('Virtual Machines', 3, (i: Item) => this.getVms(i), this.formatName, (i: Item) => this.viewItem(i)),
      new ListColumn('Available Apps', 4, (i: Item) => this.getVmApps(i),  this.formatName),
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


  /**
   * Removes childItem from this.item.virtueTemplates and its id from this.item.virtueTemplateIds.
   * Remember this.item is a user here, and childItem is a Virtue.
   *
   * @param childItem the Item to be removed from this.[[item]]'s child lists.
   * @override parent [[ItemFormMainTabComponent.removeChildObject]]()
   */
  removeChildObject(childObj: IndexedObj): void {
    if (childObj instanceof Virtue) {
      this.item.removeChild(childObj.getID(), DatasetNames.VIRTUES);
    }
    else {
      console.log("The given object doesn't appear to be a Virtue.");
    }
  }
}
