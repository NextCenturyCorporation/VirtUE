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

  submitBtn: any;
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

    this.neededDatasets = ["apps", "vms", "virtues", "users"];

    this.serviceConfigUrl = ConfigUrlEnum.USERS;

    this.datasetName = 'allUsers';
    this.childDatasetName = 'allVirtues';

  }

  activateModal(mode: string): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    if (mode === 'add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    let dialogRef = this.dialog.open( VirtueModalComponent, {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        id: this.item.getName(),
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath,
        selectedIDs: this.item.childIDs
      },
      panelClass: 'virtue-modal-overlay'
    });

    let virtueList = dialogRef.componentInstance.getSelections.subscribe((selectedVirtues) => {
      this.updateChildList(selectedVirtues);
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
  }

  //create and fill the fields the backend expects to see, record any
  //uncollected inputs, and check that the item is valid to be saved
  finalizeItem():boolean {
    //TODO perform checks here, so none of the below changes happen if the item
    //isn't valid

    // remember check enabled

    this.item['roles'] = [];
    if (this.roleUser) {
      this.item['roles'].push('ROLE_USER');
    }
    if (this.roleAdmin) {
      this.item['roles'].push('ROLE_ADMIN');
    }

    if (!this.item['username']) {
      return confirm("You need to enter a username.");
    }

    this.item['username'] = this.item.name,
    this.item['authorities'] = this.item['roles'], //since this is technically an item
    this.item['virtueTemplateIds'] = this.item.childIDs

    //so we're not trying to stringify a bunch of extra fields and data
    this.item.children = undefined;
    this.item.childIDs = undefined;
    this.item['roles'] = undefined;
    return true;
  }
}
