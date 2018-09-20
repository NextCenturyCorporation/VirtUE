import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { ItemService } from '../shared/services/item.service';

import { VirtueModalComponent } from '../modals/virtue-modal/virtue-modal.component';

import { Item } from '../shared/models/item.model';
import { User } from '../shared/models/user.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { Virtue } from '../shared/models/virtue.model';
import { DictList } from '../shared/models/dictionary.model';
import { Column } from '../shared/models/column.model';
import { Mode, Datasets } from '../shared/enums/enums';

import { UserMainTabComponent } from './form/main-tab/main-user-tab.component';

import { ConfigUrls } from '../shared/enums/enums';

import { GenericFormComponent } from '../shared/abstracts/gen-form/gen-form.component';

/**
 *
 * @class
 * This class represents a detailed view of a User.
 * See comment on [[GenericFormComponent]] for generic info.
 *
 * This form has:
 *  - a main tab for viewing the Virtues made available to the User
 *
 * It also will in the future #TODO have:
 *  - a tab for User activity (tables of what Virtues/VMs/Applications the User is currently running)
 *  - a tab for history of changes made to this user account, Possibly some sort of versioning system,
 *    or possibly just a list of dates/times that certain privileges/settings were given/revoked.
 *    It'd probably need to be linked/correlated somehow with a list of when any descendant Virtues/Vms/Apps
 *    have been enabled/disabled/deleted. No one would want to correlate those manually.
 *    A simple log should defintely be available though.
 *
 */
