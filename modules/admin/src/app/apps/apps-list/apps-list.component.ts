import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';
import { RowOptions } from "../../shared/models/rowOptions.model";

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { Application } from '../../shared/models/application.model';
import { AddAppComponent } from '../add-app/add-app.component';

import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrlEnum } from '../../shared/enums/enums';

@Component({
  selector: 'app-apps-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class AppsListComponent extends GenericListComponent {

  file: string;
  url: string;

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


  getColumns(): Column[] {
    // This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    //  to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    // Note: colWidths of all columns must add to exactly 12.
    // Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    // See note next to a line containing "mui-col-md-12" in gen-list.component.html
    return [
      new Column('name',    'Application Name', undefined, 'asc', 5),
      new Column('version', 'Version',          undefined, 'asc', 3),
      new Column('os',      'Operating System', undefined, 'desc', 4)
    ];
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.APPS,
      neededDatasets: ["apps"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Available Applications",
      itemName: "Application",
      pluralItem: "Applications",
      domain: '/applications'
    };
  }

  getNoDataMsg(): string {
    return "No apps appear to be available at this time. To add an application, click on the button \"Add Application\" above.";
  }

  // Apps can't be disabled, so nothing to filter
  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  getOptionsList(): RowOptions[] {
    return [new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))];
  }

  // called after all the datasets have loaded
  onPullComplete(): void {
    this.setItems(this.allApps.asList());
  }

  openAppsDialog(): void {
    let dialogRef = this.dialog.open(AddAppComponent, {
      width: '480px',
      data: { file: this.file, url: this.url }
    });

    let sub = dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    },
    () => {},
    () => { // when finished
      this.refreshData();
      sub.unsubscribe();
    });
  }
}
