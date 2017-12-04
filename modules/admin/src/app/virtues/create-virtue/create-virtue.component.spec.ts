import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateVirtueComponent } from './create-virtue.component';

describe('CreateVirtueComponent', () => {
  let component: CreateVirtueComponent;
  let fixture: ComponentFixture<CreateVirtueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateVirtueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateVirtueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
