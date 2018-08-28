import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { VirtueModalComponent } from '../../modals/virtue-modal/virtue-modal.component';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { DictList } from '../../shared/models/dictionary.model';
import { Column } from '../../shared/models/column.model';
import { Mode } from '../../shared/enums/enums';
import { RowOptions } from '../../shared/models/rowOptions.model';


import { ConfigUrlEnum } from '../../shared/enums/enums';

import { GenericFormComponent } from '../../shared/abstracts/gen-form/gen-form.component';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService ]
})

export class UserComponent extends GenericFormComponent {

  roleUser: boolean;
  roleAdmin: boolean;

  fullImagePath: string;

  constructor(
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/users', activatedRoute, router, baseUrlService, itemService, dialog);

    //gets overwritten once the datasets load, if mode is EDIT or DUPLICATE
    this.item = new User(undefined);

    this.datasetName = 'allUsers';
    this.childDatasetName = 'allVirtues';

    this.childDomain = "/virtues";
  }

  setUpFormValues(): void {
    this.roleUser = this.item['roles'].includes("ROLE_USER");
    this.roleAdmin = this.item['roles'].includes("ROLE_ADMIN");
  }

  getColumns(): Column[] {
    return [
      //See note in gen-form getOptionsList
      // {name: 'name',            prettyName: 'Template Name',      isList: false,  sortDefault: 'asc', colWidth:3, formatValue: undefined, link:(i:Item) => this.editItem(i)},
      {name: 'name',            prettyName: 'Template Name',      isList: false,  sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'childNamesHTML',  prettyName: 'Virtual Machines',   isList: true,   sortDefault: undefined, colWidth:3, formatValue: this.getChildNamesHtml},
      {name: 'apps',            prettyName: 'Applications',       isList: true,   sortDefault: undefined, colWidth:3, formatValue: this.getGrandchildrenHtmlList},
      {name: 'version',         prettyName: 'Version',            isList: false,  sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'status',          prettyName: 'Status',             isList: false,  sortDefault: 'asc', colWidth:1, formatValue: this.formatStatus}
    ];
  }

  getNoDataMsg(): string {
    return "No users have been created yet. To add a user, click on the button \"Add User\" above.";
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.USERS,
      neededDatasets: ["apps", "vms", "virtues", "users"]
    };
  }

  //create and fill the fields the backend expects to see, record any
  //uncollected inputs, and check that the item is valid to be saved
  finalizeItem():boolean {
    //TODO perform checks here, so none of the below changes happen if the item
    //isn't valid

    //remember these aren't security checks, merely checks to prevent the user
    //from accidentally putting in bad data

    // remember to check enabled

    this.item['roles'] = [];
    if (this.roleUser) {
      this.item['roles'].push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item['roles'].push('ROLE_ADMIN');
    }

    if (this.mode === Mode.CREATE && !this.item['username']) {
      return confirm("You need to enter a username.");
    }

    //if not editing, make sure username isn't taken

    this.item['username'] = this.item.name,
    this.item['authorities'] = this.item['roles'], //since this is technically an item
    this.item['virtueTemplateIds'] = this.item.childIDs

    //so we're not trying to stringify a bunch of extra fields and data
    this.item.children = undefined;
    this.item.childIDs = undefined;
    this.item['roles'] = undefined;
    return true;
  }

  //overrides parent
  //remember this is for the table, holding the user's virtues
  hasColoredLabels() {
    return true;
  }

  getModal(
    params:{width:string, height:string, data:{id:string, selectedIDs:string[] }}
  ): any {
    return this.dialog.open( VirtueModalComponent, params);
  }
}
