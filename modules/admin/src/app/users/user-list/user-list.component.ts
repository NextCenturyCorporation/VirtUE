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
    this.refreshData();
  }

  getBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  refreshData() {
    setTimeout(() => {
      this.router.navigated = false;
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
      // this.sortUsers(userList); apparently an initial sort isn't needed?
      // the called function is commented out below as well.
    });
  }

  userStatus(username: string, newStatus: string) {
    console.log("here");
    
    this.usersService.setUserStatus(this.baseUrl, username, newStatus).subscribe(userList => {
      this.users = userList;
    });
    this.refreshData();
    this.router.navigate(['/users']);
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


  // Appears to be unnecessary. See comment in getUsers()
  // sortUsers(sortDirection: string) {
  //   console.log("sortUsers");
  //   console.log("***", this.users[0]["username"]);
  //   if (sortDirection === 'asc') {
  //     this.users.sort((leftSide, rightSide): number => {
  //       console.log("asc");
  //       console.log(JSON.stringify(leftSide));
  //       if (leftSide['username'] < rightSide['username']) {
  //         return -1;
  //       }
  //       if (leftSide['username'] > rightSide['username']) {
  //         return 1;
  //       }
  //       return 0;
  //     });
  //   } else {
  //     this.users.sort((leftSide, rightSide): number => {
  //       console.log("dsc");
  //       console.log(JSON.stringify(leftSide));
  //       if (leftSide['username'] < rightSide['username']) {
  //         return 1;
  //       }
  //       if (leftSide['username'] > rightSide['username']) {
  //         return -1;
  //       }
  //       return 0;
  //     });
  //   }
  // }

    enabledUserList(sortType: string, enabledValue: any, sortBy) {
      console.log('enabledUserList() => ' + enabledValue);
      if (this.sortValue !== enabledValue) {
        this.sortBy = 'asc';
      } else {
        this.sortListBy(sortBy);
      }
      this.sortValue = enabledValue;
      this.sortType = sortType;
    }

    sortUserColumns(sortColumn: string, sortBy: string) {
      if (this.sortColumn === sortColumn) {
        this.sortListBy(sortBy);
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

    sortListBy(sortDirection: string) {
      if (sortDirection === 'asc') {
        this.sortBy = 'desc';
      } else {
        this.sortBy = 'asc';
      }
    }


}
