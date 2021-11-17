import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { GiftList } from '../model/giftList';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-friends-lists',
  templateUrl: './friends-lists.component.html',
  styleUrls: ['./friends-lists.component.css']
})
export class FriendsListsComponent implements OnInit {
  allOwners : string[]=[]
  ownerGiftListMap = new Map()
  errorMessage=""

  constructor(private giftListService : GiftListService, private router : Router) { }

  ngOnInit(): void {
    this.loadAuthorizedLists()
  }

  loadAuthorizedLists(){
    this.errorMessage=""
    this.giftListService.authorizedLists()
      .subscribe({
        next:(res)=>{
          this.allOwners=[]
          let responseMap=new Map()    
          for(let aList of res){
            if(!responseMap.has(aList.owner)){
              this.allOwners.push(aList.owner)
              let gListArray : GiftList[]=[]
              responseMap.set(aList.owner,gListArray)
            }
          }
          for(let aList of res){
            let gListArray=responseMap.get(aList.owner)
            gListArray.push(aList)
            responseMap.set(aList.owner,gListArray)
          }
          this.ownerGiftListMap= responseMap         
        },
        error: (err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400) {
              this.errorMessage="Une erreur est survenue lors du partage : "+err.error
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }  
        }
      })
  }

  countAvailableGifts( aList : GiftList ) : number {
    let count =0
    for(let aGift of aList.gifts){
      if(aGift.status==="AVAILABLE"){
        count++
      }
    }
    return count
  }

  countReservedGifts( aList : GiftList ) : number {
    let count =0
    for(let aGift of aList.gifts){
      if(aGift.status==="RESERVED"){
        count++
      }
    }
    return count
  }
}
