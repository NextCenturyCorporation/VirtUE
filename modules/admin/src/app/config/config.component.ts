import { Component, OnInit, ViewChild } from '@angular/core';
import { HashLocationStrategy, Location, LocationStrategy } from '@angular/common';

import { GenericTabComponent } from '../shared/abstracts/gen-tab/gen-tab.component';
import { ConfigActiveDirTabComponent } from './config-activeDir-tab/config-activeDir-tab.component';
import { ConfigSensorsTabComponent } from './config-sensors-tab/config-sensors-tab.component';
import { ConfigFileSysTabComponent } from './config-fileSys-tab/config-fileSys-tab.component';
import { ConfigPrinterTabComponent } from './config-printer-tab/config-printer-tab.component';
import { ConfigSettingsHistoryTabComponent } from './config-settingsHistory-tab/config-settingsHistory-tab.component';

/**
 * @class
 *
 * #unimplemented
 *
 * So, this needs to somehow request data from the backend that doesn't exist yet.
 * Dataflow currently is molded around requesting info on Items. So, either add this global info to the backend so that the existing
 * dataflow can request it with minimal changes from how it currently requests Items, or create a whole new dataflow for requesting
 * this global data (Absolutely not. It'd be almost identical to the existing one)
 * Or we could just switch over to GraphQL now.
 *
 *
 * A different note, I would like there to be a single button, defined in this component, that saves the data on only the current tab.
 * Maybe a save-all button too, but at least one that only saves the current page. I certainly wouldn't expect a butotn at the bottom of
 * a page to save stuff on other pages. I don't want five identical buttons with 5 copies of identical functionality/layout code.
 *
 *
 *
 */
@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrls: ['./config.component.css'],
  providers: [
    Location,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ]
})
export class ConfigComponent implements OnInit {

  /** #uncommented */
  @ViewChild('activeDirTab') activeDirTab: ConfigActiveDirTabComponent;

  /** #uncommented */
  @ViewChild('fileSysTab') fileSysTab: ConfigFileSysTabComponent;

  /** #uncommented */
  @ViewChild('printerTab') printerTab: ConfigPrinterTabComponent;

  /** #uncommented */
  @ViewChild('sensorsTab') sensorsTab: ConfigSensorsTabComponent;

  /** #uncommented */
  @ViewChild('settingsHistoryTab') settingsHistoryTab: ConfigSettingsHistoryTabComponent;

  /**
   * #unimplemented
   */
  constructor(
    /** #uncommented */
    protected location: Location
   ) {}

  /**
   * #unimplemented
   */
  ngOnInit(): void {
    this.activeDirTab.init();
    this.fileSysTab.init();
    this.printerTab.init();
    this.sensorsTab.init();
    this.settingsHistoryTab.init();


    this.activeDirTab.setUp();
    this.fileSysTab.setUp();
    this.printerTab.setUp();
    this.sensorsTab.setUp();
    this.settingsHistoryTab.setUp();
  }

}
