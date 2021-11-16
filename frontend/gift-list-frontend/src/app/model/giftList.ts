import { Gift } from "./gift";

export class GiftList {
    id:string="";
    title:string="";
    description:string="";
    authorizedViewers:string[]=[];
    gifts:Gift[]=[];
    owner:string="";
}