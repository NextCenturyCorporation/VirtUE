import { Component, Input, OnInit } from '@angular/core';

import { VirtualMachineService } from '../../shared/services/vm.service';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';

@Component({
  selector: 'app-test-child',
  // templateUrl: './test-child.component.html',
  template: `<p>{{vmInput.name}}</p>`
  // styleUrls: ['./test-child.component.css']
})
export class TestChildComponent implements OnInit {
  @Input() vmInput : VirtualMachine;
  @Input() appInput : Application;

  constructor() { }

  ngOnInit() {
  }

}
