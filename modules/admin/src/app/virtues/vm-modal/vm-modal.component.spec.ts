import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VmModalComponent } from './vm-modal.component';

describe('VmModalComponent', () => {
  let component: VmModalComponent;
  let fixture: ComponentFixture<VmModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VmModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VmModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
