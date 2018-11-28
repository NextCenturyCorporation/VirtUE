import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { Observable } from 'rxjs/Observable';

import { RouterService } from '../../../services/router.service';

import { Item } from '../../../models/item.model';
import { DictList } from '../../../models/dictionary.model';
import { Column } from '../../../models/column.model';
import { Mode } from '../../../abstracts/gen-form/mode.enum';

import { DatasetNames } from '../../../abstracts/gen-data-page/datasetNames.enum';

import { DialogsComponent } from '../../../../dialogs/dialogs.component';

import { GenericFormTabComponent } from '../gen-form-tab.component';

/**
 * @class
 * This class represents a tab in a [[ItemFormComponent]].
 * Each tab should have a different, unified, focus, be organized so as to require
 * a minimal amount of scrolling, and should give some indication of unsaved changes.
 *
 * This family of classes all deal with displaying many pieces of a single, locally-held, object.
 * Any tab can change those pieces, and the central parent form [[ItemFormComponent]] just watches the
 * event emitters of each tab, to know when to reload the Item's data, using local (previously pulled) datasets.
 * E.g. if a User's virtueIds change, the form will use the new virtueIds to re-create item.virtues from the
 * same Virtue dataset that was pulled when the page first loaded.
 * These tabs aren't in charge of updating the backend themselves.
 *
 * Extension of [[GenericFormTabComponent]] - see the comments on all of that class' abstract methods.
 */
export abstract class ItemFormTabComponent extends GenericFormTabComponent implements OnInit {

  /**
   * what the user is doing to the item: {CREATE, EDIT, DUPLICATE, VIEW}
   * Should always be the same as [[ItemFormComponent.mode]]
   */
  protected mode: Mode;

  /**
   * @param dialog Injected. This is a pop-up for verifying irreversable user actions
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog) {
    super(routerService, dialog);
  }

  /**
   * This does whatever setup can be done at render time, before data is available. Called by containing component.
   * Usually, if the tab has a table this is where table.setUp({...}) would be called.
   *
   * Re-definition of [[GenericFormTabComponent.init]])(), to specify input type.
   * @param mode the mode to set up this tab as being in. Must be passed in.
   */
  abstract init(mode: Mode): void;

  /**
   * This finishes setting up the page, once all data requested by the parent form has returned and
   * [[ItemFormComponent.item]] has been given a value.
   *
   * Called in [[ItemFormComponent.onPullComplete]]
   * Re-definition of [[GenericFormTabComponent.setUp]])(), to specify input type.
   *
   * @param item a reference to [[ItemFormComponent.item]], the thing being viewed/edited/etc.
   */
  abstract setUp(item: Item): void;

  /**
   * Sets the tab's mode.
   * Overridden by most tabs that show a table, to re-define it based on whether the page is in view mode.
   *
   * @param newMode the Mode to set the page as.
   */
  setMode(newMode: Mode): void {
    this.mode = newMode;
  }

  /**
   * @return true iff the page is in view mode.
   * Used in the html to prevent some types of action when the page is in 'View' mode.
   */
  inViewMode(): boolean {
    return this.mode === Mode.VIEW;
  }

  /**
   * @return true iff the page is in EDIT mode.
   * Used in the html to prevent some actions while in EDIT mode.
   */
  inEditMode(): boolean {
    return this.mode === Mode.EDIT;
  }

  /**
   * @return true iff the page is in CREATE mode.
   * Used in the html to change how the page gets displayed in CREATE mode.
   */
  inCreateMode(): boolean {
    return this.mode === Mode.CREATE;
  }

  /**
   * @return true iff the page is in DUPLICATE mode.
   * Used in the html to change how the page gets displayed in DUPLICATE mode.
   */
  inDuplicateMode(): boolean {
    return this.mode === Mode.DUPLICATE;
  }

  /**
  * Called when item is being saved, to pull in and set any disconnected fields and check the data in its sphere
  * for validity.
  *
  * @return false if the data that needs to be collected isn't available/valid/finished/applied
  */
  abstract collectData(): boolean;

}
