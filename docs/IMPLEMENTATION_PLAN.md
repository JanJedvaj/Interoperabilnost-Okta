# Plan implementacije kroz 10 MVP commitova

Svaki MVP završava funkcionalnom, provjerljivom cjelinom i jednim Git commitom. Redoslijed prati ovisnosti između zahtjeva, tako da projekt ostaje pokretljiv nakon svakog koraka.

## Ciljana arhitektura

- **Domena:** Okta aplikacijske integracije (`Application`).
- **Javni izvor:** Okta Management Applications API, `/api/v1/apps`.
- **Lokalni izvor:** PostgreSQL i vlastiti REST/GraphQL API.
- **Prekidač izvora:** `app.api-mode=OKTA|LOCAL`; poslovni sloj koristi zajedničko sučelje, a ne `if` grananje po kontrolerima.
- **Backend:** Java 21, Spring Boot, Spring Data JPA, Spring Security, Apache CXF i gRPC.
- **Klijent:** HTML/CSS/JavaScript web aplikacija poslužena iz Spring Boota.
- **Sigurnost:** access i refresh JWT, BCrypt lozinke, uloge `READ_ONLY` i `FULL_ACCESS`.

## MVP 1 - Inicijalni IntelliJ/Gradle projekt

**Commit:** `chore: initialize Okta interoperability project`

Isporuka:

- Spring Boot/Gradle kostur za Java 21.
- Početna struktura paketa `hr.algebra.iis.okta`.
- Ovisnosti potrebne za REST, JPA, validaciju, SOAP, gRPC, JWT i GraphQL.
- `application.yml`, `.env.example` i PostgreSQL Docker Compose.
- `.gitignore` za IntelliJ, Gradle, tajne, `agents.md`, `.agents/` i `.codex/`.
- Minimalni test kojim se potvrđuje da je ulazna klasa dostupna.

Kriterij prihvata: projekt se u IntelliJ IDEA-i uveze kao Gradle projekt i `test` task prolazi.

## MVP 2 - Lokalni Application model i CRUD

**Commit:** `feat: add local application CRUD API`

Isporuka:

- JPA entitet `ApplicationEntity` s lokalnim ID-em i zajedničkim Okta poljima: `externalId`, `name`, `label`, `status`, `signOnMode`, `createdAt`, `updatedAt`.
- DTO sloj, repository, service i mapiranje bez izlaganja JPA entiteta kontroleru.
- REST rute `GET/POST/PUT/DELETE /api/applications`.
- Bean Validation i centralni `@RestControllerAdvice` s konzistentnim odgovorima grešaka.
- Integracijski testovi CRUD-a i 404/400 scenarija.

Kriterij prihvata: sva četiri HTTP glagola rade nad PostgreSQL bazom, a neispravni zahtjevi vraćaju strukturirane poruke.

## MVP 3 - JSON Schema i XSD unos

**Commit:** `feat: validate and import applications from JSON and XML`

Isporuka:

- `application-schema.json` i `application.xsd` za isti ugovorni model.
- `POST /api/applications/import/json` i `POST /api/applications/import/xml`.
- Validacija prije mapiranja i spremanja; neispravan dokument nikada ne ulazi u bazu.
- Sigurna XML konfiguracija: ugašeni DTD i vanjski entiteti, ograničeni vanjski pristupi sheme.
- Povrat svih korisnih validacijskih poruka, ne samo prve pogreške.
- Testni valjani i nevaljani JSON/XML primjeri.

Kriterij prihvata: oba formata spremaju isti domenski zapis, a pogreške jasno navode polje i pravilo.

## MVP 4 - Integracija Okta Applications API-ja

**Commit:** `feat: integrate Okta applications API`

Isporuka:

- Konfiguracijski objekt za domenu, token, timeout i putanju bez hardkodiranih tajni.
- Okta klijent za listanje, dohvat, stvaranje, zamjenu i brisanje aplikacija.
- Autentikacija Okta API tokenom; dizajn klijenta ostavlja mogućnost prelaska na OAuth 2.0 scopeove `okta.apps.read` i `okta.apps.manage`.
- Paginacija praćenjem neprozirnog `next` linka iz odgovora; link se ne parsira ručno.
- Mapiranje heterogenog Okta odgovora na zajednički DTO uz očuvanje potrebnih sirovih polja.
- Mock-server testovi za 200/204, 400/401/403, 429 i nedostupnu Oktu.

Kriterij prihvata: aplikacija može sigurno dohvatiti sve stranice Okta aplikacija bez spremanja tokena u repozitorij ili log.

## MVP 5 - Prekidač OKTA/LOCAL i sinkronizacija

**Commit:** `feat: switch between Okta and local application providers`

Isporuka:

- Zajedničko sučelje `ApplicationProvider` s `OktaApplicationProvider` i `LocalApplicationProvider` implementacijama.
- `app.api-mode=OKTA|LOCAL` bira implementaciju pri pokretanju.
- Jednake operacije kontrolera bez dupliranja poslovne logike.
- Endpoint za eksplicitnu sinkronizaciju Okta zapisa u lokalnu bazu, s upsertom po `externalId`.
- Dokumentirano ponašanje polja koja Okta tretira kao read-only ili ovisi o vrsti integracije.

