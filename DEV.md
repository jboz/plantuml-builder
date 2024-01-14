# Github actions

TODO:

- tag on release
- upload github package
- création release page avec doc
- ajout changelog
- mettre à jour les boutons dans le README principal
- remove github page/maven site settings

# Publication maven centrale en local

A titre d'exemple, voici les commande permettant de chiffrer et publier les artefacts.

## 1. Signature du jar

https://central.sonatype.org/publish/requirements/gpg/#gpg-signed-components

Generate keys:

```bash
gpg --gen-key
```

```bash
gpg --list-signatures --keyid-format 0xshort

pub   rsa3072/0xtututututu 2023-03-28 [SC] [expire : 2025-03-27]
      6666666666666666666666666666666666666666
uid          [  ultime ] Julien Boz <julienboz@gmail.com>
sig 3        0xtututututu 2023-03-28  Julien Boz <julienboz@gmail.com>
sub   rsa3072/0xBBBBBBBB 2023-03-28 [E] [expire : 2025-03-27]
sig          0xtututututu 2023-03-28  Julien Boz <julienboz@gmail.com>
```

Optional but best practice, distribute public key on the internet to allow people to verify files:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys 6666666666666666666666666666666666666666
```

Optional, Export keys:

```bash
gpg --output .release/pubring.gpg --armor --export 6666666666666666666666666666666666666666
gpg --output .release/secring.gpg --armor --export-secret-key 6666666666666666666666666666666666666666
```

Test jar signatures:

```bash
export GPG_KEYNAME=0xtututututu
export GPG_PASSPHRASE=gpg-passphrase-defined

mvn clean verify -P release
```

## 2. Upload maven centrale

Documentation is here: https://central.sonatype.org/publish/manage-user/

Generate an access token on this page https://oss.sonatype.org/#profile;User%20Token (some time the web interface bugs and you have to reconnect yourself)

Create settings.xml :

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.1.0.xsd">
    <servers>
        <server>
            <id>ossrh</id>
            <username>${env.SONATYPE_USERNAME}</username>
            <password>${env.SONATYPE_PASSWORD}</password>
        </server>
    </servers>
</settings>
```

```bash
export SONATYPE_USERNAME=toto
export SONATYPE_PASSWORD=titi

mvn clean deploy -P release --settings .release/settings.xml
```

## 3. Lost secret ?

Use this in github action :

```yaml
- name: Set env as secret
  env:
    MY_VAL: ${{ secrets.MAVEN_GPG_PASSPHRASE }}
  run: |
    import os
    for q in (os.getenv("MY_VAL")):
    print(q)
  shell: python
```
