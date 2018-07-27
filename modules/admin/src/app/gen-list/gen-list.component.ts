import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

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
  //don't initialize these immediately.
  // note that this isn't very pretty, but we need lists in order to do sorting,
  // and we need to be able to link the virtue ids in each user with the virtue objects quickly
  //(and vms in virtues, and apps in vms), and so we probably want to use dicts to do that lookup.
  //There has to be a better way to do this
  // Maybe the dict class could be written to allow access like a list? it has to be sortable.
  // So
  allUsers: DictList<User>;
  allVirtues: DictList<Virtue>;
  allVms: DictList<VirtualMachine>;
  allApps: DictList<Application>;
  // allUsersD: Dict<User>;
  // allVirtuesD: Dict<Virtue>;
  // allVmsD: Dict<VirtualMachine>;
  // allAppsD: Dict<Application>;
  // allUsersL: User[];
  // allVirtuesL: Virtue[];
  // allVmsL: VirtualMachine[];
  // allAppsL: Application[];

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
    // this.allUsersD = {};
    // this.allUsersL = [];
    // this.allVirtuesD = {};
    // this.allVirtuesL = [];
    // this.allVmsD = {};
    // this.allVmsL = [];
    // this.allAppsD = {};
    // this.allAppsL = [];
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

  // virtual function, must be overriden
  // updateItems() {}

  // virtual function, must be overriden
  //this should update items too. ??
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
    }, 600);
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

  pullApplications() {
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

  pullVms() {
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
    });
  }

  pullVirtues() {
    // console.log("pulling virtues");
    this.virtuesService.getVirtues(this.baseUrl).subscribe( virtues => {
      this.allVirtues.clear()
      this.allVirtues = new DictList<Virtue>();
      for (let v of virtues) {
        // v.childIDs = v.vmIDs;
        v.status = v.enabled ? 'enabled' : 'disabled';
        this.allVirtues.add(v.id, v);
      }
      virtues = null;
    });
  }

  pullUsers(): void {
    // console.log("pulling users");
    this.usersService.getUsers(this.baseUrl).subscribe(users => {
    // console.log("**", users[0]);
      this.allUsers.clear() // not sure if this is needed
      this.allUsers = new DictList<User>();
      let user = null;
      for (let u of users) {
        user = new User(u);
        u.childIDs = u.virtueTemplateIds;
        user.formatChildrenList();
        this.allUsers.add(u.username, user);
      }
      user = null;
      users = null;
      // console.log(this.allUsers);
    });
  }

  //see if this works. If so, then do below.
  // pullUsers(): void {
  //   this.allUsers = this.usersService.getUsers(this.baseUrl).subscribe(userList => {
  //     for (let u of userList) {
  //       u['childIDs'] = u.
  //       u['status'] = u.enabled ? 'enabled' : 'disabled';
  //     }
  //     return userList;
  //   });
  //   // And just do this for everything?
  //   //this.allUsers = this.usersService.getUsers(this.baseUrl).subscribe();
  // }


  //These could be condensed into one function if childIDs were implemented
  // linkUsersToVirtues() {
  //   for (let u of this.allUsers) {
  //     u['virtues'] = Dict<Virtue>{};
  //     for (let v of this.allVirtues) {
  //       u.virtues[v.id] = v;
  //     }
  //   }
  // }

  //All parents and all children of all parents [like: linkItems(this.allUsers, this.allVirtues, new Dict)  ]
  // Can type this once
  linkItems(parents: DictList<Item>, allChildren: DictList<Item>) {
    for (let p of parents.getL()) {
      // console.log(p, "CHECK HERE THAT the value is an Item, not a number/string/index");
      p.children = new DictList<Item>();
      // p['children']: Dict = {};
      for (let cID of p.childIDs) {
        p.children.add(cID, allChildren.get(cID));
      }
    }
  }

    // for (let p of parents) {
    //   p.children = newChildDict;
    //   // p['children']: Dict = {};
    //   for (let cID of p.childIDs) {
    //     p.children[cID] = allChildren[cID];
    //   }
    // }

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
