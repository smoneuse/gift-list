import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  templateUrl: './star-rating.component.html',
  styleUrls: ['./star-rating.component.css']
})
export class StarRatingComponent implements OnInit {

  @Input() rating :number=0;
  @Input('mode') mode : string ="";
  @Input("fontSize") fontSize :string ="2rem"
  @Output() ratingChange = new EventEmitter<number>();

  constructor() { }

  ngOnInit(): void {
  }

  setRating(value : number) : void {
    if(this.mode =="edit"){
      this.rating=value;
      this.ratingChange.emit(this.rating);
    }
  }
}
