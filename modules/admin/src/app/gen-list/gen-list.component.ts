import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { DatePipe } from '@angular/common';
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
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService, DatePipe ]
})

export class GeneralListComponent implements OnInit {

  private datePipe: DatePipe;

  prettyTitle: string;
  itemName: string;
  pluralItem: string;

  items: Item[];

  colData: Column[];

  domain: string; // like /users, /virtues, etc.

  //will cause error if not instantialized before page loads; the pull functions
  //don't initialize these immediately
  allUsers: DictList<User>;
  allVirtues: DictList<Virtue>;
  allVms: DictList<VirtualMachine>;
  allApps: DictList<Application>;

  noDataMessage: string;


  /**
  This holds functions, to be called in turn to load/reload data. When each
    function finishes, it calls the next one in the supplied list (updateQueue).
  Must be set in derived classes.
  Declaration not fully specific, because it must hold function signatures
    which take as a parameter an array of this type.
  */
  updateFuncQueue: ((scope, updateQueue: any[], completedUpdates: any[])=> void)[];

  //Ideally the four pulling functions below could combined into one, where the
  //parents and children are generically defined. Virtues makes this tricky
  //though, since they need vms and apps, in addition to virtues. And it doesn't
  //seem efficient to build the appsListHTML on the fly. I believe that's the
  //only trade-off though.
  // allParents: Dict<Item>;
  // allChildren: Dict<Item>;

  baseUrl: string;

  // these are the default properties the list sorts by
  sortColumn: Column;
  filterValue: string = '*';
  sortDirection: string = 'asc';


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

    this.datePipe = new DatePipe('en-US');

