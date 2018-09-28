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

import { ConfigUrls } from '../../services/config-urls.enum';

import { GenericPageComponent } from '../gen-page/gen-page.component';

import { Mode } from '../gen-form/mode.enum';

import { Datasets } from './datasets.enum';

/**
 * @interface
 * This defines a dataset type
 */
interface DatasetType {

  /** The url on the backend from which to request this sort of dataset */
  serviceUrl: string;

  /** the class members of this dataset should be created as. Apparently needs to be 'any'. */
  class: any;

  /**
   * The name of the dataset attribute within this class. E.g. 'allApps', 'allUsers', etc.
   *
   * Specified as a Dataset enum to encapsulate that.
   */
  datasetName: Datasets;

  /**
   * The dataset which members of this dataset have links to, and so which must be already loaded in order to fully-flesh
   * out the items in this dataset.
   * At the moment it's just used to build item.children from item.childIDs for each item in *this* dataset.
   */
  depends: Datasets;
}

/**
* @class
 * This is the generic class which all pages that must load Item data from the backend must extend.
 * It has functions for pulling that data, as well as linking it together; a Virtue containing a list of VM childIDs will
 * be generated a list of references to the actual VM objects. This significantly reduces the number of requests made to
 * the backend, as compared to having to request and wait for the data on every object referenced via a childID separately.
 *
 * Children must define:
 *  - neededDatasets
 *      a list of Dataset enums telling this page what information to request from the backend.
 *      currently, if a page needs data on a user, it must request info on all users. That could be changed,
 *      but I haven't noticed any discernible lag. Should eventually see how many users can be added before things start to slow down,
 *      and where the bottleneck there is.
 *
 * and also must implement:
 *  - getPageOptions
 *      defines some page-specific values that dictate where data is pulled, and where later requests to the backend should be made.
 *  - onPullComplete
 *      a function to be called once all the requested datasets have been pulled and processed. Usually needed for setting page values
 *      to some part of the data that was requested, and so which wasn't available at render time.
 *
 * @extends [[GenericPageComponent]] to make use of the common formatting and navigation functions
 */
@Component({
providers: [ BaseUrlService, ItemService  ]
})
export abstract class GenericDataPageComponent extends GenericPageComponent {

  /**
  * the highest-level, base url from which the backend is accessible.
  * Currently only used within dashboard.
  */
  baseUrl: string;

  /**
  * tells ItemService where to make changes on the backend - specific to each type
  * of item. Must be set in constructor of derived class. Not used to make the initial data pull.
  */
  serviceConfigUrl: ConfigUrls;

  /** a dataset of all users known to the system. Populated only if requested through [[neededDatasets]]. */
  allUsers: DictList<Item>;

  /** a dataset of all Virtue Templates known to the system. Populated only if requested through [[neededDatasets]]. */
  allVirtues: DictList<Item>;

  /** a dataset of all VM templates known to the system. Populated only if requested through [[neededDatasets]]. */
  allVms: DictList<Item>;

  /** a dataset of all Applications known to the system. Populated only if requested through [[neededDatasets]]. */
  allApps: DictList<Item>;


  /** holds the names and data types of each of the four datasets. */
  datasetMeta: Dict<DatasetType>;


 /**
  * Must hold a list of enumerations of the datasets to be loaded on page load or refresh,
  * in the order in which they should be loaded.
  * Generally, this is lowest-highest. (ordering is app < vm < virtue < user)
  */
  neededDatasets: Datasets[];

  /**
   * @param router Handles the navigation to/from different pages. Injected, and so is constant across components.
   * @param baseUrlService Injected. Just requests the base URL from which to request data
   * @param itemService Injected. Uses the base URL and a ConfigUrl to pull data from datasets on the backend.
   * @param dialog Injected. This is a pop-up for verifying irreversable user actions
   */
  constructor(
    router: Router,
    protected baseUrlService: BaseUrlService,
    protected itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, dialog);
    // Initialize these empty. These get overwritten if/when their data is pulled from the backend.
    this.allUsers = new DictList<User>();
    this.allVirtues = new DictList<Virtue>();
    this.allVms = new DictList<VirtualMachine>();
    this.allApps = new DictList<Application>();

    this.buildDatasetMeta();

