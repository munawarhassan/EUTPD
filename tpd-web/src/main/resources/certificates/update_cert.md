#Mise à jour des certificats UE

Pour l'encryption et la décryption des messages, des certificats sont utilisés.
Ceux-ci sont fournis par l'UE, qui parfois les modifie (Pas toujours avec préavis).

## Récupération d'un nouveau certificat
En temps normal, les nouveaux certificats sont publié sur le CIRCAB.
Il faut qu'une personne avec un accès s'occupe de les télécharger.
Ceux-ci sont normalement au format .cer
Il est toutefois arrivé que l'UE nous fournisse des certificats par e-mail 
et même qu'ils nous donnent directement des .jks

## Intégration d'un nouveau certificat dans le keystore
Copier le nouveau certificat dans le dossier où se trouve le keystore voulu.
Si il s'agit du certificat d'encryption pour l'envoi, il s'agit de celui configuré par la propriété
"app.domibus.trustore".
Si c'est celui pour la décryption des réponses, il s'agit de celui configuré par "app.domibus.keystore".

Il faut ensuite ajouter le certificat au keystore avec l'outil keytool :
`keytool -import  -keystore keystore.jks -file newCert.cer`
Il faudra indiquer le mot de passe du keystore (Qui se trouve dans la configuration, par défaut `test123`)
Il est possible de changer l'alias avec `-alias`.
C'est ce nom qu'il faudra indiquer dans la configuration `alias`.

Il est également possible de récupérer l'alias en listant les certificats disponibles dans le keystore :
`keytool -list -keystore keystore.jks`.


