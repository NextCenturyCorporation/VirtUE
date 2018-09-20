import { Component, Input, ViewChild, ElementRef, OnInit, Injectable, EventEmitter } from '@angular/core';
import {  MatButtonModule,
          MatDialog,
          MatIconModule
} from '@angular/material';

import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { NetworkPermission } from '../../../shared/models/networkPerm.model';
import { Printer } from '../../../shared/models/printer.model';
import { Mode, ConfigUrls, Datasets, Protocols } from '../../../shared/enums/enums';
import { SubMenuOptions } from '../../../shared/models/subMenuOptions.model';

import { ColorModalComponent } from "../../../modals/color-picker/color-picker.modal";
import { VirtueModalComponent } from "../../../modals/virtue-modal/virtue-modal.component";
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';
import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';

@Component({
  selector: 'app-virtue-settings-tab',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsTabComponent extends GenericFormTabComponent implements OnInit {

  @ViewChild('allowedPasteTargetsTable') private allowedPasteTargetsTable: GenericTableComponent;

  // re-classing item, to make it easier and less error-prone to work with.
  protected item: Virtue;

  // local reference to the virtue-form's allVirtues variable.
  private allVirtues: DictList<Virtue>;

  browsers: string[];

  private keys = Object.keys;
  private protocols = Protocols;

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

      this.populatePasteableVirtuesTable();
    }

    if (changes.mode) {
      this.setMode(changes.mode);
      this.setUpPasteableVirtuesTable();
    }
  }

  collectData(): boolean {
    console.log("collectData");

    // TODO check all entries for validity before allowing save

    if (!this.checkNetworkPerms()) {
      // TODO tell the user
      return false;
    }
    return true;
  }

  checkNetworkPerms(): boolean {
    for (let networkPermission of this.item.networkWhiteList) {
      if ( !this.checkEnteredPermValid(networkPermission) ) {
        return false;
      }
    }
    return true;
  }

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

    if (netPerm.localPort < 0 || netPerm.remotePort < 0) {
      console.log("Ports on network permissions must be greater than zero.");
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

  allChecked(colName: string): boolean {
    for (let perm of this.item.fileSysPerms) {
      if (perm[colName] === false) {
        return false;
      }
    }
    return true;
  }

  checkAll(event: any, colName: string): void {
    for (let perm of this.item.fileSysPerms) {
      perm[colName] = event.checked;
    }
  }

  addNewPrinter(): void {
    this.item.allowedPrinters.push(new Printer("And another printer"));
  }

  removePrinter(index: number): void {
    this.item.allowedPrinters.splice(index, 1);
  }

  addNewNetworkPermission(): void {
    this.item.networkWhiteList.push(new NetworkPermission());
  }

  setUpPasteableVirtuesTable(): void {
    if (this.allowedPasteTargetsTable === undefined) {
      return;
    }

    this.allowedPasteTargetsTable.setUp({
      cols: this.getPasteColumns(),
      opts: this.getPasteSubMenu(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 10,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }

  /**
   * @return a message telling the user that no paste permissions have been given to this
   * virtue, in a way that is clear and relevant for the current mode.
   */
  getNoDataMsg(): string {
    if (this.mode === Mode.VIEW) {
      return "This virtue hasn't been granted access to paste data into any other virtues.";
    }
    else {
      return 'Click "Add Virtue" to give this virtue permission to paste data into that one';
    }
  }

  /**
   * Once data has been pulled, fill in the table with the Virtue's current allow paste targets, if it has any.
   */
  populatePasteableVirtuesTable(): void {

    if ( !(this.allVirtues) ) {
      return;
    }

    this.allowedPasteTargetsTable.items = [];
    for (let vID of this.item.allowedPasteTargets) {
      if (this.allVirtues.has(vID)) {
        this.allowedPasteTargetsTable.items.push(this.allVirtues.get(vID));
      }
    }
  }

  getPasteColumns(): Column[] {
    let cols: Column[] = [
      new Column('apps',    'Available Applications',  4, undefined,  this.formatName, this.getGrandchildren),
      new Column('version', 'Version',                2, 'desc'),
      new Column('enabled',  'Status',                 1, 'asc',       this.formatStatus)
    ];

    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('name', 'Virtue Template Name', 4, 'asc', this.formatName, undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('name', 'Virtue Template Name', 4, 'asc'));
    }

    return cols;
  }

  getNetworkColumns(): Column[] {
    return [
      new Column('destination',        'Host',         5, 'asc'),
      new Column('protocol',    'Protocol',     3, 'asc'),
      new Column('localPort',   'Local Port',   2, 'asc'),
      new Column('remotePort',  'Remote Port',  2, 'asc')
    ];
  }

  getFileSysColumns(): Column[] {
    return [
      new Column('location', 'Server & Drive',  6, 'asc'),
      new Column('enabled',  'Enabled',         3),
      new Column('read',     'Read',            1),
      new Column('write',    'Write',           1),
      new Column('execute',  'Execute',         1)
    ];
  }

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
         this.populatePasteableVirtuesTable();
       }
     )];
  }

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

  // this brings up the modal to add/remove children
  // this could be refactored into a "MainTab" class, which is the same for all
  // forms, but I'm not sure that'd be necessary.
  activatePastableVirtueModal(): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    let dialogRef = this.dialog.open( VirtueModalComponent,  {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        selectedIDs: this.item.allowedPasteTargets
      }
    });

    // This is hacky, and should be fixed eventually. This makes the Virtue being edited not show up in the list of
    // Virtues which this one can paste data into.
    dialogRef.componentInstance.table.filterColumn = "id";

    dialogRef.componentInstance.table.filterCondition = (attribute: any) => {
      return String(attribute) !== this.item.getID();
    };

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedVirtues) => {
      this.item.allowedPasteTargets = selectedVirtues;
      this.populatePasteableVirtuesTable();
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
}
