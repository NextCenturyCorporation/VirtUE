import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
//
import { BaseUrlService } from '../../services/baseUrl.service';
// import { ApplicationsService } from '../../services/applications.service';
// import { VirtuesService } from '../../services/virtues.service';
// import { VirtualMachineService } from '../../services/vm.service';
// import { UsersService } from '../../services/users.service';
import { ItemService } from '../../services/item.service';

import { Column } from '../../models/column.model';
import { DictList, Dict } from '../../models/dictionary.model';

import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Item } from '../../models/item.model';

import { ConfigUrlEnum } from '../../enums/enums';

//TODO
type datasetType = {serviceUrl: string, class: any, name: string, depends: string};

@Component({
providers: [ BaseUrlService, ItemService  ]
// providers: [ BaseUrlService, UsersService, VirtuesService, VirtualMachineService, ApplicationsService  ]
})
export abstract class GenericPageComponent {

  //tells the service and backend where to make changes - specific to each type
  //of item. Must be set in construcotr of derived class.
  serviceConfigUrl: ConfigUrlEnum;

  //will cause error if not instantialized before page loads; the pull functions
  //don't initialize these immediately.
  allUsers: DictList<User>;
  allVirtues: DictList<Virtue>;
  allVms: DictList<VirtualMachine>;
  allApps: DictList<Application>;

  //holds the names and data types of each of the four datasets.
  datasetMeta: Dict<datasetType>;

  //string, holds the data types whose datasets must be loaded on page load or
  //refresh. Must be in order, from lowest child to highest parent - like ["apps, vms", "virtues"]
  //The last one is
  neededDatasets: string[];

  service: ItemService;

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
    protected itemService: ItemService,
    // protected usersService: UsersService,
    // protected virtuesService: VirtuesService,
    // protected vmService: VirtualMachineService,
    // protected appsService: ApplicationsService,
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

    this.buildDatasetMeta();

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

  buildDatasetMeta() {
    //{()=>Observable<any>;}
    this.datasetMeta = new Dict<datasetType>();
    this.datasetMeta['apps'] = {
          serviceUrl: ConfigUrlEnum.APPS,
          class: Application,
          name: 'allApps',
          depends: undefined
        };
    this.datasetMeta['vms'] = {
          serviceUrl: ConfigUrlEnum.VMS,
          class: VirtualMachine,
          name: 'allVms',
          depends: 'allApps'
        };
    this.datasetMeta['virtues'] = {
          serviceUrl: ConfigUrlEnum.VIRTUES,
          class: Virtue,
          name: 'allVirtues',
          depends: 'allVms'
        };
    this.datasetMeta['users'] = {
          serviceUrl: ConfigUrlEnum.USERS,
          class: User,
          name: 'allUsers',
          depends: 'allVirtues'
        };
  }

  /*
  TODO check this given below update
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


  Ideally now, there should be a central collection that defines what the values
  should be for all the inputs, so you can just say "I want apps, vms, and virtues",
  and not have to type in what they each depend on.
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

  /*
   * Calls
   *
   * onComplete is an optional function, which would be called after all requests
   * in the updateQueue have completed.
  */
  pullDatasets2(onComplete?: {(any): void}): void {
    //neededDatasets must hold the names (as strings) of the datasets to be loaded:
    //i.e. ['allApps', 'allVms']
    //Must be in the order in which they should be loaded.
    let updateQueue: any[] = [];

    for (let dName of this.neededDatasets) {
      if ( !(dName in this.datasetMeta)) {
        //throw error TODO
      }
      updateQueue.push(this.datasetMeta[dName]);
    }

    //TODO check that updateQueue isn't empty here

    this.recursivePullData(this, updateQueue, [], onComplete);
  }

