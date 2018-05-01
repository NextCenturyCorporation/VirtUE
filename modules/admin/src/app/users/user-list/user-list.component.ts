import { Component, EventEmitter, Input, OnInit, OnDestroy } from '@angular/core';
import { Routes, RouterModule, Router } from '@angular/router';
import { Location } from '@angular/common';

import { User } from '../../shared/models/user.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
  providers: [ BaseUrlService, UsersService ]
})
export class UserListComponent implements OnInit {
  @Input() user: User;

  awsServer: string;
  saviorUsers: string;
  users = [];
  virtues = [];
  isInitialized = false;

  constructor(
    private router: Router,
    private location: Location,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe( _url => {
      let awsServer = _url[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getUsers(awsServer);
      this.getVirtues(awsServer);
    });
  }

  getBaseUrl( url: string ) {
    this.awsServer = url;
  }

  reloadPage() {
    setTimeout(() => {
      this.router.navigated = false;
      this.router.navigate([this.router.url]);
    }, 500);
  }

  getUsers( baseUrl: string ): void {
    this.usersService.getUsers(baseUrl).subscribe(data => {
      this.users = data;
    });
  }

  deleteUser(username: string) {
    // console.log(username);
    this.usersService.deleteUser(this.awsServer, username);
    this.reloadPage();
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl).subscribe( virtues => {
      this.virtues = virtues;
    });
  }

  getVirtueName(id: string) {
    for (let virtue of this.virtues) {
      if (id === virtue.id) {
        return virtue.name;
      }
    }
  }

  openDialog(id, type, text): void {

    const dialogRef = this.dialog.open( DialogsComponent, {
      width: '450px',
      data:  {
          dialogText: text,
          dialogType: type
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog to delete {{data.dialogText}} was closed');
    });
  }

}
