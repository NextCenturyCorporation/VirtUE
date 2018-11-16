import { Component, OnInit, ViewChild } from '@angular/core';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

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

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

/**
 * @class
 * This component allows the user (an admin) to set up activ directories. For something.
 * TODO ask about active directories
 * #uncommented, because this is a stub.
 * also #move to a tab on the global settings form
 */
@Component({
  selector: 'app-config-file-sys-tab',
  templateUrl: './config-fileSys-tab.component.html',
  styleUrls: ['../config.component.css']
})
export class ConfigFileSysTabComponent extends GenericDataTabComponent implements OnInit {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) fileSystemsTable: GenericTableComponent<FileSystem>;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      dataRequestService: DataRequestService,
      dialog: MatDialog) {
    super(router, baseUrlService, dataRequestService, dialog);
    this.tabName = "File Systems";
  }

  /**
   * #unimplemented
   */
  init(): void {
    this.setUpTable();
  }

  /**
   * #unimplemented
   */
  onPullComplete(): void {
    this.fileSystemsTable.populate(this.datasets[DatasetNames.FILE_SYSTEMS].asList());
  }

  /**
   * #unimplemented
   * See [[GenericDataPageComponent.getDataPageOptions]]() for details on return values
   */
  getDataPageOptions(): {
      neededDatasets: DatasetNames[]} {
    return {
      neededDatasets: [DatasetNames.FILE_SYSTEMS]
    };
  }


  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
  setUpTable(): void {
    if (this.fileSystemsTable === undefined) {
      return;
    }
    this.fileSystemsTable.setUp({
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "Not yet implemented.",
      elementIsDisabled: (fs: FileSystem) => !fs.enabled
    });
  }

  /** #unimplemented */
  getColumns(): Column[] {
    return [
      new TextColumn("File system", 3, (fs: FileSystem) => fs.name, SORT_DIR.ASC),
      new TextColumn("Address", 3, (fs: FileSystem) => fs.address, SORT_DIR.ASC),
      new CheckboxColumn("Enabled", 1, "enabled", undefined,
              (fs: FileSystem, checked: boolean) => this.setItemAvailability(fs, checked)),
      new CheckboxColumn("Read", 1, "readPerm", undefined,
              (fs: FileSystem, checked: boolean) => {fs.readPerm = checked; this.updateItem(fs); }),
      new CheckboxColumn("Write", 1, "writePerm", undefined,
              (fs: FileSystem, checked: boolean) => {fs.writePerm = checked; this.updateItem(fs); }),
      new CheckboxColumn("Execute", 1, "executePerm", undefined,
              (fs: FileSystem, checked: boolean) => {fs.executePerm = checked; this.updateItem(fs); }),
      new IconColumn("Options", 1, "settings", (fs: FileSystem) => this.printFileSystem(fs)),
      new IconColumn("Remove File System", 1, "delete", (fs: FileSystem) => this.deleteItem(fs))
    ];
  }

  addFileSystem() {
    let fs = new FileSystem({name: "Long-term storage", address: "123.4.5.6.7:~/lts"});
    this.createItem(fs);
    this.dataRequestService.getRecords(fs.getSubdomain()).subscribe( (d) => { console.log(d); } );
  }

  printFileSystem(fs: FileSystem) {
    console.log(fs);
  }
}
