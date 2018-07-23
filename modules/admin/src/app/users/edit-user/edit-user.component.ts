import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { User } from '../../shared/models/user.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  providers: [ ApplicationsService, BaseUrlService, UsersService, VirtuesService ]
})

export class EditUserComponent implements OnInit {

  baseUrl: string;
  submitBtn: any;
  fullImagePath: string;
  user: {
    username: string,
    data: string,
    enabled: boolean,
    roles: any,
    virtueIDs: any,
    virtues: any
  }

  mode: string;
  actionName: string;

  // userRoles = [];
  // userData = [];
  // userVirtueIDs = [];
  allVirtues = [];
  allApps = [];
  adUserCtrl: FormControl;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {
    this.user = {
      username : '',
      data : '',
      enabled : false,
      roles: [],
      virtueIDs: [],
      virtues: [],
    };
  }

  ngOnInit() {
    this.user.username = this.activatedRoute.snapshot.params['id'];

    this.baseUrlService.getBaseUrl().subscribe(data => {
      let url = data[0].aws_server;
      this.setBaseUrl(url);
      this.getUserData();
      this.getAllVirtues();
      this.getAllApps();
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.getUserData();
    }, 1000);
  }

  setBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  getUserData() {
    this.usersService.getUser(this.baseUrl, this.user.username).subscribe(uData => {
      this.user.data = uData;
      this.user.roles = uData.authorities;
      this.user.enabled = uData.enabled;
      this.updateVirtueList(uData.virtueTemplateIds);
    });
  }

  selectedRole(role: string) {
    if (this.user.roles.length > 0) {
      for (let sel of this.user.roles) {
        if (sel === role) {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  getAllVirtues() {
    if (this.baseUrl !== null) {
      this.virtuesService.getVirtues(this.baseUrl).subscribe(virts => {
        this.allVirtues = virts;
      });
    }
  }

  getAllApps() {
    if (this.baseUrl !== null) {
      this.appsService.getAppsList(this.baseUrl).subscribe(apps => {
        this.allApps = apps;
      });
    }
  }

  getVirtueName(id: string) {
    let userVirtue = [];
    if (id !== null) {
      userVirtue = this.allVirtues.filter(virt => virt.id === id)
        .map(virtue => virtue.name);
      return userVirtue;
    }
  }

  // getAppsHTML(virtue: any) {
  //   let virtueApps: any;
  //   let virtueAppsList: any;
  //   if (this.baseUrl !== null) {
  //     virtueAppsList = this.allVirtues.filter(virt => virt.id === vID)
  //       .map(virtue => virtue.applicationIds);
  //     virtueAppsString = virtueAppsList.toString();
  //     // console.log(virtueAppsList);
  //     return this.generateAppsListHTML(virtueAppsString);
  //   }
  // }

  generateAppsListHTML(virtue: any) {
    let appsString = virtue.applicationIds.toString();
    let appList: any;
    let appInfo: any;
    let appNames: string = '';
    let i: number = 0;
    appList = appsString.split(',');
    for (let id of appList) {
      i++;
      appInfo = this.allApps.filter(data => data.id === id)
        .map(app => app.name);
      // console.log(appInfo.toString());
      appNames = appNames + `<li>${appInfo.toString()}</li>`;
    }
    return appNames;
  }

  updateVirtueList(newVirtueIDs: any) {
    this.user.virtueIDs = newVirtueIDs;
    this.user.virtues = [];
      for (let vID of newVirtueIDs) {
        for (let virtue of this.allVirtues) {
          if (vID === virtue.id) {
            this.user.virtues.push(virtue);
            break;
          }
        }
      }
  }

  removeVirtue(id: string, index: number): void {
    this.user.virtues = this.user.virtues.filter(virt => {
      return virt.id !== id;
    });
    this.user.virtueIDs.splice(index, 1);
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
        id: this.user.username,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath,
        userVirtueIDs: this.user.virtueIDs
      },
      panelClass: 'virtue-modal-overlay'
    });

    let virtueList = dialogRef.componentInstance.addVirtues.subscribe((selectedVirtues) => {
      this.updateVirtueList(selectedVirtues);
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
  }

  updateThisUser(roleUser: boolean, roleAdmin: boolean) {
    let roles = [];
    if (roleUser) {
      roles.push('ROLE_USER');
    }
    if (roleAdmin) {
      roles.push('ROLE_ADMIN');
    }
    let body = {
      'username': this.user.username,
      'authorities': roles,
      'virtueTemplateIds': this.user.virtueIDs,
      'enabled': this.user.enabled
    };
    console.log(body);
    this.usersService.updateUser(this.baseUrl, this.user.username, JSON.stringify(body)).subscribe(
      error => {
        console.log(error);
      });
    this.resetRouter();
    this.router.navigate(['/users']);
  }

  toggleUserStatus() {
    this.user.enabled = !this.user.enabled;
  }
}
