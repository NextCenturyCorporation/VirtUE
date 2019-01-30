import { Component, OnInit, ViewChild, QueryList, OnDestroy } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { FormControl } from '@angular/forms';
import { MatDialog, MatSlideToggleModule } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { RouterService } from '../shared/services/router.service';
import { BaseUrlService } from '../shared/services/baseUrl.service';
import { DataRequestService } from '../shared/services/dataRequest.service';

import { Item } from '../shared/models/item.model';
import { Application } from '../shared/models/application.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { Virtue } from '../shared/models/virtue.model';
import { DictList } from '../shared/models/dictionary.model';
import { Column } from '../shared/models/column.model';

import { Mode } from '../shared/abstracts/gen-form/mode.enum';
import { DatasetNames } from '../shared/abstracts/gen-data-page/datasetNames.enum';

import { VirtueMainTabComponent } from './form/main-tab/virtue-main-tab.component';
import { VirtueSettingsTabComponent } from './form/settings-tab/virtue-settings.component';
import { VirtueUsageTabComponent } from './form/usage-tab/virtue-usage-tab.component';

import { ItemFormComponent } from '../shared/abstracts/gen-form/item-form/item-form.component';


/**
 *
 * @class
 * This class represents a detailed view of a Virtue Template.
 * See comment on [[ItemFormComponent]] for generic info.
 *
 * This form has:
 *  - a main tab for viewing the Vms made available to the Virtue, its version, and
 *    its name (which can be changed). The version automatically increases on every edit.
 *  - A settings tab, where the user can select tailor the Virtue to meet their particular needs.
 *    See [[VirtueSettingsTabComponent]] for details.
 *  - A 'usage' tab, showing what users have been granted access to this template.
 *    The activity tab described below could be merged with this tab, if the tables tend to be small, since
 *    'Who has access' and 'who's currently running it' will probably want to be known at the same time.
 *
 * It also will in the future #TODO have:
 *  - a tab for activity (tables of what instances of this template are running, and what VMs/Applications
 *    have been instantiated on them.)
 *  - a tab for version history.
 *    It'd probably need to be linked/correlateable somehow with a list of when any descendant Vms/Apps
 *    were enabled/disabled/deleted. No one would want to correlate those manually.
 *    A simple log should defintely be available though.
 *
 */
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
          <button  *ngIf=" !inViewMode() " class="button-submit" (click)="saveAndReturn();" >Save and Return</button>
          <button  *ngIf=" inEditMode()" class="button-submit" (click)="save();" >Save</button>
          <button  *ngIf=" !inViewMode() " class="button-cancel" (click)="cancel()">Cancel</button>

          <button  *ngIf="inViewMode()" class="button-submit" (click)="toEditMode();" >Edit</button>
          <button  *ngIf="inViewMode()" class="button-cancel" (click)="cancel();" >Return</button>
        </div>
        <div class="mui-col-md-4"></div>
      </div>
    </div>
  </div>
    `,
  styleUrls: ['../shared/abstracts/item-list/item-list.component.css']
})
export class VirtueComponent extends ItemFormComponent implements OnDestroy {

  /** A tab for displaying and/or editing the Virtue's name, status, version, and attached vms */
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

  /** reclassing */
  item: Virtue;

  /**
   * see [[ItemFormComponent.constructor]] for notes on parameters
   */
  constructor(
    activatedRoute: ActivatedRoute,
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super('/virtues', activatedRoute, routerService, baseUrlService, dataRequestService, dialog);

    // set up empty (except for a default color), will get replaced in render (ngOnInit) if
    // mode is not 'CREATE'
    this.item = new Virtue();

    this.datasetName = DatasetNames.VIRTUE_TS;
    this.childDatasetName = DatasetNames.VM_TS;

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

    this.mainTab.onChildrenChange.subscribe((newChildIds: string[]) => {
      this.item.vmTemplateIds = newChildIds;
      this.updatePage();
    });

    this.mainTab.onStatusChange.subscribe((newStatus) => {
      if ( this.mode === Mode.VIEW ) {
        this.setItemAvailability(this.item, newStatus);
      }
    });

    this.settingsTab.onChildrenChange.subscribe(() => {
      this.updatePage();
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

  /**
   * Updates data on a form's tabs. Used generally when one tab makes a change to the item's data.
   * called whenever item's child list is set or changes
   */
  updateTabs(): void {
    this.mainTab.update({mode: this.mode});

    // These won't need to be updated when child list changes, but would other times:

    // This may need updating whenever the list of printers or whatever gets reset.
    // If I know printers, a refresh button for that list in particular will be greatly appreciated.
    this.settingsTab.update({[DatasetNames.VIRTUE_TS]: this.datasets[DatasetNames.VIRTUE_TS], mode: this.mode});

    // needs an initial update to populate the parent table.
    // this could use periodic updating, to get a somewhat live-feed of what's currently running.
    this.usageTab.update({
                          [DatasetNames.USERS]: this.datasets[DatasetNames.USERS],
                          [DatasetNames.VIRTUES]: this.datasets[DatasetNames.VIRTUES],
                          mode: this.mode});
  }

  /**
   * This page needs all 6 datasets, because there's a Table of Vms, wich includes the apps available in each VM.
   * It also has a table showing the users that have been given access to this Virtue template.
   * The settings tab now also allows connection to printers and filesystems.
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [
            DatasetNames.APPS,
            DatasetNames.VM_TS,
            DatasetNames.PRINTERS,
            DatasetNames.FILE_SYSTEMS,
            DatasetNames.VIRTUE_TS,
            DatasetNames.VIRTUES,
            DatasetNames.USERS];
  }

  /**
   * Pull in/record any uncollected inputs, and check that the item is valid to be saved
   *
   * @return true if [[item]] is valid, false otherwise.
   */
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

    // note that vmTemplates is set to undefined for a brief instant before the
    // page navigates away, during which time an exception would occur on the
    // table - that chunk of html has now been wrapped in a check, to not check
    // the 's list size if vmTemplates is undefined
    this.item.vmTemplates = undefined;
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
