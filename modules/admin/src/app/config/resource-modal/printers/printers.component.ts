import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-printers',
  templateUrl: './printers.component.html',
  styleUrls: ['./printers.component.css']
})
export class PrintersComponent implements OnInit {

  printerValue: string;
  printers = [
    {value: '1', viewValue: 'HP Color DeskJet CP5225dn', printerLoc: '29.50.123.7 on FCVA'},
    {value: '2', viewValue: 'HP OfficeJet Pro 8710', printerLoc: '29.50.244.236 on FCVA'},
    {value: '3', viewValue: 'Printer name here', printerLoc: '29.495.66.123 on FCVA'},
  ];
  constructor() { }

  ngOnInit() {
  }

}
