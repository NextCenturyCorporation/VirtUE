import { Component, OnInit } from '@angular/core';

import { MatDialog } from '@angular/material';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * @class
 * This component allows the user (an admin) to connect the VirtUE system to (an?) Active Directory - AD is what all users will use to
 * log into the desktop, as well as what the admin will use to log into this workbench.
 *
 * #uncommented, because this is a stub.
 *
 * Should this be a GenericDataTabComponent? What would this pull from the backend?
 * I assume a list of currently-set-up ADs. But then how would you set up the first one? That's probably a general question:
 *    - how do you add the first admin?
 * Is there a way to make sure that an admin can't change their own privileges? Is that desireable?
 *
 */
@Component({
  selector: 'app-config-active-dir-tab',
  templateUrl: './config-activeDir-tab.component.html',
  styleUrls: ['../config.component.css', './config-activeDir-tab.component.css']
})
export class ConfigActiveDirTabComponent extends GenericDataTabComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      routerService: RouterService,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(routerService, baseUrlService, dataRequestService, dialog);
    this.tabLabel = "Active Directories";
  }

  init(): void {
  }

  /**
   * #unimplemented
   */
  onPullComplete(): void {
    // this.[a Table?].populate(this.datasets[DatasetNames.FILE_SYSTEMS].asList());
  }

  /**
   * #unimplemented
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return []; // DatasetNames.SOMETHING Is this how this will be populated?
  }
}
