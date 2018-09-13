import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-virtue-main-tab',
  templateUrl: './virtue-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})
export class VirtueMainTabComponent extends GenericMainTabComponent implements OnInit {

  /** #uncommented */
  private newVersion: number;

  /** #uncommented */
  protected item: Virtue;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";
  }


  /**
   * #uncommented
   * @param
   *
   * @return
   */
  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-main-tab which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;

    this.setMode(mode);
  }

  /**
   * Overrides parent, [[GenericMainTabComponent.setMode]]
   * @param newMode the Mode to set the page as.
   *
   */
  setMode(newMode: Mode): void {
    this.mode = newMode;

    this.newVersion = Number(this.item.version);

    if (this.mode !== Mode.VIEW && this.mode !== Mode.CREATE) {
      this.newVersion++;
    }
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // TODO start implementing view page from this.
  getColumns(): Column[] {
    let cols: Column[] = [
      new Column('os',          'OS',                    2, 'asc'),
      new Column('childNames',  'Assigned Applications', 4, undefined,  this.formatName, this.getChildren),
      new Column('status',      'Status',                2, 'asc',      this.formatStatus)
    ];
    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('name', 'VM Template Name', 4, 'asc', undefined, undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('name', 'VM Template Name', 4, 'asc'));
    }

    return cols;
  }


  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getNoDataMsg(): string {
    return "No virtual machine templates have been given to this Virtue yet. \
To add a virtual machine template, click on the button \"Add VM\" above.";
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  collectData(): boolean {
    this.item.version = String(this.newVersion);
    return true;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getDialogRef(params: {height: string, width: string, data: any}) {
    return this.dialog.open( VmModalComponent, params);
  }
}
