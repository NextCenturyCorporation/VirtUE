import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigActiveDirComponent } from './config-active-dir.component';

describe('ConfigActiveDirComponent', () => {
  let component: ConfigActiveDirComponent;
  let fixture: ComponentFixture<ConfigActiveDirComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigActiveDirComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigActiveDirComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
