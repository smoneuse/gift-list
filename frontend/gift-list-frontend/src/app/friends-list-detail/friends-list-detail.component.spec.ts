import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsListDetailComponent } from './friends-list-detail.component';

describe('FriendsListDetailComponent', () => {
  let component: FriendsListDetailComponent;
  let fixture: ComponentFixture<FriendsListDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FriendsListDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FriendsListDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
