import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendsListsComponent } from './friends-lists.component';

describe('FriendsListsComponent', () => {
  let component: FriendsListsComponent;
  let fixture: ComponentFixture<FriendsListsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FriendsListsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FriendsListsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
