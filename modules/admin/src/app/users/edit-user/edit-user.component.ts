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
  @Input() user: User;

  baseUrl: string;
  userToEdit: { id: string };
  submitBtn: any;
  fullImagePath: string;
  userRoles = [];
  userData = [];
  storedVirtues = [];
  selVirtues = [];
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
      let url = data[0].aws_server;
      this.getBaseUrl(url);
      this.getUserToEdit(url, this.userToEdit.id);
      this.getVirtues(url);
      this.getApps(url);
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  getBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  getUserToEdit(baseUrl: string, username: string) {
    this.usersService.getUser(baseUrl, username).subscribe(data => {
      this.userData = data;
      this.userRoles = data.authorities;
      this.selVirtues = data.virtueTemplateIds;
      this.storedVirtues = data.virtueTemplateIds;
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

  getVirtues(baseUrl: string) {
    if (baseUrl !== null) {
      this.virtuesService.getVirtues(baseUrl).subscribe(data => {
        this.virtues = data;
      });
    }
  }

  getApps(baseUrl: string) {
    if (baseUrl !== null) {
      this.appsService.getAppsList(baseUrl).subscribe(data => {
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
      // console.log(appInfo.toString());
      appNames = appNames + `<li>${appInfo.toString()}</li>`;
    }
    return appNames;
  }

  getUpdatedVirtueList(virtues: any) {
    this.virtues = [];
    this.virtuesService.getVirtues(this.baseUrl)
      .subscribe(data => {
        for (let sel of this.selVirtues) {
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
    this.selVirtues.splice(index, 1);
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
        storedVirtues: this.selVirtues
      },
      panelClass: 'virtue-modal-overlay'
    });

    let virtueList = dialogRef.componentInstance.addVirtues.subscribe((data) => {
      this.selVirtues = data;
      if (this.storedVirtues.length > 0) {
        this.storedVirtues = [];
      }
      this.storedVirtues = this.selVirtues;
      this.getUpdatedVirtueList(this.storedVirtues);
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
  }

  updateThisUser(username: string, roleUser: any, roleAdmin: any) {
    let authorities = [];
    if (roleUser) {
      authorities.push('ROLE_USER');
    }
    if (roleAdmin) {
      authorities.push('ROLE_ADMIN');
    }
    let body = {
      'username': username,
      'authorities': authorities,
      'virtueTemplateIds': this.selVirtues
    };
    console.log(body);
    this.usersService.updateUser(this.baseUrl, username, JSON.stringify(body)).subscribe(
      error => {
        console.log(error);
      });
    this.resetRouter();
    this.router.navigate(['/users']);
  }

}
