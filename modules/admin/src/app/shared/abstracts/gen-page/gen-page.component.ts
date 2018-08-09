import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApplicationsService } from '../../services/applications.service';
import { BaseUrlService } from '../../services/baseUrl.service';
import { VirtuesService } from '../../services/virtues.service';
import { VirtualMachineService } from '../../services/vm.service';
import { UsersService } from '../../services/users.service';

import { Column } from '../../models/column.model';
import { DictList, Dict } from '../../models/dictionary.model';

import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Item } from '../../models/item.model';

@Component({
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService, UsersService ]
})
export abstract class GenericPageComponent {

  baseUrl: string;

  //will cause error if not instantialized before page loads; the pull functions
  //don't initialize these immediately
  allUsers: DictList<User>;
  allVirtues: DictList<Virtue>;
  allVms: DictList<VirtualMachine>;
  allApps: DictList<Application>;

  /**
  This holds functions, to be called in turn to load/reload data. When each
    function finishes, it calls the next one in the supplied list (updateQueue).
  Must be set in derived classes.
  Declaration not fully specific, because it holds function signatures
    which take as a parameter an array of this type. So the type is recursive.
    Sorry.
  */
  updateFuncQueue: ((scope, updateQueue: any[], completedUpdates: any[])=> void)[];

  constructor(
    protected router: Router,
    protected baseUrlService: BaseUrlService,
    protected usersService: UsersService,
    protected virtuesService: VirtuesService,
    protected vmService: VirtualMachineService,
    protected appsService: ApplicationsService,
    protected dialog: MatDialog
  ) {
    //see comment by declaration
    this.allUsers = new DictList<User>();
    this.allVirtues = new DictList<Virtue>();
    this.allVms = new DictList<VirtualMachine>();
    this.allApps = new DictList<Application>();

    //TODO what is this?
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  abstract pullData();

  resetRouter(): void {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData(): void {
    setTimeout(() => {
      this.pullData();
    }, 300);
  }

  //returns true if s is a subset of l
  isSubset(s:any[], l:any[]): boolean {
    for (let e of s) {
      console.log(e);
      if( ! l.some(x=> x===e) ) {
        return false;
      }
    }
    return true;
  }

  /*
  We need to call a series of functions in order, where each function can
  only start once the service the previous one started has come back; that is,
  once the previous function has completely finished setting up the data it
  is told to.
  UpdateQueue holds a list of the functions to be called, in order. Once each
  finishes processing their data, they add their name to the "completedUpdates"
  list, remove their name from updateQueue, and then call the next function in
  updateQueue. Once updateQueue is empty, the chain returns. Note that the chain
  of functions are within a spawned sub-process, and so control in the rest of
  the program doesn't wait on them. Once they finish, the page automatically
  updates to show the newly-acquired data.


  */
  pullDatasets(): void {

    let updateQueue = Object.assign([], this.updateFuncQueue);
    let completedUpdates = [];

    //Every page should need to request some sort of data.
    if (updateQueue.length === 0 || !(updateQueue[0] instanceof Function)) {
      console.log("No datasets requested in updateQueue.");
      return;
    }

    // Pass in 'this' as scope, because 'this' within the called functions
    // refers to the updateQueue list.
    updateQueue[0](this, updateQueue, completedUpdates);
  }

  //Ideally the four below functions should be merged into one.
  pullApps(scope, updateQueue: any[], completedUpdates: any[]): void {
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
        if (updateQueue.length !== 0) {
          updateQueue[0](scope, updateQueue, completedUpdates);
        }
        else {
          scope.items = scope.allApps.asList();
        }
      });
  }

  pullVms(scope, updateQueue: any[], completedUpdates: any[]): void {
    scope.vmService.getVmList(scope.baseUrl).subscribe( vms => {
      scope.allVms.clear();
      scope.allVms = new DictList<VirtualMachine>();
      let vm = null;
      for (let v of vms) {
        vm = new VirtualMachine(v);
        scope.allVms.add(vm.id, vm);
        //if pullApps has completed, and so allApps is populated
        //will always happen under current config, but may not if vms are ever
        //pulled without apps being pulled first.
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
      if (updateQueue.length !== 0) {
        updateQueue[0](scope, updateQueue, completedUpdates);
      }
      else {
        scope.items = scope.allVms.asList();
      }
    });
  }

  pullVirtues(scope, updateQueue: any[], completedUpdates: any[]): void {
    scope.virtuesService.getVirtues(scope.baseUrl).subscribe( virtues => {
      scope.allVirtues.clear(); // not sure if this is needed
      scope.allVirtues = new DictList<Virtue>();
      let virt = null;
      for (let v of virtues) {
        virt = new Virtue(v);
        scope.allVirtues.add(v.id, virt);
        //if pullVms has completed and so allVms has been populated
        //just checking if allVms isn't empty wouldn't guarentee it's up-to-date
        if (completedUpdates.some(x=> x===scope.pullVms)) {
          virt.formatChildNames(scope.allVms);

          //generate the html for the apps available for each virtue, used on the
          //virtue list page.
          if (completedUpdates.some(x=> x===scope.pullApps)) {
            virt.generateAppListHtml(scope.allVms, scope.allApps);
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
      if (updateQueue.length !== 0) {
        updateQueue[0](scope, updateQueue, completedUpdates);
      }
      else {
        scope.items = scope.allVirtues.asList();
      }
    });
  }

  // hopefully these will all be refactored into one function later.
  pullUsers(scope, updateQueue: any[], completedUpdates: any[]): void {
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
        scope.items = scope.allUsers.asList();
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

  setBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getChildrenListHTMLstring(item: Item): string {
    return item.childNamesHtml;
  }

  //I don't know what this does, but it was in almost every file before the refactor.
  //Doesn't seem to break anything when removed though
  // intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  //   return next.handle(req);
  // }
}
