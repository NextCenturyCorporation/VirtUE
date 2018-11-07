import { Component, OnInit } from '@angular/core';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';

import { ConfigUrls } from '../../shared/services/config-urls.enum';
import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * @class
 * This component allows the user (an admin) to set up activ directories. For something.
 * TODO ask about active directories
 * #uncommented, because this is a stub.
 *
 * This probably shouldn't be a GenericDataTabComponent. Maybe.
 *
 */
@Component({
  selector: 'app-config-active-dir-tab',
  templateUrl: './config-activeDir-tab.component.html',
  styleUrls: ['./config-activeDir-tab.component.css']
})
export class ConfigActiveDirTabComponent extends GenericDataTabComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(router, baseUrlService, dataRequestService, dialog);
    this.tabName = "Active Directories";
  }
  /**
   * #unimplemented
   */
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
   * See [[GenericDataPageComponent.getDataPageOptions]]() for details on return values
   */
  getDataPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: DatasetNames[]} {
    return {
      serviceConfigUrl: ConfigUrls.USERS,//ConfigUrls.is this how this will be done?
      neededDatasets: []//DatasetNames.I'm pretty sure not.
    };
  }
}
