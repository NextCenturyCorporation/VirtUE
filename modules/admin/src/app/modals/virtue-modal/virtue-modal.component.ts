import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrls, Datasets } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { Item } from '../../shared/models/item.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

/**
 * @class
 * This class represents a list of Virtue templates, which can be selected.
 *
 * @extends [[GenericModalComponent]]
 */
@Component({
  selector: 'app-virtue-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class VirtueModalComponent extends GenericModalComponent {

  /**
   * see [[GenericModalComponent.constructor]] for notes on parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<VirtueModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
  ) {
    super(router, baseUrlService, itemService, dialog, dialogRef, data);
  }

  /**
   * @return what columns should show up in the the virtue selection table
   */
  getColumns(): Column[] {
    return [
      new Column('name',    'Template Name',         3, 'asc'),
      new Column('vms',     'Virtual Machines',      3, undefined, this.formatName, this.getChildren),
      new Column('apps',    'Assigned Applications', 3, undefined, this.formatName,  this.getGrandchildren),
      // new Column('version', 'Version',               1, 'asc'), // could this be useful?
      new Column('modDate', 'Modification Date',     2, 'desc'),
      new Column('enabled',  'Status',                1, 'asc', this.formatStatus)
    ];
  }

  /**
   * @return true because this table holds Virtue Templates
   */
  hasColoredLabels(): boolean {
    return true;
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.setItems(this.allVirtues.asList());
  }

  /**
   * This page needs all datasets to load except allUsers: it displays all virtues, the VMs assigned to each virtue,
   * and the apps available to each Virtue through its VMs.
   *
   * See [[GenericPageComponent.getPageOptions]]() for details on return values
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VIRTUES,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES]
    };
  }

  /**
   * See [[GenericListComponent.getListOptions]] for details
   * @return child-list-specific information needed by the generic list page functions.
   */
  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain?: string} {
    return {
      prettyTitle: "Virtue Templates",
      itemName: "Virtue Template",
      pluralItem: "Virtue Templates"
    };
  }

  /**
   * @return a string to be displayed in the virtue table, when no virtue templates exit.
   */
  getNoDataMsg(): string {
    return "There are no virtue templates available to add. Create new templates through the Virtues tab.";
  }
}
