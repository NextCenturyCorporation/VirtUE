import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { MatDialog, MatDialogRef, MatDialogModule } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';
import { SubMenuOptions } from "../../shared/models/subMenuOptions.model";

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { Application } from '../../shared/models/application.model';
import { AddAppComponent } from '../add-app/add-app.component';

import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrls, Datasets } from '../../shared/enums/enums';

/**
 * @class
 * This class represents a table of Applications, which can be viewed, edited, duplicated, enabled/disabled, or deleted.
 *
 * Currently, very little of use is implemented with regard to applications. Only temporary boiler-plate exists for
 * uploading(?) an application, and essentially no data exists to be displayed in the table.
 *
 * @extends GenericListComponent
 */
@Component({
  selector: 'app-apps-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class AppsListComponent extends GenericListComponent {

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);

    // TODO fix app versioning - maybe make automatic?
    // Apps need versions, but they can't default to anything, and it must be made clear
    // that "version" on that modal means "the actual application's version", and not
    // "version" as in "this is the 4th change I've made to this Chrome application item".
  }

  /**
   * called after all the datasets have loaded. Pass the app list to the table.
   */
  onPullComplete(): void {
    this.setItems(this.allApps.asList());
  }

  /**
   * @return a list of the columns to show up in the table. See details in parent, [[GenericListComponent.getColumns]].
   */
  getColumns(): Column[] {
    return [
      new Column('name',    'Application Name', 5, 'asc'),
      new Column('version', 'Version',          3, 'asc'),
      new Column('os',      'Operating System', 4, 'desc')
    ];
  }

  /**
   * See [[GenericPageComponent.getPageOptions]]
   * @return child-specific information needed by the generic page functions when loading data.
   */
  getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]} {
    return {
      serviceConfigUrl: ConfigUrls.APPS,
      neededDatasets: [Datasets.APPS]
    };
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
   * overrides parent, [[GenericListComponent.getTableFilters]]
   * @return an empty list; Apps can't be disabled, so nothing to filter
   */
  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  /**
   * overrides parent, [[GenericListComponent.getSubMenu]]
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
      width: '480px'//,
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
