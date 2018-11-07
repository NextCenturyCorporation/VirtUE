import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../services/baseUrl.service';
import { DataRequestService } from '../../services/dataRequest.service';

import { Column } from '../../models/column.model';
import { DictList, Dict } from '../../models/dictionary.model';

// import { Item } from '../../models/item.model';
import { IndexedObj } from '../../models/indexedObj.model';
import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { Printer } from '../../models/printer.model';
import { FileSystem } from '../../models/fileSystem.model';

import { ConfigUrls } from '../../services/config-urls.enum';

import { GenericPageComponent } from '../gen-page/gen-page.component';

import { Mode } from '../gen-form/mode.enum';

import { DatasetNames } from './datasetNames.enum';

import { DatasetType } from './datasetType';


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
 *  - getDataPageOptions
 *      defines some page-specific values that dictate where data is pulled, and where later requests to the backend should be made.
 *  - onPullComplete
 *      a function to be called once all the requested datasets have been pulled and processed. Usually needed for setting page values
 *      to some part of the data that was requested, and so which wasn't available at render time.
 *
 * @extends [[GenericPageComponent]] to make use of the common formatting and navigation functions
 */
@Component({
providers: [ BaseUrlService, DataRequestService ]
})
export abstract class GenericDataPageComponent extends GenericPageComponent {


  /**
  * tells DataRequestService where to make changes on the backend - specific to each type
  * of item. Must be set in constructor of derived class. Not used to make the initial data pull.
  */
  serviceConfigUrl: ConfigUrls;

  /**
   * Holds all the datasets that get pulled in for the page. Doesn't need to be a DictList, but being a dictionary gives it
   * a useful interface. It shouldn't really add any overhead.
   */
  datasets: DictList<DictList<IndexedObj>>;


  /** holds the names and data types of each of the four datasets. */
  datasetMeta: Dict<DatasetType>;

 /**
  * Must hold a list of enumerations of the datasets to be loaded on page load or refresh,
  * in the order in which they should be loaded.
  * Generally, this is lowest-highest. (ordering is printer/fileSystem/app < vm < virtue < user)
  */
  neededDatasets: DatasetNames[];

  /** #uncommented */
  loadedDatasets:  DatasetNames[];