@Component({
  selector: 'app-user',
  template: `
  <div id="content-container">
    <div id="content-header">
        <h1 class="titlebar-title">{{mode}} User Account: &nbsp;&nbsp;{{item.name}}</h1>
    </div>
    <div id="content-main">
      <div id="content" class="content">
        <mat-tab-group dynamicHeight=true>
          <mat-tab label= {{mainTab.tabName}}>
            <app-main-user-tab #mainTab></app-main-user-tab>
          </mat-tab>
        </mat-tab-group>
      </div>
      <div class="mui-row">
        <hr>
        <div class="mui-col-md-4">&nbsp;</div>
        <div class="mui-col-md-4 form-item text-align-center">
        <button  *ngIf="mode !== 'View'" class="button-submit" (click)="saveAndReturn();" >Save and Return</button>
        <button  *ngIf="mode !== 'View'" class="button-submit" (click)="save();" >Save</button>
        <button  *ngIf="mode !== 'View'" class="button-cancel" (click)="toViewMode()">Discard Changes</button>
        <button  *ngIf="mode !== 'View'" class="button-cancel" (click)="toListPage()">Cancel</button>

        <button  *ngIf="mode === 'View'" class="button-submit" (click)="toEditMode();" >Edit</button>
        <button  *ngIf="mode === 'View'" class="button-cancel" (click)="toListPage();" >Return</button>
        </div>
        <div class="mui-col-md-4"></div>
      </div>
    </div>
  </div>
    `,
  styleUrls: ['../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class UserComponent extends GenericFormComponent implements OnDestroy {

  /** A tab for displaying the User's attached virtues, status, and assigned roles. */
  @ViewChild('mainTab') mainTab: UserMainTabComponent;

  /**
   * see [[GenericFormComponent.constructor]] for notes on parameters
   */
  constructor(
    location: Location,
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/users', location, activatedRoute, router, baseUrlService, itemService, dialog);

    // gets overwritten once the datasets load, if mode is EDIT or DUPLICATE
    this.item = new User(undefined);

    this.datasetName = Datasets.USERS;
    this.childDatasetName = Datasets.VIRTUES;
  }

  /**
   * calls an init() method in each of the form's tabs, to pass in any data needed/available at render time to that tab from the parent
   * called in [[ngOnInit]]
   */
  initializeTabs() {

    this.mainTab.init(this.mode);
    // These tabs don't exist yet
    // this.activityTab.init(this.mode);
    // this.historyTab.init(this.mode);

    // Must unsubscribe from all these when the UserComponent is destroyed

    this.mainTab.onChildrenChange.subscribe((newChildIDs) => {
      this.setItemChildIDs(newChildIDs);
      this.updatePage();
    });

    // newStatus is just item.enabled - item is a common reference across tabs and form, so the actual value of the
    // parameter is extraneous. It just needs to emit something to let the form know that the user toggled the status.
    // In any mode but view, nothing extra should happen. The local copy of item will have its status toggled, and that's it.
    // In view mode, this toggling should actually make a change to the backend.
    this.mainTab.onStatusChange.subscribe((newStatus) => {
      if( this.mode === Mode.VIEW ) {
        this.toggleItemStatus(this.item);
      }
    });

    // Can anything be done/changed from the activity tab?
    // this.activityTab.onSomethingChange.subscribe((data) => {
    //   // do something
    // });

    // Probably add a button to the history page to let the admin revert settings
    // back to a particular snapshot. Those settings should be pased in here,
    // and used to update everything. Doesn't roll back history to that point,
    // just adds a new edit, where all settings are changed to what they were in
    // that snapshot.
    // Only allow in edit, and it just pulls those settings in. The user can tweak or save them as desired.
    // this.historyTab.onSomethingChange.subscribe((newData) => {
    //   // do something
    // });
  }

  /**
   * Calls a setUp() method for each of the form's tabs, to perform any actions that needed to wait for
   * data requested from the backend.
   * Called in [[onPullComplete]]
   */
  setUpTabs(): void {
    // Note that within each form, the item itself can't change, though its
    // attributes can.
    this.mainTab.setUp(this.item);

    // A table showing what virtues are running, what's been instantiated, when they've logged-on
    // and logged off, all that sort of thing. Probably could do it in one table, but I'd want to add
    // some sort of custom filter.
    // at least one of these.
    // this.activityTab.setUp(this.item);

    // show the times that this user's permissions/settings have been changed by
    // the admin, with a snapshot of what they were at each point.
    // Note that some children may not exist any more, or may have been updated.
    // this.historyTab.setUp(this.item);
  }

  /**
   * Updates data on a form's tabs. Used generally when one tab makes a change to the item's data.
   * called whenever item's child list is set or changes
   */
  updateTabs(): void {

    this.mainTab.update({mode: this.mode});
  }

  /**
   * This page needs all 4 datasets, because there's a Table of Virtues, and under each Virtue
   * we want to display all the Apps it has available to it.
   * See [[GenericPageComponent.getPageOptions]]() for details on return values
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.USERS,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES, Datasets.USERS]
    };
  }

  /**
   * create and fill the fields the backend expects to see, pull in/record any
   * uncollected inputs, and check that the item is valid to be saved
   *
   * @return true if [[item]] is valid and can be saved to the backend, false otherwise.
   */
  finalizeItem(): boolean {
    // Note
    this.mainTab.collectData();


    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid
    // note that the above has to happen in order to collect

    // remember these aren't security checks, merely checks to prevent the user
    // from accidentally putting in bad data

    //  remember to check enabled



    if (this.mode === Mode.CREATE && !this.item.name) {
      confirm("You need to enter a username.");
      return false;
    }

    // if not editing, make sure username isn't taken

    this.item['username'] = this.item.name,
    this.item['authorities'] = this.item['roles'], // since this is technically an item
    this.item['virtueTemplateIds'] = this.item.childIDs;

    // so we're not trying to stringify a bunch of extra fields and data
    this.item.children = undefined;
    this.item.childIDs = [];
    this.item['roles'] = [];
    return true;
  }

  /**
   * unsubscribe all watched EventEmitters
   */
  ngOnDestroy(): void {
    this.mainTab.onChildrenChange.unsubscribe();
    this.mainTab.onStatusChange.unsubscribe();
  }

  /**
   * Overrides parent, [[GenericPageComponent.toggleItemStatus]].
   * See note at [[UserListComponent.toggleItemStatus]].
   * Copied from there ^. Should eventually change, at which point this can be removed.
   *
   * @param item the user whose status we wish to toggle.
   */
  toggleItemStatus(item: Item): void {
    if (item.getName().toUpperCase() === "ADMIN" && item.enabled) {
      console.log("Don't allow the toggling of the admin user. We need that. #TODO");
      item.enabled = true;
      return;
    }

    let sub = this.itemService.setItemStatus(this.serviceConfigUrl, item.getID(), !item.enabled).subscribe( () => {

    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

  }
}
