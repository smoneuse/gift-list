import { TestBed } from '@angular/core/testing';

import { GiftListService } from './gift-list.service';

describe('GiftListService', () => {
  let service: GiftListService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GiftListService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
