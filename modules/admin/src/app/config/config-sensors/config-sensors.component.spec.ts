import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigSensorsComponent } from './config-sensors.component';

describe('ConfigSensorsComponent', () => {
  let component: ConfigSensorsComponent;
  let fixture: ComponentFixture<ConfigSensorsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigSensorsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigSensorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
