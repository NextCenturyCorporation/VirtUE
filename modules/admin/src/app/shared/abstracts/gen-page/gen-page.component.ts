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

import { Mode, ConfigUrls, Datasets } from '../../enums/enums';


/**
 * #uncommented
 * @interface
 */
interface DatasetType {
  serviceUrl: string;
  class: any;
  datasetName: Datasets;
  depends: Datasets;
}

/**
 * #uncommented
 * @class
 * @extends
 */
@Component({
providers: [ BaseUrlService, ItemService  ]
})
export abstract class GenericPageComponent {

  /**
   * the highest-level, base url from which the backend is accessible.
   * Currently only used within dashboard.
   */
  baseUrl: string;

  /**
   * tells ItemService where to make changes on the backend - specific to each type
   * of item. Must be set in constructor of derived class. Not used to pull data.
   */
  serviceConfigUrl: ConfigUrls;

  /** a dataset of all users known to the system. Populated only if requested through neededDatasets. */
  allUsers: DictList<Item>;
  // allUsers: DictList<User>;

  /** a dataset of all Virtue Templates known to the system. Populated only if requested through neededDatasets. */
  allVirtues: DictList<Item>;
  // allVirtues: DictList<Virtue>;

  /** a dataset of all VM templates known to the system. Populated only if requested through neededDatasets. */
  allVms: DictList<Item>;
  // allVms: DictList<VirtualMachine>;

  /** a dataset of all Applications known to the system. Populated only if requested through neededDatasets. */
  allApps: DictList<Item>;
  // allApps: DictList<Application>;


  /** holds the names and data types of each of the four datasets. */
  datasetMeta: Dict<DatasetType>;


 /**
  * Must hold a list of enumerations of the datasets to be loaded on page load or refresh,
  * in the order in which they should be loaded.
  * Generally, this is lowest-highest. (ordering is app < vm < virtue < user)
  */
  neededDatasets: Datasets[];

  constructor(
    /** Handles the navigation to/from different pages. Injected, and so is constant across components. */
    protected router: Router,

    /** Injected. Just requests the base URL from which to request data */
    protected baseUrlService: BaseUrlService,

    /** Injected. Uses the base URL and a ConfigUrl to pull data from datasets on the backend. */
    protected itemService: ItemService,

    /** Injected. This is a pop-up for verifying irreversable user actions */
    protected dialog: MatDialog
  ) {
    // Initialize these empty. These get overwritten if/when their data is pulled from the backend.
    this.allUsers = new DictList<User>();
    this.allVirtues = new DictList<Virtue>();
    this.allVms = new DictList<VirtualMachine>();
    this.allApps = new DictList<Application>();

    // override the route reuse strategy
    // Tell angular to load a fresh, new, component every time a URL that needs this component loads,
    // even if the user has been on that page before.
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

    // make the page reload if the user clicks on a link to the same page they're on.
    this.router.navigated = false;

    this.buildDatasetMeta();

    // Every derivative of this class must define getPageOptions
    let params = this.getPageOptions();
    this.serviceConfigUrl = params.serviceConfigUrl;
    this.neededDatasets = params.neededDatasets;
  }