  /**
  TODO
  So what'll be passed in is a list of service functions to call, a list of
  types to save the results from each call as, a list of the datasets that need
  to be populated, and an empty list to hold the names of the datasets that have
  been successfully pulled.
  We need to pass in scope, because 'this' within the callback doesn't refer to
  the GenericPageComponent, but to //TODO
  ******************************************************************************
  i.e., informally:
    pass in:
      scope: this
      updateQueue:[
                      {
                        serviceUrl: appsService.getAppsList(...),
                        class: Application,
                        name: 'allApps',
                        depends: undefined
                      }, {
                        service: vmService.getVmList(...),
                        class: VirtualMachine,
                        name: 'allVms',
                        depends: 'allApps'
                      }, {
                        service: virtuesService.getVirtueList(...),
                        class: Virtue,
                        name: 'allVirtues',
                        depends: 'allVms'
                      }
                  ],
      pulledDatasets: [],
      onComplete(): {this.pullItemData(this.item.id)}

    at end:
      {
        updateQueue = []
        pulledDatasets  = ['allApps', 'allVms', 'allVirtues']
      }

  ******************************************************************************
  So with the above example input, pullData would first call getAppsList

  ******************************************************************************
  this isn't the prettiest function, but it eliminates code-reuse and ensures
  things are called in the correct order.
  Everything loads from one function, into an object type that can be searched
  in constant time, but can also be sorted or iterated over.

  ******************************************************************************
  */
  recursivePullData(
    scope,
    updateQueue: datasetType[],
    pulledDatasetNames: string[],
    onComplete: {(GenericPageComponent, string[]): void} //see call below
  ): void {
    scope.itemService.getItems(updateQueue[0].serviceUrl).subscribe( rawDataList => {
      scope[updateQueue[0].name].clear(); // not sure if this is needed
      scope[updateQueue[0].name] = new DictList<Item>();//does it not need '(updateQueue[0].class)'?
      let item = null;
      for (let e of rawDataList) {
        item = new (updateQueue[0].class)(e);
        scope[updateQueue[0].name].add(item.id, item);

        // If the call to build the collection for this item's child-type has
        //   been recorded as completed, build this item's 'children' list.
        // Could just check if the Child-type's collection (allVms, allApps, etc)
        //   isn't empty, but that doesn't guarentee it's up-to-date.
        if (pulledDatasetNames.some(x=> x===updateQueue[0].depends)) {
          item.buildChildren(scope[updateQueue[0].depends]);
        }

      }
      item = null;
      rawDataList = null;
    },
    error => { //TODO },
    () => { //once the dataset has been pulled and fully processed above

      //mark this set as pulled
      pulledDatasetNames.push(updateQueue[0].name);

      //remove first element
      updateQueue.shift();

      //TODO check if scope is different from 'this'
      //I think 'this' will refer to 'window' actually

      if (updateQueue.length !== 0) {
        //if there are more datasets to pull
        scope.recursivePullData(scope, updateQueue, pulledDatasetNames, onComplete);
      }
      else {
        if (onComplete) {
          //TODO check what "this" is in onComplete.
          onComplete(scope);
        }
      }
      return;
    });
  }


  pullApps(scope, updateQueue: any[], completedUpdates: any[]): void {
    // scope.appsService.getAppsList(scope.baseUrl).subscribe( apps => {
    scope.itemService.getItems(scope.serviceConfigUrl).subscribe( apps => {
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
    scope.itemService.getItems(scope.serviceConfigUrl).subscribe( vms => {
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
    scope.itemService.getItems(scope.serviceConfigUrl).subscribe( virtues => {
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

  pullUsers(scope, updateQueue: any[], completedUpdates: any[]): void {
    scope.itemService.getItems(scope.serviceConfigUrl).subscribe(users => {
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

  //
  // getName(dl: DictList<Item>, id: string) {
  //   return dl.get(id).getName();
  // }

  // getChildrenListHTMLstring(item: Item): string {
  //   return item.childNamesAsHtmlList;
  // }

  // try making these on the fly. Might not be that slow.
  getGrandchildrenHtmlList(i: Item): string {
    let grandchildrenHTMLList: string = "";
    for (let c of i.children.asList()) {
      grandchildrenHTMLList += c.childNamesAsHtmlList;
    }
    return grandchildrenHTMLList;
  }
}
