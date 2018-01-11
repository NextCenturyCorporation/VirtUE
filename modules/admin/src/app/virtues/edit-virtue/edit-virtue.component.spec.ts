import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditVirtueComponent } from './edit-virtue.component';

describe('EditVirtueComponent', () => {
  let component: EditVirtueComponent;
  let fixture: ComponentFixture<EditVirtueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditVirtueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditVirtueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
