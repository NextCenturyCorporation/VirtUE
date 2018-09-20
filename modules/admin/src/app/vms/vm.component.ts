import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { ItemService } from '../shared/services/item.service';

import { AppsModalComponent } from '../modals/apps-modal/apps-modal.component';

import { Item } from '../shared/models/item.model';
import { Application } from '../shared/models/application.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { DictList } from '../shared/models/dictionary.model';
import { Column } from '../shared/models/column.model';

import { ConfigUrls, Datasets, Mode } from '../shared/enums/enums';

import { GenericFormComponent } from '../shared/abstracts/gen-form/gen-form.component';

import { VmMainTabComponent } from './form/vm-main-tab/vm-main-tab.component';
import { VmUsageTabComponent } from './form/vm-usage-tab/vm-usage-tab.component';

@Component({
  selector: 'app-vm',
  template: `
  <div id="content-container">
    <div id="content-header">
        <h1 class="titlebar-title">{{mode}} Virtual Machine: &nbsp;&nbsp;{{item.name}}</h1>
    </div>
    <div id="content-main">
      <div id="content" class="content">
        <mat-tab-group dynamicHeight=true>
          <mat-tab label= {{mainTab.tabName}}>
            <app-vm-main-tab #mainTab></app-vm-main-tab>
          </mat-tab>
          <mat-tab label= {{usageTab.tabName}}>
            <app-vm-usage-tab #usageTab></app-vm-usage-tab>
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
export class VmComponent extends GenericFormComponent implements OnDestroy {

  @ViewChild('mainTab') mainTab: VmMainTabComponent;
  @ViewChild('usageTab') usageTab: VmUsageTabComponent;

  constructor(
    location: Location,
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/vm-templates', location, activatedRoute, router, baseUrlService, itemService, dialog);

    this.item = new VirtualMachine(undefined);

    this.datasetName = Datasets.VMS;
    this.childDatasetName = Datasets.APPS;

  }

  /**
   * calls an init() method in each of the form's tabs, to pass in any data needed/available at render time to that tab from the parent
   * called in [[ngOnInit]]
   */
  initializeTabs(): void {
    this.mainTab.init(this.mode);
    this.usageTab.init(this.mode);
    // this.historyTab.init(this.mode);

    this.mainTab.onChildrenChange.subscribe((newChildIDs) => {
      this.setItemChildIDs(newChildIDs);
      this.updatePage();
    });

    this.mainTab.onStatusChange.subscribe((newStatus) => {
      if( this.mode === Mode.VIEW ) {
        this.toggleItemStatus(this.item);
      }
    });

  }

  // called in parent's onPullComplete
  setUpTabs() {
    // Note that within each form, the item can't be reassigned; its attribute
    // can change though.
    this.mainTab.setUp(this.item);

    this.usageTab.setUp(this.item);

    // show the times that this user's permissions/settings have been changed by
    // the admin, with a snapshot of what they were at each point.
    // Note that some children may not exist any more, or may have been updated.
    // this.historyTab.setUp();
  }

  // called whenever item's child list is set or changes
  updateTabs(): void {
    this.mainTab.update({mode: this.mode});


    // needs an initial update to populate the parent table.
    // this could use periodic updating, to get a somewhat live-feed of what's currently running.
    this.usageTab.update({allVirtues: this.allVirtues, mode: this.mode});
  }

  /**
   * This page needs all datasets to load: This VM, the Virtues granted this VM template, and the Apps this VM has
   * been given.
   * See [[GenericPageComponent.getPageOptions]]() for details on return values
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VMS,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES]
    };
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {
    this.mainTab.collectData();

    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid

    // The following are required:
    //  'id'           should be ok as-is. May be empty if creating new.
    //  'name'         can't be empty
    //  'os'           must be set
    this.item['loginUser'] = 'system'; // TODO does this still exist on the backend?

    // TODO check if necessary, and what the string should be (admin vs administrator)
    //  this.item['lastEditor'] = 'administrator';

    //  'enabled'      must be either true or false
    this.item['applicationIds'] = this.item.childIDs;  // may be empty

    this.item.children = undefined;
    this.item.childIDs = [];
    return true;
  }

  ngOnDestroy() {
    this.mainTab.onChildrenChange.unsubscribe();
    this.mainTab.onStatusChange.unsubscribe();
  }
}
