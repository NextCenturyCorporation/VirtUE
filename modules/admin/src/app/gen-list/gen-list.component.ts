import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { DialogsComponent } from '../dialogs/dialogs.component';

import { Column } from '../shared/models/column.model';
import { DictList, Dict } from '../shared/models/dictionary.model';

import { User } from '../shared/models/user.model';
import { Virtue } from '../shared/models/virtue.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { Application } from '../shared/models/application.model';
import { Item } from '../shared/models/item.model';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { UsersService } from '../shared/services/users.service';
import { VirtuesService } from '../shared/services/virtues.service';
import { ItemService } from '../shared/services/item.service';
import { VirtualMachineService } from '../shared/services/vm.service';
import { ApplicationsService } from '../shared/services/applications.service';

@Component({
  selector: 'app-gen-list',
  templateUrl: './gen-list.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ]
})

export class GeneralListComponent implements OnInit {

  prettyTitle: string;
  itemName: string;

  items = [];

  colData: Column[];

  domain: string; // like /users, /virtues, etc.

  //will cause error if not instantialized before page loads; the pull functions
  //don't initialize these immediately
  allUsers: DictList<User>;
  allVirtues: DictList<Virtue>;
  allVms: DictList<VirtualMachine>;
  allApps: DictList<Application>;

  noDataMessage: string;

  // allParents: Dict<Item>;
  // allChildren: Dict<Item>;

  baseUrl: string;
  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';

  os: Observable<Array<VirtuesService>>;


  constructor(
    protected router: Router,
    protected baseUrlService: BaseUrlService,
    protected usersService: UsersService,
    protected virtuesService: VirtuesService,
    protected vmService: VirtualMachineService,
    protected appsService: ApplicationsService,
    public dialog: MatDialog
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

    //see comment by declaration
    this.allUsers = new DictList<User>();
    this.allVirtues = new DictList<Virtue>();
    this.allVms = new DictList<VirtualMachine>();
    this.allApps = new DictList<Application>();
  }

  ngOnInit() {
    this.sortColumn = this.colData[0];

    this.baseUrlService.getBaseUrl().subscribe( res => {
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);

      this.pullData();
    });

