import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
// import { MatTabsModule } from '@angular/material/tabs';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { RouterService } from '../../../shared/services/router.service';

import { Item } from '../../../shared/models/item.model';
import { IndexedObj } from '../../../shared/models/indexedObj.model';
import { User } from '../../../shared/models/user.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { VirtueInstance } from '../../../shared/models/virtue-instance.model';
import { VirtualMachineInstance } from '../../../shared/models/vm-instance.model';
import { DictList } from '../../../shared/models/dictionary.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { ItemFormTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component';

/**
* @class
 *
 * This contains a sorta-hack that should be done away with given enough time
 *  - It's due to problem where some tabs don't request data, while others can, and increasingly it's seeming like I should have made all
 *    pages data-pages. It will be non-trivial to add to the form pages, but makes things much easier in their tabs.
 *    Here, we need to call a function that stops a giving virtue, but that function is only available to the parent form tab.
 *    So it gets passed in.
 *
 *
 * @extends [[ItemFormTabComponent]]
 */
@Component({
  selector: 'app-activity-tab',
  templateUrl: './activity-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component.css']
})
export class ActivityTabComponent extends ItemFormTabComponent implements OnInit {

  stopVirtueFunction: (v: VirtueInstance, terminate?: boolean) => void;

  @ViewChild('table') private table: GenericTableComponent<Number>;

  /** re-classing item, to make it easier and less error-prone to work with. */
  protected item: User;

  /**
   * see [[ItemFormTabComponent.constructor]] for inherited parameters
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog) {
    super(routerService, dialog);
    this.tabName = "Active Virtues";

  }

  /**
   * See [[ItemFormTabComponent.init]] for generic info
   *
   * @param mode the [[Mode]] to set up the page in.
   */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpTable();
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
   *
   * @param item a reference to the Item being viewed/edited in the [[VirtueComponent]] parent
   */
  setUp(item: Item): void {
    if ( !(item instanceof User) ) {
      return;
    }
    this.item = item as User;
  }

  hasActiveVirtues(): boolean {
    return this.item && this.item.getActiveVirtues().length > 0;
  }

  /**
   * See [[ItemFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well the contents of [[parentTable]]
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated, and/or an attribute `allUsers: DictList<Item>`
   *                if parentTable is to be updated.
   *                Either attribute is optional.
   */
  update(changes: any): void {
    if (changes.mode) {
      this.setMode(changes.mode);
      this.setUpTable();
    }

    if (changes.stopVirtueFunc) {
      this.stopVirtueFunction = changes.stopVirtueFunc;
    }

    this.table.populate(this.item.getActiveVirtues());
  }

  stopVirtue(v: VirtueInstance, terminate: boolean = false): void {
    if (this.stopVirtueFunction) {
      return this.stopVirtueFunction(v, terminate);
    }
    else {
      console.log('couldn\'t stop virtue');
    }
  }

  /**
   * @return what columns should show up in [[parentTable]]
   *         Links to the parent and the parent's children should only be clickable if not in view mode.
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Template Name', 2, (v: VirtueInstance) => v.getName(), SORT_DIR.ASC,
                                              (v: VirtueInstance) => this.toDetailsPage(v), () => this.getSubMenu()),
      new TextColumn('User',         2, (v: VirtueInstance) => v.user,       SORT_DIR.ASC),
      new ListColumn('Active VMs',  2, (v: VirtueInstance) => v.getVms(),  this.formatName,
                                            (vm: VirtualMachineInstance) => this.viewVmInstance(vm)),
      new TextColumn('State',            1,  (v: VirtueInstance) => v.readableState)
    ];
  }

  getSubMenu(): SubMenuOptions[] {
    return [
      new SubMenuOptions("Stop",   (v: VirtueInstance) => !v.isStopped(), (v: VirtueInstance) => this.stopVirtue(v)),
    ];
  }

  viewVmInstance(vm): void {
    this.toDetailsPage(new VirtualMachineInstance(vm));
  }

  /**
   * Sets up the table of parents
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpTable(): void {
    this.table.setUp({
      cols: this.getColumns(),
      tableWidth: 0.66,
      noDataMsg: "This user has no running Virtues.",
      editingEnabled: () => !this.inViewMode()
    });
  }

  /*
   * #TODO This is where I left off on VRTU-629-add-virtue-controls-to-workbench
   * see also: UserComponent and GenericDataPageComponent
   */
  stopActive(): void {
    console.log("This function untested, ticket unfinished");
    for (let v of this.item.getActiveVirtues()) {
      this.stopVirtue(v);
    }
  }

  terminateActive(): void {
    console.log("This function untested, ticket unfinished");
    for (let v of this.item.getActiveVirtues()) {
      this.stopVirtue(v, true);
    }
  }

  /**
   * Do nothing at the moment - nothing about item can be changed from this tab
   *
   * @return true
   */
  collectData(): boolean {
    return true;
  }

}
