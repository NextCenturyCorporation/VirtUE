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
import { Mode, ConfigUrls, Datasets } from '../../enums/enums';
import { RowOptions } from '../../models/rowOptions.model';

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

  /** #uncommented */
  // what the user is doing to the item: {CREATE, EDIT, DUPLICATE, VIEW}
  // Holds the strings 'Create', 'Edit', 'Duplicate', or 'View' resp., for display to the user
  protected mode: Mode;

  /** #uncommented */
  public tabName: string;

  /** #uncommented */
  // this gets overriden by children tabs
  protected item: Item;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  constructor(
    router: Router,
    protected dialog: MatDialog) {
      super(router);
      // gets overwritten by the parent form's 'item' once the datasets load, even if
      // it's in create mode.
      // User chosen arbitrarily for this un-used value, because it doesn't
      // have as many attributes.
      this.item = new User(undefined);
  }

  /**
   * Do nothing in particular on render.
   */
  ngOnInit(): void {}

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // called by parent's constructor
  abstract setUp(mode: Mode, item: Item): void;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  abstract init(): void;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  abstract update(changes: any): void;

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // called when item is being saved, to set any disconnected fields as necessary
  // return false if the data that needs to be collected isn't available/valid/finished/applied
  abstract collectData(): boolean;



}
