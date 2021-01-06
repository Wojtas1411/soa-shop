import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {ShopComponent} from './shop/shop.component';

const routes: Routes = [
  {path: '', component: ShopComponent},
  {path: '**', redirectTo: ''},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
