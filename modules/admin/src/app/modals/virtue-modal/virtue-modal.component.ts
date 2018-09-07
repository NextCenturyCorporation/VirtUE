import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrlEnum } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { Item } from '../../shared/models/item.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

@Component({
  selector: 'app-virtue-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class VirtueModalComponent extends GenericModalComponent {



  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<VirtueModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
  ) {
    super(router, baseUrlService, itemService, dialog, dialogRef, data);
    this.neededDatasets = ["apps", "vms", "virtues"];
  }

  getColumns(): Column[] {
    return [
      new Column('name',    'Template Name',      undefined, 'asc',     3),
      new Column('vms',     'Virtual Machines',       this.getChildren, undefined, 3, this.formatName),
      new Column('apps',    'Assigned Applications',  this.getGrandchildren, undefined, 3, this.formatName),
      // new Column('version',         'Version',            false, 'asc',     1), // could this be useful?
      new Column('modDate', 'Modification Date',  undefined, 'desc',    2),
      new Column('status',  'Status',             undefined, 'asc',     1, this.formatStatus)
    ];
  }

  hasColoredLabels() {
    return true;
  }

  onPullComplete() {
    this.setItems(this.allVirtues.asList());
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.VIRTUES,
      neededDatasets: ["apps", "vms", "virtues"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Virtue Templates",
      itemName: "Virtue Template",
      pluralItem: "Virtue Templates",
      domain: '/virtues'
    };
  }

  getNoDataMsg(): string {
    return "No virtues have been added at this time. To add a virtue, click on the button \"Add Virtue Template\" above.";
  }
}
