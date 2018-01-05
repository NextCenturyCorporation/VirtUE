import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';
import { ActivatedRoute } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css']
})
export class EditUserComponent implements OnInit {

  screenWidth: any;
  leftPosition: any;
  submitBtn: any;
  dialogWidth: any;
  fullImagePath: string;
  user: { id: number };
  users = new FormControl();
  activeDirUsers = [
    'Anthony Wong',
    'Binoy Ravindran',
    'Chris Long',
    'Kara Cartwright',
    'Kyle Drumm',
    'Mike Day',
    'Patrick Dwyer',
    'Pierre Olivier',
    'Ruslan Nikolaev',
    'Sophie Kim',
    'Wole Omitowoju'
  ];

  constructor(public dialog: MatDialog, private router: ActivatedRoute) {}

  activateModal(id,mode): void {

    this.dialogWidth = 600;
    this.fullImagePath = './assets/images/app-icon-white.png';

    if (mode=='add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    let dialogRef = this.dialog.open( VirtueModalComponent, {
      width: this.dialogWidth+'px',
      data: {
        id: id,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath
      }
    });

    this.screenWidth = (window.screen.width);
    this.leftPosition = ((window.screen.width)-this.dialogWidth)/2;

    // console.log(this.screenWidth);
    // console.log(this.leftPosition);

    dialogRef.updatePosition({ top: '5%', left: this.leftPosition+'px' });

    // dialogRef.afterClosed().subscribe();
  }

  ngOnInit() {
    this.user = {
      id: this.router.snapshot.params['id']
    };
  }

}
