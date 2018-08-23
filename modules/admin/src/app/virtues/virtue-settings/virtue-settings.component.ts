import { Component, Input, ViewChild, ElementRef, OnInit, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material';

import { ColorModalComponent } from "../../modals/color-picker/color-picker.modal";

@Component({
  selector: 'app-virtue-settings',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsComponent implements OnInit {

  @Input() virtueColor: string;

  constructor(protected dialog: MatDialog) {
  }

  ngOnInit() {
  }


  setColor(temp: any) {
    this.virtueColor = String(temp);
  }

  getColor() {
    return this.virtueColor;
  }

  activateModal(): void {
    let wPercentageOfScreen = 40;

    let dialogRef = this.dialog.open(ColorModalComponent, {
      width: wPercentageOfScreen + '%',
      data: {
        color: this.virtueColor
      }
    });
    dialogRef.updatePosition({ top: '5%', left: String(Math.floor(50 - wPercentageOfScreen / 2)) + '%' });

    const vms = dialogRef.componentInstance.selectColor.subscribe((newColor) => {
      console.log("$$", newColor);
      this.setColor(newColor);
    });
  }
}