  /**
   * @param wait whether or not the code should wait 1/3 second before refreshing.
   * Currently the wait time is never used, so the default is to ignore it amd refresh the data
   * right away.
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
   * It sets up itemService for that page, and sends out a request for the data the page needs.
   */
  cmnComponentSetup(): void {
    let sub = this.baseUrlService.getBaseUrl().subscribe( res => {
      this.baseUrl = res[0].aws_server;

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
   * only start once the previous dataset has completely finished setting up the
   * data it was told to.
   * neededDatasets holds an ordered list of the data types that are needed
   * (as strings), and must be set in every derived class's constructor.
   * updateQueue is built based on those specified data types.
   *
   * If a refresh button is added, make sure that gen-form doesn't overwrite any
   * info that's been entered to the page
   * Do refresh the item's children though, based on its current childIDs list. (?)
   * May need to repackage this: rename this function pullDatasets, make it take an
   * onComplete() function, create a pullData() in gen-list and gen-form, where
   * gen-list's just calls pullDatasets(onPullComplete), and gen-form calls
   * pullDatasets(onPullComplete) if "item" hasn't already filled, but
   * pullDatasets(()=>{})) if it has, or if the mode is CREATE.
   * Don't check the attributes of item, just make a flag- the user could have
   * intentionally removed all of item's attributes.
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
   *
   * @return
   *  See comment above pullDatasets for overview on inputs
   *
   *  This function pulls the datasets specified in updateQueue -
   *  as a queue, only the first element (call it E) is worked with. This function:
   *  pulls data from the backend, based on the specifications in E
   *  processes that data, and saves it into the object specified by E (one of allVms,
   *       allVirtues, allApps, or allUsers)
   *   Once that processing is done, E's name is added to the pulledDatasets list,
   *       to store the fact that that dataset has been built.
   *   E is then removed from the queue, and if updateQueue isn't empty, this
   *       function is recursively called again, with the shorted queue.
   * Once updateQueue is empty, this.onPullComplete is called, and the chain
   * returns. Note that the chain of functions is a chain of sub-processes, and so
   * control in the rest of the program doesn't wait on them. Once they finish,
   * The page will automatically update, so long as onPullComplete() changes some
   * attribute that Angular is watching, like items in gen-list, or item.children in gen-form)
   *
   ******************************************************************************
   * informal usage:
   *   pass in:
   *     updateQueue:[
   *                     {
                           serviceUrl: ConfigUrls.APPS,
                           class: Application,
                           datasetName: Datasets.APPS,
                           depends: undefined
                         },
                         {
                           serviceUrl: ConfigUrls.VMS,
                           class: VirtualMachine,
                           datasetName: Datasets.VMS,
                           depends: Datasets.APPS
                         },
                         {
                           serviceUrl: ConfigUrls.VIRTUES,
                           class: Virtue,
                           datasetName: Datasets.VIRTUES,
                           depends: Datasets.VMS
   *                     }
   *                 ],
   *     pulledDatasets: []
   *
   *   at end:
   *     {
   *       updateQueue = []
   *       pulledDatasets  = ['allApps', 'allVms', 'allVirtues']
   *     }
   *
   ******************************************************************************
   * this isn't the prettiest function, but it eliminates code-reuse and ensures
   * things are called in the correct order.
   * Everything loads from one function, into an object type that can be searched
   * in constant time, but can also be sorted or iterated over.
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

  /**
   * This function is called when the data requested from the back-end returns.
   * In these list pages, it must at least set the value of this.items to whatever dataset you're trying to show.
   */
  abstract onPullComplete(): void;

  /**
   * @return an object holding the Url ItemService should be querying when making changes on the
   * type of object this page focuses on, and a list of the datasets which should be loaded upon page load or refresh.
   */
  abstract getPageOptions(): {
      serviceConfigUrl: ConfigUrls,
      neededDatasets: Datasets[]};

  /**
   * Used in pages with tables of items.
   * @param item the item whose name we want to view.
   *
   * @return the item's name/identifier, with '(disabled)' next to it when applicable.
   */
  formatName( item: Item ): string {
    if (item.enabled) {
      return item.getName();
    }
    else {
      return item.getName() + " (disabled)";
    }
  }

  /**
   * Used in pages with tables of items.
   * @param item the item whose status we want to display
   *
   * @return The item's status, in plain (and capitalized) english.
   */
  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  /**
   * The following two functions are used in tables to display the items attached
   * to an item (children) and the items attched to each of those (grandchildren).
   * It used to be all generated (as an html string) whenever an item was created
   * or its children updated, but now is doen on the fly. These functions get
   * re-called whenever the mouse enters or leaves a row in the table, so this
   * seems like a bit of a waste, given that an item's children/grandchildren won't
   * change randomly in the background.
   * Doing it this way allows us to make the items within those lists clickable.
   *
   * @param i the item whose grandchildren we want a list of.
   *
   * @return A list (not a set) of all the item's children's children.
  */
  getGrandchildren(i: Item): Item[] {
    let grandchildren: Item[] = [];
    for (let c of i.children.asList()) {
      grandchildren = grandchildren.concat(c.children.asList());
    }
    return grandchildren;
  }

  /**
   * This is used in many tables to show an Item's children. All lists in tables are gotten by passing
   * an item to a function that has the scope of the object containing and defining the table.
   *
   * @param i the item whose children we want a list of
   *
   * @return a list of the item's children.
   */
  getChildren(i: Item): Item[] {
    return i.children.asList();
  }

  /**
   * Navigates to the form page for this Item.
   *
   * @param i the Item to which we should navigate.
   */
  viewItem(i: Item): void {
    this.router.navigate([i.getPageRoute(Mode.VIEW)]);
  }

  /**
   * Navigates to and enable for editing form page for this Item.
   *
   * @param i the Item which we should navigate to and edit.
   */
  editItem(i: Item): void {
    this.router.navigate([i.getPageRoute(Mode.EDIT)]);
  }

  /**
   * Navigates to a form page pre-filled with this Item's attributes.
   *
   * @param i the Item to duplicate
   */
  dupItem(i: Item): void {
    this.router.navigate([i.getPageRoute(Mode.DUPLICATE)]);
  }

  /**
   * Deletes the Item and then refreshes the data on the page.
   * @param item the Item to be deleted from the backend.
   */
  deleteItem(item: Item): void {
    this.itemService.deleteItem(this.serviceConfigUrl, item.getID()).then(() => {
      this.refreshData();
    });

  }
}
