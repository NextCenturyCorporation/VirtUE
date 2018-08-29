import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../services/baseUrl.service';
import { ItemService } from '../../services/item.service';

import { Column } from '../../models/column.model';
import { DictList, Dict } from '../../models/dictionary.model';

import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Item } from '../../models/item.model';

import { ColorSet } from '../../sets/color.set';

import { ConfigUrlEnum } from '../../enums/enums';

interface DatasetType {
  serviceUrl: string;
  class: any;
  datasetName: string;
  depends: string;
}

@Component({
providers: [ BaseUrlService, ItemService  ]
})
export abstract class GenericPageComponent {

  // the base aws url. Currently only used within dashboard.
  baseUrl: string;

  // tells the service and backend where to make changes - specific to each type
  // of item. Must be set in constructor of derived class.
  serviceConfigUrl: ConfigUrlEnum;

  // these are filled as needed by the page extending this class, based on neededDatasets
  allUsers: DictList<User>;
  allVirtues: DictList<Virtue>;
  allVms: DictList<VirtualMachine>;
  allApps: DictList<Application>;

  // holds the names and data types of each of the four datasets.
  datasetMeta: Dict<DatasetType>;

  // string, holds the data types whose datasets must be loaded on page load or
  // refresh. Must be in order, from lowest child to highest parent - like ["apps, vms", "virtues"]
  neededDatasets: string[];

  constructor(
    protected router: Router,
    protected baseUrlService: BaseUrlService,
    protected itemService: ItemService,
    protected dialog: MatDialog
  ) {
    this.allUsers = new DictList<User>();
    this.allVirtues = new DictList<Virtue>();
    this.allVms = new DictList<VirtualMachine>();
    this.allApps = new DictList<Application>();

<<<<<<< HEAD

=======
>>>>>>> 417ea599cdb31690c33e6399b24310cc06040663
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

    this.buildDatasetMeta();

    let params = this.getPageOptions();
    this.serviceConfigUrl = params.serviceConfigUrl;
    this.neededDatasets = params.neededDatasets;
  }

