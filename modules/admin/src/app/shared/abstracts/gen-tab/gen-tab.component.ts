import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { Item } from '../../models/item.model';
import { User } from '../../models/user.model';
import { Virtue } from '../../models/virtue.model';
import { VirtualMachine } from '../../models/vm.model';
import { Application } from '../../models/application.model';
import { DictList } from '../../models/dictionary.model';
import { Column } from '../../models/column.model';
import { Mode } from '../../abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../services/config-urls.enum';
import { Datasets } from '../../abstracts/gen-data-page/datasets.enum';

import { GenericPageComponent } from '../gen-page/gen-page.component';

/**
 * @class
 * This class represents a tab in a [[GenericFormComponent]].
 * Each tab should have a different, unified, focus, be organized so as to require
 * a minimal amount of scrolling, and
 *
 * @extends [[GenericPageComponent]] to make use of its formatting and navigation functions
 *
 */
@Component({
  selector: 'app-tab',
  template: './gen-tab.component.html'
})

export abstract class GenericFormTabComponent extends GenericPageComponent implements OnInit {

  /**
   * what the user is doing to the item: {CREATE, EDIT, DUPLICATE, VIEW}
   * Should always be the same as [[GenericFormComponent.mode]]
   */
  protected mode: Mode;

  /** The label to appear on the tab */
  public tabName: string;


  /**
   * @param dialog Injected. This is a pop-up for verifying irreversable user actions
   */
  constructor(
    router: Router,
    dialog: MatDialog) {
      super(router, dialog);
      // gets overwritten by the parent form's 'item' once the datasets load, even if
      // it's in create mode.
      // User chosen arbitrarily for this un-used value, because it doesn't
      // have as many attributes.
  }

  /**
   * Do nothing in particular on render.
   */
  ngOnInit(): void {}

  /**
   * This does whatever setup can be done at render time, before data is available.
   * Usually, if the tab has a table this is where table.setUp({...}) would be called.
   *
   * #TODO itemCopy, a copy of item holding only the things which are to be saved (meaning not children), which can
   *  be compareed against every field and show an exclaimation mark if changes have been made.
   * use onChange method? or does that only happen on enter, for text fields?
   *
   * Only enable save button if any fields are marked as edited?
   *
   * show red exclaimation mark on tab with changed, unsaved data
   * @param mode the mode to set up this tab as being in. Must be passed in.
   */
  abstract init(mode: Mode): void;

  /**
   * This finishes setting up the page, once all data requested by the parent form has returned and
   * [[item]] has been given a value.
   *
   * Called in [[GenericFormComponent.onPullComplete]]
   *
   * Ideally this method should be safe to call multiple times with different inputs, idempotent, and sufficient based
   * on those inputs. Meaning that if someone calls `setUp(input1)`, some misc. other functions, and then calls `setUp(input2)`,
   *  the page must be in the same state as if they had just initialized this object and called `setUp(input2)`
   * immediately.
   *
   * @param item a reference to [[GenericFormComponent.item]], the thing being viewed/edited/etc.
   */
  abstract setUp(item: Item): void;

  /**
   * This should be sufficient to update any piece of data on the page.
   * Generally it's used for changing a tab's mode and updating the entries in tables.
   *
   * @param changes an unspecified object which each derived class may need to get different information out of,
   *                including different numbers of attributes at different times.
   */
  abstract update(changes: any): void;

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
   * Used when defining most tab's tables, to prevent some types of action when the page is in 'View' mode.
   */
  inViewMode(): boolean {
    return this.mode === Mode.VIEW;
  }

  /**
  * Called when item is being saved, to pull in and set any disconnected fields and check the data in its sphere
  * for validity.
  *
  * @return false if the data that needs to be collected isn't available/valid/finished/applied
  */
  abstract collectData(): boolean;

}
