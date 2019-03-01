import { Component, OnInit, ViewChild } from '@angular/core';

import {  MatButtonModule,
          MatDialog,
          MatIconModule
} from '@angular/material';


import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';

import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import { Printer } from '../../shared/models/printer.model';
import { SubMenuOptions } from '../../shared/models/subMenuOptions.model';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { PrinterModalComponent } from '../../modals/printer-modal/printer.modal';

import {
  Column,
  TextColumn,
  CheckboxColumn,
  IconColumn,
  BlankColumn,
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
      routerService: RouterService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(routerService, dataRequestService, dialog);
    this.tabLabel = "Printers";
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

  setUpTable(): void {
    if (this.printersTable === undefined) {
      return;
    }
    this.printersTable.setUp({
      cols: this.getColumns(),
      tableWidth: 1,
      noDataMsg: "No printers set up yet.",
      elementIsDisabled: (p: Printer) => !p.enabled
    });
  }

  getColumns(): Column[] {
    return [
      new TextColumn("Description", 4, (p: Printer) => p.name),
      new TextColumn("Printer address", 2, (p: Printer) => p.address),
      new TextColumn("Printer status", 2, (p: Printer) => p.status),
      new CheckboxColumn("Enable", 1, "enabled", undefined,
              (p: Printer, checked: boolean) => this.setItemAvailability(p, checked)),
      new BlankColumn(1),
      new IconColumn("Options", 1, "settings", (p: Printer) => this.editPrinter(p)),
      new IconColumn("Remove Printer", 1, "delete", (p: Printer) => this.deleteItem(p))
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
    this.activatePrinterModal();
  }

  editPrinter(p: Printer) {
    this.activatePrinterModal(p);
  }

  activatePrinterModal(printerToEdit?: Printer): void {
    let params: {height: string, width: string, data?: {printer: Printer}};
    let creatingNew: boolean = (printerToEdit === undefined);

    if (creatingNew) {
      params = {
        height: '70%',
        width: '40%'
      };
    }
    else {
      params = {
        height: '70%',
        width: '40%',
        data: {
          printer: printerToEdit
        }
      };
    }


    let dialogRef = this.dialog.open( PrinterModalComponent, params);

    let sub = dialogRef.componentInstance.getPrinter.subscribe((printer) => {
      if (creatingNew) {
        this.createItem(printer);
      }
      else {
        this.updateItem(printer);
      }
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%' });

  }

}
