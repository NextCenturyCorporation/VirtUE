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

import { Item } from '../../models/item.model';

import { ColorSet } from '../../sets/color.set';

import { Mode, ConfigUrls, Datasets } from '../../enums/enums';


/**
* @class
 * This is the generic class which provides a central source for many common functions, currently:
 *  - routing
 *  - dialogs
 *  - formatting
 */
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
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;

    // make the page reload if the user clicks on a link to the same page they're on.
    this.router.navigated = false;

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
   * Used in pages with tables of items.
   * @param item the item whose status we want to display
   *
   * @return The item's status, in plain (and capitalized) english.
   */
  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  /**
  * This is used in many tables to show an [[Item]]'s children. All lists in tables are gotten by passing
  * an item to a function that has the scope of the object containing and defining the table.
  *
  * @param item the [[Item]] whose children we want a list of
  *
  * @return a list of the Item's children.
  */
  getChildren(item: Item): Item[] {
    if (!item) {
      return [];
    }
    return item.children.asList();
  }

  /**
   * The following two functions are used in tables to display the items attached
   * to an [[Item]] (children) and the items attched to each of those (grandchildren).
   * It used to be all generated (as an html string) whenever an item was created
   * or its children updated, but now is doen on the fly. These functions get
   * re-called whenever the mouse enters or leaves a row in the table, so this
   * seems like a bit of a waste, given that an item's children/grandchildren won't
   * change randomly in the background.
   * Doing it this way allows us to make the items within those lists clickable.
   *
   * @param item the item whose grandchildren we want a list of.
   *
   * @return A list (not a set) of all the item's children's children.
  */
  getGrandchildren(item: Item): Item[] {
    if (!item || !item.children) {
      return [];
    }
    let grandchildren: Item[] = [];
    for (let c of item.children.asList()) {
      grandchildren = grandchildren.concat(c.children.asList());
    }
    return grandchildren;
  }

  /**
   * Navigates to the form page for this item.
   *
   * @param item the Item to which we should navigate.
   */
  viewItem(item: Item): void {
    this.router.navigate([item.getPageRoute(Mode.VIEW)]);
  }

  /**
   * Navigates to and enable for editing form page for `item`.
   *
   * @param item the Item which we should navigate to and edit.
   */
  editItem(item: Item): void {
    this.router.navigate([item.getPageRoute(Mode.EDIT)]);
  }

  /**
   * Navigates to a form page pre-filled with `item`'s attributes.
   *
   * @param item the Item to duplicate
   */
  dupItem(item: Item): void {
    this.router.navigate([item.getPageRoute(Mode.DUPLICATE)]);
  }

  /**
   * A generic method for navigating to some input page.
   * Abstracts away the router from most(!!) sub classes #TODO
   *
   * @param targetPath the path to navigate to.
   */
  goToPage(targetPath: string) {
    this.router.navigate([targetPath]);
  }


  /**
   * This opens a dialog to confirm irreversible or dangerous user actions before carrying them out.
   *
   * @param action what's being done: e.g. 'delete', 'disable'
   * @param target the item upon which the stated action would be performed.
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

    //  control goes here after either "Ok" or "Cancel" are clicked on the dialog
    let sub = dialogRef.componentInstance.getResponse.subscribe((shouldProceed) => {
      console.log(shouldProceed);
      if (shouldProceed) {
        action();
      }
    },
    ()=>{},
    ()=>{
      sub.unsubscribe();
    });
  }
}
