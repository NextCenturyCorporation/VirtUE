import { Component, OnInit, ViewChild } from '@angular/core';

import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { GenericDataTabComponent } from '../../shared/abstracts/gen-data-page/gen-data-tab.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import {
  Column,
  TextColumn,
  IconColumn,
  SORT_DIR
} from '../../shared/models/column.model';

import { ConfigUrls } from '../../shared/services/config-urls.enum';
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
  styleUrls: ['./config-fileSys-tab.component.css']
})
export class ConfigFileSysTabComponent extends GenericDataTabComponent implements OnInit {

  /** #uncommented, unimplemented */
  @ViewChild(GenericTableComponent) fileSystemsTable: GenericTableComponent<{foo: number, bar: string}>;

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
    // this.fileSystemsTable.populate(this.datasets[DatasetNames.FILE_SYSTEMS].asList());
  }

  /**
   * #unimplemented
   * See [[GenericDataPageComponent.getDataPageOptions]]() for details on return values
   */
  getDataPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: DatasetNames[]} {
    return {
      serviceConfigUrl: ConfigUrls.USERS,//ConfigUrls.FILE_SYSTEMS,
      neededDatasets: []//DatasetNames.FILE_SYSTEMS
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
      noDataMsg: "Not yet implemented."
    });
  }

  /** #unimplemented */
  getColumns(): Column[] {
    return [
      new TextColumn("File system number", 4, (s: {foo: string, bar: string}) => s.foo, SORT_DIR.ASC),
      new TextColumn("File System type name", 4, (s: {foo: string, bar: string}) => s.bar, SORT_DIR.ASC),
      new IconColumn("Remove File System (placeholder)", 4, "delete", (obj) => {obj.foo++; })
    ];
  }
}
