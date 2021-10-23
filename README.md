# Auth Service

Servizio di autenticazione e gateway della piattaforma

# ENV

E' possibile specificare alcune variabili d'ambiente:

|    Nome     | Descrizione                                        | Obbligatorio |   Default   |
| :---------: | :------------------------------------------------- | :----------: | :---------: |
|    PORT     | Specifica la porta sulla quale il servizio ascolta |              |    8080     |
|   DB_NAME   | Il nome del database da utilizzare                 |      x       |             |
|   DB_TYPE   | Il tipo di connettore jdbc da utilizzare           |              | postgresql  |
|   DB_HOST   | L'url del database                                 |      x       |             |
|   DB_PORT   | La porta del DB                                    |              |    5432     |
| DB_USERNAME | L'utente da utilizzare per accedere al DB          |              |    admin    |
| DB_PASSWORD | La password per accedere al DB                     |              | ceposto2021 |
| APP_SECRET  | Il secret usato per la generazione del JWT         |              | ceposto2021 |

## Build

Per creare il `jar` eseguibile, bisogna eseguire

```shell
./mvnw package
```

Opzionalmente si può aggiungere il flag `-DskipTests` per evitare l'avvio degli Unit/Integration test
