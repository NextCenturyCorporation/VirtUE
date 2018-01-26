import { Component, OnInit } from '@angular/core';
import { DataService } from '../../data/data.service';
import { JsonFilterPipe } from '../../data/json-filter.pipe';
import { CountFilterPipe } from '../../data/count-filter.pipe';

@Component({
  selector: 'app-vm-list',
  providers: [ DataService ],
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  noListData = false;
  vmlist: string;
  vms = [];
  vmLength : number;

  constructor( private dataService: DataService ) { }

  ngOnInit() {
    this.dataService.getData('vms')
    .subscribe(resJsonData => this.vms = resJsonData);

    this.vmLength = this.vms.length;
  }

}
