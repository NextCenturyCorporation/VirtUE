import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Location } from '@angular/common';

// import { User } from '../../shared/models/user.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
  providers: [ BaseUrlService, UsersService ]
})
export class UserListComponent implements OnInit {
  baseUrl: string;
  users = [];
  virtues = [];

  sortColumn: string = 'username';
  sortType: string = 'enabled';
  sortValue: any = '*';
  sortBy: string = 'asc';
  totalUsers: number = 0;

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
    this.resetRouter();
  }

  getBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.getUsers(this.baseUrl);
    }, 1000);
  }

  getUsers( baseUrl: string ): void {
    this.usersService.getUsers(baseUrl).subscribe(userList => {
      this.users = userList;
      this.totalUsers = userList.length;
      for (var user of this.users) {
        user.status = user.enabled ? 'enabled' : 'disabled';
      }
    });
  }

  setUserStatus(username: string, newStatus: string) {
    // console.log("here");
    if (username.toUpperCase() === "ADMIN") {
      //// TODO: Remove this message when this no longer happens. When we stop funneling all requests through admin.
      // this.openDialog('disable', username + " and lock everyone out of the system?");
      if (!confirm("Are you sure to disable "+ username + " and lock everyone out of the system, including you?")) {
        console.log("Leaving " + username + " intact.");
        return;
      }
      console.log("Request to disable " + username + " ignored.");
      //we're not disabling that
      return;
    }
    // this.usersService.setUserStatus(this.baseUrl, username, newStatus).subscribe(userList => {
    //   this.users = userList;
    // });
    // this.refreshData();
  }

  deleteUser(username: string) {
    // console.log(username);
    this.usersService.deleteUser(this.baseUrl, username);
    this.refreshData();
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

  //The message will be ""
  openDialog(verb: string, directObject: string): void {
    const dialogRef = this.dialog.open( DialogsComponent, {
      width: '450px',
      data:  {
          dialogType: verb,
          dialogDescription: directObject
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog to ' + verb + ' ' + directObject + ' was closed');
    });
  }

  enabledUserList(sortType: string, enabledValue: any, sortBy) {
    // console.log('enabledUserList() => ' + enabledValue);
    if (this.sortValue !== enabledValue) {
      this.sortBy = 'asc';
    } else {
      this.reverseSorting(sortBy);
    }
    this.sortValue = enabledValue;
    this.sortType = sortType;
  }

  setColumnSortDirection(sortColumn: string, sortBy: string) {
    if (this.sortColumn === sortColumn) {
      this.reverseSorting(sortBy);
    } else {
      if (sortColumn === 'username') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'authorities') {
        this.sortBy = 'asc';
        this.sortColumn = sortColumn;
      } else if (sortColumn === 'status') {
        this.sortBy = 'desc';
        this.sortColumn = sortColumn;
      }
    }
  }

  reverseSorting(sortDirection: string) {
    if (sortDirection === 'asc') {
      this.sortBy = 'desc';
    } else {
      this.sortBy = 'asc';
    }
  }


}
