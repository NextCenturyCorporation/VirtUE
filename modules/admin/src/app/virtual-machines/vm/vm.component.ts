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

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { DictList } from '../../shared/models/dictionary.model';

import { GenericFormComponent } from '../../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-vm',
  templateUrl: './vm.component.html',
  providers: [ BaseUrlService, ApplicationsService, VirtualMachineService, VirtuesService, UsersService ]
})
export class VmComponent extends GenericFormComponent {

  osList = [
    { 'name': 'Debian', 'os': 'LINUX' },
    { 'name': 'Windows', 'os': 'WINDOWS' }
  ];
  securityOptions = [
    { 'level': 'default', 'name': 'Default' },
    { 'level': 'email', 'name': 'Email' },
    { 'level': 'power', 'name': 'Power User' },
    { 'level': 'admin', 'name': 'Administrator' }
  ];

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
    super('/vm-templates', activatedRoute, router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);

    this.item = new VirtualMachine(undefined);

    this.updateFuncQueue = [this.pullApps, this.pullVms];

    this.serviceCreateFunc = this.vmService.createVM;
    this.serviceUpdateFunc = this.vmService.updateVM;

    this.datasetName = 'allVms';
    this.childDatasetName = 'allApps';
  }


  activateModal(): void {
    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.item.childIDs
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((dialogAppsList) => {
      this.updateChildList(dialogAppsList);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  //Nothing needs to be done atm. Should check that a name has been entered though.
  finalizeItem(): boolean {
    return true;
  }
}
