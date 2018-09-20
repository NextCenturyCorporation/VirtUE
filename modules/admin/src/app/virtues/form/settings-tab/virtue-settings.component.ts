import { Component, Input, ViewChild, ElementRef, OnInit, Injectable, EventEmitter } from '@angular/core';
import {  MatButtonModule,
          MatDialog,
          MatIconModule,
          MatIconRegistry
} from '@angular/material';

import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { NetworkPermission } from '../../../shared/models/networkPerm.model';
import { Printer } from '../../../shared/models/printer.model';
import { Mode, ConfigUrlEnum, Protocols } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

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

  constructor( private matIconRegistry: MatIconRegistry, router: Router, dialog: MatDialog) {
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

  ngOnInit() {
  }

  init() {
    this.setUpPasteableVirtuesTable();
    // until GenericTable is made more generic (like for any input object, as opposed to only Items),
    // the other tables have to be defined individually in the html.
    // GenericTable would need to allow arbitrary objects/html in any column - so one could just as
    // easily display a line of text in one column, and checkboxes in the next three.
  }


  setUp(mode: Mode, item: Item): void {
    console.log("setUp");
    this.mode = mode;
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-settings which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;
  }

  update(changes: any) {
    if (changes.allVirtues) {
      this.allVirtues = changes.allVirtues;

      this.populatePasteableVirtuesTable();
    }

    if (changes.mode) {
      this.mode = changes.mode;
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

    if ( !netPerm.host      || !netPerm.protocol
      || !netPerm.localPort || !netPerm.remotePort ) {
        console.log("Network permission fields cannot be blank");
        return false;
    }

    if (netPerm.localPort < 0 || netPerm.remotePort < 0) {
      console.log("Ports on network permissions must be greater than zero.");
      return false;
    }
    return true;
  }

  // See https://github.com/angular/angular/issues/10423 and/or https://stackoverflow.com/questions/40314732
  // and, less helpfully, https://angular.io/guide/template-syntax#ngfor-with-trackby
  // Essentially, Angular's ngFor sometimes tracks the elements it iterates over by their value, as opposed
  // to their index, and so if you put a ngModel on (apparently) any part of such an element, it loses track (?) of that item and
  // hangs - as in, Angular hangs. And the containing tab needs to be killed either through the browser's tab manager,
  // or the browser needs to be killed at the system level (xkill, system process manager, kill, killall, etc.).
  // This is prevented by manually telling it track things by index, as below (and adding "; trackBy: indexTracker" to the ngFor statement.)
  // ...
  indexTracker(index: number, value: any) {
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
      opts: this.getPasteOptionsList(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 10,
      noDataMsg: 'Click "Add Virtue" to give this virtue permission to paste data into that one',
      hasCheckBoxes: false
    });
  }

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
    return [
      // the following commented line should show up when in view mode, and the corresponding
      //  uncommented line in edit/dup/create mode.
      // new Column('name',    'Virtue Template Name',     undefined, 'asc',     4, undefined, (i: Item) => this.viewItem(i)),
      new Column('name',    'Virtue Template Name',    undefined,             'asc',     4, undefined),
      new Column('apps',    'Assigned Applications',  this.getGrandchildren,  undefined, 4, this.formatName),
      new Column('version', 'Version',                undefined,              undefined, 2),
      new Column('status',  'Status',                 undefined,              'asc',     1, this.formatStatus)
    ];
  }

  getNetworkColumns(): Column[] {
    return [
      new Column('host',        'Host',         undefined, undefined, 5),
      new Column('protocol',    'Protocol',     undefined, undefined, 3),
      new Column('localPort',   'Local Port',   undefined, undefined, 2),
      new Column('remotePort',  'Remote Port',  undefined, undefined, 2)
    ];
  }

  getFileSysColumns(): Column[] {
    return [
      new Column('location', 'Server & Drive',   undefined, undefined, 6),
      new Column('enabled',  'Enabled',   undefined, undefined, 3),
      new Column('read',     'Read',      undefined, undefined, 1),
      new Column('write',    'Write',     undefined, undefined, 1),
      new Column('execute',  'Execute',   undefined, undefined, 1)
    ];
  }

  getPasteOptionsList(): RowOptions[] {
    return [
       new RowOptions("Remove", () => true, (i: Item) => {
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
      this.item.color = newColor;
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

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedVirtues) => {
      this.item.allowedPasteTargets = selectedVirtues;
      this.populatePasteableVirtuesTable();
    },
    () => {},
    () => { // when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }
}
