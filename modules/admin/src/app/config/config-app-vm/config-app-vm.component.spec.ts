import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigAppVmComponent } from './config-app-vm.component';

describe('ConfigAppVmComponent', () => {
  let component: ConfigAppVmComponent;
  let fixture: ComponentFixture<ConfigAppVmComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigAppVmComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigAppVmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
