import { Component, OnInit, Input, Output, OnChanges, EventEmitter } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

@Component({
  selector: 'app-dialog-test',
  templateUrl: 'dialog-test.component.html',
  styleUrls: ['dialog-test.component.css'],
  animations: [
    trigger('dialog', [
      transition('void => *', [
        //style({ transform: 'scale3d(.3, .3, .3)' }),
        animate(100)
      ]),
      transition('* => void', [
        animate(100)
        //animate(1000, style({ transform: 'scale3d(.0, .0, .0)' }))
      ])
    ])
  ]
})
export class DialogTestComponent implements OnInit {
  @Input() closable = true;
  @Input() visible: boolean;
  @Output() visibleChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor() { }

  ngOnInit() { }

  close() {
    this.visible = false;
    this.visibleChange.emit(this.visible);
  }
}

// import { Component, OnInit } from '@angular/core';
//
// @Component({
//   selector: 'app-dialog-test',
//   templateUrl: 'dialog-test.component.html',
//   styleUrls: ['dialog-test.component.css']
// })
// export class DialogTestComponent implements OnInit {
//
//   constructor() { }
//
//   ngOnInit() {
//   }
//
// }
