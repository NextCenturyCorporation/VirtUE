import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { MatDialog, MatDialogRef, MatDialogModule } from '@angular/material';
import { DialogsComponent } from '../dialogs/dialogs.component';

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

  @ViewChild('activeDirTab') activeDirTab: ConfigActiveDirTabComponent;

  @ViewChild('printerTab') printerTab: ConfigPrinterTabComponent;

  @ViewChild('fileSysTab') fileSysTab: ConfigFileSysTabComponent;

  @ViewChild('sensorTab') sensorTab: ConfigSensorsTabComponent;

  @ViewChild('settingsHistoryTab') settingsHistoryTab: ConfigSettingsHistoryTabComponent;

  constructor(
    router: Router,
    dialog: MatDialog
  ) {
    super(router, dialog);
  }


}
