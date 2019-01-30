import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { RouterService } from '../../services/router.service';

import { Column } from '../../models/column.model';
import { DictList, Dict } from '../../models/dictionary.model';

import { Application } from '../../models/application.model';
import { IndexedObj } from '../../models/indexedObj.model';
import { Item } from '../../models/item.model';
import { Toggleable } from '../../models/toggleable.interface';

import { DatasetNames } from '../gen-data-page/datasetNames.enum';

/**
* @class
 * This is the generic class which provides a central source for common functions, currently:
 *  - routing
 *  - dialogs
 *  - formatting
 */
export abstract class GenericPageComponent {

  /**
   * @param routerService Handles the navigation to/from different pages. Injected, and so is constant across components.
   * @param dialog Injected. This is a pop-up for verifying irreversable user actions
   */
  constructor(
    protected routerService: RouterService,
    protected dialog: MatDialog
      ) {
  }

  /**
   * Used in pages with tables of items.
   * @param item the item whose name we want to view.
   *
   * @return the item's name/identifier, with '(disabled)' next to it when applicable.
   */
  formatName( item: Item ): string {
    if (item.enabled === false && !(item instanceof Application)) {
      return item.getName() + " (disabled)";
    }
    else {
      return item.getName();
    }
  }

  /**
   * TODO the Toggleable object is what should know how to display a status, no?
   *  -but toggleable is an interface..
   * @return The toggleable object's status, in plain (and capitalized) english.
   */
  formatStatus( obj: Toggleable ): string {
    return obj.enabled ? 'Enabled' : 'Disabled';
  }

  /**
   * Navigates to the form page for this item.
   */
  viewItem(item: Item): void {
    this.routerService.goToPage(item.getViewURL());
  }

  /**
   * Navigates to, and enable for editing, the form page for `item`.
   */
  editItem(item: Item): void {
    this.routerService.goToPage(item.getEditURL());
  }

  /**
   * Navigates to a form page pre-filled with `item`'s attributes.
   */
  dupItem(item: Item): void {
    this.routerService.goToPage(item.getDupURL());
  }

  toPreviousPage() {
    this.routerService.toPreviousPage();
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
