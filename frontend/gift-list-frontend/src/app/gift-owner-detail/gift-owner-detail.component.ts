import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Gift } from '../model/gift';
import { GiftListService } from '../services/gift-list.service';
import { TagsService } from '../services/tags/tags.service';

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
  currentTypedTag=""
  proposedTags :string[]=[]
  currentGift : Gift = new Gift()

  constructor(private route: ActivatedRoute, private giftListService : GiftListService, private tagService : TagsService, private router : Router) { }

  ngOnInit(): void {
    let identifierList = this.route.snapshot.paramMap.get('listId')
    let identifierGift = this.route.snapshot.paramMap.get('giftId')
    this.currentTypedTag="";
    this.proposedTags=[]   
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

  testMe(){
    console.log("hello")
  }

  addTag(aTag : string){
    if(aTag===""){
      aTag=this.currentTypedTag
    }
    for(let existingTag of this.currentGift.tags){
      if(existingTag===aTag.toLowerCase()){
        this.currentTypedTag="";
        return;
      }
    }
    this.currentGift.tags.push(aTag.toLowerCase())
    this.currentTypedTag="";
    this.proposedTags=[]    
  }

  removeTag(aTag :string){
    let tagList = []
    for(let existingTag of this.currentGift.tags){
      if(existingTag !== aTag.toLowerCase()){
        tagList.push(existingTag)
      }
    }
    this.currentGift.tags=tagList
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
