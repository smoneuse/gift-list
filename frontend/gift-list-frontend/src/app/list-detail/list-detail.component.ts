import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Gift } from '../model/gift';
import { GiftList } from '../model/giftList';
import { GiftListService } from '../services/gift-list.service';
import { TagsService } from '../services/tags/tags.service';

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
  currentFriend=""
  currentlink=""
  errorMessage=""
  currentTypedTag=""
  proposedTags :string[]=[]
  constructor(private route: ActivatedRoute, private giftListService : GiftListService,private tagService :TagsService, private router : Router) { }

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
          if(err instanceof HttpErrorResponse){
            if(err.status===404){
              this.errorMessage="La liste n'a pas été trouvée"
            }
            else if(err.status===500 || err.status===400){
              this.errorMessage="Problème lors de la récupération de la liste :"+err.error
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  prepareGiftCreate(){
    this.errorMessage=""
    this.currentlink=""
    this.currentTypedTag=""
    this.proposedTags=[]
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
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
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
    return resultingGifts.sort(this.compareGifts)
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
    return resultingGifts.sort(this.compareGifts)
  }

  compareGifts( a : Gift, b :Gift) {
    if ( a.title < b.title ){
      return -1;
    }
    if ( a.title > b.title ){
      return 1;
    }
    return 0;
  }

  addLink(){
    if(this.currentlink){
      this.createdGift.links.push(this.currentlink);
      this.currentlink=""
    }
  }

  addTag(aTag : string){
    if(aTag===""){
      aTag=this.currentTypedTag
    }
    for(let existingTag of this.createdGift.tags){
      if(existingTag===aTag.toLowerCase()){
        this.currentTypedTag="";
        return;
      }
    }
    this.createdGift.tags.push(aTag.toLowerCase())
    this.currentTypedTag="";
    this.proposedTags=[]    
  }

  removeTag(aTag :string){
    let tagList = []
    for(let existingTag of this.createdGift.tags){
      if(existingTag !== aTag.toLowerCase()){
        tagList.push(existingTag)
      }
    }
    this.createdGift.tags=tagList
  }

  searchTags(){
    if(this.currentTypedTag.length>0){
      console.log("Searching for tag "+this.currentTypedTag)
      this.errorMessage="";
      this.tagService.searchTags(this.currentTypedTag)
        .subscribe({
          next:(res)=>{
            this.proposedTags=res
          },
          error:(err)=>{
            if(err instanceof HttpErrorResponse){
              if(err.status=== 500 || err.status === 400){
                this.errorMessage="Problème lors du chargement des tags : "+err.error
              }
              else if (err.status===404){
                this.errorMessage="Ce cadeau n'a pas été trouvé"
              }
              else if(err.status === 401 || err.status === 403){
                this.router.navigate(['/login'])
              }
            }
            else{
              this.errorMessage="Une erreur est survenue lors de la recherche de tags :"+err.error
            }
          }
        })
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
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
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
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
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
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          } 
        }
      })
  }

}
