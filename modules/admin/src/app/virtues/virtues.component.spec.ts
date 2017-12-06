import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VirtuesComponent } from './virtues.component';

describe('VirtuesComponent', () => {
  let component: VirtuesComponent;
  let fixture: ComponentFixture<VirtuesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VirtuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VirtuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
