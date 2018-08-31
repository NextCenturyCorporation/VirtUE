import { Component, Input, ViewChild, ElementRef, OnInit, Injectable, EventEmitter } from '@angular/core';
import { HttpClientModule } from "@angular/common/http";
import {
          MatButtonModule,
          MatDialog,
          MatIconModule,
          MatIconRegistry

} from '@angular/material';

import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { User } from '../../../shared/models/user.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { DictList } from '../../../shared/models/dictionary.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrlEnum } from '../../../shared/enums/enums';
import { RowOptions } from '../../../shared/models/rowOptions.model';

import { ColorModalComponent } from "../../../modals/color-picker/color-picker.modal";
import { VirtueModalComponent } from "../../../modals/virtue-modal/virtue-modal.component";
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';
import { GenericTableComponent } from '../../../shared/abstracts/gen-table/gen-table.component';

// not used anywhere else, so just defined here
class NetworkPermission {
  host: string;
  protocol: string; //should be an enum
  localPort: number;
  remotePort: number;
}

@Component({
  selector: 'app-virtue-settings-tab',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsTabComponent extends GenericFormTabComponent implements OnInit {

  @ViewChild('pasteableVirtues') private pasteableVirtuesTable: GenericTableComponent;

  private unprovisioned: boolean;

  @Input() private virtueColor: string;

  private defaultBrowser: string; // are these options hard-coded?

  // the IDs of the virtues into which this.item is allowed to paste things
  private pasteableVirtues: string[];


  // re-classing item, to make it easier and less error-prone to work with.
  protected item: Virtue;

  // local reference to the virtue-form's allUsers variable.
  private allVirtues: DictList<Virtue>;

  private networkWhiteList: NetworkPermission[];

  private applyNewNetworkPermission: EventEmitter<boolean> = new EventEmitter<boolean>();

  private newNetwork: NetworkPermission;

  browsers: string[];


  // this appears to have a number of tables
  // - virtues into which this.item can paste
  // - white list of network connections
  // - File system permissions (might need custom table)
  // - Allowed Printers
  // - And then probably something with the sensors.

  constructor( private matIconRegistry: MatIconRegistry, router: Router, dialog: MatDialog) {
    super(router, dialog);

    this.matIconRegistry.addSvgIcon(
          `plus`,
          `../../../../assets/images/baseline-add-24px.svg`
        );

    this.tabName = 'Settings';
    this.pasteableVirtues = [];

    this.newNetwork = new NetworkPermission();
    // Is this list suppsoed to be hard coded? User-defined? Automatically generated
    // by perhaps tagging loaded browser applications as "browsers", and looking
    // through all the applications this virtue has access to, and showing that list?
    // The latter seems the most useful, but relies on the admin correctly tagging things when they load them.
    // And labelling them well. Apps need versions, but they can't default to anything, and it must be made clear
    // that "version" on that modal means "the actual application's version", and not "Version" like this is the 4th
    // change I've made to this Chrome application item.
    this.browsers = ['Chrome', 'Firefox', 'This is not workable', 'FIXME', 'TODO'];
  }

  ngOnInit() {
  }

  init() {
    this.setUpPasteableVirtuesTable();
    // this.setUpPrintersTable();
  }


  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-settings which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;

    this.setColor(this.item.color);
  }

  update(newData?: any) {
    if (newData) {
      if (newData.allVirtues) {
        this.allVirtues = newData.allVirtues;

        this.populatePasteableVirtuesTable();
      }

      // other conditionals
    }
    else {
      // TODO show error
      console.log();
    }
  }

  collectData(): boolean {
    this.item.color = this.getColor();

    if (this.newNetwork !== undefined) {
      // TODO tell the user
      alert("please hit apply or remove on your new network permission");
      return false;
    }
    return true;
  }

  addNewNetworkPermission(): void {
    this.newNetwork = new NetworkPermission();
    console.log("here", this.newNetwork);
    // this.applyNewNetworkPermission.subscribe( () => {},
    //   () => {},
    //   () => {
    //     this.networkWhiteList.push(this.newNetwork);
    //     this.newNetwork = undefined;
    //     this.applyNewNetworkPermission.unsubscribe();
    // });
  }

  saveNewNetwork(): void {
    // TODO first check it
    this.applyNewNetworkPermission.emit(true);
  }

  setUpPasteableVirtuesTable(): void {
    if (this.pasteableVirtuesTable === undefined) {
      return;
    }

    this.pasteableVirtuesTable.setUp({
      cols: this.getPasteColumns(),
      opts: this.getPasteOptionsList(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table. ?
      tableWidth: 10,
      noDataMsg: 'Click "Add Virtue" to give this virtue permission to paste data into that one',
      hasCheckBoxes: false
    });
  }

  populatePasteableVirtuesTable(): void {
    if ( !(this.allVirtues) ) {
      return;
    }

    this.pasteableVirtuesTable.items = [];
    for (let vID of this.pasteableVirtues) {
      if (this.allVirtues.has(vID)) {
        this.pasteableVirtuesTable.items.push(this.allVirtues.get(vID));
      }
    }
  }

  getPasteColumns(): Column[] {
    return [
      // new Column('name',    'Virtue Template Name',     undefined, 'asc',     4, undefined, (i: Item) => this.viewItem(i)),
      new Column('name',    'Virtue Template Name',    undefined,             'asc',     4, undefined),
      // new Column('childNamesHTML',  'Virtual Machines', true, undefined,  3, this.getChildNamesHtml),
      // new Column('apps',            'Applications',     true, undefined,  3, this.getGrandchildrenHtmlList),
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

  getPasteOptionsList(): RowOptions[] {
    return [
       new RowOptions("Remove", () => true, (i: Item) => {
         var index = this.pasteableVirtues.indexOf(i.getID(), 0);
         if (index > -1) {
            this.pasteableVirtues.splice(index, 1);
         }
         this.populatePasteableVirtuesTable();
       }
     )];
  }

  setColor(temp: any) {
    this.virtueColor = String(temp);
  }

  getColor() {
    return this.virtueColor;
  }

  activateColorModal(): void {
    let wPercentageOfScreen = 40;

    let dialogRef = this.dialog.open(ColorModalComponent, {
      width: wPercentageOfScreen + '%',
      data: {
        color: this.virtueColor
      }
    });
    dialogRef.updatePosition({ top: '5%', left: String(Math.floor(50 - wPercentageOfScreen / 2)) + '%' });

    const sub = dialogRef.componentInstance.selectColor.subscribe((newColor) => {
      this.setColor(newColor);
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
        selectedIDs: this.pasteableVirtues
      }
    });

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedVirtues) => {
      this.pasteableVirtues = selectedVirtues;
      this.populatePasteableVirtuesTable();
      // this.pasteableVirtuesTable.items = [];
      // for (let vID of this.pasteableVirtues) {
      //   this.pasteableVirtuesTable.items.
      // }
    },
    () => {},
    () => {// when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }
}
