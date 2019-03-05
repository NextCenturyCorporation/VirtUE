import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';


import { MatDialog } from '@angular/material';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../shared/models/column.model';

import { Printer } from '../../shared/models/printer.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

/**
 * @class
 * This class represents a list of Printers, which can be selected.
 *
 * @extends [[GenericModalComponent]]
 */
@Component({
  selector: 'app-printer-selection-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css']
})
export class PrinterSelectionModalComponent extends GenericModalComponent {

  /**
   * see [[GenericModalComponent.constructor]] for notes on parameters
   */
  constructor(
      routerService: RouterService,
      dataRequestService: DataRequestService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<PrinterSelectionModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
  ) {
    super(routerService, dataRequestService, dialog, dialogRef, data);
    this.pluralItem = "Printers";
  }

  /**
   * @return what columns should show up in the the printer selection table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Printer',   4, (p: Printer) => p.name, SORT_DIR.ASC),
      new TextColumn('Status',    3, (p: Printer) => String(p.status), SORT_DIR.ASC),
      new TextColumn('Address',   2, (p: Printer) => p.address, SORT_DIR.DESC)
    ];
  }

  /**
   * #uncommented
   */
  customizeTableParams(params): void {
    params['elementIsDisabled'] = (p: Printer) => !p.enabled;
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.fillTable(this.datasets[DatasetNames.PRINTERS].asList());
  }

  /**
   * This page needs all datasets to load except allUsers: it displays all printers, the VMs assigned to each printer,
   * and the apps available to each Printer through its VMs.
   *
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.PRINTERS];
  }

  /**
   * @return a string to be displayed in the virtue table, when no virtue templates exit.
   */
  getNoDataMsg(): string {
    return "No printers have been connected to this system.";
  }
}