    this.updateFuncQueue = [];
    this.items = [];
  }

  ngOnInit() {
    this.sortColumn = this.colData[0];
    this.baseUrlService.getBaseUrl().subscribe( res => {
      let awsServer = res[0].aws_server;
      this.setBaseUrl(awsServer);

      this.pullData();
    });

    //do we need this?
    // this.refreshData();
    this.resetRouter();
  }

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

  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  getChildrenListHTMLstring(item: Item): string {
    return item.childNamesHtml;
  }

  // Can't use this until I find a way to access the {x}-list component from
  // within a format function. Passing in the scope to all format functions
  // seems like a hack, and it'd only be needed for this one function.
  // formatDate( item: Item): string {
  //     return scope.datePipe.transform(item.lastModification, 'short');
  // }

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

  pullData(): void {
    // Pass in 'this' as scope, because 'this' within the called functions
    // refers to the updateQueue list.
    let updateQueue = Object.assign([], this.updateFuncQueue);
    let completedUpdates = [];
    updateQueue[0](this, updateQueue, completedUpdates);
  }

  //updateQueue is a list of function signatures, where each
  pullApps(scope, updateQueue: any[], completedUpdates: any[]): void {
    console.log("pulling apps");
    scope.appsService.getAppsList(scope.baseUrl).subscribe( apps => {
      scope.allApps.clear();
      scope.allApps = new DictList<Application>();
      let app = null
      for (let a of apps) {
        app = new Application(a);
        scope.allApps.add(a.id, app);
      }
      apps = null;
    },
      error => {},
      () => {
        completedUpdates.push(updateQueue[0]);
        updateQueue.shift();
        // console.log(updateQueue, scope.allApps);
        if (updateQueue.length !== 0) {
          updateQueue[0](scope, updateQueue, completedUpdates);
        }
        else {
          scope.items = scope.allApps.getL();
        }
      });
  }

  pullVms(scope, updateQueue: any[], completedUpdates: any[]): void {
    console.log("pulling vms");
    scope.vmService.getVmList(scope.baseUrl).subscribe( vms => {
      scope.allVms.clear();
      scope.allVms = new DictList<VirtualMachine>();
      let vm = null;
      for (let v of vms) {
        v['modDate'] = scope.datePipe.transform(v.lastModification, 'short');
        vm = new VirtualMachine(v);
        scope.allVms.add(vm.id, vm);
        //if pullApps has completed and so allApps is populated
        if (completedUpdates.some(x=> x===scope.pullApps)) {
          vm.formatChildNames(scope.allApps);
        }
      }
      vms = null;
    },
    error => {},
    () => {
      completedUpdates.push(updateQueue[0]);
      updateQueue.shift();
      // console.log(updateQueue, scope.allVms);
      if (updateQueue.length !== 0) {
        updateQueue[0](scope, updateQueue, completedUpdates);
      }
      else {
        scope.items = scope.allVms.getL();
      }
    });
  }

  pullVirtues(scope, updateQueue: any[], completedUpdates: any[]): void {
    console.log("pulling Virtues");
    scope.virtuesService.getVirtues(scope.baseUrl).subscribe( virtues => {
      scope.allVirtues.clear(); // not sure if this is needed
      scope.allVirtues = new DictList<Virtue>();
      let virt = null;
      for (let v of virtues) {
        v['modDate'] = scope.datePipe.transform(v.lastModification, 'short');
        virt = new Virtue(v);
        scope.allVirtues.add(v.id, virt);
        //if pullVms has completed and so allVms has been populated
        //just checking if allVms isn't empty wouldn't guarentee it's up-to-date
        if (completedUpdates.some(x=> x===scope.pullVms)) {
          virt.formatChildNames(scope.allVms);

          if (completedUpdates.some(x=> x===scope.pullApps)) {
            virt.formatAppListHtml(scope.allVms, scope.allApps);
          }
        }
      }
      virt = null;
      virtues = null;
    },
    error => {},
    () => {
      completedUpdates.push(updateQueue[0]);
      updateQueue.shift();
      // console.log(updateQueue, scope.allVirtues);
      if (updateQueue.length !== 0) {
        updateQueue[0](scope, updateQueue, completedUpdates);
      }
      else {
        scope.items = scope.allVirtues.getL();
      }
    });
  }

  // hopefully these will all be refactored into one function later.
  pullUsers(scope, updateQueue: any[], completedUpdates: any[]): void {
    console.log("pulling users");
    scope.usersService.getUsers(scope.baseUrl).subscribe(users => {
      scope.allUsers.clear(); // not sure if this is needed
      scope.allUsers = new DictList<User>();
      let user = null;
      for (let u of users) {
        user = new User(u);
        scope.allUsers.add(user.getName(), user);
        //if pullVirtues has completed and so allVirtues is populated
        if (completedUpdates.some(x=> x===scope.pullVirtues)) {
          user.formatChildNames(scope.allVirtues);
        }
      }
      user = null;
      users = null;
    },
    error => {},
    () => {
      completedUpdates.push(updateQueue[0]);
      updateQueue.shift();
      //currently, pullUser is only going to need to be the last one pulled, so
      //the following lines won't be needed/used unless something changes and
      //something gets put in the queue after pullUser
      // console.log(updateQueue, scope.allUsers);
      if (updateQueue.length !== 0) {
        updateQueue[0](scope, updateQueue, completedUpdates);
      }
      else {
        scope.items = scope.allUsers.getL();
      }
    });
  }

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

  // openDialog(id: string, type: string, category: string, description: string): void {
  openDialog(action: string, target: Item): void {

    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          actionType: action,
          targetObject: target
          // targetId: target.getID(),
          // targetName: target.getName()
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    //control goes here after either "Ok" or "Cancel" are clicked on the dialog
    const dialogResults = dialogRef.componentInstance.dialogEmitter.subscribe((targetObject) => {

      if (targetObject !== 0 ) {
        // console.log('Dialog Emitter: ' + targetObject.getID());
        if ( action === 'delete') {
          this.deleteItem(targetObject);
        }
        if (action === 'disable') {
          this.disableItem(targetObject);
        }
      }
    });
  }


  deleteItem(i: Item) {}

  disableItem(i: Item) {}
}
