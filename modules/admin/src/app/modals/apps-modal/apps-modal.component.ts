import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrls, Datasets } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class AppsModalComponent extends GenericModalComponent {

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<AppsModalComponent>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    super(router, baseUrlService, itemService, dialog, dialogRef, data);
  }

  getColumns(): Column[] {
    return [
      new Column('name',    'Application Name', 5, 'asc'),
      new Column('version', 'Version',          3, 'asc'),
      new Column('os',      'Operating System', 4, 'desc')
    ];
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.APPS,
      neededDatasets: [Datasets.APPS]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain?: string} {
    return {
      prettyTitle: "Available Applications",
      itemName: "Application",
      pluralItem: "Applications"
    };
  }

  getNoDataMsg(): string {
    return "There are no applications available to add. Add new applications through the Applications tab.";
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.setItems(this.allApps.asList());
  }
}
