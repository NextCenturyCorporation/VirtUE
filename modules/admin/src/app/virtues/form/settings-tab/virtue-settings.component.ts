import { Component, Input, ViewChild, ElementRef, OnInit, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material';
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
interface NetworkConnection {
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


  private networkWhiteList: NetworkConnection[];



  // this appears to have a number of tables
  // - virtues into which this.item can paste
  // - white list of network connections
  // - File system permissions (might need custom table)
  // - Allowed Printers
  // - And then probably something with the sensors.

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Settings";
    this.pasteableVirtues = [];
  }

  ngOnInit() {
  }

  init() {
    // this.setUpConnectedVirtuesTable();
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

  update() {

  }

  collectData() {
    this.item.color = this.getColor();

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
    },
    () => {},
    () => {// when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }
}
