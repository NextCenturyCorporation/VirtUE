import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { ItemService } from '../shared/services/item.service';

import { AppsModalComponent } from '../modals/apps-modal/apps-modal.component';

import { Item } from '../shared/models/item.model';
import { Application } from '../shared/models/application.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { DictList } from '../shared/models/dictionary.model';
import { Column } from '../shared/models/column.model';
import { RowOptions } from '../shared/models/rowOptions.model';

import { ConfigUrlEnum } from '../shared/enums/enums';
import { OSSet } from '../shared/sets/os.set';

import { GenericFormComponent } from '../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-vm',
  templateUrl: './vm.component.html',
  styleUrls: ['../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService, OSSet ]
})
export class VmComponent extends GenericFormComponent {


  constructor(
    protected osOptions: OSSet,
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/vm-templates', activatedRoute, router, baseUrlService, itemService, dialog);

    this.item = new VirtualMachine(undefined);

    this.datasetName = 'allVms';
    this.childDatasetName = 'allApps';

    // No place to navigate to, since apps don't currently each have their own page
    this.childDomain = undefined;
  }

  getNoDataMsg(): string {
    return 'No virtual machine templates have been created yet. To add a template, click on the button "Add VM Template" above.';
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.VMS,
      neededDatasets: ['apps', 'vms', 'virtues']
    };
  }

  getChildColumns(): Column[] {
    return [
      new Column('name',    'Application Name', false, 'asc', 5),
      new Column('version', 'Version',          false, 'asc', 3),
      new Column('os',      'Operating System', false, 'desc', 4)
    ];
  }

  getParentColumns(): Column[] {
    return [
      new Column('name',            'Template Name',    false, 'asc',     6, undefined, (i: Item) => this.viewItem(i)),
      new Column('version',         'Version',          false, 'asc',     3),
      new Column('status',          'Status',           false, 'asc',     3, this.formatStatus)
    ];
  }

  buildParentTable(): void {
    this.parentTable.setUp({
      cols: this.getParentColumns(),
      opts: [],
      coloredLabels: false,
      filters: [],
      tableWidth: 12,
      noDataMsg: "No user has been assigned this Virtue at the moment.",
      hasCheckBoxes: false
    });
  }

  populateParentTable() {
    this.parentTable.items = new Array<Item>();
    for (let u of this.allUsers.asList()) {

      if ( u.children.has(this.item.getID()) ) {
        this.parentTable.items.push(u);
      }
    }
  }

  // functionality not implemented
  buildInstanceTable(): void {}
  populateInstanceTable(): void {}

  getModal(
    params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  ): any {
    return this.dialog.open( AppsModalComponent, params);
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {

    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid

    // The following are required:
    //  'id'           should be ok as-is. May be empty if creating new.
    //  'name'         can't be empty
    //  'os'           must be set
    this.item['loginUser'] = 'system'; // TODO does this still exist on the backend?

    // TODO check if necessary, and what the string should be (admin vs administrator)
    //  this.item['lastEditor'] = 'administrator';

    //  'enabled'      must be either true or false
    this.item['applicationIds'] = this.item.childIDs;  // may be empty

    this.item.children = undefined;
    this.item.childIDs = undefined;
    return true;
  }
}
