import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

// import { BaseUrlService } from '../shared/services/baseUrl.service';
// import { ItemService } from '../shared/services/item.service';

import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';

import { Item } from '../../models/item.model';
import { User } from '../../models/user.model';
import { VirtualMachine } from '../../models/vm.model';
import { Virtue } from '../../models/virtue.model';
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

export class GenericFormTab implements OnInit {

  // what the user is doing to the item: {CREATE, EDIT, DUPLICATE}
  // Holds the strings 'Create', 'Edit', or 'Duplicate' resp., for display to the user
  mode: Mode;

  tabName: string;

  item: Item;

  constructor( protected router: Router, protected dialog: MatDialog, tabName: string, mode: Mode) {
    // gets overwritten once the datasets load, if mode is EDIT or DUPLICATE
    this.item = new VirtualMachine(undefined);

    this.mode = mode;

    this.tabName = tabName;
  }

  ngOnInit() {}

  viewItem(i: Item) {
    if (i.getDomain()) {
      this.router.navigate([i.getDomain()]);
    }
  }

  getChildNamesHtml( item: Item) {
    return item.childNamesHTML;
  }

  // try making these on the fly. Might not be that slow.
  getGrandchildrenHtmlList(i: Item): string {
    let grandchildrenHTMLList: string = "";
    for (let c of i.children.asList()) {
      grandchildrenHTMLList += c.childNamesHTML;
    }
    return grandchildrenHTMLList;
  }

  // used by many children to display their status
  formatStatus( item: Item ): string {
    return item.enabled ? 'Enabled' : 'Disabled';
  }

  // in here, at least pass in item, mode, and tabName.
  setUp(param: {tabName: String, item: Item, mode: Mode, otherData: any}) {}

  //this should ensure that the previously-passed in item has been updated however
  //it needed to be based on any changes within that tab. Called when the item is
  //saved, not when a different tab is clicked.
  tearDown(): any {}
}
