import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrls, Datasets } from '../../shared/enums/enums';


/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-virtue-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class VirtueListComponent extends GenericListComponent {

  /**
   * see parent
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);
  }

  /**
   * called after all the datasets have loaded. Pass the virtue list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.allVirtues.asList());
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent.
   */
  getColumns(): Column[] {
    return [
      new Column('name',        'Template Name',      2, 'asc',      this.formatName, undefined, (i: Item) => this.viewItem(i)),
      new Column('vms',         'Virtual Machines',   2, undefined,  this.formatName, this.getChildren, (i: Item) => this.viewItem(i)),
      new Column('apps',        'Applications',       2, undefined,  this.formatName, this.getGrandchildren),
      new Column('lastEditor',  'Last Editor',        2, 'asc'),
      new Column('version',     'Version',            1, 'asc'),
      new Column('modDate',     'Modification Date',  2, 'desc'),
      new Column('status',      'Status',             1, 'asc', this.formatStatus)
    ];
  }

  /**
   * Overrides parent
   * @return always true
   */
  hasColoredLabels(): boolean {
    return true;
  }

  /**
   * See parent
   * @return child-specific information needed by the generic page functions when loading data.
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.VIRTUES,
      neededDatasets: [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES]
    };
  }


  /**
   * See parent for details
   * @return child-list-specific information needed by the generic list page functions.
   */
  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string} {
    return {
      prettyTitle: 'Virtue Templates',
      itemName: 'Virtue Template',
      pluralItem: 'Virtues'
    };
  }

  /**
   * @return a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No virtues have been added at this time. To add a virtue, click on the button \"Add Virtue Template\" above.";
  }

}
