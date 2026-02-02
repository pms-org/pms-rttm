### Load in Windows Powershell: _(Recommended)_
---
1. Module method:
- Install Powershell Module: `Set-PsEnv`
    > Run `Install-Module -Name Set-PsEnv`
- To load env vars, Run `Set-PsEnv`
- To list all env vars, run `dir env:`

2. Script method: _(Haven't tried yet)_
- Create file `load_env.ps1`
- contents:
    ```PS
    get-content test.env | foreach {
        $name, $value = $_.split('=')
        set-content env:\$name $value
    }
    ```
- Run `./load_env.ps1`

### Load in Windows CMD Prompt:
---
- create `env.bat`
- sample file:
    ```bat
        set "KAFKA_BOOTSTRAP=localhost:9092"
        set "KAFKA_CONSUMER_GROUP_ID=pms-rttm-group"

        set "SCHEMA_REGISTRY_URL=http://localhost:8081"
    ``` 
- Run `env.bat` in CMD
- To list all env vars, run `set`


### Vs Code `launch.json` configuration:
---
Click `Run > Add Configuration > creates launch.json + conf > Add Property "envFile": "{workspace}/.env"`