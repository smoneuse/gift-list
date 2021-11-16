import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OwnedListsComponent } from './owned-lists.component';

describe('OwnedListsComponent', () => {
  let component: OwnedListsComponent;
  let fixture: ComponentFixture<OwnedListsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OwnedListsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OwnedListsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
