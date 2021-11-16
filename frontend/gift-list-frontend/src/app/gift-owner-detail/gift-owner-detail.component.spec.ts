import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GiftOwnerDetailComponent } from './gift-owner-detail.component';

describe('GiftOwnerDetailComponent', () => {
  let component: GiftOwnerDetailComponent;
  let fixture: ComponentFixture<GiftOwnerDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GiftOwnerDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GiftOwnerDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
