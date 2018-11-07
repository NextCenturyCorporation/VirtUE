import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { MatDialog, MatDialogRef, MatDialogModule } from '@angular/material';
import { DialogsComponent } from '../dialogs/dialogs.component';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { DataRequestService } from '../shared/services/dataRequest.service';

import { GenericPageComponent } from '../shared/abstracts/gen-page/gen-page.component';

import { ConfigActiveDirTabComponent } from './config-activeDir-tab/config-activeDir-tab.component';
import { ConfigPrinterTabComponent } from './config-printer-tab/config-printer-tab.component';
import { ConfigFileSysTabComponent } from './config-fileSys-tab/config-fileSys-tab.component';
import { ConfigSensorsTabComponent } from './config-sensors-tab/config-sensors-tab.component';
import { ConfigSettingsHistoryTabComponent } from './config-settingsHistory-tab/config-settingsHistory-tab.component';

import { Mode } from '../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../shared/services/config-urls.enum';
import { DatasetNames } from '../shared/abstracts/gen-data-page/datasetNames.enum';

// import { GenericDataPageComponent } from '../shared/abstracts/gen-data-page/gen-data-page.component';


/**
 * @class
 * Convert this to a generic form.
 *
 * #uncommented, because it will need to change drastically
 */
@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent extends GenericPageComponent {

  /**
   * #uncommented
   */
  @ViewChild('activeDirTab') activeDirTab: ConfigActiveDirTabComponent;

  /**
   * #uncommented
   */
  @ViewChild('printerTab') printerTab: ConfigPrinterTabComponent;

  /**
   * #uncommented
   */
  @ViewChild('fileSysTab') fileSysTab: ConfigFileSysTabComponent;

  /**
   * #uncommented
   */
  @ViewChild('sensorTab') sensorTab: ConfigSensorsTabComponent;

  /**
   * #uncommented
   */
  @ViewChild('settingsHistoryTab') settingsHistoryTab: ConfigSettingsHistoryTabComponent;

  /**
   * #uncommented
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, dataRequestService, dialog);
  }


}
