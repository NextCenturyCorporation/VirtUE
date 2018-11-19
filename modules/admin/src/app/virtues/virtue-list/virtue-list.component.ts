import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import {  Column,
          TextColumn,
          ListColumn,
          SORT_DIR  } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { ItemListComponent } from '../../shared/abstracts/item-list/item-list.component';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';


/**
 * @class
 * This class represents a table of Virtues, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 * Links to the view pages for each virtue's assigned virtual machines are also listed.
 * Also allows the creation of new Virtues.
 *
 * Currently, all the applications available to all the VMs the virtue has been assigned are listed, but there isn't a
 * way to mark which ones are unavailable due to their VM being disabled, and apps don't have a view page yet so their names
 * are only displayed as text, instead of links.
 *
 * @extends ItemListComponent
 */
@Component({
  selector: 'app-virtue-list',
  templateUrl: '../../shared/abstracts/item-list/item-list.component.html',
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css'],
  providers: [ BaseUrlService, DataRequestService  ]
})
export class VirtueListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, dataRequestService, dialog);
  }

  /**
   * called after all the datasets have loaded. Pass the virtue list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.datasets[DatasetNames.VIRTUES].asList());
    this.tempAddRandomPrinter();
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[GenericListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name',     2, (v: Virtue) => v.getName(), SORT_DIR.ASC,  (i: Item) => this.viewItem(i),
                                                                                                    () => this.getSubMenu()),
      new ListColumn('Virtual Machines',  2, (i: Item) => this.getVms(i),      this.formatName,    (i: Item) => this.viewItem(i)),
      new ListColumn('Applications',      2, (i: Item) => this.getVmApps(i), this.formatName),
      new TextColumn('Last Editor',       2, (v: Virtue) => v.lastEditor,       SORT_DIR.ASC),
      new TextColumn('Version',           1, (v: Virtue) => String(v.version),  SORT_DIR.ASC),
      new TextColumn('Modification Date', 2, (v: Virtue) => v.modDate,          SORT_DIR.DESC),
      new TextColumn('Status',            1, this.formatStatus,                 SORT_DIR.ASC)
    ];
  }

  /**
   * add colors to the table defined in [[GenericListComponent]], since here it will be showing Virtues.
   */
  customizeTableParams(params): void {
    params['coloredLabels'] = true;
    params['getColor'] = (v: Virtue) => v.color;
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS, DatasetNames.VMS, DatasetNames.VIRTUES];
  }


  /**
   * See [[GenericListComponent.getListOptions]] for details
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

  tempAddRandomPrinter() {
    // let sub = this.dataRequestService.getItems(Subdomains.PRINTERS).subscribe( data => {
    //     console.log(data);
    //   }, () => {},
    //   () => {sub.unsubscribe();}
    // );

    // let p = {info: "something", status: "active", address: "127.0.0.1", enabled: true};

    // let sub2 = this.dataRequestService.createItem(Subdomains.PRINTERS, JSON.stringify(p)).subscribe( data => {
    //     console.log(data);
    //   }, () => {},
    //   () => {sub2.unsubscribe();}
    // );

    // console.log(this.item.allowedPrinters);
    // this.dataRequestService.createItem(Subdomains.PRINTERS, new Printer("some printer"));
    // this.updateFileSysPermsTable();
  }

}
