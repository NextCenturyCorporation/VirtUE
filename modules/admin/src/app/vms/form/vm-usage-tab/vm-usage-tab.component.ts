import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { MatTabsModule } from '@angular/material/tabs';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../../dialogs/dialogs.component';

import { IndexedObj } from '../../../shared/models/indexedObj.model';
import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { VirtualMachineInstance } from '../../../shared/models/vm-instance.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { VirtueInstance } from '../../../shared/models/virtue-instance.model';
import { DictList } from '../../../shared/models/dictionary.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';

import { RouterService } from '../../../shared/services/router.service';

import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { ItemFormTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component';

/**
* @class
 * This class represents a tab in [[VmComponent]], listing places this VM template has been used
 *
 * It holds two tables:
 *    - Virtues that have been assigned this template
 *    - Running VMs that have been built from this template (currently unimplemented)
 *
 * @extends [[ItemFormTabComponent]]
 */
@Component({
  selector: 'app-vm-usage-tab',
  templateUrl: './vm-usage-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component.css']
})
export class VmUsageTabComponent extends ItemFormTabComponent implements OnInit {

  /** A table listing what virtues have been given access to this VM template */
  @ViewChild('parentTable') private parentTable: GenericTableComponent<Virtue>;

  /**
   * this would show the running virtues that have been built from this template.
   * This may be unnecessary/unteneble. It could be a lot.
   * Tables may need filters.
   * Change any to the correct type (probably "VirtueInstance" or something)
   */
  @ViewChild('instanceTable') private instanceTable: GenericTableComponent<any>;

  /** re-classing item, to make it easier and less error-prone to work with. */
  protected item: VirtualMachine;

  /**
   * see [[ItemFormTabComponent.constructor]] for inherited parameters
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog) {
    super(routerService, dialog);
    this.tabName = "Virtual Machine Usage";

  }

  /**
   * See [[ItemFormTabComponent.init]] for generic info
   *
   * @param mode the [[Mode]] to set up the page in.
   */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpParentTable();
    this.setUpInstanceTable();
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
   *
   * @param item a reference to the Item being viewed/edited in the [[VmComponent]] parent
   */
  setUp(item: Item): void {
    if ( !(item instanceof VirtualMachine) ) {
      // TODO throw error
      console.log("item passed to vm-usage-tab which was not a VirtualMachine: ", item);
      return;
    }
    this.item = item as VirtualMachine;
  }

  /**
   * See [[ItemFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well the contents of [[parentTable]]
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated, and/or an attribute `allVirtues: DictList<Item>`
   *                if parentTable is to be updated.
   *                Either attribute is optional.
   */
  update(changes: any): void {
    if (changes[DatasetNames.VIRTUE_TS]) {
      let allVirtues: DictList<Item> = changes[DatasetNames.VIRTUE_TS];
      let parents: Item[] = [];

      for (let v of allVirtues.asList()) {
        if ((v as Virtue).getVmTemplates().has(this.item.getID())) {
         parents.push(v);
        }
      }
      this.parentTable.populate(parents);
    }

    // Can't be implemented yet because VM instances don't come with a record of the template that was used to make them
    // if (changes[DatasetNames.VMS]) {
    //   let items: IndexedObj[] = [];
    //   let vms: DictList<VirtualMachineInstance> = changes[DatasetNames.VMS];
    //
    //   for (let vm of vms.asList()) {
    //     if (vm.templateId === this.item.getID()) {
    //       items.push(vm);
    //     }
    //   }
    //   this.instanceTable.populate(items);
    // }

    if (changes.mode) {
      this.setMode(changes.mode);
      this.setUpParentTable();
    }
  }

  /**
   * @return what columns should show up in [[parentTable]]
   *         Links to the parent and the parent's children should only be clickable if not in view mode.
   */
  getParentColumns(): Column[] {
    return [
      new TextColumn('Template Name',     3, (v: Virtue) => v.getName(), SORT_DIR.ASC, (v: Virtue) => this.viewItem(v),
                                                                                            () => this.getParentSubMenu()),
      new ListColumn('Virtual Machines',  3, (v: Virtue) => v.getVms(),  this.formatName, (vm: VirtualMachine) => this.viewItem(vm)),
      new TextColumn('Version',           2, (v: Virtue) => String(v.version), SORT_DIR.ASC),
      new TextColumn('Status',            3, this.formatStatus, SORT_DIR.ASC)
    ];
  }

  /**
   * @return a list of links to show up as a submenu on each parent. Links are to edit the parent, or
   *         view the parent. Only show this list if page is in view mode.
   */
  getParentSubMenu(): SubMenuOptions[] {
    if (this.mode === Mode.VIEW) {
      return [
        new SubMenuOptions("View", () => this.inViewMode(), (v: Virtue) => this.viewItem(v)),
        new SubMenuOptions("Edit", () => this.inViewMode(), (v: Virtue) => this.editItem(v))
      ];
    }
    else {
      return [];
    }
  }

  /**
   * Sets up the table of parents.
   *
   * Would be nicer if refactored the way gen-main-tab has the setUpChildTable function.
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpParentTable(): void {
    this.parentTable.setUp({
      cols: this.getParentColumns(),
      // opts: this.getParentSubMenu(),
      coloredLabels: true,
      getColor: (v: Virtue) => v.color,
      tableWidth: 0.66,
      noDataMsg: "No virtue template has been assigned this virtual machine template at the moment.",
      elementIsDisabled: (v: Virtue) => !v.enabled,
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    });
  }
  /**
   * @return what columns should show up in [[parentTable]]
   *         Links to the parent and the parent's children should only be clickable if not in view mode.
   */
  getInstanceColumns(): Column[] {
    return [
      new TextColumn('Template Name',     3, (v: Virtue) => v.getName(), SORT_DIR.ASC, (v: Virtue) => this.viewItem(v),
                                                                                            () => this.getParentSubMenu()),
      new ListColumn('Virtual Machines',  3, (v: Virtue) => v.getVms(),  this.formatName, (vm: VirtualMachine) => this.viewItem(vm)),
      new TextColumn('Version',           2, (v: Virtue) => String(v.version), SORT_DIR.ASC),
      new TextColumn('Status',            3, this.formatStatus, SORT_DIR.ASC)
    ];
  }

  /**
   * @return a list of links to show up as a submenu on each parent. Links are to edit the parent, or
   *         view the parent. Only show this list if page is in view mode.
   */
  getInstanceSubMenu(): SubMenuOptions[] {
    if (this.mode === Mode.VIEW) {
      return [
        new SubMenuOptions("View", () => this.inViewMode(), (v: Virtue) => this.viewItem(v)),
        new SubMenuOptions("Edit", () => this.inViewMode(), (v: Virtue) => this.editItem(v))
      ];
    }
    else {
      return [];
    }
  }

  /**
   * Sets up the table of parents
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpInstanceTable(): void {
    this.instanceTable.setUp({
      cols: this.getInstanceColumns(),
      tableWidth: 0.66,
      noDataMsg: "Not yet implemented",
      editingEnabled: () => !this.inViewMode()
    });
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
