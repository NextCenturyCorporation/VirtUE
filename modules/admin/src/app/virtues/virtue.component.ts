import { Component, OnInit, ViewChild, QueryList, OnDestroy } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog, MatSlideToggleModule } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { ItemService } from '../shared/services/item.service';

import { Item } from '../shared/models/item.model';
import { Application } from '../shared/models/application.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { Virtue } from '../shared/models/virtue.model';
import { DictList } from '../shared/models/dictionary.model';
import { Column } from '../shared/models/column.model';

import { Mode, ConfigUrls, Datasets } from '../shared/enums/enums';

import { VirtueMainTabComponent } from './form/main-tab/virtue-main-tab.component';
import { VirtueSettingsTabComponent } from './form/settings-tab/virtue-settings.component';
import { VirtueUsageTabComponent } from './form/usage-tab/virtue-usage-tab.component';

import { GenericFormComponent } from '../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-virtue',
  template: `
  <div id="content-container">
    <div id="content-header">
        <h1 class="titlebar-title">{{mode}} Virtue: &nbsp;&nbsp;{{item.name}}</h1>
    </div>
    <div id="content-main">
      <div id="content" class="content">
        <mat-tab-group dynamicHeight=true>
          <mat-tab label= {{mainTab.tabName}}>
            <app-virtue-main-tab #mainTab></app-virtue-main-tab>
          </mat-tab>
          <mat-tab label= {{settingsTab.tabName}}>
            <app-virtue-settings-tab #settingsTab></app-virtue-settings-tab>
          </mat-tab>
          <mat-tab label= {{usageTab.tabName}}>
            <app-virtue-usage-tab #usageTab></app-virtue-usage-tab>
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

export class VirtueComponent extends GenericFormComponent implements OnDestroy {

  @ViewChild('mainTab') mainTab: VirtueMainTabComponent;

  /**
   * A tab for displaying and editing the many settings which can be specified within a Virtue.
   * See [[VirtueSettingsTabComponent]]
   */
  @ViewChild('settingsTab') settingsTab: VirtueSettingsTabComponent;

  /**
   * A tab for displaying what Users have access to this template, and what instances of this template are
   * currently running.
   */
  @ViewChild('usageTab') usageTab: VirtueUsageTabComponent;

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
    super('/virtues', location, activatedRoute, router, baseUrlService, itemService, dialog);

    // set up empty (except for a default color), will get replaced in ngOnInit if
    // mode is not 'create'
    this.item = new Virtue({color: this.defaultColor()});

    this.datasetName = Datasets.VIRTUES;
    this.childDatasetName = Datasets.VMS;

  }

  /**
   * This only stays until the data loads, if the data has a color (or if mode is CREATE).
   * @return 'transparent'
   */
  defaultColor(): string {
    return 'transparent';
  }

  /**
   * calls an init() method in each of the form's tabs, to pass in any data needed/available at render time to that tab from the parent
   * called in [[ngOnInit]]
   */
  initializeTabs(): void {
    this.mainTab.init(this.mode);
    this.settingsTab.init(this.mode);
    this.usageTab.init(this.mode);
    // this.historyTab.init(this.mode);

    // Must unsubscribe from all these when the VirtueComponent is destroyed

    this.mainTab.onChildrenChange.subscribe((newChildIDs) => {
      this.setItemChildIDs(newChildIDs);
      this.updatePage();
    });

    this.mainTab.onStatusChange.subscribe((newStatus) => {
      if( this.mode === Mode.VIEW ) {
        this.toggleItemStatus(this.item);
      }
    });

    // TODO build activity table showing running instances of this virtue template,
    // put on usageTab.
    // If anything can be changed from the usageTab, listen for it below.
    // this.usageTab.onSomethingChange.subscribe((data) => {
    //   // do something
    // });

    // Probably add a button to the history page to let the admin revert settings
    // back to a particular snapshot. Those settings should be passed in here,
    // and used to update everything. Doesn't roll back history to that point,
    // just adds a new edit, where all settings are changed to what they were in
    // that snapshot.
    // this.historyTab.onSomethingChange.subscribe((newData) => {
    //   // save current version to history
    //   // change all settings to the supplied ones (except version and history)
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

    // A table showing what virtues are running
    // Custom filter?
    this.settingsTab.setUp(this.item);

    this.usageTab.setUp(this.item);

    // show the times that this user's permissions/settings have been changed by
    // the admin, with a snapshot of what they were at each point.
    // Note that some children may not exist any more, or may have been updated.
    // Remember to make this sortable by version.
    // this.historyTab.setUp(this.item);
  }

  // called whenever item's child list is set or changes
  updateTabs(): void {
    this.mainTab.update({mode: this.mode});

    // These won't need to be updated when child list changes, but would other times:

    // This may need updating whenever the list of printers or whatever gets reset.
    // If I know printers, a refresh button for that list in particular will be greatly appreciated.
    this.settingsTab.update({allVirtues: this.allVirtues, mode: this.mode});

    // needs an initial update to populate the parent table.
    // this could use periodic updating, to get a somewhat live-feed of what's currently running.
    this.usageTab.update({allUsers: this.allUsers, mode: this.mode});
  }

  /**
   * This page needs all 4 datasets, because there's a Table of Vms, wich includes the apps available in each VM.
   * It also has a table showing the users that have been given access to this Virtue template.
   * See [[GenericPageComponent.getPageOptions]]() for details on return values
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VIRTUES,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES, Datasets.USERS]
    };
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {
    if ( !this.mainTab.collectData() ) {
      return false;
    }
    if ( !this.settingsTab.collectData() ) {
      return false;
    }
    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid

    // The following are required:
    //  this.item.name,     can't be empty
    //  this.item.version,  will be valid
    //  this.item.enabled,  should either be true or false
    //  this.item.color,    should be ok? make sure it has a default in the settings pane
    this.item['virtualMachineTemplateIds'] = this.item.childIDs;

    // note that children is set to undefined for a brief instant before the
    // page navigates away, during which time an exception would occur on the
    // table - that chunk of html has now been wrapped in a check, to not check
    // children's list size if children is undefined
    this.item.children = undefined;
    this.item.childIDs = [];
    return true;
  }

  /**
   * unsubscribe all watched EventEmitters
   */
  ngOnDestroy(): void {
    this.mainTab.onChildrenChange.unsubscribe();
    this.mainTab.onStatusChange.unsubscribe();
  }
}
