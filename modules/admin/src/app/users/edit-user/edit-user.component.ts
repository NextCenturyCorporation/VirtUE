import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpParams } from '@angular/common/http';

import { User } from '../../shared/models/user.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { MatDialog, MatDialogRef } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css'],
  providers: [ BaseUrlService, UsersService, VirtuesService ]
})

export class EditUserComponent implements OnInit {
  @Input() user: User;

  baseUrl: string;
  fullImagePath: string;
  submitBtn: string;
  userToEdit: {id: string};
  adUserCtrl: FormControl;

  userRoleUser = false;
  userRoleAdmin = false;
  userData = [];
  userVirtues = [];
  selUserVirtues = [];
  storedVirtues = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
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
    });
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
      this.router.navigate(['/users']);
    }, 500);
  }

  getBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  getUserToEdit(baseUrl: string, username: string) {
    this.usersService.getUser(baseUrl, username).subscribe(data => {
      this.userData = data;
      this.getUserAuth(data.authorities);
      this.getUserVirtues(baseUrl, data.virtueTemplateIds);
      this.displayUser(data);
    });
  }

  getUserAuth( auth: any[] ) {
    for (let role of auth) {
      if (role === 'ROLE_USER') {
        this.userRoleUser = true;
      }
      if (role === 'ROLE_ADMIN') {
        this.userRoleAdmin = true;
      }
    }
  }

  getUserVirtues(baseUrl: string, virtues: any[]) {
    if (this.storedVirtues.length > 0) {
      this.userVirtues = [];
    }

    for (let virtue of virtues) {
      this.virtuesService.getVirtue(baseUrl, virtue).subscribe(data => {
        let _virtue = {
          'id': data.id,
          'name': data.name,
          'enabled': data.enabled,
          'virtualMachineTemplateIds': data.virtualMachineTemplateIds,
          'applicationIds': data.applicationIds
        };
        this.userVirtues.push(_virtue);

      });
    }
    return this.userVirtues;
  }

  displayUser(userData: any) {
    this.userData = userData;
    this.storedVirtues = userData.virtueTemplateIds;
    console.log(this.storedVirtues);
  }

  updateUser(username: string, roleUser: string, roleAdmin: string) {
    username = username.trim().replace(' ', '').toLowerCase();
    let authorities = [];
    let virtueTemplateIds = [];

    if (roleUser) {
      authorities.push('ROLE_USER');
    }
    if (roleAdmin) {
      authorities.push('ROLE_ADMIN');
    }
    for (let item of this.storedVirtues) {
      virtueTemplateIds.push(item);
    }
    console.log('virtueTemplateIds: ' + this.storedVirtues)

    let body = {
      "username": username,
      "authorities": authorities,
      "virtueTemplateIds": this.storedVirtues
    };

    if (!body.username) { return; }

    this.usersService.updateUser(this.baseUrl, this.userToEdit.id, JSON.stringify(body)).subscribe(
      data => {
        return true;
      },
      error => {
        console.log('Error');
      });
    // this.assignUserVirtues(baseUrl, username, virtueTemplateIds)
    this.resetRouter();
    this.router.navigate(['/users']);
  }

  assignUserVirtues(baseUrl: string, username: string, virtues: any[]) {
    for ( let item of virtues ) {
      this.usersService.assignVirtues(baseUrl, username, item);
    }
  }

  removeVirtue(username: string, virtue: string) {
    console.log('revoking virtue: ' + virtue + ' for user ' + username);
    this.usersService.revokeVirtues(this.baseUrl, username, virtue);
  }

  activateModal(): void {
    this.submitBtn = 'Update List';

    let dialogRef = this.dialog.open(VirtueModalComponent, {
      width: '750px',
      data: {
        dialogButton: this.submitBtn,
        selUserVirtues: this.storedVirtues
      }
    });

    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const userVirtues = dialogRef.componentInstance.userVirtues.subscribe((data) => {
      this.selUserVirtues = data;
      if (this.selUserVirtues.length > 0) {
        this.storedVirtues = [];
      }
      this.storedVirtues = this.selUserVirtues;
      this.getUserVirtues(this.baseUrl, this.storedVirtues);
    });

    dialogRef.afterClosed().subscribe(() => {
      userVirtues.unsubscribe();
    });
  }
}
