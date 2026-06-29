# SOAP WSDL export

SOAP servis je objavljen kroz Apache CXF na:

```text
http://localhost:8080/ws/applications?wsdl
```

Kada se aplikacija pokrene, WSDL se sprema u ovaj folder:

```powershell
Invoke-WebRequest `
  -Uri "http://localhost:8080/ws/applications?wsdl" `
  -OutFile "docs/wsdl/application-search-service.wsdl"
```

`application-search-service.wsdl` treba commitati nakon što je SOAP ugovor stabilan.
