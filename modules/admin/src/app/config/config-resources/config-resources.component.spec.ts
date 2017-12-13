import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigResourcesComponent } from './config-resources.component';

describe('ConfigResourcesComponent', () => {
  let component: ConfigResourcesComponent;
  let fixture: ComponentFixture<ConfigResourcesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigResourcesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigResourcesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