  /**
   * #uncommented
   */
  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, dataRequestService, dialog);
    // Initialize these empty. These get overwritten if/when their data is pulled from the backend.
    // this.allUsers = new DictList<IndexedObj>();
    // this.allVirtues = new DictList<IndexedObj>();
    // this.allVms = new DictList<IndexedObj>();
    // this.allApps = new DictList<IndexedObj>();
    // this.allPrinters = new DictList<Printer>();
    // this.allFileSystems = new DictList<FileSystem>();

    // This really only needs to be a dictionary (i.e. it could just be a normal {} object), but I like the DictList interface.
    this.datasets = new DictList<DictList<IndexedObj>>();

    this.buildDatasetMeta();

    // Every derivative of this class must define getDataPageOptions
    let params = this.getDataPageOptions();
    this.serviceConfigUrl = params.serviceConfigUrl;
    this.neededDatasets = params.neededDatasets;
    this.loadedDatasets = [];
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
    this.datasetMeta[DatasetNames.PRINTERS] = {
          serviceUrl: ConfigUrls.PRINTERS,
          class: Printer,
          datasetName: DatasetNames.PRINTERS,
          depends: []
        };
    this.datasetMeta[DatasetNames.FILE_SYSTEMS] = {
          serviceUrl: ConfigUrls.FILE_SYSTEMS,
          class: FileSystem,
          datasetName: DatasetNames.FILE_SYSTEMS,
          depends: []
        };
    this.datasetMeta[DatasetNames.APPS] = {
          serviceUrl: ConfigUrls.APPS,
          class: Application,
          datasetName: DatasetNames.APPS,
          depends: []
        };
    this.datasetMeta[DatasetNames.VMS] = {
          serviceUrl: ConfigUrls.VMS,
          class: VirtualMachine,
          datasetName: DatasetNames.VMS,
          depends: [DatasetNames.APPS]
        };
    this.datasetMeta[DatasetNames.VIRTUES] = {
          serviceUrl: ConfigUrls.VIRTUES,
          class: Virtue,
          datasetName: DatasetNames.VIRTUES,
          depends: [DatasetNames.VMS, DatasetNames.PRINTERS, DatasetNames.FILE_SYSTEMS]
        };
    this.datasetMeta[DatasetNames.USERS] = {
          serviceUrl: ConfigUrls.USERS,
          class: User,
          datasetName: DatasetNames.USERS,
          depends: [DatasetNames.VIRTUES]
        };
  }

  /**
   * This is the standard method that most components need to run upon load.
   * It sets up the tool used to load data ([[DataRequestService.baseUrl]]), and sends out a request for the data the page needs.
   */
  cmnDataComponentSetup(): void {
    let sub = this.baseUrlService.getBaseUrl().subscribe( res => {
      this.baseUrl = res[0].virtue_server;

      this.dataRequestService.setBaseUrl(this.baseUrl);

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
      this.loadedDatasets = [];
      this.recursivePullData(updateQueue);
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
   * The page will automatically re-render though when [[onPullComplete]]() changes some
   * attribute that Angular is watching on that page, like item in [[GenericListComponent]],
   * or item or item.{virtues, printes, etc.} in [[GenericTabbedFormComponent]])
   *
   ******************************************************************************
   * informal usage:
   *   pass in:
   *     updateQueue:[
   *                     {
   *                       serviceUrl: ConfigUrls.APPS,
   *                       class: Application,
   *                       datasetName: DatasetNames.APPS,
   *                       depends: undefined
   *                     },
   *                     {
   *                       serviceUrl: ConfigUrls.VMS,
   *                       class: VirtualMachine,
   *                       datasetName: DatasetNames.VMS,
   *                       depends: DatasetNames.APPS
   *                     },
   *                     {
   *                       serviceUrl: ConfigUrls.VIRTUES,
   *                       class: Virtue,
   *                       datasetName: DatasetNames.VIRTUES,
   *                       depends: DatasetNames.VMS
   *                     }
   *                 ],
   * Where
   *   this.loadedDatasets: []
   * .
   * Then
   *   at end:
   *     {
   *       updateQueue = []
   *       this.loadedDatasets  = [DatasetNames.APPS, DatasetNames.VMS, DatasetNames.VIRTUES]
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
    updateQueue: DatasetType[]
  ): void {
    let sub = this.dataRequestService.getItems(updateQueue[0].serviceUrl).subscribe( rawDataList => {

      this.datasets[updateQueue[0].datasetName] = new DictList<IndexedObj>();

      let obj: IndexedObj = null;
      for (let e of rawDataList) {
        // if (updateQueue[0].datasetName === DatasetNames.PRINTERS) {
        if (e.id === "6b3d4784-0fe5-4443-a08c-644c90f609ae") {
          console.log("\t", e);
        }

        // these objects come in with some number of lists of IDs pertaining to objects they need to be linked to.
        // Like a User has a list of Virtues that needs to be populated, based on the virtueTemplateIDs list it comes in with.
        // Virtue has one list each for vms, printers, and filesystems.
        // Application has none.
        // It's here in the constructor that those id lists are set.
        obj = new (updateQueue[0].class)(e);
        if (obj.getID() === "6b3d4784-0fe5-4443-a08c-644c90f609ae") {
          console.log("\t", obj);
        }

        this.datasets[updateQueue[0].datasetName].add(obj.getID(), obj);

        this.buildAllIndexedObjAttributes(obj, updateQueue[0].depends);

      }
      obj = null;
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
      this.loadedDatasets.push(updateQueue[0].datasetName);

      // deal with self-referential objects.
      for (let obj of this.datasets[updateQueue[0].datasetName].asList()) {
        this.buildIndexedObjAttribute(obj, updateQueue[0].datasetName);
      }

      // remove this set (the front element) from queue
      updateQueue.shift();

      if (updateQueue.length !== 0) {
        // if there are more datasets to pull
        this.recursivePullData(updateQueue);
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
   * @return an object holding the url that [[DataRequestService]] should be querying when making changes on the
   * type of object this page focuses on, and a list of the datasets which should be loaded upon page load or refresh.
   */
  abstract getDataPageOptions(): {
        serviceConfigUrl: ConfigUrls,
        neededDatasets: DatasetNames[]
      };


  /** #uncommented */
  buildAllIndexedObjAttributes(obj: IndexedObj, dependencies: DatasetNames[]): void {
      // go through each of the datasets that this dataset depends on.
      for (let dependencySet of dependencies) {

        // if this childSet has been built already (i.e., if it was on the neededDatasets list ahead of this current dataset)
        // then assume we want to use it to build indexedObj's attributes.
        // So if we're processing the Virtue dataset and we've already loaded the vm and printer dataset, then we should set up
        // this virtue's printer and vm lists (but not the filesystem list).
        if ( this.loadedDatasets.indexOf(dependencySet) !== -1) {
          this.buildIndexedObjAttribute(obj, dependencySet);
        }
      }
  }

  /** #uncommented */
  buildIndexedObjAttribute(obj: IndexedObj, datasetName: DatasetNames): void {
      // tell the record which of the datasets it depends on we're dealing with, and pass it that dataset, so it can build its children.
      obj.buildAttribute(datasetName, this.datasets[datasetName] );
  }



  /**
   * saves the item's current state to the backend, overwriting whatever record corresponds with item.getID().
   *
   * @param redirect a redirect function to call (only) after the saving process has successfully completed.
   */
  updateItem(obj: IndexedObj, redirect?: () => void): void {
    if (this.serviceConfigUrl === undefined) {
      console.log("Destination url not set! [[serviceConfigUrl]]");
      return;
    }
    let sub = this.dataRequestService.updateItem(this.serviceConfigUrl, obj.getID(), JSON.stringify(obj)).subscribe(
      data => {
        if (redirect) {
          redirect();
        }
        else {
          this.refreshPage();
        }
      },
      error => {
        console.log(error);
        sub.unsubscribe();
      },
      () => {// when finished
        sub.unsubscribe();
      });
  }

  /**
   * saves the item's current state to the backend. For non-User Items, an ID is generated on the backend.
   *
   * @param redirect a redirect function to call (only) after the saving process has successfully completed.
   */
  createItem(obj: IndexedObj, redirect?: () => void): void {
    console.log("Here!", this.serviceConfigUrl);
    if (this.serviceConfigUrl === undefined) {
      console.log("Destination url not set! [[serviceConfigUrl]]");
      return;
    }
    let sub = this.dataRequestService.createItem(this.serviceConfigUrl, JSON.stringify(obj)).subscribe(
      createdItem => {
        console.log("Here2!");
        // note that the returned created item is just ignored.
        if (redirect) {
          redirect();
        }
        else {
          this.refreshPage();
        }
      },
      error => {
        console.log(error.message);
        sub.unsubscribe();
      },
      () => {// when finished
        sub.unsubscribe();
      });
  }

  /**
   * Deletes the [[Item]] and then refreshes the data on the page.
   * @param obj the IndexedObj to be deleted from the backend.
   */
  deleteItem(obj: IndexedObj): void {
    if (this.serviceConfigUrl === undefined) {
      console.log("Destination url not set! [[serviceConfigUrl]]");
      return;
    }
    this.dataRequestService.deleteItem(this.serviceConfigUrl, obj.getID()).then(() => {
      this.refreshPage();
    });

  }

  /**
   * overriden by user-list, to instead call the setItemStatus method below.
   * TODO Change backend so everything works the same way.
   * Probably just make every work via a setStatus method, and remove the toggle.
   *
   * @param obj the IndexedObj we're toggling the status of.
   */
  toggleItemStatus(obj: IndexedObj): void {
    this.setItemStatus(obj, obj.enabled);
    //
    // if (this.serviceConfigUrl === undefined) {
    //   console.log("Destination url not set! [[serviceConfigUrl]]");
    //   return;
    // }
    // let sub = this.dataRequestService.toggleItemStatus(this.serviceConfigUrl, obj.getID()).subscribe((updatedRecord) => {
    //   // note that updatedRecord is ignored - no need to use it.
    //   // Perhaps could check that its status is the opposite of what was went in, in case maybe the update
    //   // didn't go through. Don't know if that's a reasonable check though.
    //   // TODO
    //
    //   this.refreshPage();
    // },
    // error => { // on error
    //   sub.unsubscribe();
    // },
    // () => {
    //   sub.unsubscribe();
    // });
  }

  /**
   * Sets the status of an IndexedObj.
   * This is only called by an overriding toggleItemStatus defined in userList.
   * See note there, and on toggleItemStatus above, for why.
   *
   * @param obj
   * @param newStatus
   *
   */
  setItemStatus(obj: IndexedObj, newStatus: boolean): void {
    if (this.serviceConfigUrl === undefined) {
      console.log("Destination url not set! [[serviceConfigUrl]]");
      return;
    }
    let sub = this.dataRequestService.setItemStatus(this.serviceConfigUrl, obj.getID(), newStatus).subscribe(() => {
      this.refreshPage();
    },
    error => {
      sub.unsubscribe();
    },
    () => {
      sub.unsubscribe();
    });
  }

  /** # so subclasses can override it, and have something that will be called whenever a change is sent to the backend. */
  refreshPage() {}
}
