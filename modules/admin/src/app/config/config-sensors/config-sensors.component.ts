import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

/**
 * @class
 * This is kept because the html has a list of sensor types - in case we need those. Probably don't.
 *
 * #delete
 *
 */
@Component({
  selector: 'app-config-sensors',
  templateUrl: './config-sensors.component.html',
  styleUrls: ['./config-sensors.component.css']
})
export class ConfigSensorsComponent implements OnInit {

  /**
   *
   */
  constructor() { }

  /**
   *
   */
  ngOnInit(): void {
  }

}
