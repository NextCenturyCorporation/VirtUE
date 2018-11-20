import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { Column } from '../../models/column.model';
import { DictList, Dict } from '../../models/dictionary.model';

import { IndexedObj } from '../../models/indexedObj.model';
import { Item } from '../../models/item.model';
import { Toggleable } from '../../models/toggleable.interface';

import { DatasetNames } from '../gen-data-page/datasetNames.enum';

/**
* @class
 * This is the generic class which provides a central source for many common functions, currently:
 *  - routing
 *  - dialogs
 *  - formatting
 */
@Component({
providers: [ Router, MatDialog ]
})
export abstract class GenericPageComponent {

  /**
   * @param router Handles the navigation to/from different pages. Injected, and so is constant across components.
   * @param dialog Injected. This is a pop-up for verifying irreversable user actions
   */
  constructor(
    protected router: Router,
    protected dialog: MatDialog
      ) {
    // override the route reuse strategy
    // Tell angular to load a fresh, new, component every time a URL that needs this component loads,
    // even if the user has been on that page before.
    // this.router.routeReuseStrategy.shouldReuseRoute = () => false;

    // make the page reload if the user clicks on a link to the same page they're on.
    // this.router.navigated = false;

  }

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
   * @return The toggleable object's status, in plain (and capitalized) english.
   */
  formatStatus( obj: Toggleable ): string {
    return obj.enabled ? 'Enabled' : 'Disabled';
  }


  /**
   * The following two functions are generally just used in tables to display the indexedObjs attached
   * to an [[IndexedObj]] (ie the `children`), and the indexedObjs attached to each of those (i.e. the `grandchildren`).
   * It used to be all generated (as an html string) whenever an item was created or its children updated, but now
   * is done on the fly. These functions get re-called whenever the mouse enters or leaves a row in the table, so this
   * seems like a bit of a waste, given that an item's children/grandchildren won't change randomly in the background,
   * but it doesn't seem to slow anything down atm, with our small datasets.
   *
   * Doing it this way is much more generic, and even within the tables it allows us to make the items within those an
   * indexedObj's children list clickable, because we're giving the lists of full objects to the html, rather than just lists of names.
   */
  private getChildren(obj: IndexedObj, childDatasetName: DatasetNames): IndexedObj[] {
    if (!obj) {
      return [];
    }
    return obj.getRelatedDict(childDatasetName).asList();
  }

  /**
   * @return A list (not a set) of the requested type of the obj's children's children.
   * Example: For an input User, look through that user's Virtue children, and generate a list of all of those
   * virtues' collective Printers.
   */
  private getGrandChildren(obj: IndexedObj, childDatasetName: DatasetNames, grandChildDatasetName: DatasetNames): IndexedObj[] {
    if (!obj) {
      return [];
    }
    let grandchildren: IndexedObj[] = [];
    for (let c of this.getChildren(obj, childDatasetName)) {
      grandchildren = grandchildren.concat(this.getChildren(c, grandChildDatasetName));
    }
    return grandchildren;
  }

  getVirtues(i: Item): IndexedObj[] {
    return this.getChildren(i, DatasetNames.VIRTUES);
  }

  getVirtueVms(i: Item): IndexedObj[] {
    return this.getGrandChildren(i, DatasetNames.VIRTUES, DatasetNames.VMS);
  }

  getVms(i: Item): IndexedObj[] {
    return this.getChildren(i, DatasetNames.VMS);
  }

  getVmApps(i: Item): IndexedObj[] {
    return this.getGrandChildren(i, DatasetNames.VMS, DatasetNames.APPS);
  }

  getApps(i: Item): IndexedObj[] {
    return this.getChildren(i, DatasetNames.APPS);
  }

  getPrinters(i: Item): IndexedObj[] {
    return this.getChildren(i, DatasetNames.PRINTERS);
  }

  getFileSystems(i: Item): IndexedObj[] {
    return this.getChildren(i, DatasetNames.FILE_SYSTEMS);
  }

  /**
   * Navigates to the form page for this item.
   */
  viewItem(item: Item): void {
    this.router.navigate([item.getViewURL()]);
  }

  /**
   * Navigates to, and enable for editing, the form page for `item`.
   *
   * @param item the Item which we should navigate to and edit.
   */
  editItem(item: Item): void {
    this.router.navigate([item.getEditURL()]);
  }

  /**
   * Navigates to a form page pre-filled with `item`'s attributes.
   */
  dupItem(item: Item): void {
    this.router.navigate([item.getDupURL()]);
  }

