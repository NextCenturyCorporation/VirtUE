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
 * This component allows the user (an admin) to set up printers
 */
@Component({
  selector: 'app-config-printer-tab',
  templateUrl: './config-printer-tab.component.html',
  styleUrls: ['../config.component.css']
})
export class ConfigPrinterTabComponent extends GenericDataTabComponent {

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

  init(): void {
    this.setUpTable();
  }

  /**
   * @override [[GenericDataPageComponent.onPullComplete]]
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
      new TextColumn("Printer", 4, (p: Printer) => p.name, SORT_DIR.ASC, undefined, () => this.getSubMenu()),
      new TextColumn("Printer address", 3, (p: Printer) => p.address, SORT_DIR.ASC),
      new TextColumn("Printer status", 2, (p: Printer) => p.status, SORT_DIR.ASC),
      new CheckboxColumn("Enable", 1, "enabled", undefined,
              (p: Printer, checked: boolean) => this.setItemAvailability(p, checked)),
      new IconColumn("Options", 1, "settings", (p: Printer) => this.printPrinter(p)),
      new IconColumn("Remove Printer", 1, "delete", (p: Printer) => this.deleteItem(p))
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
  * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.PRINTERS];
  }

  togglePrinter(printer: Printer) {
    this.setItemAvailability(printer, !printer.enabled);
  }

  addPrinter() {
    this.createItem(new Printer({id: "2", name: "Brother HL", info: "b/w", address: "127.0.0.1", status: "on", enabled: true}));
  }

  printPrinter(p: Printer) {
    console.log(p);
  }

  addName(p: Printer) {
    p.name = "Epson 5480";
    this.updateItem(p);
  }


}
