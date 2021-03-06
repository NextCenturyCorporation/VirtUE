import { Component, Input, ViewChild, ElementRef, OnInit, Injectable, EventEmitter, Output } from '@angular/core';
import {  MatButtonModule,
          MatDialog,
          MatIconModule
} from '@angular/material';


import { Item } from '../../../shared/models/item.model';
import { Virtue, ClipboardPermission, ClipboardPermissionOption } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { IndexedObj } from '../../../shared/models/indexedObj.model';

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

 /** For convenience until this page gets refactored out into its own data tab page */
 allVirtues: DictList<Virtue> = new DictList<Virtue>();

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
    // this.setUpSensorTable();
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
        this.allVirtues = changes[DatasetNames.VIRTUE_TS];
      }

      if (changes.mode) {
        this.setMode(changes.mode);
      }
    }
    this.updatePasteableVirtuesTable();
    this.updateNetworkPermsTable();
    this.updateFileSysPermsTable();
    this.updatePrinterTable();

    // if (changes.printers) { // up-to-date data on printer status ould be cool
    //   // TODO update something
    // }

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
      new TextColumn('Printer Description',    4, (p: Printer) => p.name),
      new TextColumn('Address',    2, (p: Printer) => p.address),
      new TextColumn('Printer Status',  4, (p: Printer) => p.status),
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
    this.printerTable.populate(this.item.getPrinters().asList());
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
    this.fileSystemsPermsTable.populate(this.item.getFileSystems().asList());
  }

  /**
   * Note that this is done in the style of a GenericTable, but doesn't actually use one, because
   * these things aren't Items. Generalizing the GenericTable further would be useful.
   * @return what columns should show up in the network permissions table
   */
  getFileSysColumns(): Column[] {
    return [
      new TextColumn('Drive name',  3, (fs: FileSystem) => fs.name, SORT_DIR.ASC, (fs: FileSystem) => this.toDetailsPage(fs)),
      new TextColumn('Address',     3, (fs: FileSystem) => fs.address),
      new TextColumn('Enabled',     2, (fs: FileSystem) => String(fs.enabled)),
      new TextColumn('Permissions', 2, (fs: FileSystem) => fs.formatPerms()),
      new BlankColumn(1),
      new IconColumn('Revoke access', 1, 'delete', (fs: FileSystem) => this.removeFileSystem(fs))
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
      tableWidth: 1,
      noDataMsg: "This virtue hasn't been given access to any file systems.",
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
      new TextColumn('Direction',   1, (netPerm: NetworkPermission) => this.getDirection(netPerm)),
      new TextColumn('CIDR IP',     2, (netPerm: NetworkPermission) => netPerm.cidrIp),
      new TextColumn('Protocol',    1, (netPerm: NetworkPermission) => this.formatIfBlank(netPerm.ipProtocol)),
      new TextColumn('Port Range',   2, (netPerm: NetworkPermission) => this.formatPortRange(netPerm)),
      new TextColumn('Description', 5, (netPerm: NetworkPermission) => netPerm.description),
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

  setUpNetworkPermsTable(): void {
    if (this.networkPermsTable === undefined) {
      return;
    }

    this.networkPermsTable.setUp({
      cols: this.getNetworkColumns(),
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
    this.item.getNewSecurityPermissions().push(newPermission);
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
    this.item.getRevokedSecurityPermissions().push(removedPermission);
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
      tableWidth: 0.55,
      noDataMsg: this.getNoPasteDataMsg(),
      filters: this.getClipboardFilters(),
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
   * @return what columns should show up in the virtue's paste-permission table
   *         The first column, the Virtue's name, should be clickable if and only if the page is in view mode.
   */
  getPasteColumns(): Column[] {
    let cols: Column[] = [
      new TextColumn('Destination Template',  5, (clip: ClipboardPermission) => this.getVirtName(clip.dest), SORT_DIR.ASC,
                                                    (clip: ClipboardPermission) => this.viewItem(this.getVirt(clip.dest))),
      new ListColumn('Contained Apps', 4, (clip: ClipboardPermission) => this.getVirtApps(clip.dest),  this.formatName),
    ];
    if (this.mode !== Mode.VIEW) {
      cols.push(new DropdownColumn('Permission', 3, 'permission', () => Object.values(ClipboardPermissionOption),
                      (option: ClipboardPermissionOption) => option));
    }
    else {
      cols.push(new TextColumn('Permission', 3, (clip: ClipboardPermission) => String(clip.permission)));
    }
    return cols;
  }

  getVirt(virtueID: string): Virtue {
    if (this.allVirtues && this.allVirtues.asList().length === 0) {
      return undefined;
    }
    if (!this.allVirtues.has(virtueID)) {
      return undefined;
    }
    return this.allVirtues.get(virtueID);
  }

  getVirtName(virtueID: string): string {
    let v: Virtue = this.getVirt(virtueID);
    if (!v) {
      return virtueID;
    }
    return v.getName();
  }

  getVirtApps(virtueID: string): IndexedObj[] {
    let v: Virtue = this.getVirt(virtueID);
    if (!v) {
      return [];
    }
    return v.getVmApps();
  }

  getClipboardFilters(): {objectField: string, options: {value: string, text: string}[] } {
    return {
            objectField: 'permission',
            options: [
              {value: '*', text: 'All Permissions'},
              {value: ClipboardPermissionOption.ALLOW, text: 'Allowed'},
              {value: ClipboardPermissionOption.DENY, text: 'Denied'},
              {value: ClipboardPermissionOption.ASK, text: 'Ask'}
            ]
          };
  }

  /**
   * Fill in the table with the Virtue's current allow paste targets, if it has any.
   * Use more recent definitions, if they are given.
   */
  updatePasteableVirtuesTable(): void {
    if (this.allowedPasteTargetsTable === undefined) {
      return;
    }
    this.setUpPasteableVirtuesTable();
    this.allowedPasteTargetsTable.populate(this.item.clipboardPermissions);

  }

  /**
  * @return a message telling the user that no paste permissions have been given to this
  * virtue, in a way that is clear and relevant for the current mode.
  */
  getNoPasteDataMsg(): string {
    return "It appears no other Virtue templates exist yet.";
  }

/************************************************************************************/
/**
 * This is currrently all place-holder, since individually-customized sensing levels isn't a priority right now.
 *
 */
  setUpSensorTable(): void {
    if (this.sensorTable === undefined) {
      return;
    }
    this.sensorTable.setUp({
      cols: this.getSensorColumns(),
      tableWidth: 1,
      noDataMsg: "No sensors have been connected."
    });
  }

  getSensorColumns(): Column[] {
    return [
      new TextColumn("Sensor Context", 3, (s: Sensor) => s.name),
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
