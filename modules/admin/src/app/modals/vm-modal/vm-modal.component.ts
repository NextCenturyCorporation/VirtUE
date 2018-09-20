import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrls, Datasets } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { Item } from '../../shared/models/item.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-vm-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class VmModalComponent extends GenericModalComponent implements OnInit {

  /**
   * see [[GenericModalComponent.constructor]] for notes on parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<GenericModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
    ) {
      super(router, baseUrlService, itemService, dialog, dialogRef, data);
    }

  getColumns(): Column[] {
    return [
      new Column('name',  'Template Name',          5, 'asc'),
      new Column('os',    'OS',                     3, 'asc'),
      new Column('apps',  'Assigned Applications',  3, undefined, this.formatName, this.getChildren),
    ];
  }
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VMS,
      neededDatasets: [Datasets.APPS, Datasets.VMS]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain?: string} {
    return {
      prettyTitle: "Virtual Machine Templates",
      itemName: "Vm Template",
      pluralItem: "VM Templates"
    };
  }

  getNoDataMsg(): string {
    return "There are no virtual machine templates available to add. Create new templates through the Virtual Machines tab.";
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.setItems(this.allVms.asList());
  }
}