Kriterij prihvata: promjena jedne konfiguracijske vrijednosti prebacuje GET pozive između Okte i baze; sinkronizacija je idempotentna.

## MVP 6 - XML snapshot, XPath SOAP i Jakarta XML provjera

**Commit:** `feat: add SOAP XPath search over validated Okta XML snapshot`

Isporuka:

- Servis koji poziva odabrani provider i generira UTF-8 XML snapshot aplikacija.
- Jakarta XML/XSD validacija generiranog snapshota i endpoint za prikaz svih poruka.
- Apache CXF SOAP usluga, primjerice `searchApplications(term)`.
- XPath filtriranje po `name`, `label`, `status` i `signOnMode` nad pripremljenim XML-om.
- XPath varijabla ili sigurno literal kodiranje umjesto spajanja korisničkog unosa u izraz.
- WSDL i SOAP integracijski testovi, uključujući navodnike i specijalne znakove u pojmu.

Kriterij prihvata: SOAP rezultat dolazi iz XPath pretrage XML-a nastalog iz javnog API-ja u OKTA načinu rada.

## MVP 7 - DHMZ gRPC servis

**Commit:** `feat: add DHMZ weather gRPC service`

Isporuka:

- `weather.proto`, generirane Java klase i gRPC server na portu 9090.
- Siguran dohvat i parsiranje `https://vrijeme.hr/hrvatska_n.xml`.
- Pretraga bez obzira na velika/mala slova i podrška za dio naziva grada.
- Vraćanje svih podudaranja, temperature i vlage.
- REST adapter za web klijent te timeout/fallback poruke za nedostupan DHMZ.
- Testovi s lokalnim DHMZ XML fixtureom, bez ovisnosti o mreži.

Kriterij prihvata: unos `zag` vraća sva odgovarajuća mjesta, a web klijent može pozvati uslugu preko REST adaptera.

## MVP 8 - JWT autentikacija, refresh i uloge

**Commit:** `feat: secure APIs with JWT roles and refresh tokens`

Isporuka:

- Lokalni korisnici, BCrypt hash lozinke i uloge `READ_ONLY`/`FULL_ACCESS`.
- Login, refresh i logout tok.
- Kratkotrajni access JWT i zasebno označen/rotiran refresh token u `HttpOnly`, `Secure` i odgovarajućem `SameSite` cookieju.
- `READ_ONLY` dopušta samo GET/GraphQL query; `FULL_ACCESS` dopušta mutacije.
- CSRF/CORS odluke dokumentirane prema načinu posluživanja web klijenta.
- Sigurnosni testovi za 401, 403, istekao token i pokušaj korištenja refresh tokena kao access tokena.

Kriterij prihvata: dozvole vrijede jednako za REST, GraphQL i pomoćne rute; tajne se ne pojavljuju u odgovoru ili logovima.

## MVP 9 - GraphQL API

**Commit:** `feat: expose secured application GraphQL API`

Isporuka:

- GraphQL tipovi, queryji za listu/detalj i mutacije create/update/delete.
- Ponovna uporaba istog service/provider sloja kao REST, bez paralelne implementacije pravila.
- Bean Validation i GraphQL mapiranje grešaka.
- Method security za queryje i mutacije prema ulozi.
- GraphQL integracijski testovi za oba načina izvora i obje uloge.

Kriterij prihvata: GraphQL i REST daju semantički iste podatke i ista pravila pristupa.

## MVP 10 - Web klijent, završno povezivanje i dokumentacija

**Commit:** `feat: deliver complete interoperability web client`

Isporuka:

- Responzivni web UI za login, listanje, detalje, CRUD, JSON/XML import i prikaz validacijskih grešaka.
- SOAP pretraga, validacija XML snapshota, DHMZ pretraga i GraphQL demonstracija iz sučelja.
- Vidljivo prilagođene akcije za `READ_ONLY` i `FULL_ACCESS`, uz backend kao konačni autoritet.
- Prikaz aktivnog `OKTA`/`LOCAL` načina i stanja vanjskih servisa.
- README s pokretanjem u IntelliJ IDEA-i, Dockerom, primjerima zahtjeva, WSDL/GraphQL/gRPC uputama i demo korisnicima.
- Završni end-to-end smoke test i provjera da u Gitu nema tokena, lozinki ni generiranih artefakata.

Kriterij prihvata: svi zahtjevi iz projektnog zadatka mogu se demonstrirati iz jednog grafičkog sučelja.

## Važne odluke za Okta domenu

1. Okta aplikacije nisu potpuno homogen objekt: potrebna polja za stvaranje ovise o `name` i `signOnMode`. Zato lokalni model čuva zajednička polja, dok Okta klijent koristi tip-specifične request DTO-e.
2. Okta listanje je paginirano. Sljedeći URL uzima se iz `Link` zaglavlja i tretira kao neproziran podatak.
3. `status`, `created` i `lastUpdated` tipično dolaze iz Okte kao udaljena/read-only polja; lokalni API može upravljati vlastitim statusom, ali mapiranje mora jasno razlikovati lokalni i udaljeni identitet.
4. Okta token pripada isključivo backendu. Web klijent nikad ga ne prima niti izravno poziva Okta Management API.
5. SOAP zahtjev iz zadatka mora pretraživati XML snapshot dohvaćenih aplikacija, a ne slučajno samo lokalne JPA zapise.