  resetRouter(): void {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData(wait?: boolean): void {
    if (wait) {
      setTimeout(() => {
        this.pullData();
      }, 300);
      return;
    }
    // else
    this.pullData();
  }

  buildDatasetMeta() {
    this.datasetMeta = new Dict<DatasetType>();
    this.datasetMeta['apps'] = {
          serviceUrl: ConfigUrlEnum.APPS,
          class: Application,
          datasetName: 'allApps',
          depends: undefined
        };
    this.datasetMeta['vms'] = {
          serviceUrl: ConfigUrlEnum.VMS,
          class: VirtualMachine,
          datasetName: 'allVms',
          depends: 'allApps'
        };
    this.datasetMeta['virtues'] = {
          serviceUrl: ConfigUrlEnum.VIRTUES,
          class: Virtue,
          datasetName: 'allVirtues',
          depends: 'allVms'
        };
    this.datasetMeta['users'] = {
          serviceUrl: ConfigUrlEnum.USERS,
          class: User,
          datasetName: 'allUsers',
          depends: 'allVirtues'
        };
  }

  cmnComponentSetup() {
    let sub = this.baseUrlService.getBaseUrl().subscribe( res => {
      this.baseUrl = res[0].aws_server;

      this.itemService.setBaseUrl(this.baseUrl);

      this.pullData();
    }, error => {
      console.log("Error retrieving base url."); // TODO tell user something?
    }, () => {
      sub.unsubscribe();
    });

    this.resetRouter();
  }

  /*
  We need to make an ordered set of calls to load and process data from the
  backend, where each call (or more specifically, each processing section) can
  only start once the previous dataset has completely finished setting up the
  data it was told to.
  neededDatasets holds an ordered list of the data types that are needed
  (as strings), and must be set in every derived class's constructor.
  updateQueue is built based on those specified data types.

  neededDatasets must hold the names (as strings) of the datasets to be loaded:
  i.e. ['allApps', 'allVms']
  Must be in the order in which they should be loaded.

  If a refresh button is added, make sure that gen-form doesn't overwrite the
  item being edited - that'd just throw away whatever changes the user  had made
  so far. Do refresh the item's children though, based on its current childIDs list.
  May need to repackage this: rename this function pullDatasets, make it take an
  onComplete() function, create a pullData() in gen-list and gen-form, where
  gen-list's just calls pullDatasets(onPullComplete), and gen-form calls
  pullDatasets(onPullComplete) if "item" hasn't already filled, but
  pullDatasets(()=>{})) if it has, or if the mode is CREATE.
  Don't check the attributes of item, just make a flag - the user could have
  intentionally removed all of item's attributes.
  */
  pullData(): void {
    let updateQueue: any[] = [];

    for (let dName of this.neededDatasets) {
      if ( !(dName in this.datasetMeta)) {
        // throw error TODO
        console.log("Data \"" + dName + "\" requested in this.neededDatasets is not valid.\n\
Expected one of {\"apps\", \"vms\", \"virtues\", and/or \"users\"}");
      }
      else {
        updateQueue.push(this.datasetMeta[dName]);
      }
    }
    if (updateQueue.length > 0) {
      this.recursivePullData(updateQueue, []);
    }
    else {
      console.log("No valid datasets specified in this.neededDatasets");
    }

  }

  /**
  See comment above pullDatasets for overview on inputs

  This function pulls the datasets specified in updateQueue -
  as a queue, only the first element (call it E) is worked with. This function:
    pulls data from the backend, based on the specifications in E
    processes that data, and saves it into the object specified by E (one of allVms,
        allVirtues, allApps, or allUsers)
    Once that processing is done, E's name is added to the pulledDatasets list,
        to store the fact that that dataset has been built.
    E is then removed from the queue, and if updateQueue isn't empty, this
        function is recursively called again, with the shorted queue.
  Once updateQueue is empty, this.onPullComplete is called, and the chain
  returns. Note that the chain of functions is a chain of sub-processes, and so
  control in the rest of the program doesn't wait on them. Once they finish,
  The page will automatically update, so long as onPullComplete() changes some
  attribute that Angular is watching, like items in gen-list, or item.children in gen-form)

  ******************************************************************************
  informal usage:
    pass in:
      updateQueue:[
                      {
                        serviceUrl: ConfigUrlEnum.APPS,
                        class: Application,
                        datasetName: 'allApps',
                        depends: undefined
                      }, {
                        serviceUrl: ConfigUrlEnum.VMS,
                        class: VirtualMachine,
                        datasetName: 'allVms',
                        depends: 'allApps'
                      }, {
                        serviceUrl: ConfigUrlEnum.VIRTUES,
                        class: Virtue,
                        datasetName: 'allVirtues',
                        depends: 'allVms'
                      }
                  ],
      pulledDatasets: []

    at end:
      {
        updateQueue = []
        pulledDatasets  = ['allApps', 'allVms', 'allVirtues']
      }

  ******************************************************************************
  this isn't the prettiest function, but it eliminates code-reuse and ensures
  things are called in the correct order.
  Everything loads from one function, into an object type that can be searched
  in constant time, but can also be sorted or iterated over.

  ******************************************************************************
  */
  recursivePullData(
    updateQueue: DatasetType[],
    pulledDatasetNames: string[]
  ): void {
    let sub = this.itemService.getItems(updateQueue[0].serviceUrl).subscribe( rawDataList => {
    // The correct way would be to use a promise or something, since we're only making one call
    // and don't want a stream. But I don't think there's a harm in doing it this way.

      this[updateQueue[0].datasetName].clear(); // slightly paranoic attempt to preclude memory leaks
      this[updateQueue[0].datasetName] = new DictList<Item>();
      let item = null;
      for (let e of rawDataList) {
        item = new (updateQueue[0].class)(e);
        this[updateQueue[0].datasetName].add(item.getID(), item);

        // If the call to build the collection for this item's child-type has
        //   been recorded as completed, build this item's 'children' list.
        // Could just check if the Child-type's collection (allVms, allApps, etc)
        //   isn't empty, but that doesn't guarentee it's up-to-date.
        if (pulledDatasetNames.some(x => x === updateQueue[0].depends)) {
          item.buildChildren(this[updateQueue[0].depends]);
        }

      }
      item = null;
      rawDataList = null;
    },
    error => {
      console.log("Error in pulling dataset \'", updateQueue[0].datasetName, "\'");
      sub.unsubscribe();
      // TODO notify user
    },
    () => { // once the dataset has been pulled and fully processed above
      // mark this set as pulled
      pulledDatasetNames.push(updateQueue[0].datasetName);

      // remove front element
      updateQueue.shift();

      if (updateQueue.length !== 0) {
        // if there are more datasets to pull
        this.recursivePullData(updateQueue, pulledDatasetNames);
      }
      else {
        this.onPullComplete();
      }

      // can only close subscription once it's done everything it needed to.
      // So a new sub variable will be created
      sub.unsubscribe();
      return;
    });
  }

  abstract onPullComplete();

  // try making these on the fly. Might not be that slow.
  getGrandchildrenHtmlList(i: Item): string {
    let grandchildrenHTMLList: string = "";
    for (let c of i.children.asList()) {
      grandchildrenHTMLList += c.childNamesHTML;
    }
    return grandchildrenHTMLList;
  }


  abstract getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]};

}
