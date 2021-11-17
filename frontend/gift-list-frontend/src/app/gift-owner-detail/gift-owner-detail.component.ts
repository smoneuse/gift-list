import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Gift } from '../model/gift';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-gift-owner-detail',
  templateUrl: './gift-owner-detail.component.html',
  styleUrls: ['./gift-owner-detail.component.css']
})
export class GiftOwnerDetailComponent implements OnInit {
  listId =""
  giftId=""
  errorMessage=""
  currentlink=""
  currentGift : Gift = new Gift()

  constructor(private route: ActivatedRoute, private giftListService : GiftListService, private router : Router) { }

  ngOnInit(): void {
    let identifierList = this.route.snapshot.paramMap.get('listId')
    let identifierGift = this.route.snapshot.paramMap.get('giftId')
    if(identifierList && identifierGift){
      this.listId=identifierList
      this.giftId=identifierGift
      this.loadCurrentGift(this.listId, this.giftId)
    }
  }

  updateGift(aGift :Gift){
    this.giftListService.updateGift(aGift)
      .subscribe({
        next:(res)=> {
            this.router.navigate(["/listDetail/"+this.listId])
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400){
              this.errorMessage="Problème lors du chargement des détails du cadeau : "+err.error
            }
            else if (err.status===404){
              this.errorMessage="Ce cadeau n'a pas été trouvé"
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  addLink(){
    this.currentGift.links.push(this.currentlink)
    this.currentlink=""
  }

  removeLink(aLink : string){
    let linksList = []
    for(let aGiftLink of this.currentGift.links){
      if(aGiftLink !== aLink){
        linksList.push(aGiftLink)
      }
    }
    this.currentGift.links=linksList
  }

  loadCurrentGift(listId : string, giftId : string){
    this.giftListService.giftDetail(listId, giftId)
      .subscribe({
        next:(res)=>{
          this.currentGift=res
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400){
              this.errorMessage="Problème lors du chargement des détails du cadeau : "+err.error
            }
            else if (err.status===404){
              this.errorMessage="Ce cadeau n'a pas été trouvé"
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }
}
