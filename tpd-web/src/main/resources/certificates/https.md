# Ajout des certificats racines PMI pour la JVM Tomcat

La connexion à Domibus se fait en HTTPS.
Cette connexion HTTPS est signée par un certificat interne à PMI qui n'est pas directement accessible à la JVM de Tomcat.

La procédure ci-dessous explique comment rendre ce certificat accessible à la JVM.

## Récupération du certificat racine
Accéder avec un navigateur à
[https://pmpsa-as4-inttst.app.pmi/domibus/home/](https://pmpsa-as4-inttst.app.pmi/domibus/home/)
(Pas besoin d'être identifié)
Afficher les propriétés de la connexion, et les certificats.
Remonter le chemin d'accès de certification jusqu'au certificat racine "PMI Root CA v2".
Faire "View certificate", puis dans l'onglet "Details" faire "Copy to file" pour exporter le certificat en Base-64 encoded X.509 (.CER) à un endroit accessible. Il sera référencé dans la suite de ce document par `pmiRootCav2.CER`.

## Import du certificat dans les certificats racines de la JVM
Aller dans le dossier où est installée la JVM utilisée par le Tomcat de TPD.
Ouvrir une ligne de commande dans `lib/security`.
Copier le certificat récupéré plus tôt dans ce dossier.
Importer le certificat via : `keytool -import -alias PMIrootCa -keystore cacerts -file pmiRootCav2.cer`
Le mot de passe par défaut du keystore est `changeit`.
Accepter de truster le certificat.
Il peut être nécessaire de redémarrer Tomcat.


`Ce qui suis est uniquement pour référence, mais n'est pas recommandé`
## Création du Truststore utilisé par Tomcat
Ouvrir une ligne de commande dans le dossier où le certificat a été enregistré.
Créer un Truststore au format JKS avec l'outil keytool :
* `keytool -keystore catalina.jks -genkey -alias PMI`
* Saisir les informations correspondantes (Laisser vide est acceptable).
* Saisir un mot de passe pour le store nouvellement créé. Il sera référencé par `jksPassword` dans la suite de ce document.
Importer le certificat racine dans le keystore :
* `keytool -import -alias PMIrootca -keystore catalina.jks -trustcacerts -file pmiRootCav2.CER`.
* Entrer le mot de passe `jksPassword` lorsque demandé.


## Configuration de Tomcat pour utiliser le Truststore correspondant
Copier le truststore `catalina.jks`dans le dossier `conf` de Tomcat.
Ajouter dans les options passées à Tomcat :
    javax.net.ssl.trustStore=${catalina.home}/conf/catalina.jks
    javax.net.ssl.trustStorePassword=jksPassword
Redémarrer Tomcat le cas échéant.

