import { Component, OnInit } from '@angular/core';
import { JsondataService } from '../../shared/jsondata.service';
import { JsonFilterPipe } from '../../shared/json-filter.pipe';
import { CountFilterPipe } from '../../shared/count-filter.pipe';

@Component({
  selector: 'app-vm-list',
  providers: [ JsondataService ],
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  noListData = false;
  vmlist: string;
  vms = [];
  vmLength : number;

  constructor( private jsondataService: JsondataService ) { }

  ngOnInit() {
    this.jsondataService.getJSON('vms')
    .subscribe(resJsonData => this.vms = resJsonData);

    this.vmLength = this.vms.length;
  }

}