    this.refreshData();
    this.resetRouter();
  }


  //must be overriden
  pullData() {}


  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req);
  }

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.pullData();
    }, 300);
  }

  filterList(filterValue: string) {
    this.filterValue = filterValue;
  }

  setColumnSortDirection(sortColumn: Column, sortDirection: string) {
    if (this.sortColumn === sortColumn) {
      this.reverseSorting();
    } else {
      this.sortColumn = sortColumn;
    }
  }

  reverseSorting() {
    if (this.sortDirection === 'asc') {
      this.sortDirection = 'desc';
    } else {
      this.sortDirection = 'asc';
    }
  }

  getChildrenListHTMLstring(item: Item): string {
    return item.childrenListHTMLstring;
  }

  pullApplications(isChildType: boolean) {
    // console.log("pulling apps");
    this.appsService.getAppsList(this.baseUrl).subscribe( apps => {
      this.allApps.clear()
      this.allApps = new DictList<Application>();
      for (let a of apps) {
        this.allApps.add(a.id, a);
      }
      apps = null;
    });
  }

  pullVms(isChildType: boolean) {
    // console.log("pulling vms");
    this.vmService.getVmList(this.baseUrl).subscribe( vms => {
      this.allVms.clear()
      this.allVms = new DictList<VirtualMachine>();
      for (let v of vms) {
        // v['childIDs'] = v.applicationIds;
        v.status = v.enabled ? 'enabled' : 'disabled';
        this.allVms.add(v.id, v);
      }
      vms = null;
    },
    error => {},
    () => {
      if (!isChildType) {
        this.items = this.allVms.getL();
        for (let v of this.allVms.getL()) {
          v.formatChildrenList(this.allApps);
        }
      }
    });
  }

  pullVirtues(isChildType: boolean) {

    // console.log("pulling virtues");
    this.virtuesService.getVirtues(this.baseUrl).subscribe( virtues => {
      this.allVirtues.clear() // not sure if this is needed
      this.allVirtues = new DictList<Virtue>();
      let virt = null;
      for (let v of virtues) {
        virt = new Virtue(v);
        // virt.childIDs = v.virtualMachineTemplateIds;
        // virt.formatChildrenList(this.allVms);
        this.allVirtues.add(v.id, virt);
      }
      virt = null;
      virtues = null;
    },
    error => {},
    () => {
      if (!isChildType) {
        this.items = this.allVirtues.getL();
        for (let v of this.allVirtues.getL()) {
          v.formatChildrenList(this.allVms);
        }
      }
    });
  }

  //note isChildType is never used, bc Users can't be something else' child, but
  // hopefully these will all be refactored into one function later.
  pullUsers(isChildType: boolean) {
    this.usersService.getUsers(this.baseUrl).subscribe(users => {
      // console.log("start users", this.allVirtues.length);
    // console.log("**", users[0]);
      this.allUsers.clear(); // not sure if this is needed
      this.allUsers = new DictList<User>();
      let user = null;
      for (let u of users) {
        user = new User(u);
        this.allUsers.add(user.getName(), user);
      }
      user = null;
      users = null;
      // console.log(this.allUsers);
    },
    error => {},
    () => {
      if (!isChildType) {
        console.log(this.allVirtues.length);
        this.items = this.allUsers.getL();
        for (let u of this.allUsers.getL()) {
          u.formatChildrenList(this.allVirtues);
        }
      }
    });
    // console.log("pulling users");
    // this.usersService.getUsers(this.baseUrl).subscribe(userDict => {
    //     console.log(userDict);
    //     this.allUsers = userDict;
    //     this.items = this.allUsers.getL();
    //     for (let u of this.allUsers.getL()) {
    //       u.formatChildrenList(this.allVirtues);
    //     }
    //     console.log("back here", this.allVirtues.length, this.allUsers.length);
    // });
    //
    // let usersDict = new DictList<User>();
    // this.pullUserData(baseUrl).subscribe(users => {
    //   console.log(users.length);
    //   let user: User = null;
    //   for (let u of users) {
    //     user = new User(u);
    //     usersDict.add(user.getID(), user);
    //   }
    //   user = null;
    //   users = null;
    //   // console.log(usersDict);
    //   let temp = Observable.of(usersDict);
    //   console.log("leaving second", usersDict.length, temp);
    //   return temp;
    // },
    // error => {},
    // () => {});
    //
    //
    //
    // console.log("end2", this.allVirtues.length, this.allUsers.length);
  }
  // this.usersService.getUsers(this.baseUrl).subscribe(users => {
  //   // console.log("start users", this.allVirtues.length);
  // // console.log("**", users[0]);
  //   this.allUsers.clear() // not sure if this is needed
  //   this.allUsers = new DictList<User>();
  //   let user = null;
  //   for (let u of users) {
  //     user = new User(u);
  //     this.allUsers.add(u.username, user);
  //   }
  //   user = null;
  //   users = null;
  //   // console.log(this.allUsers);
  // },
  // error => {},
  // () => {
  //   this.items = this.allUsers.getL();
  //   for (let u of this.allUsers.getL()) {
  //     u.formatChildrenList(this.allVirtues);
  //   }
  // }
  // );

  getName(dl: DictList<Item>, id: string) {
    return dl.get(id).getName();
  }

  getAppName(id: string): string {
    return this.allApps.get(id).name;
  }

  getVmName(id: string): string {
    return this.allVms.get(id).name;
  }

  getVirtueName(id: string): string {
    return this.allVirtues.get(id).name;
  }

  // deleteVirtue(id: string) {
  //   // console.log('deleting ' + id);
  //   this.virtuesService.deleteVirtue(this.baseUrl, id);
  //   this.refreshData();
  // }

  //overridden
  // toggleItemStatus(item: Item) {
  // }

  openDialog(verb: string, directObject: string): void {
    const dialogRef = this.dialog.open( DialogsComponent, {
      width: '450px',
      data:  {
          dialogType: verb,
          dialogDescription: directObject
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    // console.log(dialogRef);
    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog to ' + verb + ' ' + directObject + ' was closed');
      console.log(result);
      console.log(dialogRef);
    });

  }
}
