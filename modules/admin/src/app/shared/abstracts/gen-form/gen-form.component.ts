import { Component, OnInit, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { Observable } from 'rxjs/Observable';

import { RouterService } from '../../services/router.service';
import { BaseUrlService } from '../../services/baseUrl.service';
import { DataRequestService } from '../../services/dataRequest.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Item } from '../../models/item.model';

import { GenericDataPageComponent } from '../gen-data-page/gen-data-page.component';

import { GenericModalComponent } from '../../../modals/generic-modal/generic.modal';
import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';
import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { DatasetNames } from '../gen-data-page/datasetNames.enum';

/**
 * @class __
 * This class represents a detailed view of a single object.
 *
 * Generally, each tab should make only local changes to data that can be saved by this containing component.
 * That usually requires modes, but we don't need multiple types of forms right now, and so I'm not going to spend time
 * fixing this and [[ItemFormComponent]]. The mode stuff there should be partially moved here, and made generic.
 * Nothing else uses this class, except ItemFormComponent.
 *
 * Each type of form page contains a set of tabs.
 *
 * @extends [[GenericDataPageComponent]] because it needs to load data about an Item and its children, as well as other available children
 */
export abstract class GenericTabbedFormComponent extends GenericDataPageComponent implements OnInit {


  /**
   * Record whether an intial pull has already taken place.
   * If a refresh button is added, then this will allow onPullComplete to only
   * set the page data the first time, so as to not overwrite in-progress changes if the user presses the refresh
   * button while editing or duplicating an item. Note this referes to an as-of-yet hypothetical refresh button within the page, and
   * not the browser's refresh button, though the latter would also be nice. It'd probably require making a component persist in some
   * cases after navigation away (routeReuseStrategy).
   */
  initialPullComplete: boolean = false;

  /**
   * see [[GenericDataPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(routerService, baseUrlService, dataRequestService, dialog);

  }

  /** on render */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    this.initializeTabs();
  }

  /**
   * See comments at [[GenericPageComponent.onPullComplete]]()
   *
   * Set up the parts of the page/tabs that rely on the data that was requested from the backend, and if this is the first time data was
   * requested for this component, set up the page.
   *
   * @see [[initialPullComplete]]
   */
  onPullComplete(): void {
    // Refreshes to the datasets should only update the actual datasets in the background, and not overwrite/undo
    // any changes/edits made to the data on the page.
    if (! this.initialPullComplete) {
      this.setUpTabs();
    }

    this.updatePage();
    this.initialPullComplete = true;
  }


  /**
   * abstracts away what needs to happen when the page loads or reloads
   */
  abstract updatePage(): void;

  /**
   * calls an init() method in each of the form's tabs, to pass in any data needed/available at render time to that tab from the parent
   * called on render in [[ngOnInit]]
   */
  abstract initializeTabs(): void;

  /**
   * Calls a setUp() method for each of the form's tabs, to perform any actions that needed to wait for
   * data requested from the backend.
   * Called in [[onPullComplete]]
   */
  abstract setUpTabs(): void;

  /**
   * Updates data on a form's tabs. Used generally when one tab makes a change to the item's data.
   * called whenever item's child list is set or changes
   */
  abstract updateTabs(): void;

}
