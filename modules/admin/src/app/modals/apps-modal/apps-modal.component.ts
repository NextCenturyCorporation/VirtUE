import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import {
  Column,
  TextColumn,
  SORT_DIR
} from '../../shared/models/column.model';
import { Application } from '../../shared/models/application.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/**
 * @class
 * This class represents a list of applications, which can be selected.
 *
 *
 * @extends [[GenericModalComponent]]
 */
@Component({
  selector: 'app-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, DataRequestService ]
})
export class AppsModalComponent extends GenericModalComponent {

  /**
   * see [[GenericModalComponent.constructor]] for notes on parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<AppsModalComponent>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    super(router, baseUrlService, dataRequestService, dialog, dialogRef, data);
    this.pluralItem = "Applications";
  }

  /**
   * @return what columns should show up in the the app selection table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Application Name', 5, (a: Application) => a.getName(), SORT_DIR.ASC),
      new TextColumn('Version',          3, (a: Application) => String(a.version), SORT_DIR.ASC),
      new TextColumn('Operating System', 4, (a: Application) => a.os, SORT_DIR.DESC)
    ];
  }

  /**
   * This page only needs to list all available apps, and doesn't need to request any other data.
   *
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS];
  }

  /**
   * @return a string to be displayed in the virtue table, when no apps exit.
   */
  getNoDataMsg(): string {
    return "There are no applications available to add. Add new applications through the Applications tab.";
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.fillTable(this.datasets[DatasetNames.APPS].asList());
  }
}
