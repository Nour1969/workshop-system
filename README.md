# WorkshopSystemMaven

Install the provided API JARs into your local Maven repository from PowerShell:

```powershell
mvn install:install-file -Dfile="C:\Users\nourg\Downloads\model-2.0.jar" -DgroupId=patmal.workshops -DartifactId=model -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile="C:\Users\nourg\Downloads\logic-2.0.jar" -DgroupId=patmal.workshops -DartifactId=logic -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
```

These commands install `patmal.workshops:model:2.0` and
`patmal.workshops:logic:2.0`, respectively.
