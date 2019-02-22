import { Component, OnInit, ViewChild } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { Column, TextColumn, ListColumn, SORT_DIR } from '../../../models/column.model';
import { SubMenuOptions } from '../../../models/subMenuOptions.model';
import { DictList } from '../../../models/dictionary.model';

import { GenericDataPageComponent } from '../../gen-data-page/gen-data-page.component';
import { GenericTableComponent } from '../gen-table.component';

import { RouterService } from '../../../services/router.service';
import { BaseUrlService } from '../../../services/baseUrl.service';
import { DataRequestService } from '../../../services/dataRequest.service';

import { DatasetNames } from '../../gen-data-page/datasetNames.enum';
import { Mode } from '../../gen-form/mode.enum';

/**
* @class
 */
@Component({
  selector: 'app-simple-table',
  templateUrl: '../gen-table.component.html',
  styleUrls: ['../gen-table.component.css']
})
export class SimpleTableComponent extends GenericTableComponent<{key: string, value: string}> implements OnInit {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super();
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    // meaningless, just to have something before data loads completely


    this.setUp(this.getTableParams());
  }


  getTableParams() {
    return {
      cols: this.getColumns(),
      tableWidth: 12,
      noDataMsg: this.getNoDataMsg()
    };
  }

  /**
   * Populates the table with the input list of items.
   * Abstracts away table from subclasses
   *
   * @param newItems the list of items to be displayed in the table.
   */
  setItems(newItems: any[]): void {

    this.populate(newItems);
  }


  /**
   * This defines what columns show up in the table.
   *
   * Note: the summed widths of all columns must add to exactly 12.
   * Too low will not scale to fit, and too large will cause columns to wrap, within each row.
   *
   * @return a list of columns to be displayed within the table of Items.
   */
  getColumns(): Column[] {
    let cols: Column[] = [
      new TextColumn("Attribute", 4, (obj) => Object.keys(obj)[0], SORT_DIR.ASC),
      new TextColumn("Value",     4, (obj) => this.formatValue(obj), SORT_DIR.ASC)
    ];
    return cols;
  }

  formatValue(obj): string {
    let attr = Object.keys(obj)[0];
    let value = obj[attr];
    // if (Array.isArray(value)) {
    //   return value.toString();
    // }
    if (typeof value === 'object') {
      return JSON.stringify(value, undefined, 4);
    }
    return value;
  }

  /**
   * @returns a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "";
  }

}
