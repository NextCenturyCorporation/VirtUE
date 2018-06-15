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
  selVirtues = [];
  storedVirtues = [];
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
      let url = data[0].aws_server;
      this.getBaseUrl(url);
      this.getVirtues(url);
      this.getApps(url);
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 500);
  }

  getBaseUrl( url: string ) {
    this.awsServer = url;
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
    for (let item of this.selVirtues) {
      virtueTemplateIds.push(item);
    }

    let body = {
      'username': username,
      'authorities': authorities,
      'virtueTemplateIds': virtueTemplateIds
    };

    if (!body.username) { return; }

    let baseUrl = this.awsServer;
    this.usersService.createUser(baseUrl, JSON.stringify(body)).subscribe(
      data => {
        return true;
      },
      error => {
        console.error('Error');
      }
    );
    this.assignUserVirtues(baseUrl, username, virtueTemplateIds);
    this.resetRouter();
    this.router.navigate(['/users']);
  }

  assignUserVirtues(baseUrl: string, username: string, virtues: any[]) {
    for ( let item of virtues ) {
      this.usersService.assignVirtues(baseUrl, username, item);
    }
  }

  getVirtues(baseUrl: string) {
    let selectedVirtue = this.storedVirtues;
    this.virtuesService.getVirtues(baseUrl)
    .subscribe(data => {
      if (this.selVirtues.length < 1) {
        for (let virtue of data) {
          for (let sel of selectedVirtue) {
            if (sel === virtue.id) {
              this.selVirtues = data;
              break;
            }
          }
        }
      } else {
        this.getUpdatedVirtueList(baseUrl);
      }
    });
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
  getUpdatedVirtueList(baseUrl: string) {
    this.virtues = [];
    this.virtuesService.getVirtues(baseUrl)
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
    this.storedVirtues.splice(index, 1);
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
        storedVirtues: this.storedVirtues
      },
      panelClass: 'virtue-modal-overlay'
    });

    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
    const virtueList = dialogRef.componentInstance.addVirtues.subscribe((data) => {
      this.selVirtues = data;
      console.log('Received selected virtues: ');
      console.log(this.selVirtues);

      if (this.storedVirtues.length > 0) {
        this.storedVirtues = [];
      }
      this.storedVirtues = this.selVirtues;

      this.getVirtues(this.awsServer);
    });
  }

}
