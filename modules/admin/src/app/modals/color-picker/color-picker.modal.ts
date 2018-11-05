import { Component, EventEmitter, Inject, OnInit } from '@angular/core';

import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';

import { ColorSet, Color } from '../../virtues/color.set';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/**
* @class
 * This class represents a dialoge with a list of selectable colors. When a color is selected, and 'Submit' pressed,
 * the hex value for that color is passed back to the [[VirtueSettingsTabComponent]]
 *
 * Currently this is only used to assign colors to virtues, as an easily distinguishable label.
 *
 * Eventually this should add support for custom colors. Probably not saving them as a perisistent option in this modal though.
 * Could let the user pick a color though by showing a list of Virtues, and letting them pick one to copy.
 */
@Component({
  selector: 'app-color-picker-modal',
  templateUrl: './color-picker.modal.html',
  styleUrls: ['./color-picker.modal.css']
})
export class ColorModalComponent implements OnInit {

  /** what the calling component subscribes to, in order to receive back the selected color */
  selectColor = new EventEmitter();

  /** a predefined list of colors, with names and hex values. */
  colorSet = new ColorSet();

  /**
   * he currently selected color. The corresponding color card gets a border, and its hex value is what is passed
   * back to the watching component when submit is pressed.
   */
  selectedColor: Color;

  /**
   * Initialize selection to whatever the input color is.
   * if the supplied color isn't in our list, set initial selection to a default
   *    (currently white, which is rendered as transparent in [[GenericTableComponent]]
   *
   * When custom colors are added, that second part will have to be changed, so an invalid color is
   * set to default, but a color not in ColorSet is treated as if the user had selected a custom color.
   */
  constructor(
    /** a reference to the dialog itself */
    private dialogRef: MatDialogRef<ColorModalComponent>,
    /** the data the calling ocmponent passes in. Should contain a 'color' field, to initialize the selection to. */
    @Inject(MAT_DIALOG_DATA) public data: {color: string}
  ) {
      this.selectedColor = this.colorSet.getList().find(c => c.hex === data.color);

      if (!this.selectedColor) {
        this.selectedColor = this.colorSet.getList().find(c => c.prettyName === "None");
      }

  }

  /**
   * Don't do anything special on render.
   */
  ngOnInit() {}

  /**
   * sets selectedColor to the specified input
   * @param color the Color the user clicked
   */
  onSelect(color: Color) {
    this.selectedColor = color;
  }

  /**
   * if the user clicks cancel. Emits the currently-selected color, and closes the dialog.
   */
  submit(): void {
    this.selectColor.emit(this.selectedColor.hex);
    this.dialogRef.close();
  }

  /**
   * if the user clicks cancel. Emits an obvious non-color before closing, so that the subscribed component
   * knows to ignore it and unsubscribe.
   */
  cancel() {
    this.selectColor.emit("");
    this.dialogRef.close();
  }

}
