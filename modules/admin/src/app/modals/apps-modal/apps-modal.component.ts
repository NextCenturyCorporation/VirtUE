import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrlEnum } from '../../shared/enums/enums';
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
      new Column('name',    'Application Name', false, 'asc', 5),
      new Column('version', 'Version',          false, 'asc', 3),
      new Column('os',      'Operating System', false, 'desc', 4)
    ];
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.APPS,
      neededDatasets: ["apps"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Available Applications",
      itemName: "Application",
      pluralItem: "Applications",
      domain: '/applications'
    };
  }

  getNoDataMsg(): string {
    return "No apps appear to be available at this time.";
  }

  onPullComplete() {
    this.setItems(this.allApps.asList());
  }
}
