package com.massal.controller;

import com.massal.model.Carte;
import com.massal.model.Casino;
import com.massal.model.Deck;
import com.massal.model.Joueur;
import com.massal.view.View;

import java.util.List;

public class GameControlleur {

    enum EtatduJeux{
        Findepartie,tourJoueur,initialisation
    }

    private Deck deck;
    private Casino casino;
    private Joueur joueur;
    private View view;
    private EtatduJeux etatduJeux;
    private boolean splited;
    private boolean hasbeensplited;

    public GameControlleur (View view){

    this.joueur = new Joueur(100);
    this.casino = new Casino();
    this.view=view;
    this.etatduJeux = EtatduJeux.initialisation;

    }

    public void run(){
     while(this.etatduJeux!=EtatduJeux.Findepartie) {
         if (this.etatduJeux == EtatduJeux.initialisation) {
             this.deck = new Deck();
             this.deck.bruleCarte();
             this.joueur.miser(this.view.askMise(this.joueur));
             this.joueur.tirerCardHand(this.deck.getFirstCarte());
             this.joueur.tirerCardHand(this.deck.getFirstCarte());
             this.casino.tirerCard(this.deck.getFirstCarte());
             this.view.affichageCarte(joueur,casino);
             this.splited = false;
             this.hasbeensplited = false;
             this.etatduJeux = EtatduJeux.tourJoueur;
             checkBlackJack();
         }
         if (this.etatduJeux == EtatduJeux.tourJoueur) {
             String actionJoueur = this.view.askjoueur();
             switch (actionJoueur) {
                 case "double":
                     if (this.joueur.getMise() * 2 > this.joueur.getJetons()) {
                         this.view.actionImpossible();
                     } else {
                         if (splited) {
                             this.view.actionImpossible();
                         } else {
                             this.view.affichageMise(joueur);
                             this.joueur.doubler();
                             this.view.affichageMise(joueur);
                             this.joueur.tirerCardHand(this.deck.getFirstCarte());
                             this.view.affichageCarte(joueur,casino);
                             this.view.affichageJetons(joueur);
                             checkBlackJack();
                             this.view.affichageCarte(joueur,casino);
                         }
                     }
                     break;
                 case "tirer":
                     if (splited) {
                         this.joueur.tirerCarteHandsplited(this.deck.getFirstCarte());
                     } else {
                         this.joueur.tirerCardHand(this.deck.getFirstCarte());
                     }
                     this.view.affichageCarte(joueur,casino);
                     if(checkScore(joueur.getHand())>=21){
                         findePartie();
                     }
                     break;
                 case "split":
                     if (this.joueur.getMise() * 2 > this.joueur.getJetons()) {
                         this.view.actionImpossible();
                     } else {
                         if (!splited) {
                             if (joueur.getHand().get(0).getRank() == joueur.getHand().get(1).getRank()) {
                                 joueur.split();
                                 splited = true;
                                 hasbeensplited=true;
                             }
                         } else {
                             this.view.actionImpossible();
                         }
                     }
                     break;
                 case "assurance":
                     if (splited) {
                         this.view.actionImpossible();
                     } else {
                         if (this.joueur.getHand().size() != 1) {
                             this.view.actionImpossible();
                         } else {
                             if (joueur.getJetons() > (joueur.getMise() / 2)) {
                                 this.joueur.assurance();
                             }
                         }
                     }
                     break;
                 case "hold":
                     findePartie();
                     break;
             }

         }
     }
        this.joueur.reset();
        this.view.affichageCarte(joueur,casino);
        this.view.affichageJetons(joueur);
        this.view.affichageScore(checkScore(joueur.getHand()),checkScore(casino.getHand()));

    }

    public void findePartie() {
        if (splited) {
            splited = false;
        } else {
        while (checkScore(casino.getHand()) <17){
                casino.tirerCard(deck.getFirstCarte());
            }
            if (hasbeensplited = true) {
                gain(joueur, joueur.getHandsplited(), casino.getHand());
            }
            gain(joueur, joueur.getHand(), casino.getHand());
            etatduJeux = EtatduJeux.Findepartie;
        }
    }

    public void gain(Joueur joueur,List<Carte> handJoueur,List<Carte> handcasino){
        if(checkScore(handJoueur)==21) {
            if(checkScore(casino.getHand())==21){
                joueur.gain(joueur.getMise(), 0);
                this.view.egalite();
            }else{
                joueur.gain(joueur.getMise(), 1);
                this.view.victoire();
            }
        }else {
            if (checkScore(handJoueur) < 21) {
                if(checkScore(casino.getHand())>21) {
                    joueur.gain(joueur.getMise(), 1);
                    this.view.victoire();
                }else {
                    if (checkScore(casino.getHand()) == checkScore(handJoueur)) {
                        joueur.gain(joueur.getMise(), 0);
                        this.view.egalite();
                    }
                    if (checkScore(casino.getHand()) > checkScore(handJoueur)) {
                        this.view.defaite();
                        return;
                    }
                    if (checkScore(casino.getHand()) < checkScore(handJoueur)) {
                        joueur.gain(joueur.getMise(), 1);
                        this.view.victoire();
                    }
                }
            }else {
                this.view.defaite();
            }
        }

        if(checkScore(casino.getHand())==21) {
            if (casino.getHand().size() == 2) {
                joueur.gainAssurance(joueur.getAssurance());
            }
        }
    }


    public void checkBlackJack(){
        if (checkScore(joueur.getHand()) == 21) {
            while(checkScore(casino.getHand())<=17){
                casino.tirerCard(deck.getFirstCarte());
            }
            if(checkScore(casino.getHand())==21){
                if(casino.getHand().size()==2){
                    joueur.gain(joueur.getMise(), 0);
                    joueur.gainAssurance(joueur.getAssurance());
                    this.view.egalite();
                }else {
                    joueur.gain(joueur.getMise(), 1);
                    this.view.victoire();
                }
            }else {
                joueur.gain(joueur.getMise(), 1);
                this.view.victoire();
            }
            etatduJeux = EtatduJeux.Findepartie;
        }
    }
    private int checkScore(List<Carte> hand){
        int score = 0;
        for(int i=0;i<hand.size();i++){
                score = score + hand.get(i).getRank().value();

        }
        return score;

    }

   /*
    Distribution des cartes
    Check blackJack
    Premiere action du joueurs

     */







}
