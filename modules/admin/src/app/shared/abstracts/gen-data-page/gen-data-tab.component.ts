import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../services/baseUrl.service';
import { RouterService } from '../../services/router.service';
import { DataRequestService } from '../../services/dataRequest.service';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { DatasetNames } from '../../abstracts/gen-data-page/datasetNames.enum';

import { GenericDataPageComponent } from './gen-data-page.component';

/**
 * @class
 * This does almost nothing, but provides a minimal interface for using these sorts of tabs (i.e., tabs that have their own data),
 * in constrast to the tabs that must be managed by a [[GenericTabbedFormComponent]] parent.
 *
 * Pulling data separately for each tab works much better in this case, so long as the tabs don't use the same datasets.
 * If they do (like if multiple tabs need access to the list of vm-templates), and the amount of shared data is large, then we should
 * probably figure something out.
 * Perhaps use the update method to pass in the common data (would need to make containing page inherit from GenericDataPageComponent).
 *
 * Remember that all tabs are built simulataneously when their containing page loads.
 *
 * @extends [[GenericDataPageComponent]] since all subclasses need to request their own datasets.
 *
 */
export abstract class GenericDataTabComponent extends GenericDataPageComponent implements OnInit {

  /** The label to appear on the tab */
  public tabLabel: string;

  constructor(
    routerService: RouterService,
    dataRequestService: DataRequestService,
    dialog: MatDialog) {
      super(routerService, dataRequestService, dialog);
  }

  /**
   * initialize the tab upon render
   */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    this.init();
  }

  /**
   * Tab-specific initialization, called on render.
   */
  abstract init(): void;

  /**
   * This allows the containing page to pass data into the tabs.
   * Child subclasses that need data passed to them should override this.
   * Empty function instead of abstract because it won't usually be needed, but would like to leave this as a stub just in case.
   */
  update(data: any): void {}

}
