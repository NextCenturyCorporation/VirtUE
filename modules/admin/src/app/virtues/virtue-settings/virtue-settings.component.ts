import { Component, Input, ViewChild, ElementRef, OnInit, Injectable } from '@angular/core';

@Component({
  selector: 'app-virtue-settings',
  templateUrl: './virtue-settings.component.html',
  styleUrls: ['./virtue-settings.component.css']
})
export class VirtueSettingsComponent implements OnInit {

  @Input('virtue-color') virtueColor;

  constructor() {
  }

  ngOnInit() {
  }

  ngAfterViewInit() {
  }


  setColor(temp : any) {
    // console.log("Color was ", this.virtueColor);
    this.virtueColor = String(temp);
    // console.log("Color now ", this.virtueColor);
  }

  getColor() {
    // console.log("Color is ", this.virtueColor);
    return this.virtueColor;
  }

}
