import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Gift } from '../model/gift';
import { GiftList } from '../model/giftList';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-list-detail',
  templateUrl: './list-detail.component.html',
  styleUrls: ['./list-detail.component.css']
})
export class ListDetailComponent implements OnInit {
  listId =""
  currentList:GiftList=new GiftList()
  editedList: GiftList= new GiftList()
  createdGift:Gift= new Gift()
  giftToDelete:Gift= new Gift()
  currentFriend=""
  currentlink=""
  errorMessage=""
  constructor(private route: ActivatedRoute, private giftListService : GiftListService, private router : Router) { }

  ngOnInit(): void {
    let identifier = this.route.snapshot.paramMap.get('listId')
    if(identifier){
      this.listId=identifier;
      this.loadList()
    }
  }

  loadList(){
    this.giftListService.listDetail(this.listId)
      .subscribe({
        next:(res) =>{
          this.currentList=res
          this.editedList.title=res.title
          this.editedList.description = res.description
          this.editedList.id = res.id
        },
        error: (err)=>{
          console.log(err);
        }
      })
  }

  prepareGiftCreate(){
    this.errorMessage=""
    this.currentlink=""
    this.createdGift=new Gift()
    this.createdGift.listId=this.listId
  }

  addGift(){
    this.currentlink=""
    this.errorMessage=""    
    if(this.createdGift.title===""){
      this.errorMessage="Un cadeau doit au moins avoir un titre"
      return;
    }
    this.giftListService.createGift(this.createdGift)
      .subscribe({
        next:(res)=>{
          console.log(`Gift ${res.giftId} created`)
          this.loadList()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400){
              this.errorMessage="Une erreur est survenue lors de la mise à jour : "+err.error
            }
          }
        }
      })
  }

  availableGifts(giftList:GiftList) : Gift[]{
    let resultingGifts :Gift[]=[]
    if(!giftList.gifts){
      return resultingGifts
    }
    for(let aGift of giftList.gifts){
      if(aGift.status ==="AVAILABLE"){
        resultingGifts.push(aGift)
      }
    }
    return resultingGifts
  }

  givenGifts( giftList : GiftList) : Gift[]{
    let resultingGifts :Gift[]=[]
    if(!giftList.gifts){
      return resultingGifts
    }
    for(let aGift of giftList.gifts){
      if(aGift.status ==="GIVEN"){
        resultingGifts.push(aGift)
      }
    }
    return resultingGifts   
  }

  prepareDeleteGif(gift : Gift){
    this.giftToDelete=gift
  }

  deleteGift(giftId: string){
    this.errorMessage=""    
    this.giftListService.deleteGift(this.listId, giftId)
      .subscribe({
        next:(res)=>{
          console.log(`Gift ${giftId} deleted`)
          this.giftToDelete=new Gift()
          this.loadList()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400){
              this.errorMessage="Une erreur est survenue lors de la suppression : "+err.error
            }
          }
        }
      })
  }

  addLink(){
    if(this.currentlink){
      this.createdGift.links.push(this.currentlink);
      this.currentlink=""
    }
  }

  removeLink( aLink: string){
    let renewedLinks : string[] =[]
    for( let someLink of this.createdGift.links){
      if(someLink !== aLink){
        renewedLinks.push(someLink)
      }
    }
    this.createdGift.links=renewedLinks
  }

  updateList(){
    this.errorMessage=""
    if(this.editedList.title.trim()===""){
      this.errorMessage="Merci de renseigner un titre à la liste à mettre à jour."
      return
    }
    this.giftListService.updateList(this.editedList)
      .subscribe({
        next:(res) =>{
          console.log("Liste mise à jour")
          this.router.navigate(["/ownedLists"])
        },
        error: (err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400){
              this.errorMessage="Une erreur est survenue lors de la mise à jour : "+err.error
            }
          }  
        }
      })
  }

  shareList(){
    this.errorMessage=""
    if(this.currentFriend===""){
      this.errorMessage="Merci de renseigner de login d'un utilisateur avec qui partager"
      return
    }
    this.giftListService.addViewer(this.listId, this.currentFriend)
      .subscribe({
        next:(res)=>{
          this.currentFriend=""
          this.loadList()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400){
              this.errorMessage="Une erreur est survenue du partage : "+err.error
            }
          } 
        }
      })
  }

  unShareList(aViewer : string){
    this.errorMessage=""
    this.giftListService.revokeUser(this.listId, aViewer)
      .subscribe({
        next:(res)=>{
          this.currentFriend=""
          this.loadList()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400){
              this.errorMessage="Une erreur est survenue de la suppression : "+err.error
            }
          } 
        }
      })
  }

}
