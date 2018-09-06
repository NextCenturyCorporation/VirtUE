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
import { Mode } from '../shared/enums/enums';
import { RowOptions } from '../shared/models/rowOptions.model';

import { UserMainTabComponent } from './form/main-tab/main-user-tab.component';

import { ConfigUrlEnum } from '../shared/enums/enums';

import { GenericFormComponent } from '../shared/abstracts/gen-form/gen-form.component';

@Component({
  selector: 'app-user',
  // templateUrl: './user.component.html',
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
          <button class="button-submit" (click)="createOrUpdate(true);" >Save</button>
          <button class="button-cancel" (click)="cancel()">Cancel</button>
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

  @ViewChild('mainTab') mainTab: UserMainTabComponent;

  roleUser: boolean;
  roleAdmin: boolean;

  fullImagePath: string;

  constructor(
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/users', activatedRoute, router, baseUrlService, itemService, dialog);

    // gets overwritten once the datasets load, if mode is EDIT or DUPLICATE
    this.item = new User(undefined);

    this.datasetName = 'allUsers';
    this.childDatasetName = 'allVirtues';

    this.childDomain = "/virtues";
  }

  // called on parent's ngInit
  initializeTabs() {
    this.mainTab.init();
    // this.activityTab.buildParentTable();
    // this.historyTab.buildInstanceTable();

    // Must unsubscribe from all these when the UserComponent is destroyed

    this.mainTab.onChildrenChange.subscribe((newChildIDs) => {
      this.buildItemChildren(newChildIDs);
      this.updateTabs();
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
    // this.historyTab.onSomethingChange.subscribe((newData) => {
    //   // do something
    // });
  }

  // called in parent's onPullComplete
  setUpTabs() {
    // Note that within each form, the item itself can't change, though its
    // attributes can.
    this.mainTab.setUp(this.mode, this.item);

    // A table showing what virtues are running, what's been instantiated, when they've logged-on
    // and logged off, all that sort of thing. Probably could do it in one table, but I'd want to add
    // some sort of custom filter.
    // at least one of these.
    // this.activityTab.setUp(this.mode, this.item);

    // show the times that this user's permissions/settings have been changed by
    // the admin, with a snapshot of what they were at each point.
    // Note that some children may not exist any more, or may have been updated.
    // this.historyTab.setUp();
  }

  // called whenever item's child list is set or changes
  updateTabs(): void {
    this.mainTab.update({mode:this.mode});
  }

  // only called on initial page load at the moment.
  updatePage() {
    this.buildItemChildren();
  }

  // if nothing is passed in, we just want to populate item.children
  buildItemChildren( newChildIDs?: string[] ) {
    if (newChildIDs instanceof Array) {
      this.item.childIDs = newChildIDs;
    }

    this.item.buildChildren(this[this.childDatasetName]);
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["apps", "vms", "virtues", "users"]
    };
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
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
    this.item.childIDs = undefined;
    this.item['roles'] = undefined;
    return true;
  }

  ngOnDestroy() {
    this.mainTab.onChildrenChange.unsubscribe();
  }
}
