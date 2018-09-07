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
import { Mode, ConfigUrlEnum } from '../../enums/enums';
import { RowOptions } from '../../models/rowOptions.model';


@Component({
  selector: 'app-tab',
  template: './gen-tab.component.html',
  // styleUrls: ['../shared/abstracts/gen-list/gen-list.component.css']//,
  // providers: [ BaseUrlService, ItemService ]
})

export abstract class GenericFormTabComponent implements OnInit {

  // what the user is doing to the item: {CREATE, EDIT, DUPLICATE, VIEW}
  // Holds the strings 'Create', 'Edit', 'Duplicate', or 'View' resp., for display to the user
  protected mode: Mode;

  public tabName: string;

  // this gets overriden by children tabs
  protected item: Item;

  constructor(
    protected router: Router,
    protected dialog: MatDialog) {
    // gets overwritten once the datasets load, if mode is not CREATE
    // Application chosen arbitrarily for this un-used value, because it doesn't
    // have as many attributes.
    this.item = new User(undefined);
    // this doesn't appear to be used anywhere. So redefining it in each subclass,
    // with the most correct type.

  }

  ngOnInit() {}

  viewItem(i: Item) {
    this.router.navigate([i.getPageRoute(Mode.VIEW)]);
  }

  // this is now just done on the fly. Seems like a waste to regenerate the same
  // list in html every mouse movement, but was necessary to let children and
  // grandchildren be click-able.
  getGrandchildren(i: Item): Item[] {
    // if the item has been saved to the backend
    if (!this.item || !this.item.children) {
      return [];
    }
    let grandchildren: Item[] = [];
    for (let c of i.children.asList()) {
      grandchildren = grandchildren.concat(c.children.asList());
    }
    return grandchildren;
  }

  // try making these on the fly. Might not be that slow.
  getChildren(i: Item): Item[] {
    // if the item has been saved to the backend
    if (!this.item || !this.item.children) {
      return [];
    }
    return i.children.asList();
  }

  // used by many children to display their status
  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  // used by many children to display their status
  formatName( item: Item ): string {
    return item.getName();
  }

  // called by parent's constructor
  abstract setUp(mode: Mode, item: Item): void;

  abstract init(): void;

  abstract update(changes: any): void;

  // called when item is being saved, to set any disconnected fields as necessary
  // return false if the data that needs to be collected isn't available/valid/finished/applied
  abstract collectData(): boolean;



}
