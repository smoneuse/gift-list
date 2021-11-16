import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GiftList } from '../model/giftList';
import { Gift } from '../model/gift';
import { AddRevokeViewer } from '../model/addRevokeViewerToListModel';
import { ReserveOrRelaseGiftModel } from '../model/reserveOrReleaseGiftReqModel';

@Injectable({
  providedIn: 'root'
})
export class GiftListService {
  private userListsUrl=this.baseUrl+"/list/all"
  private createListUrl=this.baseUrl+"/list"
  private deleteListUrl=this.baseUrl+"/list"
  private listDetailUrl=this.baseUrl+"/list/detail"
  private giftDetailUrl=this.baseUrl+"/gifts/detail"
  private updateListUrl=this.baseUrl+"/list"
  private createGiftUrl=this.baseUrl+"/gifts"
  private deleteGiftUrl=this.baseUrl+"/gifts/remove"
  private updateGiftUrl=this.baseUrl+"/gifts"
  private addViewerToListUrl=this.baseUrl+"/list"
  private revokeViewerFromListUrl=this.baseUrl+"/list"
  private addViewerToAllListUrl=this.baseUrl+"/list/all/viewers"
  private revokeViewerToAllListUrl=this.baseUrl+"/list/all/viewers"
  private authorizedListsUrl=this.baseUrl+"/list/authorized"
  private reserveGiftUrl=this.baseUrl+"/gifts/reservation"
  private releaseGiftUrl=this.baseUrl+"/gifts/release"
  private friendsUrl=this.baseUrl+"/account/friends"

  constructor(private http : HttpClient, @Inject('BASE_BACKEND_URL') private baseUrl : string) { }

  userLists(){
    return this.http.get<GiftList[]>(this.userListsUrl)
  }

  createList( aList : GiftList){
    return this.http.post<GiftList>(this.createListUrl, aList)
  }

  deleteList(listId : string){
    return this.http.delete<any>(this.deleteListUrl+`/${listId}`)
  }

  listDetail(listId : string){
    return this.http.get<GiftList>(this.listDetailUrl+"/"+listId)
  }

  giftDetail(listId:string , giftId:string) {
    return this.http.get<Gift>(this.giftDetailUrl+`/${listId}/${giftId}`)
  }

  updateList( aList : GiftList){
    return this.http.put<GiftList>(this.updateListUrl, aList)
  }

  createGift( aGift : Gift){
    return this.http.post<Gift>(this.createGiftUrl, aGift)
  }

  deleteGift( listId: string, giftId : string){
    return this.http.delete<any>(this.deleteGiftUrl+`/${listId}/${giftId}`)
  }

  updateGift( aGift : Gift ){
    return this.http.put<Gift>(this.updateGiftUrl, aGift)
  }

  addViewer(listId : string, viewerLogin : string){
    return this.http.post<GiftList>(this.addViewerToListUrl+`/${listId}/viewers/${viewerLogin}`, {})
  }

  revokeUser(listId : string, viewerLogin : string){
    return this.http.delete<any>(this.revokeViewerFromListUrl+`/${listId}/viewers/${viewerLogin}`)
  }

  addViewerToAllLists( reqModel :AddRevokeViewer){
    return this.http.post<GiftList[]>(this.addViewerToAllListUrl, reqModel)
  }

  revokeViewerToAllLists( reqModel :AddRevokeViewer){
    return this.http.request<any>('delete', this.revokeViewerToAllListUrl, { body: reqModel })
  }

  authorizedLists(){
    return this.http.get<GiftList[]>(this.authorizedListsUrl)
  }

  reserveGift( reqModel : ReserveOrRelaseGiftModel){
    return this.http.post<Gift>(this.reserveGiftUrl, reqModel)
  }

  releaseGift(reqModel : ReserveOrRelaseGiftModel){
    return this.http.post<any>(this.releaseGiftUrl, reqModel)
  }

  getFriends(){
    return this.http.get<string[]>(this.friendsUrl)
  }

  addFriend(aFriend : string){
    return this.http.post<string[]>(this.friendsUrl+"/"+aFriend,{})
  }

  removeFriend(aFriend : string){
    return this.http.delete<any>(this.friendsUrl+"/"+aFriend)
  }
}

