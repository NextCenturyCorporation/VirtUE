import { Component, OnInit, ViewChild } from '@angular/core';

import {  MatButtonModule,
          MatDialog,
          MatIconModule
} from '@angular/material';

import { ActivatedRoute, Router } from '@angular/router';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';

import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import { Printer } from '../../shared/models/printer.model';
import { SubMenuOptions } from '../../shared/models/subMenuOptions.model';

import { ConfigUrls } from '../../shared/services/config-urls.enum';
import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import {
  Column,
  TextColumn,
  CheckboxColumn,
  IconColumn,
  SORT_DIR
} from '../../shared/models/column.model';


/**
 * @class
 * This component allows the user (an admin) to set up activ directories. For something.
 * TODO ask about active directories
 * #uncommented, because this is a stub.
 * also #move to a tab on the global settings form
 */
@Component({
  selector: 'app-config-printer-tab',
  templateUrl: './config-printer-tab.component.html',
  styleUrls: ['./config-printer-tab.component.css']
})
export class ConfigPrinterTabComponent extends GenericDataTabComponent {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) printersTable: GenericTableComponent<Printer>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(router, baseUrlService, dataRequestService, dialog);
    this.tabName = "Printers";
  }

  /**
   * #unimplemented
   */
  init(): void {
    this.setUpTable();
  }

  /**
   * #commented
   */
  onPullComplete(): void {
    this.printersTable.populate(this.datasets[DatasetNames.PRINTERS].asList());
  }

  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
  setUpTable(): void {
    if (this.printersTable === undefined) {
      return;
    }
    this.printersTable.setUp({
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "No printers set up yet.",
      elementIsDisabled: (p: Printer) => !p.enabled
    });
  }

  /** #unimplemented */
  getColumns(): Column[] {
    return [
      new TextColumn("Printer", 5, (p: Printer) => p.name, SORT_DIR.ASC, undefined, () => this.getSubMenu()),
      new TextColumn("Printer address", 3, (p: Printer) => p.address, SORT_DIR.ASC),
      new TextColumn("Printer status", 1, (p: Printer) => p.status, SORT_DIR.ASC),
      new CheckboxColumn("Enable", 2, "enabled"),
      new IconColumn("Options", 1, "settings", (p: Printer) => this.addName(p))
    ];
  }


  /**
   * @return a list of links to show up as a submenu on each printer. Currently just an enable/disable option.
   */
  getSubMenu(): SubMenuOptions[] {
    return [
      new SubMenuOptions("Enable", (p: Printer) => !p.enabled, (p: Printer) => this.togglePrinter(p)),
      new SubMenuOptions("Disable", (p: Printer) => p.enabled, (p: Printer) => this.togglePrinter(p)),
      new SubMenuOptions("Delete", (p: Printer) => true, (p: Printer) => this.deleteItem(p)),
    ];
  }

  /**
   * #unimplemented
   * See [[GenericDataPageComponent.getDataPageOptions]]() for details on return values
   */
  getDataPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: DatasetNames[]} {
    return {
      serviceConfigUrl: ConfigUrls.PRINTERS,
      neededDatasets: [DatasetNames.PRINTERS]
    };
  }

  togglePrinter(printer: Printer) {
    // remember this is 'status' like 'enabled'/'disabled', not a printer status like 'idle'/'busy'/'broken'/etc
    this.setItemStatus(printer, !printer.enabled);
  }

  addPrinter() {
    this.createItem(new Printer({id: "2", name: "Makerbot 3D printer", info: "b/w", address: "127.0.0.2", status: "on", enabled: true}));
  }

  addName(p: Printer) {
    p.name = "Epson 5480";
    this.updateItem(p);
  }


  /**
   * Reload page, dropping unsaved changes. Saving must be done separately.
   */
  refreshPage(): void {
    this.cmnDataComponentSetup();
  }

}
