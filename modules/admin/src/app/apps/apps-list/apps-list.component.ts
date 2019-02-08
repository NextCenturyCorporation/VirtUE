import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { MatDialog, MatDialogRef, MatDialogModule } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { IndexedObj } from '../../shared/models/indexedObj.model';
import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import {  Column,
          TextColumn,
          ListColumn,
          SORT_DIR  } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';
import { SubMenuOptions } from '../../shared/models/subMenuOptions.model';

import { RouterService } from '../../shared/services/router.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { Application } from '../../shared/models/application.model';
import { AddAppComponent } from '../add-app/add-app.component';

import { ItemListComponent } from '../../shared/abstracts/item-list/item-list.component';

import { DatasetNames } from '../../shared/abstracts/gen-data-page/datasetNames.enum';

/**
 * @class
 * This class represents a table of Applications, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 *
 * Currently, very little of use is implemented with regard to applications. Only temporary boiler-plate exists for
 * uploading(?) an application, and essentially no data exists to be displayed in the table.
 *
 * @extends ItemListComponent
 */
@Component({
  selector: 'app-apps-list',
  templateUrl: '../../shared/abstracts/item-list/item-list.component.html',
  styleUrls: ['../../shared/abstracts/item-list/item-list.component.css']
})
export class AppsListComponent extends ItemListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(routerService, baseUrlService, dataRequestService, dialog);

    // TODO fix app versioning - maybe make automatic?
    // Apps need versions, but they can't default to anything, and it must be made clear
    // that "version" on that modal means "the actual application's version", and not
    // "version" as in "this is the 4th change I've made to this Chrome application item".
  }

  getDatasetToDisplay(): DatasetNames {
    return DatasetNames.APPS;
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[ItemListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Application Name',  5, (a: Application) => a.getName(), SORT_DIR.ASC,
                                                (a: Application) => this.toDetailsPage(a), () => this.getSubMenu()),
      new TextColumn('Version',           3, (a: Application) => String(a.version), SORT_DIR.ASC),
      new TextColumn('Operating System',  4, (a: Application) => a.os, SORT_DIR.DESC),
    ];
  }

  /**
   * @override [[GenericDataPageComponent.getNeededDatasets]]()
   */
  getNeededDatasets(): DatasetNames[] {
    return [DatasetNames.APPS];
  }

  /**
   * See [[ItemListComponent.getListOptions]] for details
   * @return child-list-specific information needed by the generic list page functions.
   */
  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string} {
    return {
      prettyTitle: "Available Applications",
      itemName: "Application",
      pluralItem: "Applications"
    };
  }

  /**
   * @return a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No apps appear to be available at this time. To add an application, click on the button \"Add Application\" above.";
  }

  /**
   * overrides parent, [[ItemListComponent.getTableFilters]]
   * @return an empty list; Apps can't be disabled, so nothing to filter
   */
  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  /**
   * overrides parent, [[ItemListComponent.getSubMenu]]
   *
   * see [[GenericPageComponent.openDialog]] for notes on that call.
   *
   * @return a submenu just with a "remove" option, to delete the Item from the backend
   */
  getSubMenu(): SubMenuOptions[] {
    return [new SubMenuOptions("Remove", () => true, (i: Item) => this.openDialog('Delete ' + i.getName(), () => this.deleteItem(i)))];
  }

  /**
   * Open a form in which the user can 'upload' an application. Currently just boiler-plate, and will probably change
   * substantially in the future. May not remain a dialog.
   */
  openAppsDialog(): void {
    let dialogRef = this.dialog.open(AddAppComponent, {
      width: '480px'// ,
      // data: { file: this.file, url: this.url }
    });

    let sub = dialogRef.afterClosed().toPromise().then(result => {
      console.log(result);
    });
    // let sub = dialogRef.afterClosed().subscribe(result => {
    //   console.log('The dialog was closed');
    // },
    // () => { // on error
    //   sub.unsubscribe();
    // },
    // () => { // when finished
    //   this.refreshData();
    //   sub.unsubscribe();
    // });
  }
}
