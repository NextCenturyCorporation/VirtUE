import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { MatDialog, MatDialogRef, MatDialogModule } from '@angular/material';
import { DialogsComponent } from '../dialogs/dialogs.component';

import { RouterService } from '../shared/services/router.service';

import { GenericPageComponent } from '../shared/abstracts/gen-page/gen-page.component';

import { ConfigActiveDirTabComponent } from './config-activeDir-tab/config-activeDir-tab.component';
import { ConfigPrinterTabComponent } from './config-printer-tab/config-printer-tab.component';
import { ConfigFileSysTabComponent } from './config-fileSys-tab/config-fileSys-tab.component';
import { ConfigSensorsTabComponent } from './config-sensors-tab/config-sensors-tab.component';
import { ConfigSettingsHistoryTabComponent } from './config-settingsHistory-tab/config-settingsHistory-tab.component';

/**
 * @class
 * This holds the tabs on which various global settings can be defined.
 * See the individual tab classes for their respective details.
 */
@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css']
})
export class ConfigComponent extends GenericPageComponent {

  // @ViewChild('activeDirTab') activeDirTab: ConfigActiveDirTabComponent;

  @ViewChild('fileSysTab') fileSysTab: ConfigFileSysTabComponent;

  // @ViewChild('printerTab') printerTab: ConfigPrinterTabComponent;

  // @ViewChild('sensorTab') sensorTab: ConfigSensorsTabComponent;

  // @ViewChild('settingsHistoryTab') settingsHistoryTab: ConfigSettingsHistoryTabComponent;

  constructor(
    routerService: RouterService,
    dialog: MatDialog
  ) {
    super(routerService, dialog);
  }


}
