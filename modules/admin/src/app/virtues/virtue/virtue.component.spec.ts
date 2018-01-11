import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VirtueComponent } from './virtue.component';

describe('VirtueComponent', () => {
  let component: VirtueComponent;
  let fixture: ComponentFixture<VirtueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VirtueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VirtueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
