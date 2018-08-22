import { Component, EventEmitter, Inject, OnInit } from '@angular/core';

import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';

import { ColorSet, Color } from '../../shared/sets/color.set';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'color-picker-modal',
  templateUrl: './color-picker.modal.html',
  styleUrls: ['./color-picker.modal.css']
})
export class ColorModal implements OnInit {

  selectColor = new EventEmitter();

  colorSet = new ColorSet();

  selectedColor: Color;

  constructor(
    private dialogRef: MatDialogRef<ColorModal>,
    @Inject(MAT_DIALOG_DATA) public data: {color: string}
  ) {
      this.selectedColor = this.colorSet.getList().find(c => c.hex === data.color);
      console.log(this.selectedColor);
      if (!this.selectedColor) {
        this.selectedColor = this.colorSet.getList().find(c => c.prettyName === "None");
      }
      console.log(this.selectedColor);

    }

  ngOnInit() {}

  onSelect(color: Color) {
    this.selectedColor = color;
  }

  submit(): void {
    this.selectColor.emit(this.selectedColor.hex);
    this.dialogRef.close();
  }

  cancel() {
    this.dialogRef.close();
  }

}
