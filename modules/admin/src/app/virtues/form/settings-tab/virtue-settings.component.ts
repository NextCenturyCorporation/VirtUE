import { Component, Input, ViewChild, ElementRef, OnInit, Injectable, EventEmitter, Output } from '@angular/core';
import {  MatButtonModule,
          MatDialog,
          MatIconModule
} from '@angular/material';


import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';

import {
  Column,
  BlankColumn,
  TextColumn,
  ListColumn,
  CheckboxColumn,
  DropdownColumn,
  InputFieldColumn,
  IconColumn,
  RadioButtonColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { RouterService } from '../../../shared/services/router.service';

import { NetworkPermission } from '../../../shared/models/networkPerm.model';
import { FileSystem } from '../../../shared/models/fileSystem.model';
import { Printer } from '../../../shared/models/printer.model';
import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { PrinterSelectionModalComponent } from '../../../modals/printer-modal/printer-selection.modal';
import { FileSystemSelectionModalComponent } from '../../../modals/fileSystem-modal/fileSystem-selection.modal';
import { NetworkPermissionModalComponent } from '../../../modals/networkPerm-modal/networkPerm.modal';

import { ColorModalComponent } from '../../../modals/color-picker/color-picker.modal';
import { VirtueModalComponent } from '../../../modals/virtue-modal/virtue-modal.component';
import { ItemFormTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component';

import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';
import { SelectionMode } from '../../../shared/abstracts/gen-table/selectionMode.enum';

import { NetworkProtocols } from '../../protocols.enum';

/**
 * Temporary, just for showcasing the radio button column
 */
class Sensor {
  public status: string;
  constructor (
    public name: string,
    public level: VigilenceLevel
  ) { }
}

/**
 * This is also temporary, but will probably eventually just get moved to its own file.
 */
enum VigilenceLevel {
  OFF = "off",
  DEFAULT = "default",
  LOW = "low",
  HIGH = "high",
  ADVERSARIAL = "adversarial"
}

/**
 * @class
 *
 * This class represents  a tab in [[VirtueComponent]], describing all the settings a virtue can be set to have.
 *
 * This has four sub-tabs at the moment:
 *    - 'General'
 *        - Color
 *        - default browser
 *        - provisioned or not (?) #TODO
 *        - list of virtues this one can paste data into
 *    - 'Network'
 *        - list of permitted connections
 *    - 'Resources'
 *        - file system permissions
 *            - editable list, generated from global settings #TODO
 *            - for each file system, R/W/E permissions can be given.
 *        - Printers
 *            - editable list, pulled from global settings.
 *    - 'Sensors'
 *        - Nothing at the moment #TODO
 *
 * At the moment, only 'color' is saved to the backend.
 *
 * @extends [[ItemFormTabComponent]]
 */
@Component({
  selector: 'app-virtue-settings-tab',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsTabComponent extends ItemFormTabComponent implements OnInit {

  /** the Virtues that this Virtue is allowd to paste data into */
  @ViewChild('allowedPasteTargetsTable') private allowedPasteTargetsTable: GenericTableComponent<Virtue>;

  /** the network permissions granted to this Virtue */
  @ViewChild('networkPermsTable') private networkPermsTable: GenericTableComponent<NetworkPermission>;

  /** the file system permissions of this Virtue */
  @ViewChild('fileSystemsPermsTable') private fileSystemsPermsTable: GenericTableComponent<FileSystem>;

  /** the printers this Virtue can access */
  @ViewChild('printerTable') private printerTable: GenericTableComponent<Printer>;

  @ViewChild('sensorTable') private sensorTable: GenericTableComponent<Printer>;

  /** re-classing item, to make it easier and less error-prone to work with.
  * Must be public to be used in template html file in production mode.
  */
 public item: Virtue;

  /** to notify the parent item form that one of this.item's lists of child ids has been changed */
  @Output() onChildrenChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  /**
   * see [[ItemFormTabComponent.constructor]] for inherited parameters
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog) {
    super(routerService, dialog);

    this.item = new Virtue();

    this.tabName = 'Settings';
  }

  /**
   * See [[ItemFormTabComponent.init]] for generic info
   *
   * @param mode the [[Mode]] to set up the page in.
   */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpPasteableVirtuesTable();
    this.setUpNetworkPermsTable();
    this.setUpFileSysPermsTable();
    this.setUpPrinterTable();
    this.setUpSensorTable();
    // until GenericTable is made more generic (like for any input object, as opposed to only Items),
    // the other tables have to be defined individually in the html.
    // GenericTable would need to allow arbitrary objects/html in any column - so one could just as
    // easily display a line of text in one column, and checkboxes in the next three.
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
   *
   * @param item a reference to the Item being viewed/edited in the [[VirtueComponent]] parent
   */
  setUp(item: Item): void {
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-settings which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;
  }

  /**
   * See [[ItemFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well its allowedPasteTargetsTable
   *
   * This re-builds some parts of the table upon mode update, to allow the columns and row options to change dynamically
   * upon mode change.
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated, and/or an attribute `allVirtues: DictList<Item>`
   *                if the paste-permission table is to be updated.
   *                Either attribute is optional.
   */
  update(changes?: any): void {
    if (changes) {
      if (changes[DatasetNames.VIRTUE_TS]) {
        this.updatePasteableVirtuesTable(changes[DatasetNames.VIRTUE_TS]);
      }

      if (changes.mode) {
        this.setMode(changes.mode);
        this.setUpPasteableVirtuesTable();
      }
    }

    // if (changes.printers) {
    //   // TODO update something
    // }
    this.updateNetworkPermsTable();
    this.updateFileSysPermsTable();
    this.updatePrinterTable();

    // temporary hard-coding
    this.sensorTable.populate([ new Sensor("In-resource (Unikernel)", VigilenceLevel.OFF),
                              new Sensor("In-Virtue Controller", VigilenceLevel.OFF),
                              new Sensor("Logging - Aggregate", VigilenceLevel.OFF),
                              new Sensor("Logging - Archive", VigilenceLevel.OFF),
                              new Sensor("Certificates Infrastructure", VigilenceLevel.OFF)]);
  }

  /**
   * At the moment there's nothing to pull in, everything gets set directly on the Virtue item.
   * But we will need to add checks eventually, to ensure everything is valid. #TODO
   *
   * Maybe make that network field be 4 subfields? #TODO
   *
   * @return true if all network permission fields have been filled out
   */
  collectData(): boolean {
    // check all entries for validity before allowing save

    if (!this.checkNetworkPerms()) {
      // TODO tell the user
      return false;
    }
    return true;
  }

  /**
   * See https://github.com/angular/angular/issues/10423 and/or https://stackoverflow.com/questions/40314732
   * and, less helpfully, the actual docs: https://angular.io/guide/template-syntax#ngfor-with-trackby
   * Essentially, Angular's ngFor sometimes tracks the elements it iterates over by their value (?), as opposed
   * to their index, and so if you put a ngModel on (apparently) any part of an element within an ngFor, it loses track (??) of
   * that item and hangs - as in, Angular hangs. And the containing tab needs to be killed either through the browser's
   * tab manager, or the browser needs to be killed at the system level (xkill, system process manager, kill, killall, etc.).
   * This is prevented by manually telling it track things by index, using the below code, and adding "; trackBy: indexTracker" to
   * the end of the offending ngFor statement.
   * ...
   *
   * @param index an index, automagically passed in.
   * @param value a value, automagically passed in.
   * @return the index that was passed in.
   */
  indexTracker(index: number, value: any): number {
    return index;
  }

  /************************************************************************************/
  /************************************************************************************/

  /**
   * Note that this is done in the style of a GenericTable, but doesn't actually use one, because
   * these things aren't Items. Generalizing the GenericTable further would be useful.
   * @return what columns should show up in the network permissions table
   */
  getPrinterColumns(): Column[] {
    return [
      new TextColumn('Printer Name',    6, (p: Printer) => p.name, SORT_DIR.ASC),
      new TextColumn('Printer Status',  4, (p: Printer) => p.status, SORT_DIR.ASC),
      new IconColumn('Revoke access',  2, 'delete', (p: Printer) => this.removePrinter(p)
      )
    ];
  }

  /**
  * Sets up the table describing what Virtues this Virtue is allowed to paste data into.
  *
  * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
  */
  setUpPrinterTable(): void {
    if (this.printerTable === undefined) {
      return;
    }

    this.printerTable.setUp({
      cols: this.getPrinterColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "No printers have been added yet to this Virtue.",
      elementIsDisabled: (p: Printer) => !p.enabled,
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  updatePrinterTable() {
    if (this.printerTable === undefined) {
      return;
    }
    this.printerTable.populate(this.item.printers.asList());
  }


  /**
   * This removes a printer from the Virtue's printer list, using the printer's address as an ID.
   * Note that if there are several matching printers, only the first one is removed.
   */
  removePrinter(toDelete: Printer): void {
    this.item.removePrinter(toDelete);
    this.updatePrinterTable();
  }


  /**
   * this brings up the subclass-defined-modal to add/remove children.
   *
   * Note the distinction between this and DialogsComponent;
   * This pops up to display options, or a selectable table, or something. DialogsComponent just checks
   * potentially dangerous user actions.
   */
  activatePrinterModal(): void {

    let params = {
      height: '70%',
      width: '70%',
      data: {
        selectedIDs: this.item.getRelatedIDList(DatasetNames.PRINTERS)
      }
    };

    let dialogRef = this.dialog.open( PrinterSelectionModalComponent, params);

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedIds) => {
      this.item.printerIds = selectedIds;
      this.onChildrenChange.emit();
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%' });

  }

/************************************************************************************/
/************************************************************************************/

  updateFileSysPermsTable(): void {
    if (this.fileSystemsPermsTable === undefined) {
      return;
    }
    this.fileSystemsPermsTable.populate(this.item.fileSystems.asList());
  }

  /**
   * Note that this is done in the style of a GenericTable, but doesn't actually use one, because
   * these things aren't Items. Generalizing the GenericTable further would be useful.
   * @return what columns should show up in the network permissions table
   */
  getFileSysColumns(): Column[] {
    return [
      new TextColumn('Server & Drive',  3, (fs: FileSystem) => fs.name, SORT_DIR.ASC),
      new TextColumn('Address',         3, (fs: FileSystem) => fs.address, SORT_DIR.ASC),
      new CheckboxColumn('Enabled',     2, 'enabled'),
      new CheckboxColumn('Read',        1, 'readPerm'),
      new CheckboxColumn('Write',       1, 'writePerm'),
      new CheckboxColumn('Execute',     1, 'executePerm'),
      new IconColumn('Revoke access',   1, 'delete', (fs: FileSystem) => this.removeFileSystem(fs))
    ];
  }

  /**
   * Sets up the table describing what Virtues this Virtue is allowed to paste data into.
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpFileSysPermsTable(): void {
    if (this.fileSystemsPermsTable === undefined) {
      return;
    }
    this.fileSystemsPermsTable.setUp({
      cols: this.getFileSysColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "No file systems have been set up in the global settings",
      elementIsDisabled: (fs: FileSystem) => !fs.enabled,
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  removeFileSystem(toDelete: FileSystem): void {
    this.item.removeFileSystem(toDelete);
    this.updateFileSysPermsTable();
  }

  /**
   * this brings up the subclass-defined-modal to add/remove children.
   *
   * Note the distinction between this and DialogsComponent;
   * This pops up to display options, or a selectable table, or something. DialogsComponent just checks
   * potentially dangerous user actions.
   */
  activateFileSystemModal(): void {

    let params = {
      height: '70%',
      width: '70%',
      data: {
        selectedIDs: this.item.getRelatedIDList(DatasetNames.FILE_SYSTEMS)
      }
    };

    let dialogRef = this.dialog.open( FileSystemSelectionModalComponent, params);

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedIds) => {
      this.item.fileSystemIds = selectedIds;
      this.onChildrenChange.emit();
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%' });

  }
/************************************************************************************/
/************************************************************************************/

  /**
   * Note that this is done in the style of a GenericTable, but doesn't actually use one, because
   * these things aren't Items. Generalizing the GenericTable further would be useful.
   * @return what columns should show up in the network permissions table
   */
  getNetworkColumns(): Column[] {
    return [
      new TextColumn('Direction',   1, (netPerm: NetworkPermission) => this.getDirection(netPerm), SORT_DIR.ASC),
      new TextColumn('CIDR IP',     2, (netPerm: NetworkPermission) => netPerm.cidrIp, SORT_DIR.ASC),
      new TextColumn('Protocol',    1, (netPerm: NetworkPermission) => this.formatIfBlank(netPerm.ipProtocol), SORT_DIR.ASC),
      new TextColumn('Port Range',   2, (netPerm: NetworkPermission) => this.formatPortRange(netPerm), SORT_DIR.ASC),
      new TextColumn('Description', 5, (netPerm: NetworkPermission) => netPerm.description, SORT_DIR.ASC),
      new IconColumn('Revoke',      1, 'delete', (netPerm: NetworkPermission) => this.removeNetwork(netPerm))
    ];
  }

  formatPortRange( netPerm: NetworkPermission ) {
    if (netPerm.fromPort === undefined && netPerm.toPort === undefined) {
      return "";
    }
    return netPerm.fromPort + " - " + netPerm.toPort;
  }

  formatIfBlank( value ) {
    if (value === undefined) {
      return "";
    }
    else {
      return String(value);
    }
  }

  getDirection(netPerm: NetworkPermission): string {
    return netPerm.ingress ? "Incoming" : "Outgoing";
  }

  /**
   * Sets up the table describing what Virtues this Virtue is allowed to paste data into.
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpNetworkPermsTable(): void {
    if (this.networkPermsTable === undefined) {
      return;
    }

    this.networkPermsTable.setUp({
      cols: this.getNetworkColumns(),
      filters: [],
      tableWidth: 0.95,
      noDataMsg: "This Virtue has not been granted permission to access any network",
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
  * Once data has been pulled, fill in the table with
  */
  updateNetworkPermsTable(): void {
    if (this.networkPermsTable === undefined) {
      return;
    }
    this.networkPermsTable.populate(this.item.networkSecurityPermWhitelist);
  }

  /**
  * Add a new netork permission to the virtue.
  */
  addNewNetworkPermission(newPermission: NetworkPermission): void {
    this.item.networkSecurityPermWhitelist.push(newPermission);
    this.item.newSecurityPermissions.push(newPermission);
    this.updateNetworkPermsTable();
  }

  activateNetworkPermissionModal(): void {
    /** Note: networkPermissions need to hold the id of their virtue template.
     * When creating or duplicating, that ID isn't given until after the object is saved.
     * Luckily (sort-of), networkPermissions can't be saved with the virtue, and must be saved separately.
     * Therefore, we have to:
     *    - load old permissions using this.item.getID(), if it's there
     *          (in case it's in duplicate mode - we want that template's permissions)
     *    - create the permissions without a templateID.
     *    - save the virtue
     *    - use the returned virtue object to get the correct templateID
     *    - save the items using the new templateID.
     *
     * Be mindful of changes.
     */
    let params = {
      height: '70%',
      width: '40%'
    };

    let dialogRef = this.dialog.open( NetworkPermissionModalComponent, params);

    let sub = dialogRef.componentInstance.getNetPerm.subscribe((newPerm) => {
      this.addNewNetworkPermission(newPerm);
      this.onChildrenChange.emit();
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%' });

  }

  /**
   * This removes a network from the virtue's whitelist.
   */
  removeNetwork(netPerm: NetworkPermission): void {
    if (this.item.networkSecurityPermWhitelist === undefined || this.item.networkSecurityPermWhitelist.length === 0) {
      return;
    }

    let idx = 0;
    for (let nP of this.item.networkSecurityPermWhitelist) {
      if (netPerm.equals(nP)) {
        break;
      }
      idx++;
    }
    let removedPermission = this.item.networkSecurityPermWhitelist.splice(idx, 1)[0];
    this.item.revokedSecurityPermissions.push(removedPermission);
    this.updateNetworkPermsTable();
  }

  /**
  * Iterate through and check all the network permissions
  * @return true if all lines of network permission are fully filled-out (and TODO valid).
  *         false otherwise
  */
  checkNetworkPerms(): boolean {
    for (let networkPermission of this.item.networkSecurityPermWhitelist) {
      if ( !networkPermission.checkValid() ) {
        return false;
      }
    }
    return true;
  }

/************************************************************************************/
/************************************************************************************/

  /**
   * See [[ItemListComponent.getSubMenu]] for more details on this sort
   * of method.
   *
   * @return Just a list with one option: to remove the attached Virtue from the list of ones this one can paste data into.
   */
  getPasteSubMenu(): SubMenuOptions[] {
    return [
       new SubMenuOptions("Remove", () => true, (i: Item) => {
         let index = this.item.allowedPasteTargetIds.indexOf(i.getID(), 0);
         if (index > -1) {
            this.item.allowedPasteTargetIds.splice(index, 1);
         }
         this.item.allowedPasteTargets.remove(i.getID());
         this.updatePasteableVirtuesTable();
       }
     )];
  }

  /**
   * @return what columns should show up in the virtue's paste-permission table
   *         The first column, the Virtue's name, should be clickable if and only if the page is in view mode.
   */
  getPasteColumns(): Column[] {
    return [
      new TextColumn('Template Name',  4, (v: Virtue) => v.getName(), SORT_DIR.ASC, (v: Virtue) => this.viewItem(v),
                                                                                        () => this.getPasteSubMenu()),
      new ListColumn('Available Applications', 4, (v: Virtue) => v.getVmApps(),  this.formatName),
      new TextColumn('Version',               2, (v: Virtue) => String(v.version), SORT_DIR.ASC),
      new TextColumn('Status',                1, this.formatStatus, SORT_DIR.ASC)
    ];
  }

  /**
  * Sets up the table describing what Virtues this Virtue is allowed to paste data into.
  *
  * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
  */
  setUpPasteableVirtuesTable(): void {
    if (this.allowedPasteTargetsTable === undefined) {
      return;
    }

    this.allowedPasteTargetsTable.setUp({
      cols: this.getPasteColumns(),
      coloredLabels: true,
      getColor: (v: Virtue) => v.color,
      filters: [],
      tableWidth: 0.85,
      noDataMsg: this.getNoPasteDataMsg(),
      elementIsDisabled: (v: Virtue) => !v.enabled,
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
   * Fill in the table with the Virtue's current allow paste targets, if it has any.
   * Use more recent definitions, if they are given.
   */
  updatePasteableVirtuesTable(allVirtues?: DictList<Virtue>): void {
    if (this.allowedPasteTargetsTable === undefined) {
      return;
    }

    let items: Item[] = this.item.allowedPasteTargets.asList();
    if (allVirtues) {
      items = allVirtues.getSubset(this.item.allowedPasteTargetIds).asList();
    }
    this.allowedPasteTargetsTable.populate(items);

  }

  /**
   * this brings up the modal to add/remove virtues that this Virtue has permission to paste data into.
   */
   activatePastableVirtueModal(): void {
     this.activateVirtueSelectionModal( this.item.allowedPasteTargetIds, (selectedVirtueIds: string[]) => {
         this.item.allowedPasteTargetIds = selectedVirtueIds;
         this.onChildrenChange.emit();
       });
   }
  /**
  * @return a message telling the user that no paste permissions have been given to this
  * virtue, in a way that is clear and relevant for the current mode.
  */
  getNoPasteDataMsg(): string {
    if (this.mode === Mode.VIEW) {
      return "This virtue hasn't been granted access to paste data into any other virtues.";
    }
    else {
      return 'Click "Add Virtue" to give this virtue permission to paste data into that one';
    }
  }

/************************************************************************************/

  setUpSensorTable(): void {
    if (this.sensorTable === undefined) {
      return;
    }
    this.sensorTable.setUp({
      cols: this.getSensorColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: "No sensors have been connected."
    });
  }

  getSensorColumns(): Column[] {
    return [
      new TextColumn("Sensor Context", 3, (s: Sensor) => s.name, SORT_DIR.ASC),
      new RadioButtonColumn("Off",          1, "level", VigilenceLevel.OFF),
      new RadioButtonColumn("Default",      1, "level", VigilenceLevel.DEFAULT),
      new RadioButtonColumn("Low",          1, "level", VigilenceLevel.LOW),
      new RadioButtonColumn("High",         1, "level", VigilenceLevel.HIGH),
      new RadioButtonColumn("Adversarial",  2, "level", VigilenceLevel.ADVERSARIAL),
      new RadioButtonColumn("On",           1, "status", "ON"),
      new RadioButtonColumn("Off",          1, "status", "OFF")
    ];
  }


/************************************************************************************/


  /**
   * this brings up a modal
   * Note that the virtue selection modal will
   * May want to define a filter so as to only show Virtues with browser applications?
   * Perhaps they'd want to set a virtue as default that doesn't have a browser at the moment, but they're about to set one on it.
   * Is it even possible to filter on virtues that have a browser? Unless browser applications can be flagged as a "browser" upon creation,
   * probably not.
   */
  activateDefaultBrowserVirtueModal(): void {
    this.activateVirtueSelectionModal(
        [this.item.defaultBrowserVirtueId],
        (selectedVirtueIds: string[]) => { this.item.defaultBrowserVirtueId = selectedVirtueIds[0]; },
        SelectionMode.SINGLE
      );
  }

  /**
   * this brings up a modal through which the user can select one or more Virtues, and have those selections be passed to some
   * caller-defined function when the user hits 'Submit'.
   *
   * @param currentSelection A list of virtue IDs, that should be marked as 'selected' when the modal gets initialized.
   * @param onComplete A function to pass the modal's list of selected objects to, once the user hits 'Submit'
   * @param selectionMode Optional. Can be either SelectionMode.MULTI or SelectionMode.SINGLE, but defaults to MULTI
   *                      if not given.
   *
   * When implementing filters, the genericModal should take in arbitray filters (which means this function needs to take them in as well)
   * which are passed to the table as default filters, which can't be removed or edited. Other filters can be defined in the table, which
   * stack on top of those defaults.
   *
   */
  activateVirtueSelectionModal(
        currentSelection: string[],
        onComplete: ((selectedVirtues: string[]) => void),
        selectionMode?: SelectionMode
  ): void {

    if (selectionMode === undefined) {
      selectionMode = SelectionMode.MULTI;
    }

    let dialogRef = this.dialog.open( VirtueModalComponent,  {
      height: '70%',
      width: '70%',
      data: {
        selectedIDs: currentSelection,
        selectionMode: selectionMode
      }
    });

    // This is hacky, and should be fixed eventually. This makes the Virtue being edited not show up in the list of
    // Virtues which this one can paste data into.
    dialogRef.componentInstance.table.filterColumnName = "id";

    dialogRef.componentInstance.table.filterCondition = (attribute: any) => {
      return String(attribute) !== this.item.getID();
    };

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedVirtues) => {
      onComplete(selectedVirtues);
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%'});

  }

  /**
   * This brings up a modal with many colors, to let the user select a color as a label for a Virtue.
   */
  activateColorModal(): void {
    let wPercentageOfScreen = 40;

    let dialogRef = this.dialog.open(ColorModalComponent, {
      width: wPercentageOfScreen + '%',
      data: {
        color: this.item.color
      }
    });
    dialogRef.updatePosition({ top: '5%', left: String(Math.floor(50 - wPercentageOfScreen / 2)) + '%' });

    const sub = dialogRef.componentInstance.selectColor.subscribe((newColor) => {
        this.item.color = newColor;
      });

    let closedSub = dialogRef.afterClosed().subscribe(() => {
      sub.unsubscribe();
      closedSub.unsubscribe();
    });
  }

}
