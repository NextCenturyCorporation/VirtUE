import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { UsersService } from '../../shared/services/users.service';

import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { DictList } from '../../shared/models/dictionary.model';

import { GenericFormComponent } from '../../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  providers: [ BaseUrlService, ApplicationsService, VirtualMachineService, VirtuesService, UsersService ]
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
    usersService: UsersService,
    virtuesService: VirtuesService,
    vmService: VirtualMachineService,
    appsService: ApplicationsService,
    dialog: MatDialog
  ) {
    super('/users', activatedRoute, router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);

    this.item = new User(undefined);

    this.updateFuncQueue = [this.pullVirtues, this.pullUsers];

    this.serviceCreateFunc = this.usersService.createUser;
    this.serviceUpdateFunc = this.usersService.updateUser;

    this.datasetName = 'allUsers';
    this.childDatasetName = 'allVirtues';

  }

  activateModal(mode: string): void {
    let dialogHeight = 600;
    let dialogWidth = 800;
    // let fullImagePath = './assets/images/app-icon-white.png';

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
        userVirtueIDs: this.item.childIDs
      },
      panelClass: 'virtue-modal-overlay'
    });

    let virtueList = dialogRef.componentInstance.addVirtues.subscribe((selectedVirtues) => {
      this.updateChildList(selectedVirtues);
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
  }

  //Doesn't need to do anything
  finalizeItem():boolean {
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
  }
}