    // Every derivative of this class must define getPageOptions
    let params = this.getPageOptions();
    this.serviceConfigUrl = params.serviceConfigUrl;
    this.neededDatasets = params.neededDatasets;
  }

  /**
   * @param wait whether or not the code should wait 1/3 second before refreshing.
   * Currently the wait time is never used, so the default is to ignore it amd refresh the data
   * right away. Before onPullComplete was implemented, this was used to delay the initialization of page
   * components until data from the backend had probably been retrieved.
   */
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

  /**
   * This defines the metadata about the 4 main datasets, so they can be used generically when pulling data.
   * Could be put into a different file, along with the dataset interface.
   * Called in constructor.
   */
  buildDatasetMeta() {
    this.datasetMeta = new Dict<DatasetType>();
    this.datasetMeta[Datasets.APPS] = {
          serviceUrl: ConfigUrls.APPS,
          class: Application,
          datasetName: Datasets.APPS,
          depends: undefined
        };
    this.datasetMeta[Datasets.VMS] = {
          serviceUrl: ConfigUrls.VMS,
          class: VirtualMachine,
          datasetName: Datasets.VMS,
          depends: Datasets.APPS
        };
    this.datasetMeta[Datasets.VIRTUES] = {
          serviceUrl: ConfigUrls.VIRTUES,
          class: Virtue,
          datasetName: Datasets.VIRTUES,
          depends: Datasets.VMS
        };
    this.datasetMeta[Datasets.USERS] = {
          serviceUrl: ConfigUrls.USERS,
          class: User,
          datasetName: Datasets.USERS,
          depends: Datasets.VIRTUES
        };
  }

  /**
   * This is the standard method that most components need to run upon load.
   * It sets up the tool used to load data ([[ItemService.baseUrl]]), and sends out a request for the data the page needs.
   */
  cmnDataComponentSetup(): void {
    let sub = this.baseUrlService.getBaseUrl().subscribe( res => {
      this.baseUrl = res[0].virtue_server;

      this.itemService.setBaseUrl(this.baseUrl);

      this.pullData();
    }, error => {
      console.log("Error retrieving base url."); // TODO notify user
    }, () => {
      sub.unsubscribe();
    });

  }

  /**
   * We need to make an ordered set of calls to load and process data from the
   * backend, where each call (or more specifically, each processing section) can
   * only start once the previous dataset has completely finished being processed.
   *
   * [[neededDatasets]] holds an ordered list of the datasets that are needed.
   * It must be defined in every derived class's constructor.
   * an update queue is built based on those specified data types. This updateQueue
   * is what [[recursivePullData]] recurses through in order to build everything.
   *
   */
  pullData(): void {
    let updateQueue: any[] = [];

    for (let dName of this.neededDatasets) {
      if ( !(dName in this.datasetMeta)) {
        // throw error TODO
        console.log("Unrecognized dataset name");
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
   * @param updateQueue
   * @param
   *  See comment above [[pullDatasets]] for other details on inputs
   *
   *  This function pulls the datasets specified in updateQueue -
   *  as a queue, only the first element (call it E) is worked with. This function:
   *  - pulls data from the backend, based on the specifications in E
   *  - processes that data, and saves it into the object specified by E (one of allVms,
   *       allVirtues, allApps, or allUsers)
   *   - Once that processing is done, E's name is added to the pulledDatasets list,
   *       to store the fact that that dataset has been built.
   *   - E is then removed from the queue, and if updateQueue isn't empty, this
   *       function is recursively called again, with the shorted queue.
   *
   * Once updateQueue is empty, this.[[onPullComplete]] is called, after which the chain
   * returns. Note that the chain of functions is a chain of ansynchronous sub-processes, and so
   * while they wait on each other, control in the rest of the program doesn't wait on them.
   *
   * The page will automatically update though when [[onPullComplete]]() changes some
   * attribute that Angular is watching on that page, like items in [[GenericListComponent]],
   * or item or item.children in [[GenericFormComponent]])
   *
   ******************************************************************************
   * informal usage:
   *   pass in:
   *     updateQueue:[
   *                     {
   *                       serviceUrl: ConfigUrls.APPS,
   *                       class: Application,
   *                       datasetName: Datasets.APPS,
   *                       depends: undefined
   *                     },
   *                     {
   *                       serviceUrl: ConfigUrls.VMS,
   *                       class: VirtualMachine,
   *                       datasetName: Datasets.VMS,
   *                       depends: Datasets.APPS
   *                     },
   *                     {
   *                       serviceUrl: ConfigUrls.VIRTUES,
   *                       class: Virtue,
   *                       datasetName: Datasets.VIRTUES,
   *                       depends: Datasets.VMS
   *                     }
   *                 ],
   *     pulledDatasets: []
   *
   *   at end:
   *     {
   *       updateQueue = []
   *       pulledDatasets  = [Datasets.APPS, Datasets.VMS, Datasets.VIRTUES]
   *     }
   *
   ******************************************************************************
   * this isn't the prettiest function, but it eliminates code-reuse and ensures
   * things are called and built in the correct order.
   * Furthermore, everything loads from one function, into an object type that can be searched
   * in constant time, but can also be sorted or iterated over.
   ******************************************************************************
   *
   * The correct way of making this chain of ansynchronous calls would be to use promises, not
   * subscriptions, since we only want to receive a single value, not listen to a stream. Services are set up
   * using subscriptions though, and so long as we make sure to unsubscribe from everything that gets subscribed to,
   * it should work out the same. Note that not unsubscribing led to a large memory leak in Firefox.
   * (Apparently chrome automatically stopped subscriptions by components being destroyed/GC'd.)
   */
  recursivePullData(
    updateQueue: DatasetType[],
    pulledDatasetNames: string[]
  ): void {
    let sub = this.itemService.getItems(updateQueue[0].serviceUrl).subscribe( rawDataList => {

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
      // close stream on error.
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
        // if all datasets have been pulled and set up
        this.onPullComplete();
      }

      // Close this subscription once it's done everything it needed to.
      // Remember that 'let' creates scope-bound variables, and so each recursive layer
      // of this function will have its own 'sub' object. The current sub hides the subs
      // of the outer (older) scopes, and so all references to 'sub' after its declaration refer
      // to the 'sub' object of the current scope, and no other.
      sub.unsubscribe();
      return;
    });
  }

  /**
   * Must be implemented by all sub-classes.
   *
   * This function is called when the data requested from the back-end returns.
   * See comment on [[recursivePullData]]()
   */
  abstract onPullComplete(): void;

  /**
   * Must be implemented by all sub-classes.
   *
   * @return an object holding the url that [[ItemService]] should be querying when making changes on the
   * type of object this page focuses on, and a list of the datasets which should be loaded upon page load or refresh.
   */
  abstract getPageOptions(): {
        serviceConfigUrl: ConfigUrls,
        neededDatasets: Datasets[]
      };

  /**
   * Deletes the [[Item]] and then refreshes the data on the page.
   * @param item the Item to be deleted from the backend.
   */
  deleteItem(item: Item): void {
    this.itemService.deleteItem(this.serviceConfigUrl, item.getID()).then(() => {
      this.refreshData();
    });

  }

  /**
   * overriden by user-list, to instead call the setItemStatus method below.
   * TODO Change backend so everything works the same way.
   * Probably just make every work via a setStatus method, and remove the toggle.
   *
   * @param item the item we're toggling the status of.
   */
  toggleItemStatus(item: Item): void {
    let sub = this.itemService.toggleItemStatus(this.serviceConfigUrl, item.getID()).subscribe((updatedRecord) => {
      // note that updatedRecord is ignored - no need to use it.
      // Perhaps could check that its status is the opposite of item.status, in case maybe the update
      // didn't go through. Don't know if that's a reasonable check though.
      // TODO
      this.refreshData();
    },
    error => { // on error
      sub.unsubscribe();
    },
    () => {
      sub.unsubscribe();
    });
  }

  /**
   * Sets the status of an item.
   * This is only called by an overriding toggleItemStatus defined in userList.
   * See note there, and on toggleItemStatus above, for why.
   *
   * @param item
   * @param newStatus
   *
   */
  setItemStatus(item: Item, newStatus: boolean): void {
    let sub = this.itemService.setItemStatus(this.serviceConfigUrl, item.getID(), newStatus).subscribe(() => {
      this.refreshData();
    },
    error => {
      sub.unsubscribe();
    },
    () => {
      sub.unsubscribe();
    });
  }
}
