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
 * #uncommented
 * @class
 * @extends
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
   * #uncommented
   * @param
   *
   * @return
   */
  getColumns(): Column[] {
    return [
      new Column('name',    'Template Name',         3, 'asc'),
      new Column('vms',     'Virtual Machines',      3, undefined, this.formatName, this.getChildren),
      new Column('apps',    'Assigned Applications', 3, undefined, this.formatName,  this.getGrandchildren),
      // new Column('version', 'Version',               1, 'asc'), // could this be useful?
      new Column('modDate', 'Modification Date',     2, 'desc'),
      new Column('status',  'Status',                1, 'asc', this.formatStatus)
    ];
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  hasColoredLabels(): boolean {
    return true;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  onPullComplete(): void {
    this.setItems(this.allVirtues.asList());
  }

  /**
   * #uncommented
   * @param
   *
   * @return
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
   * #uncommented
   * @param
   *
   * @return
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
   * #uncommented
   * @param
   *
   * @return
   */
  getNoDataMsg(): string {
    return "No virtues have been added at this time. To add a virtue, click on the button \"Add Virtue Template\" above.";
  }
}
