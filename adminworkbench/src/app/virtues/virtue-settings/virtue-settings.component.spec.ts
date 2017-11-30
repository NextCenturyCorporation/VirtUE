import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VirtueSettingsComponent } from './virtue-settings.component';

describe('VirtueSettingsComponent', () => {
  let component: VirtueSettingsComponent;
  let fixture: ComponentFixture<VirtueSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VirtueSettingsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VirtueSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
