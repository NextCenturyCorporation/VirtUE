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

import { FileSystem } from '../../shared/models/fileSystem.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

/**
 * @class
 * This class represents a selectable list of FileSystems.
 *
 * @extends [[GenericModalComponent]]
 */
@Component({
  selector: 'app-file-system-selection-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css']
})
export class FileSystemSelectionModalComponent extends GenericModalComponent {

  /**
   * see [[GenericModalComponent.constructor]] for notes on parameters
   */
  constructor(
      routerService: RouterService,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<FileSystemSelectionModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
  ) {
    super(routerService, baseUrlService, dataRequestService, dialog, dialogRef, data);
    this.pluralItem = "FileSystems";
  }

  /**
   * @return what columns should show up in the the fileSystem selection table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Name',   3, (fs: FileSystem) => fs.name, SORT_DIR.ASC),
      new TextColumn('Address',   2, (fs: FileSystem) => fs.address, SORT_DIR.ASC),
      new TextColumn('Default perms',      1, (fs: FileSystem) => fs.formatPerms(), SORT_DIR.ASC)
    ];
  }

  /**
   * @override [[GenericModalComponent.customizeTableParams]]
   */
  customizeTableParams(params): void {
    params['elementIsDisabled'] = (fs: FileSystem) => !fs.enabled;
  }

  /**
   * populates the table once data is available.
   */
  onPullComplete(): void {
    this.fillTable(this.datasets[DatasetNames.FILE_SYSTEMS].asList());
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.FILE_SYSTEMS];
  }

  /**
   * @return a string to be displayed in the virtue table, when no virtue templates exit.
   */
  getNoDataMsg(): string {
    return "No file systems have been set up on the global settings page.";
  }
}
