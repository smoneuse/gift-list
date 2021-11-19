import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaginateGiftsComponent } from './paginate-gifts.component';

describe('PaginateGiftsComponent', () => {
  let component: PaginateGiftsComponent;
  let fixture: ComponentFixture<PaginateGiftsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PaginateGiftsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PaginateGiftsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
