import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AddRevokeViewer } from '../model/addRevokeViewerToListModel';
import { GiftList } from '../model/giftList';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-owned-lists',
  templateUrl: './owned-lists.component.html',
  styleUrls: ['./owned-lists.component.css']
})
export class OwnedListsComponent implements OnInit {

  userLists:GiftList[]=[]
  createdList:GiftList=new GiftList();
  listToDelete:GiftList=new GiftList();
  errorMessage=""
  currentFriend=""
  constructor( private giftListService : GiftListService) { }

  ngOnInit(): void {
    this.loadUserLists()
  }

  loadUserLists() {
    this.giftListService.userLists()
      .subscribe({
        next: (response) =>{
            this.userLists=response
        },
        error:(err)=> console.log(err)
      })
  }

  countAvailableGifts(gList : GiftList) : number{
    if(!gList.gifts){
      return 0;
    }
    let count=0
    for(let aGift of gList.gifts){
      if(aGift.status==="AVAILABLE"){
        count++
      }
    }
    return count
  }

  preDeleteList(aList : GiftList){
    this.listToDelete=aList
  }

  deleteList(listId : string){
    this.errorMessage=""    
    this.listToDelete=new GiftList();
    console.log("Suppression de la liste "+listId)
    this.giftListService.deleteList(listId)
      .subscribe({
        next: (res) =>{
          console.log(`liste ${listId} supprimée`)
          this.loadUserLists()
        },
        error: (err)=>{
          if(err instanceof HttpErrorResponse){
            this.errorMessage="Une erreur est survenue lors de la suppression : "+err.error
          }  
        }
      })
  }

  createNewList(){
    if(this.createdList.title.trim()===""){
      this.errorMessage="Merci de renseigner un titre à la liste à créer."
      return
    }
    this.errorMessage=""
    this.giftListService.createList(this.createdList)
      .subscribe({
        next: (res)=>{
          console.log("Liste cree avec identifiant :"+res.id)
          this.createdList=new GiftList();
          this.loadUserLists()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500){
              this.errorMessage="Une erreur est survenue lors de la création : "+err.error
            }
          }          
        }
      })
  }

  commonViewers() : string[]{
    let commonViewers : string[]=[]
    let viewersMap = new Map()
    for(let aList of this.userLists){
      for(let aViewer of aList.authorizedViewers){
        if(!viewersMap.has(aViewer)){
          viewersMap.set(aViewer,1)
        }
        else{
          viewersMap.set(aViewer,viewersMap.get(aViewer)+1)
        }
      }
    }
    for (let aViewer of viewersMap.keys()) {
      if(viewersMap.get(aViewer)===this.userLists.length){
        commonViewers.push(aViewer)
      }
    }
    return commonViewers
  }

  shareAllLists(){
    this.errorMessage=""
    let reqModel : AddRevokeViewer = new AddRevokeViewer()
    reqModel.listIds=this.allListsIds()
    reqModel.viewerLogin=this.currentFriend
    if(reqModel.listIds.length===0){
      this.errorMessage="Vous n'avez aucune liste à partager"
      return
    }
    this.giftListService.addViewerToAllLists(reqModel)
      .subscribe({
        next:(res)=>{
          this.currentFriend=""
          this.loadUserLists()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400) {
              this.errorMessage="Une erreur est survenue lors du partage : "+err.error
            }
          }  
        }
      })
  }

  allListsIds() : string[]{
    let response : string[]=[]
    for(let aList of this.userLists){
      response.push(aList.id)
    }
    return response
  }

  unShareAllLists(aViewer : string){    
    this.errorMessage=""
    let reqModel : AddRevokeViewer = new AddRevokeViewer()
    reqModel.listIds=this.allListsIds()
    reqModel.viewerLogin=aViewer
    if(reqModel.listIds.length===0){
      this.errorMessage="Vous n'avez aucune liste à ne plus partager"
      return
    }
    this.giftListService.revokeViewerToAllLists(reqModel)
      .subscribe({
        next:(res)=>{
          this.loadUserLists()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400) {
              this.errorMessage="Une erreur est survenue lors de la suppression : "+err.error
            }
          }  
        }
      })
  }
}
