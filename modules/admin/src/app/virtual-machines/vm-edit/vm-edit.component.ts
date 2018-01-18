import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-vm-edit',
  templateUrl: './vm-edit.component.html',
  styleUrls: ['./vm-edit.component.css']
})
export class VmEditComponent implements OnInit {

  vm: { id: number };

  constructor(private router: ActivatedRoute) { }

  ngOnInit() {
    this.vm = {
      id: this.router.snapshot.params['id']
    };
  }

}
