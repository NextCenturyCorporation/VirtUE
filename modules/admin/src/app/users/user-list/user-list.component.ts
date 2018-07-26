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
    }, 200);
  }

  getUsers( baseUrl: string ): void {
    this.usersService.getUsers(baseUrl).subscribe(userList => {
      this.users = userList;
      this.totalUsers = userList.length;
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
    //for some reason, I need to subscribe in order for the toggle
    //to work, even if I don't do anything with the stuff I'm subscribed to.
    // That data certainly isn't supposed to go there.
    this.usersService.setUserStatus(this.baseUrl, username, newStatus).subscribe();//data => {
    //   this.users = data;
    //   // console.log(data);
    // });

    this.refreshData();
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

  openDialog(verb: string, directObject: string): void {
    const dialogRef = this.dialog.open( DialogsComponent, {
      width: '450px',
      data:  {
          dialogType: verb,
          dialogDescription: directObject
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    console.log(dialogRef);
    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog to ' + verb + ' ' + directObject + ' was closed');
      console.log(result);
      console.log(dialogRef);
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
      } else if (sortColumn === 'enabled') {
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
