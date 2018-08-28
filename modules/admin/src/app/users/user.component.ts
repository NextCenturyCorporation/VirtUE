import { Component, OnInit, ViewChild } from '@angular/core';
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

import { UserMainTabComponent } from './form/main-user-tab.component';

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
          <mat-tab label='General Info'>
            <app-main-user-tab #mainTab></app-main-user-tab>
          </mat-tab>
        </mat-tab-group>
      </div>
      <div class="mui-row">
        <hr>
        <div class="mui-col-md-4">&nbsp;</div>
        <div class="mui-col-md-4 form-item text-align-center">
          <button class="button-submit" (click)="createOrUpdate();" >Save</button>
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

export class UserComponent extends GenericFormComponent {

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

  setUpForm(): void {
    this.mainTab.setUp(this.mode, this.item);

    // A table showing what virtues rae running, what's been instantiated, when they've logged-on
    // and logged off, all that sort of thing. Probably could do it in one table, but I'd want to add
    // some sort of custom filter.
    // this.activityTab.setUp(this.mode, this.item);
    // this.usageTab.populateActivityTable(); // at least one of these.
    // this.usageTab.populateInstanceTable(); // ?
  }

  buildChildren() {
    this.item.buildChildren(this[this.childDatasetName]);
    this.mainTab.childrenTable.items = this.item.children.asList();
  }

  buildTabs() {

    this.mainTab.buildChildTable();
    // this.usageTab.buildParentTable();
    // this.usageTab.buildInstanceTable();
  }

  getChildColumns(): Column[] {
    return [
      // See note in gen-form getOptionsList
      new Column('name',            'Template Name',    false, 'asc',     3, undefined, (i: Item) => this.viewItem(i)),
      // new Column('name',            'Template Name',      false, 'asc',     2),
      new Column('childNamesHTML',  'Virtual Machines', true, undefined,  3, this.getChildNamesHtml),
      new Column('apps',            'Applications',     true, undefined,  3, this.getGrandchildrenHtmlList),
      new Column('version',         'Version',          false, 'asc',     2),
      new Column('status',          'Status',           false, 'asc',     1, this.formatStatus)
    ];
  }

  getNoDataMsg(): string {
    return "No virtues have been added yet. To add a virtue, click on the button \"Add Virtue\" above.";
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["apps", "vms", "virtues", "users"]
    };
  }

  // Does nothing, because Users are the top level.
  buildParentTable() {}
  populateParentTable() {}


  // do nothing - could show currently logged-in users though
  buildInstanceTable(): void {}
  populateInstanceTable(): void {}

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {
    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid

    // remember these aren't security checks, merely checks to prevent the user
    // from accidentally putting in bad data

    //  remember to check enabled

    this.item['roles'] = [];
    if (this.roleUser) {
      this.item['roles'].push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item['roles'].push('ROLE_ADMIN');
    }

    if (this.mode === Mode.CREATE && !this.item['username']) {
      return confirm("You need to enter a username.");
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

}
