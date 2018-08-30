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
import { GenericFormTabComponent } from '../../../shared/abstracts/gen-tab/gen-tab.component';

@Component({
  selector: 'app-virtue-settings-tab',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsTabComponent extends GenericFormTabComponent implements OnInit {

  @Input() virtueColor: string;

  // this appears to have a number of tables
  // - virtues into which this virtue can paste
  // - white list of network connections
  // - File system permissions (might need custom table)
  // - Allowed Printers
  // - And then probably something with the sensors.

  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "Settings";

  }

  ngOnInit() {
  }

  init() {
    // this.setUpConnectedVirtuesTable();
    // this.setUpPrintersTable();
  }


  setUp(mode: Mode, item: Item): void {
    this.mode = mode;
    this.item = item;

    this.setColor((this.item as Virtue).color);
  }

  update() {

  }

  collectData() {
    this.item['color'] = this.getColor();
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
}