  /**
   * A generic method for navigating to some input page.
   *
   * @param targetPath the path to navigate to.
   */
  goToPage(targetPath: string) {
    this.router.navigate([targetPath]);
  }

  getRouterUrl(): string {
    return this.router.routerState.snapshot.url;
  }

  /**
   * Abstracts away the router from subclasses
   */
  getRouterUrlPieces(): string[] {
    let url = this.getRouterUrl();
    if (url[0] === '/') {
      url = url.substr(1);
    }
    return url.split('/');
  }


  /**
   * This opens a dialog to confirm irreversible or dangerous user actions before carrying them out.
   * It could be merged with the modal creation below.
   *
   * @param actionDescription a user-readable description of what's being done, to display in the dialog
   * @param action a function to be called if the user selects 'OK'.
   */
  openDialog(actionDescription: string, action: () => void): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          actionType: actionDescription
        }
    });

    // TODO ask why the dialog is deliberately off-centered.
    dialogRef.updatePosition({ top: '15%', left: '36%' });

    // control goes here after either "Ok" or "Cancel" are clicked on the dialog
    let sub = dialogRef.componentInstance.getResponse.subscribe((shouldProceed) => {
      if (shouldProceed) {
        action();
      }
    },
    () => {},
    () => {
      sub.unsubscribe();
    });
  }

  // Use some version of this later if time. Currently, all item-selection modals use the (now-poorly named) 'genericModalComponent', and
  // any other modals must be built separately, and their layout has to be fiddled with the same way that one was, until there's only one
  // scrollbar. Ideally there's one modal frame, with subclasses which use the same html? Regardless, the content needs to be completely
  // contained by the modal's template - so (as long as the content's width isn't set to some constant pixel value) the content could be
  // any size, and there'd only be one scrollbar. That wrapper would have to be passed a callback function to be called on emit, and all
  // of the inner classes would have to fit an interface with some function that gets called on submit, which returns the data that should
  // be emitted. So all the classes that create a modal only need to know the type of modal they want, and what function should be called
  // with the information that type of modal will return.
  //
  // The current GenericModalComponent should be renamed "SelectionModalComponent", but its html should stay, to be used by all modals.
  // I think.
  // I don't know if you can, say, inject an abstract object in the html, and then have every class that uses that html file define
  // what subclass that abstract object should be.
  // Oh maybe
  // https://stackoverflow.com/questions/36325212/angular-dynamic-tabs-with-user-click-chosen-components?rq=1
  // https://stackoverflow.com/questions/51271550/angular-2-inject-html-content-to-component-in-bootstrap-modal
  //
  // /**
  //  * this wrapper brings up a modal of a supplied type, passing along input data.
  //  * When the emitter whose name was passed in emits a value, pass the value to the supplied onComplete function.
  //  *
  //  * This function really just exists so all the modals are the same size and position, and to abstract away all the boilerplate, so it
  //  * doesn't have to be repeated everywhere a modal can be created.
  //  *
  //  * #uncommented unimplemented
  //  *
  //  * @param onComplete A function to pass the modal's list of selected objects to, once the user hits 'Submit'
  //  */
  // activateModal(
  //       params: {
  //         modalClass: any,
  //         inData: {},
  //         onComplete: (any) => void,
  //         scale?: {x: number, y : number}
  //       }
  // ): void {
  //   let scale = {x: 0.7, y: 0.7};
  //   if (params.scale) {
  //     let scale = params.scale;
  //   }
  //   // I'd think this way would be better, but modals have extra scroll bars this way.
  //   let dialogHeight = Math.floor(scale.x * 100);
  //   let dialogWidth = Math.floor(scale.y * 100);
  //
  //   let dialogRef = this.dialog.open( params.modalClass,  {
  //     height: dialogHeight + '%',
  //     width: dialogWidth + '%',
  //     data: params.inData
  //   });
  //
  //   // untested, because none of the things this function would need have been implemented
  //   // Not sure if the scope inside onComplete is the same between the two below styles of passing in the callback
  //   // Note that submitButtonWatcher is an emitter defined in the (future) abstract parent GenericModalComponent.
  //   //
  //   let sub = dialogRef.componentInstance.submitButtonWatcher.subscribe(
  //     params.onComplete,
  //     // (returnedData) => {
  //     //   params.onComplete(returnedData);
  //     // },
  //     () => { // on error
  //       sub.unsubscribe();
  //     },
  //     () => { // when finished
  //       sub.unsubscribe();
  //     }
  //   );
  //
  //   dialogRef.updatePosition({ top: '5%'});
  //
  // }

}
