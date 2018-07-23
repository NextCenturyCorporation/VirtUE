import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';

import { Observable } from 'rxjs/Observable';

import { User } from '../../shared/models/user.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  providers: [ApplicationsService, BaseUrlService, UsersService, VirtuesService]
})

export class AddUserComponent implements OnInit {

  adUserCtrl: FormControl;
  filteredUsers: Observable<any[]>;
  appsList = [];
  userVirtueIDs = [];
  virtues = [];
  awsServer: any;
  baseUrl: string;
  fullImagePath: string;
  submitBtn: any;

  constructor(
    public dialog: MatDialog,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    private usersService: UsersService,
    private virtuesService: VirtuesService
  ) {
    this.adUserCtrl = new FormControl();
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(data => {
      let awsServer = data[0].aws_server;
      this.setBaseUrl(awsServer);
      this.updateVirtueList([]);
      this.getApps();
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 500);
  }

  setBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  addUser( username: string, roleUser: boolean, roleAdmin: boolean ) {
    username = username.trim().replace(' ', '').toLowerCase();
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
      'virtueTemplateIds': this.userVirtueIDs
    };

    if (!body.username) {
      return;
    }

    this.usersService.createUser(this.baseUrl, JSON.stringify(body)).subscribe(
      data => {
        return true;
      },
      error => {
        console.error('Error');
      }
    );
    this.assignUserVirtues(username, this.userVirtueIDs);
    this.resetRouter();
    this.router.navigate(['/users']);
  }

  assignUserVirtues(username: string, virtues: any[]) {
    for ( let item of virtues ) {
      this.usersService.assignVirtues(this.baseUrl, username, item);
    }
  }

  getApps() {
    if (this.baseUrl !== null) {
      this.appsService.getAppsList(this.baseUrl).subscribe(data => {
        this.appsList = data;
      });
    }
  }

  updateVirtueList( newVirtIDs : any[] ) {
    // console.log("updateVirtList");
    // console.log(this.userVirtueIDs);
    // console.log(this.virtues);
    // console.log("[\\]");
    // loop through the selected VM list
    this.virtues = [];
    this.userVirtueIDs = newVirtIDs;
    const virtueVmIds = newVirtIDs;
    for (let vID of virtueVmIds) {
      this.virtuesService.getVirtue(this.baseUrl, vID).subscribe(
        data => {
          this.virtues.push(data);
        },
        error => {
          console.log(error.message);
        }
      );
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
      // console.log(virtueApps);
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
    // console.log(appNames);
    return appNames;
  }

  removeVirtue(id: string, index: number): void {
    this.virtues = this.virtues.filter(data => {
      return data.id !== id;
    });
    this.userVirtueIDs.splice(index, 1);
  }

  activateModal(id, mode): void {

    let dialogWidth = 900;
    let dialogHeight = 748;
    this.fullImagePath = './assets/images/app-icon-white.png';

    if (mode === 'add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    const dialogRef = this.dialog.open(VirtueModalComponent, {
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

    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
    const virtueList = dialogRef.componentInstance.addVirtues.subscribe((dialogVirtueIDS) => {
      this.updateVirtueList(dialogVirtueIDS);
    });
  }

}
