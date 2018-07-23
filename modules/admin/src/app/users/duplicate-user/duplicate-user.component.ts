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
  selector: 'app-duplicate-user',
  templateUrl: './duplicate-user.component.html',
  providers: [ ApplicationsService, BaseUrlService, UsersService, VirtuesService ]
})

export class DuplicateUserComponent implements OnInit {
  @Input() user: User;

  baseUrl: string;
  userToEdit: { id: string };
  submitBtn: any;
  fullImagePath: string;
  userRoles = [];
  userData = [];
  userVirtueIDs = [];
  virtues = [];
  appsList = [];
  adUserCtrl: FormControl;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    this.userToEdit = {
      id: this.activatedRoute.snapshot.params['id']
    };

    this.baseUrlService.getBaseUrl().subscribe(data => {
      let awsServer = data[0].aws_server;
      this.setBaseUrl(awsServer);
      this.getUserToEdit(this.userToEdit.id);
      this.getVirtues();
      this.getApps();
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  setBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  getUserToEdit( username: string) {
    this.usersService.getUser(this.baseUrl, username).subscribe(data => {
      this.userData = data;
      this.userRoles = data.authorities;
      this.userVirtueIDs = data.virtueTemplateIds;
    });
  }

  selectedRole(role: string) {
    if (this.userRoles.length > 0) {
      for (let sel of this.userRoles) {
        if (sel === role) {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  getVirtues() {
    if (this.baseUrl !== null) {
      this.virtuesService.getVirtues(this.baseUrl).subscribe(data => {
        this.virtues = data;
      });
    }
  }

  getApps() {
    if (this.baseUrl !== null) {
      this.appsService.getAppsList(this.baseUrl).subscribe(data => {
        this.appsList = data;
      });
    }
  }

  getVirtueName(id: string) {
    let userVirtue = [];
    if (id !== null) {
      userVirtue = this.virtues.filter(data => data.id === id)
        .map(virtue => virtue.name);
      return userVirtue;
    }
  }

  getVirtueAppIds(id: string) {
    let virtueApps: any;
    let virtueAppsList: any;
    if (this.baseUrl !== null) {
      virtueApps = this.virtues.filter(data => data.id === id)
        .map(virtue => virtue.applicationIds);
      virtueAppsList = virtueApps.toString();
      // console.log(virtueAppsList);
      return this.getVirtueApps(virtueAppsList);
    }
  }

  getVirtueApps(apps: any) {
    let appList: any;
    let appInfo: any;
    let appNames: string = '';
    let i: number = 0;
    appList = apps.split(',');
    for (let id of appList) {
      i++;
      appInfo = this.appsList.filter(data => data.id === id)
        .map(app => app.name);
      appNames = appNames + `<li>${appInfo.toString()}</li>`;
    }
    return appNames;
  }

  getUpdatedVirtueList(virtues: any) {
    this.virtues = [];
    this.virtuesService.getVirtues(this.baseUrl)
      .subscribe(data => {
        for (let sel of this.userVirtueIDs) {
          for (let virtue of data) {
            if (sel === virtue.id) {
              this.virtues.push(virtue);
              break;
            }
          }
        }
      });
  }

  removeVirtue(id: string, index: number): void {
    this.virtues = this.virtues.filter(data => {
      return data.id !== id;
    });
    this.userVirtueIDs.splice(index, 1);
  }

  addUser( username: string, roleUser: boolean, roleAdmin: boolean ) {
    username = username.trim().replace(' ', '').toLowerCase();
    let authorities = [];
    let virtueTemplateIds = [];

    if (roleUser) {
      authorities.push('ROLE_USER');
    }
    if (roleAdmin) {
      authorities.push('ROLE_ADMIN');
    }
    for (let item of this.userVirtueIDs) {
      virtueTemplateIds.push(item);
    }

    let body = {
      'username': username,
      'authorities': authorities,
      'virtueTemplateIds': virtueTemplateIds
    };

    if (!body.username) { return; }

    this.usersService.createUser(this.baseUrl, JSON.stringify(body)).subscribe(success => {
        console.log(success);
      },
      err => {
        console.log(err);
      });
    this.resetRouter();
    this.router.navigate(['/users']);
  }

  activateModal(id, mode): void {
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
        id: id,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath,
        userVirtueIDs: this.userVirtueIDs
      },
      panelClass: 'virtue-modal-overlay'
    });

    let virtueList = dialogRef.componentInstance.addVirtues.subscribe((data) => {
      this.userVirtueIDs = data;
      if (this.userVirtueIDs.length > 0) {
        this.userVirtueIDs = [];
      }
      this.userVirtueIDs = this.userVirtueIDs;
      this.getUpdatedVirtueList(this.userVirtueIDs);
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
  }
}
