import { Component, OnInit, ViewChild } from '@angular/core';

import { MatDialog } from '@angular/material';


import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import {
  Column,
  TextColumn,
  CheckboxColumn,
  IconColumn,
  SORT_DIR
} from '../../shared/models/column.model';

import { FileSystem } from '../../shared/models/fileSystem.model';
import { SubMenuOptions } from '../../shared/models/subMenuOptions.model';

import { FileSystemModalComponent } from '../../modals/fileSystem-modal/fileSystem.modal';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * @class
 */
@Component({
  selector: 'app-config-file-sys-tab',
  templateUrl: './config-fileSys-tab.component.html',
  styleUrls: ['../config.component.css']
})
export class ConfigFileSysTabComponent extends GenericDataTabComponent implements OnInit {

  @ViewChild(GenericTableComponent) fileSystemsTable: GenericTableComponent<FileSystem>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      routerService: RouterService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(routerService, dataRequestService, dialog);
    this.tabLabel = "File Systems";
  }

  init(): void {
    this.setUpTable();
  }

  onPullComplete(): void {
    this.fileSystemsTable.populate(this.datasets[DatasetNames.FILE_SYSTEMS].asList());
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.FILE_SYSTEMS];
  }

  setUpTable(): void {
    if (this.fileSystemsTable === undefined) {
      return;
    }
    this.fileSystemsTable.setUp({
      cols: this.getColumns(),
      tableWidth: 1,
      noDataMsg: "No file systems have been made known to the system.",
      elementIsDisabled: (fs: FileSystem) => !fs.enabled
    });
  }

  getColumns(): Column[] {
    return [
      new TextColumn("File system", 3, (fs: FileSystem) => fs.name, SORT_DIR.ASC),
      new TextColumn("Address", 2, (fs: FileSystem) => fs.address, SORT_DIR.ASC),
      new CheckboxColumn("Enabled", 2, "enabled", undefined,
              (fs: FileSystem, checked: boolean) => this.setItemAvailability(fs, checked)),
      new CheckboxColumn("Read", 1, "readPerm", undefined,
              (fs: FileSystem, checked: boolean) => {fs.readPerm = checked; this.updateItem(fs); }),
      new CheckboxColumn("Write", 1, "writePerm", undefined,
              (fs: FileSystem, checked: boolean) => {fs.writePerm = checked; this.updateItem(fs); }),
      new CheckboxColumn("Execute", 1, "executePerm", undefined,
              (fs: FileSystem, checked: boolean) => {fs.executePerm = checked; this.updateItem(fs); }),
      new IconColumn("Options", 1, "settings", (fs: FileSystem) => this.editFileSystem(fs)),
      new IconColumn("Remove File System", 1, "delete", (fs: FileSystem) => this.deleteItem(fs))
    ];
  }

  editFileSystem(fs: FileSystem): void {
    this.activateFileSystemModal(false, fs);
  }

  createNewFileSystem(): void {
    this.activateFileSystemModal(true);
  }

  activateFileSystemModal(creatingNew: boolean, fileSystemToEdit?: FileSystem): void {
    let params: {height: string, width: string, data?: {fileSystem: FileSystem}};
    if (creatingNew) {
      params = {
        height: '70%',
        width: '40%'
      };
    }
    else {
      params = {
        height: '70%',
        width: '40%',
        data: {
          fileSystem: fileSystemToEdit
        }
      };
    }


    let dialogRef = this.dialog.open( FileSystemModalComponent, params);

    let sub = dialogRef.componentInstance.getFileSystem.subscribe((fileSys) => {
      if (creatingNew) {
        this.createItem(fileSys);
      }
      else {
        this.updateItem(fileSys);
      }
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%' });

  }
}
