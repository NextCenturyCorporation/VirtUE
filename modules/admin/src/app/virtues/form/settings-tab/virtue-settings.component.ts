import { Component, Input, ViewChild, ElementRef, OnInit, Injectable, EventEmitter } from '@angular/core';
import {  MatButtonModule,
          MatDialog,
          MatIconModule
} from '@angular/material';

import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';

import {
  Column,
  TextColumn,
  ListColumn,
  CheckboxColumn,
  DropdownColumn,
  InputFieldColumn,
  IconColumn,
  RadioButtonColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { NetworkPermission } from '../../../shared/models/networkPerm.model';
import { FileSysPermission } from '../../../shared/models/fileSysPermission.model';
import { Printer } from '../../../shared/models/printer.model';
import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../../shared/services/config-urls.enum';
import { Datasets } from '../../../shared/abstracts/gen-data-page/datasets.enum';

import { ColorModalComponent } from "../../../modals/color-picker/color-picker.modal";
import { VirtueModalComponent } from "../../../modals/virtue-modal/virtue-modal.component";
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';
import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';

import { NetworkProtocols } from '../../protocols.enum';

/**
 * @class
 *
 * This class represents  a tab in [[VirtueComponent]], describing all the settings a virtue can be set to have.
 *
 * This has four sub-tabs at the moment:
 *    - 'General'
 *      - Color
 *      - default browser
 *      - provisioned or not (?) #TODO
 *      - list of virtues this one can paste data into
 *    - 'Network'
 *      - list of permitted connections
 *    - 'Resources'
 *      - file system permissions
 *        - editable list, generated from global settings? #TODO
 *        - for each file system, R/W/E permissions can be given.
 *      - Printers
 *        - editable list, pulled from global settings.
 *    - 'Sensors'
 *      - Nothing at the moment #TODO
 *
 * At the moment, only 'color' is saved to the backend.
 *
 * @extends [[GenericFormTabComponent]]
 */
@Component({
  selector: 'app-virtue-settings-tab',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsTabComponent extends GenericFormTabComponent implements OnInit {

  /** a table to display the Virtues that this Virtue is allowd to paste data into */
  @ViewChild('allowedPasteTargetsTable') private allowedPasteTargetsTable: GenericTableComponent<Virtue>;

  /** a table to display the network permissions granted to this Virtue */
  @ViewChild('netWorkPermsTable') private netWorkPermsTable: GenericTableComponent<NetworkPermission>;

  /** a table to display the file system permissions of this Virtue */
  @ViewChild('fileSysPermsTable') private fileSysPermsTable: GenericTableComponent<FileSysPermission>;

  /** a table to display the printers this Virtue can access */
  @ViewChild('printerTable') private printerTable: GenericTableComponent<Printer>;

  /** re-classing item, to make it easier and less error-prone to work with. */
  protected item: Virtue;

  /** local reference to the virtue-form's allVirtues variable. */
  private allVirtues: DictList<Virtue>;

  /** a list of available browsers, one of which can be set as default. Currently a placeholder. */
  browsers: string[];

  /**
   * see [[GenericFormTabComponent.constructor]] for inherited parameters
   */
  constructor(
    router: Router,
    dialog: MatDialog) {
    super(router, dialog);

    this.item = new Virtue({});

    this.tabName = 'Settings';

    // TODO browser list is a placeholder
    // Is this list supposed to be hard coded? User-defined? Automatically generated
    // by perhaps tagging loaded browser applications as "browsers", and looking
    // through all the applications this virtue has access to, and showing that list?
    // The latter seems the most useful, but relies on the admin correctly tagging
    // things when they load them.
    this.browsers = ['Chrome', 'Firefox', 'This is a placeholder', 'TODO'];

  }

  /**
   * See [[GenericFormTabComponent.init]] for generic info
   *
   * @param mode the [[Mode]] to set up the page in.
   */
  init(mode: Mode): void {
    this.setMode(mode);
    this.setUpPasteableVirtuesTable();
    this.setUpNetworkPermsTable();
    this.setUpFileSysPermsTable();
    this.setUpPrinterTable();
    // until GenericTable is made more generic (like for any input object, as opposed to only Items),
    // the other tables have to be defined individually in the html.
    // GenericTable would need to allow arbitrary objects/html in any column - so one could just as
    // easily display a line of text in one column, and checkboxes in the next three.
  }

  /**
   * See [[GenericFormTabComponent.setUp]] for generic info
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
   * See [[GenericFormTabComponent.update]] for generic info
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
  update(changes: any): void {
    if (changes.allVirtues) {
      this.allVirtues = changes.allVirtues;

      this.updatePasteableVirtuesTable();
    }

    if (changes.mode) {
      this.setMode(changes.mode);
      this.setUpPasteableVirtuesTable();
    }

    // if (changes.networks) {
    //   // TODO update something
    // }
    // if (changes.printers) {
    //   // TODO update something
    // }
    this.updateNetworkPermsTable();
    this.updateFileSysPermsTable();
    this.updatePrinterTable();

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
    console.log("collectData");

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
   * This is prevented by manually telling it track things by index, using the below code, adding "; trackBy: indexTracker" to the
   * end of the ngFor statement.
   * ...
   *
   * @param index an index, automagically passed in.
   * @param value a value, auto{black}magically passed in.
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
      new TextColumn('Printer Info',    5, (p: Printer) => p.info, SORT_DIR.ASC),
      new TextColumn('Printer Status',  4, (p: Printer) => p.status, SORT_DIR.ASC),
      new IconColumn('Revoke access',  3, 'delete', (p: Printer) => this.removePrinter(p)
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
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 12,
      noDataMsg: "No printers have been added yet to this Virtue.",
      editingEnabled: () => !this.inViewMode()
    });
  }

  updatePrinterTable() {
    this.printerTable.populate(this.item.allowedPrinters);
  }

  /**
   * This adds a new printer object to the Virtue's printer list.
   * This is very much a temporary measure.
   */
  addNewPrinter(): void {
    this.item.allowedPrinters.push(new Printer("And another printer"));
    this.updatePrinterTable();
  }

  /**
   * This removes a printer from the Virtue's printer list, using the printer's address as an ID.
   * Note that if there are several matching printers, only the first one is removed.
   */
  removePrinter(toDelete: Printer): void {
    let index = 0;
    for (let printer of this.item.allowedPrinters) {
      if (printer.address === toDelete.address) {
        break;
      }
      index++;
    }
    this.item.allowedPrinters.splice(index, 1);
    this.updatePrinterTable();
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
      new InputFieldColumn('Host',        4, 'destination', (netPerm: NetworkPermission) => netPerm.destination),
      new DropdownColumn(  'Protocol',    3, 'protocol', () => Object.keys(NetworkProtocols),
                          (protocol: NetworkProtocols) => protocol, (netPerm: NetworkPermission) => String(netPerm.protocol)),
      new InputFieldColumn('Local Port',  2, 'localPort', (netPerm: NetworkPermission) => String(netPerm.localPort)),
      new InputFieldColumn('Remote Port', 2, 'remotePort', (netPerm: NetworkPermission) => String(netPerm.remotePort)),
      new IconColumn('Revoke',  1, 'delete', (idx: number) => this.removeNetwork(idx))
    ];
  }
  /**
   * Sets up the table describing what Virtues this Virtue is allowed to paste data into.
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpNetworkPermsTable(): void {
    if (this.netWorkPermsTable === undefined) {
      return;
    }

    this.netWorkPermsTable.setUp({
      cols: this.getNetworkColumns(),
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 10,
      noDataMsg: "This Virtue has not been granted permission to access any network",
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
  * Once data has been pulled, fill in the table with
  */
  updateNetworkPermsTable(): void {
    this.netWorkPermsTable.populate(this.item.networkWhiteList);
  }

  /**
  * Add a new netork permission to the virtue.
  */
  addNewNetworkPermission(): void {
    this.item.networkWhiteList.push(new NetworkPermission());
    this.updateNetworkPermsTable();
  }

  removeNetwork(idx: number): void {
    this.item.networkWhiteList.splice(idx, 1);
    this.updateNetworkPermsTable();
  }

  /**
  * Iterate through and check all the network permissions
  * @return true if all lines of network permission are fully filled-out (and TODO valid).
  *         false otherwise
  */
  checkNetworkPerms(): boolean {
    for (let networkPermission of this.item.networkWhiteList) {
      if ( !this.checkEnteredPermValid(networkPermission) ) {
        return false;
      }
    }
    return true;
  }

  /**
  * Check a particular network permission - all 4 of its fields should be filled out and valid.
  * @return true if all fields are valid.
  */
  checkEnteredPermValid(netPerm: NetworkPermission): boolean {

    // instead of checking  '<=='
    // first make sure that the ports aren't 0, because checking !port will be true
    // if port === 0. Which would make the wrong error message appear.
    if (netPerm.localPort === 0 || netPerm.remotePort === 0) {
      console.log("Ports on network permissions must be greater than zero.");
      return false;
    }

    if ( !netPerm.destination|| !netPerm.protocol
      || !netPerm.localPort  || !netPerm.remotePort ) {
      console.log("Network permission fields cannot be blank");
      return false;
    }

    // if ( !(netPerm.localPort instanceof Number) || !(netPerm.remotePort instanceof Number) ) {
    //   console.log("Local and Remote ports must be numbers.");
    //   return false;
    // }

    if (netPerm.localPort < 0 || netPerm.remotePort < 0) {
      console.log("Ports on network permissions must be greater than zero.");
      return false;
    }
    return true;
  }


/************************************************************************************/
/************************************************************************************/

  updateFileSysPermsTable(): void {
    this.fileSysPermsTable.populate(this.item.fileSysPerms);
  }
  /**
   * Note that this is done in the style of a GenericTable, but doesn't actually use one, because
   * these things aren't Items. Generalizing the GenericTable further would be useful.
   * @return what columns should show up in the network permissions table
   */
  getFileSysColumns(): Column[] {
    return [
      new TextColumn('Server & Drive',  6, (fs: FileSysPermission) => fs.location, SORT_DIR.ASC),
      new CheckboxColumn('Enabled',     3, 'enabled'),
      new CheckboxColumn('Read',        1, 'read', (fs: FileSysPermission) => !fs.enabled),
      new CheckboxColumn('Write',       1, 'write', (fs: FileSysPermission) => !fs.enabled),
      new CheckboxColumn('Execute',     1, 'execute', (fs: FileSysPermission) => !fs.enabled)
    ];
  }

  /**
   * Sets up the table describing what Virtues this Virtue is allowed to paste data into.
   *
   * See [[GenericTable.setUp]]() for details on what needs to be passed into the table's setUp function.
   */
  setUpFileSysPermsTable(): void {
    if (this.fileSysPermsTable === undefined) {
      return;
    }
    this.fileSysPermsTable.setUp({
      cols: this.getFileSysColumns(),
      filters: [],
      tableWidth: 10,
      noDataMsg: "No file systems have been set up in the global settings",
      editingEnabled: () => !this.inViewMode()
    });
  }


/************************************************************************************/
/************************************************************************************/

  /**
   * See [[GenericListComponent.getSubMenu]] for more details on this sort
   * of method.
   *
   * @return Just a list with one option: to remove the attached Virtue from the list of ones this one can paste data into.
   */
  getPasteSubMenu(): SubMenuOptions[] {
    return [
       new SubMenuOptions("Remove", () => true, (i: Item) => {
         let index = this.item.allowedPasteTargets.indexOf(i.getID(), 0);
         if (index > -1) {
            this.item.allowedPasteTargets.splice(index, 1);
         }
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
      new TextColumn('Template Name',  4, (v: Virtue) => v.getName(), SORT_DIR.ASC, (i: Item) => this.viewItem(i),
                                                                                        ()=> this.getPasteSubMenu()),
      new ListColumn<Item>('Available Applications', 4, this.getGrandchildren,  this.formatName),
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
      // opts: this.getPasteSubMenu(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 10,
      noDataMsg: this.getNoPasteDataMsg(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
   * Once data has been pulled, fill in the table with the Virtue's current allow paste targets, if it has any.
   */
  updatePasteableVirtuesTable(): void {

    if ( !(this.allVirtues) ) {
      return;
    }

    let items = [];
    for (let vID of this.item.allowedPasteTargets) {
      if (this.allVirtues.has(vID)) {
        items.push(this.allVirtues.get(vID));
      }
    }
    this.allowedPasteTargetsTable.populate(items);
  }

  /**
   * this brings up the modal to add/remove virtues that this Virtue has permission to paste data into.
   */
  activatePastableVirtueModal(): void {
    this.activateVirtueSelectionModal( this.item.allowedPasteTargets, (selectedVirtues: string[]) => {
        this.item.allowedPasteTargets = selectedVirtues;
        this.updatePasteableVirtuesTable();
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



  /**
   * this brings up a modal #uncommented
   * Note that the virtue selection modal will
   */
  activateDefaultBrowserVirtueModal(): void {
    this.activateVirtueSelectionModal( [this.item.defaultBrowserVirtue], (selectedVirtues: string[]) => {
        this.item.defaultBrowserVirtue = selectedVirtues[0];
      });
  }

  /**
   * this brings up a #uncommented
   */
  activateVirtueSelectionModal( currentSelection: string | string[],
                      onComplete: ((selectedVirtues: string[]) => void)): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    let dialogRef = this.dialog.open( VirtueModalComponent,  {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        selectedIDs: currentSelection
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
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

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
      if(newColor !== "") {
        this.item.color = newColor;
      }
    },
    () => {},
    () => {
      sub.unsubscribe();
    });
  }

}
