import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-vm-list',
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  enabledVmMenu = false;
  disabledVmMenu = false;

  constructor() { }

  showMenu(menu) {
    if (menu === 'enabled') {
      this.enabledVmMenu = true;
    } else {
      this.disabledVmMenu = true;
    }
  }

  ngOnInit() {
  }

}
