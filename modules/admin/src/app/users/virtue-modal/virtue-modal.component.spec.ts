import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VirtueModalComponent } from './virtue-modal.component';

describe('VirtueModalComponent', () => {
  let component: VirtueModalComponent;
  let fixture: ComponentFixture<VirtueModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VirtueModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VirtueModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
