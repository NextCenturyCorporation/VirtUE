import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrls } from '../../shared/services/config-urls.enum';
import { Datasets } from '../../shared/abstracts/gen-data-page/datasets.enum';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../shared/models/column.model';
import { Virtue } from '../../shared/models/virtue.model';
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
    this.pluralItem = "Virtue Templates";
  }

  /**
   * @return what columns should show up in the the virtue selection table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name',         3, (v: Virtue) => v.getName(), SORT_DIR.ASC),
      new ListColumn('Virtual Machines',      2, this.getChildren,      this.formatName),
      new ListColumn('Assigned Applications', 3, this.getGrandchildren, this.formatName),
      new TextColumn('Version',               1, (v: Virtue) => String(v.version), SORT_DIR.ASC),
      new TextColumn('Modification Date',     2, (v: Virtue) => v.modDate, SORT_DIR.DESC),
      new TextColumn('Status',                1, this.formatStatus, SORT_DIR.ASC)
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
    this.fillTable(this.allVirtues.asList());
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
   * @return a string to be displayed in the virtue table, when no virtue templates exit.
   */
  getNoDataMsg(): string {
    return "There are no virtue templates available to add. Create new templates through the Virtues tab.";
  }
}
