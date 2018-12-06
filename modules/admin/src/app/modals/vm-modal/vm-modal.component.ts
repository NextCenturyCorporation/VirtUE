import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
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
import { VirtualMachine } from '../../shared/models/vm.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/**
 * @class
 * This class represents a list of Virtual machine templates, which can be selected.
 *
 * @extends [[GenericModalComponent]]
 */
@Component({
  selector: 'app-vm-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css']
})
export class VmModalComponent extends GenericModalComponent implements OnInit {

  /**
   * see [[GenericModalComponent.constructor]] for notes on parameters
   */
  constructor(
      routerService: RouterService,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<GenericModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
    ) {
      super(routerService, baseUrlService, dataRequestService, dialog, dialogRef, data);
      this.pluralItem = "Virtual Machine Templates";
    }

  /**
   * #uncommented
   */
  customizeTableParams(params): void {
    params['elementIsDisabled'] = (vm: VirtualMachine) => !vm.enabled;
  }

  /**
   * @return what columns should show up in the the VM selection table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name',         3, (v: VirtualMachine) => v.getName(), SORT_DIR.ASC),
      new ListColumn('Assigned Applications', 3, (v: VirtualMachine) => v.getApps(), this.formatName),
      new TextColumn('Modification Date',      2, (v: VirtualMachine) => v.readableModificationDate, SORT_DIR.DESC),
      new TextColumn('Operating System',      2, (v: VirtualMachine) => v.os, SORT_DIR.ASC),
      new TextColumn('Version',               1, (v: VirtualMachine) => String(v.version), SORT_DIR.ASC),
      new TextColumn('Status',                1, this.formatStatus, SORT_DIR.ASC)
    ];
  }

  /**
   * This page just needs to show all VMs, and the apps assigned to each VM.
   *
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS, DatasetNames.VMS];
  }

  /**
   * @return a string to be displayed in the virtue table, when no VM templates exit.
   */
  getNoDataMsg(): string {
    return "There are no virtual machine templates available to add. Create new templates through the Virtual Machines tab.";
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.fillTable(this.datasets[DatasetNames.VMS].asList());
  }
}
